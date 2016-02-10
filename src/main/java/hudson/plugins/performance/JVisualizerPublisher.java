package hudson.plugins.performance;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JVisualizerPublisher extends Recorder {

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
    @Override
    public String getDisplayName() {
      //return "performace publisher";
      return Messages.Publisher_DisplayName();
    }

    @Override
    public String getHelpFile() {
      return "/plugin/performance/help.html";
    }

    //only gets the descriptors (annotated with extension points) that extend PerformanceReportParserDecriptor
    public List<JVisualizerParserDescriptor> getParserDescriptors() {
      return JVisualizerParserDescriptor.all();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    /**
     *
     * Populate the comparison type dynamically based on the user selection from
     * the previous time
     *
     * @return the name of the option selected in the previous run
     */
  }


  private int nthBuildNumber = 0;

  private boolean compareBuildPrevious = false;

  File xmlfile = null;

  String xmlDir = null;

  String xml = "";

  private static final String archive_directory = "archive";

  /**
   * @deprecated as of 1.3. for compatibility
   */
  private transient String filename;

  /**
   * Configured report parsers.
   */
  private List<JVisualizerParser> parsers;

  private boolean modeThroughput;


  @DataBoundConstructor
  public JVisualizerPublisher(int errorFailedThreshold,
                              int errorUnstableThreshold,
                              String errorUnstableResponseTimeThreshold,
                              int nthBuildNumber,
                              boolean modeOfThreshold,
                              boolean compareBuildPrevious,
                              List<? extends JVisualizerParser> parsers,
                              boolean modeThroughput) {


    this.nthBuildNumber = nthBuildNumber;
    this.compareBuildPrevious = compareBuildPrevious;

    if (parsers == null)
      parsers = Collections.emptyList();
    this.parsers = new ArrayList<JVisualizerParser>(parsers);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////
  public static File getPerformanceReport(AbstractBuild<?, ?> build,
                                          String parserDisplayName, String performanceReportName) {
    return new File(build.getRootDir(),
            JVisualizerReportMap.getPerformanceReportFileRelativePath(
                    parserDisplayName,
                    getPerformanceReportBuildFileName(performanceReportName)));
  }

  @Override
  public Action getProjectAction(AbstractProject<?, ?> project) {
    return new JVisualizerProjectAction(project);
  }

  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  public List<JVisualizerParser> getParsers() {
    return parsers;
  }


  /**
   * <p>
   * Delete the date suffix appended to the Performance result files by the
   * Maven Performance plugin
   * </p>
   *
   * @param performanceReportWorkspaceName
   * @return the name of the JVisualizerReport in the Build
   */
  public static String getPerformanceReportBuildFileName(
          String performanceReportWorkspaceName) {
    String result = performanceReportWorkspaceName;
    if (performanceReportWorkspaceName != null) {
      Pattern p = Pattern.compile("-[0-9]*\\.xml");
      Matcher matcher = p.matcher(performanceReportWorkspaceName);
      if (matcher.find()) {
        result = matcher.replaceAll(".xml");
      }
    }
    return result;
  }

  /**
   * look for performance reports based in the configured parameter includes.
   * 'includes' is - an Ant-style pattern - a list of files and folders
   * separated by the characters ;:,
   */
  protected static List<FilePath> locatePerformanceReports(FilePath workspace,
                                                           String includes) throws IOException, InterruptedException {
    //System.out.println("includes is: " + includes);

    // First use ant-style pattern
    /*
      try {
      FilePath[] ret = workspace.list(includes);
      if (ret.length > 0) {
        return Arrays.asList(ret);
      }
    */
    //Agoley : Possible fix, if we specify more than one result file pattern
    try {
      String parts[] = includes.split("\\s*[;:,]+\\s*");


      List<FilePath> files = new ArrayList<FilePath>();
      for (String path : parts) {
        FilePath[] ret = workspace.list(path);
        if (ret.length > 0) {
          files.addAll(Arrays.asList(ret));
        }
      }
      if (!files.isEmpty()) return files;

    } catch (IOException e) {
    }

    //Agoley:  seems like this block doesn't work
    // If it fails, do a legacy search
    ArrayList<FilePath> files = new ArrayList<FilePath>();
    String parts[] = includes.split("\\s*[;:,]+\\s*");
    for (String path : parts) {
      FilePath src = workspace.child(path);
      if (src.exists()) {
        if (src.isDirectory()) {
          files.addAll(Arrays.asList(src.list("**/*")));
          //System.out.println("size of files: " + files.size());
        } else {
          files.add(src);
        }
      }
    }
    if (!files.isEmpty()) return files;

    //give up and just try direct matching on string
    File directFile = new File(includes);
    if (directFile.exists()) files.add(new FilePath(directFile));
    return files;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
          throws InterruptedException, IOException {

    PrintStream logger = listener.getLogger();
    double thresholdTolerance = 0.00000001;
    Result result = Result.SUCCESS;
    EnvVars env = build.getEnvironment(listener);

//    //For absolute error/unstable threshold..
//      try {
//        List<UriReport> curruriList = null;
//        HashMap<String, String> responseTimeThresholdMap = null;
//
//        if (!"".equals(this.errorUnstableResponseTimeThreshold) && this.errorUnstableResponseTimeThreshold != null) {
//
//          responseTimeThresholdMap = new HashMap<String, String>();
//          String[] lines = this.errorUnstableResponseTimeThreshold.split("\n");
//
//          for (String line : lines) {
//            String[] components = line.split(":");
//            if (components.length == 2) {
//              logger.println("Setting threshold: " + components[0] +":"+ components[1]);
//              responseTimeThresholdMap.put(components[0], components[1]);
//            }
//          }
//        }
//
//        if (errorUnstableThreshold >= 0 && errorUnstableThreshold <= 100) {
//            logger.println("Performance: Percentage of errors greater or equal than "
//                    + errorUnstableThreshold + "% sets the build as "
//                    + Result.UNSTABLE.toString().toLowerCase());
//        }
//        else {
//            logger.println("Performance: No threshold configured for making the test "
//                    + Result.UNSTABLE.toString().toLowerCase());
//        }
//        if (errorFailedThreshold >= 0 && errorFailedThreshold <= 100) {
//            logger.println("Performance: Percentage of errors greater or equal than "
//                    + errorFailedThreshold + "% sets the build as "
//                    + Result.FAILURE.toString().toLowerCase());
//        }
//        else {
//            logger.println("Performance: No threshold configured for making the test "
//                    + Result.FAILURE.toString().toLowerCase());
//        }

      //System.out.println("the parsers var size is; " + parsers.size());
      // add the report to the build object.
      JVisualizerBuildAction a = new JVisualizerBuildAction(build, logger, parsers);
      build.addAction(a);
      logger.print("\n\n\n");

      for (JVisualizerParser parser : parsers) {
        //System.out.println("The glob pattern is: " + parser.getDefaultGlobPattern());

        String glob = parser.glob;
        //System.out.println("The parser.glob is: " + parser.glob);

        //Replace any runtime environment variables such as ${sample_var}
        glob = env.expand(glob);
        logger.println("Performance: Recording " + parser.getReportName() + " reports '" + glob + "'");
        //System.out.println("Performance: Recording " + parser.getReportName() + " reports '" + glob + "'");

        List<FilePath> files = locatePerformanceReports(build.getWorkspace(), glob);

        if (files.isEmpty()) {
          if (build.getResult().isWorseThan(Result.UNSTABLE)) {
            return true;
          }
          build.setResult(Result.FAILURE);
          logger.println("Performance: no " + parser.getReportName()
                  + " files matching '" + glob
                  + "' have been found. Has the report generated?. Setting Build to "
                  + build.getResult());
//          //System.out.println("Performance: no " + parser.getReportName()
//                  + " files matching '" + glob
//                  + "' have been found. Has the report generated?. Setting Build to "
//                  + build.getResult());
          return true;
        }

        List<File> localReports = copyReportsToMaster(build, logger, files, parser.getDescriptor().getDisplayName());
        for(File f : localReports){
          //System.out.println("name of file: "+f.getName());
        }
        Collection<JVisualizerReport> parsedReports = parser.parse(build, localReports, listener);

//          // mark the build as unstable or failure depending on the outcome.
//          for (JVisualizerReport r : parsedReports) {
//
//            xmlDir = build.getRootDir().getAbsolutePath();
//            xmlDir += "/"+archive_directory;
//
//            String[] arr = glob.split("/");
//            if(!new File(xmlDir).exists()){
//                new File(xmlDir).mkdirs();
//            }
//
//            xmlfile = new File(xmlDir+"/dashBoard_"+arr[arr.length-1].split("\\.")[0]+".xml");
//            xmlfile.createNewFile();
//
//            FileWriter fw = new FileWriter(xmlfile.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//
//            xml = "<?xml version=\"1.0\"?>\n";
//            xml += "<results>\n";
//            xml += "<absoluteDefinition>\n";
//
//            String unstable = "\t<unstable>";
//            String failed = "\t<failed>";
//            String calc = "\t<calculated>";
//
//            unstable += errorUnstableThreshold;
//            failed += errorFailedThreshold;
//
//            String avg = "", med = "", perct = "";
//
//            avg += "<average>\n";
//            med += "<median>\n";
//            perct += "<percentile>\n";
//
//            r.setBuildAction(a);
//            double errorPercent = r.errorPercent();
//            calc += errorPercent;
//
//            curruriList = r.getUriListOrdered();
//
//            if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
//                result = Result.FAILURE;
//                build.setResult(Result.FAILURE);
//            } else if (errorUnstableThreshold >= 0 && errorPercent - errorUnstableThreshold > thresholdTolerance) {
//                result = Result.UNSTABLE;
//            }
//
//            long average = r.getAverage();
//            logger.println(r.getReportFileName() + " has an average of: "+ Long.toString(average));
//
//            try {
//              if (responseTimeThresholdMap != null && responseTimeThresholdMap.get(r.getReportFileName()) != null) {
//                if (Long.parseLong(responseTimeThresholdMap.get(r.getReportFileName())) <= average) {
//                    logger.println("UNSTABLE: " + r.getReportFileName() + " has exceeded the threshold of ["+Long.parseLong(responseTimeThresholdMap.get(r.getReportFileName()))+"] with the time of ["+Long.toString(average)+"]");
//                    result = Result.UNSTABLE;
//                }
//              }
//            } catch (NumberFormatException nfe) {
//                logger.println("ERROR: Threshold set to a non-number [" + responseTimeThresholdMap.get(r.getReportFileName()) + "]");
//                result = Result.FAILURE;
//                build.setResult(Result.FAILURE);
//
//            }
//            if (result.isWorseThan(build.getResult())) {
//                build.setResult(result);
//            }
//            logger.println("Performance: File " + r.getReportFileName()
//                    + " reported " + errorPercent
//                    + "% of errors [" + result + "]. Build status is: "
//                    + build.getResult());
//
//            for (int i = 0; i < curruriList.size(); i++){
//                avg += "\t<"+curruriList.get(i).getStaplerUri()+">\n";
//                avg += "\t\t<currentBuildAvg>"+curruriList.get(i).getAverage()+"</currentBuildAvg>\n";
//                avg += "\t</"+curruriList.get(i).getStaplerUri()+">\n";
//
//
//                med += "\t<"+curruriList.get(i).getStaplerUri()+">\n";
//                med += "\t\t<currentBuildMed>"+curruriList.get(i).getMedian()+"</currentBuildMed>\n";
//                med += "\t</"+curruriList.get(i).getStaplerUri()+">\n";
//
//
//                perct += "\t<"+curruriList.get(i).getStaplerUri()+">\n";
//                perct += "\t\t<currentBuild90Line>"+curruriList.get(i).get90Line()+"</currentBuild90Line>\n";
//                perct += "\t</"+curruriList.get(i).getStaplerUri()+">\n";
//
//            }
//            unstable += "</unstable>";
//            failed += "</failed>";
//            calc += "</calculated>";
//
//            avg += "</average>\n";
//            med += "</median>\n";
//            perct += "</percentile>\n";
//
//            xml += unstable+"\n";
//            xml += failed+"\n";
//            xml += calc+"\n";
//            xml += "</absoluteDefinition>\n";
////
////            xml += avg;
////            xml += med;
////            xml += perct;
////            xml += "</results>";
////
////            bw.write(xml);
////            bw.close();
////            fw.close();
////
////            logger.print("\n\n\n");
////          }
//        }
//      } catch(Exception e) {
//      }
//    } else {
//
//      // For relative comparisons between builds...
//      try {
//
//        String name ="";
//        FileWriter fw = null;
//        BufferedWriter bw = null;
//
//        String relative = "<relativeDefinition>\n";
//        String unstable = "\t<unstable>\n";
//        String failed = "\t<failed>\n";
//        String buildNo = "\t<buildNum>";
//
//        String inside = "";
//        String avg = "", med = "", perct = "";
//
//
//        unstable += "\t</unstable>\n";
//        failed += "\t</failed>\n";
//
//        avg += "<average>\n";
//        med += "<median>\n";
//        perct += "<percentile>\n";
//
//        List<UriReport> curruriList = null;
//
//        // add the report to the build object.
//        JVisualizerBuildAction a = new JVisualizerBuildAction(build, logger, parsers);
//        build.addAction(a);
//        logger.print("\n\n\n");
//
//
//        for (JVisualizerParser parser : parsers) {
//          String glob = parser.glob;
//          glob = env.expand(glob);
//          name = glob;
//          List<FilePath> files = locatePerformanceReports(build.getWorkspace(), glob);
//
//          if (files.isEmpty()) {
//            if (build.getResult().isWorseThan(Result.UNSTABLE)) {
//                return true;
//            }
//            build.setResult(Result.FAILURE);
//            logger.println("Performance: no " + parser.getReportName()
//                    + " files matching '" + glob
//                    + "' have been found. Has the report generated?. Setting Build to "
//                    + build.getResult());
//            return true;
//          }
//
//          List<File> localReports = copyReportsToMaster(build, logger, files, parser.getDescriptor().getDisplayName());
//          Collection<JVisualizerReport> parsedReports = parser.parse(build, localReports, listener);
//
//
//          for (JVisualizerReport r : parsedReports) {
//            r.setBuildAction(a);
//            // URI list is the list of labels in the current JMeter results file
//            curruriList = r.getUriListOrdered();
//            break;
//          }
//        }
//
//        xmlDir = build.getRootDir().getAbsolutePath();
//        xmlDir += "/"+archive_directory;
//
//        String[] arr = name.split("/");
//        if(!new File(xmlDir).exists()){
//            new File(xmlDir).mkdirs();
//        }
//
//        xmlfile = new File(xmlDir+"/dashBoard_"+arr[arr.length-1].split("\\.")[0]+".xml");
//        xmlfile.createNewFile();
//
//        fw = new FileWriter(xmlfile.getAbsoluteFile());
//        bw = new BufferedWriter(fw);
//
//        bw.write("<?xml version=\"1.0\"?>\n");
//        bw.write("<results>\n");
//
//        // getting previous build/nth previous build..
//        AbstractBuild<?,?> prevBuild = null;
//
//        if(compareBuildPrevious){
//          buildNo += "previous";
//          prevBuild = build.getPreviousSuccessfulBuild();
//        } else {
//          buildNo += nthBuildNumber;
//          prevBuild = getnthBuild(build, listener);
//        }
//
//        buildNo += "</buildNum>\n";
//        relative += buildNo + unstable + failed;
//        relative += "</relativeDefinition>";
//
//        bw.write(relative+"\n");
//
//        List<UriReport> prevuriList = null;
//
//        if (prevBuild != null) {
//          JVisualizerBuildAction b = new JVisualizerBuildAction(prevBuild, logger, parsers);
//          prevBuild.addAction(b);
//
//          //getting files related to the previous build selected
//          for (JVisualizerParser parser : parsers) {
//            String glob = parser.glob;
//            logger.println("Performance: Recording " + parser.getReportName()+ " reports '" + glob + "'");
//
//            List<File> localReports = getExistingReports(prevBuild, logger, parser.getDescriptor().getDisplayName());
//            Collection<JVisualizerReport> parsedReports = parser.parse(prevBuild, localReports, listener);
//
//
//            for (JVisualizerReport r : parsedReports) {
//              r.setBuildAction(b);
//
//              //uri list is the list of labels in the previous jmeter results file
//              prevuriList = r.getUriListOrdered();
//              break;
//            }
//          }
//
//          result = Result.SUCCESS;
//          String failedLabel = null, unStableLabel = null;
//          double relativeDiff=0, relativeDiffPercent=0;
//
//          logger.print("\nComparison build no. - "+prevBuild.number+" and "+build.number +" using ");
//
//
//          //Comparing both builds based on either average, median or 90 percentile response time...
//
//
//          //comparing the labels and calculating the differences...
//          for (int i = 0; i < prevuriList.size(); i++) {
//            for (int j = 0; j < curruriList.size(); j++) {
//              if(prevuriList.get(i).getStaplerUri().equalsIgnoreCase(curruriList.get(j).getStaplerUri())) {
//
//                relativeDiff = curruriList.get(j).getAverage() - prevuriList.get(i).getAverage();
//                relativeDiffPercent = ((double) relativeDiff * 100) / prevuriList.get(i).getAverage();
//                relativeDiffPercent = Math.round(relativeDiffPercent * 100);
//                relativeDiffPercent = relativeDiffPercent/100;
//
//                avg += "\t<"+curruriList.get(j).getStaplerUri()+">\n";
//                avg += "\t\t<previousBuildAvg>"+prevuriList.get(i).getAverage()+"</previousBuildAvg>\n";
//                avg += "\t\t<currentBuildAvg>"+curruriList.get(j).getAverage()+"</currentBuildAvg>\n";
//                avg += "\t\t<relativeDiff>"+relativeDiff+"</relativeDiff>\n";
//                avg += "\t\t<relativeDiffPercent>"+relativeDiffPercent+"</relativeDiffPercent>\n";
//                avg += "\t</"+curruriList.get(j).getStaplerUri()+">\n";
//
//                relativeDiff = curruriList.get(j).getMedian() - prevuriList.get(i).getMedian();
//                relativeDiffPercent = ((double) relativeDiff * 100) / prevuriList.get(i).getMedian();
//                relativeDiffPercent = Math.round(relativeDiffPercent * 100);
//                relativeDiffPercent = relativeDiffPercent/100;
//
//                med += "\t<"+curruriList.get(j).getStaplerUri()+">\n";
//                med += "\t\t<previousBuildMed>"+prevuriList.get(i).getMedian()+"</previousBuildMed>\n";
//                med += "\t\t<currentBuildMed>"+curruriList.get(j).getMedian()+"</currentBuildMed>\n";
//                med += "\t\t<relativeDiff>"+relativeDiff+"</relativeDiff>\n";
//                med += "\t\t<relativeDiffPercent>"+relativeDiffPercent+"</relativeDiffPercent>\n";
//                med += "\t</"+curruriList.get(j).getStaplerUri()+">\n";
//
//                relativeDiff = curruriList.get(j).get90Line() - prevuriList.get(i).get90Line();
//                relativeDiffPercent = ((double) relativeDiff * 100) / prevuriList.get(i).get90Line();
//                relativeDiffPercent = Math.round(relativeDiffPercent * 100);
//                relativeDiffPercent = relativeDiffPercent/100;
//
//                perct += "\t<"+curruriList.get(j).getStaplerUri()+">\n";
//                perct += "\t\t<previousBuild90Line>"+prevuriList.get(i).get90Line()+"</previousBuild90Line>\n";
//                perct += "\t\t<currentBuild90Line>"+curruriList.get(j).get90Line()+"</currentBuild90Line>\n";
//                perct += "\t\t<relativeDiff>"+relativeDiff+"</relativeDiff>\n";
//                perct += "\t\t<relativeDiffPercent>"+relativeDiffPercent+"</relativeDiffPercent>\n";
//                perct += "\t</"+curruriList.get(j).getStaplerUri()+">\n";
//
//                if (result.isWorseThan(build.getResult())) {
//                  build.setResult(result);
//                }
//              }
//
//            }
//          }
//
//          logger.println("------------------------------------------------------------------------------------------------------------------------------------");
//          String labelResult = "\nThe label ";
//          logger.print((failedLabel != null) ? labelResult + "\"" + failedLabel + "\"" + " caused the build to fail\n" : (unStableLabel != null) ? labelResult + "\"" + unStableLabel + "\"" + " made the build unstable\n" : "");
//
//          avg += "</average>\n";
//          med += "</median>\n";
//          perct += "</percentile>";
//
//          inside += avg + med + perct;
//          bw.write(inside+"\n");
//
//        }
//        bw.write("</results>");
//        bw.close();
//        fw.close();
//
//      } catch (Exception e){
//      }
//    }
//    return true;
      }

    return true;

  }

  private List<File> copyReportsToMaster(AbstractBuild<?, ?> build,
                                         PrintStream logger, List<FilePath> files, String parserDisplayName)
          throws IOException, InterruptedException {
    List<File> localReports = new ArrayList<File>();
    for (FilePath src : files) {
      final File localReport = getPerformanceReport(build, parserDisplayName,
              src.getName());
      if (src.isDirectory()) {
        logger.println("Performance: File '" + src.getName()
                + "' is a directory, not a Performance Report");
        continue;
      }
      src.copyTo(new FilePath(localReport));
      localReports.add(localReport);
    }
    return localReports;
  }

  public Object readResolve() {
    // data format migration
    if (parsers == null)
      parsers = new ArrayList<JVisualizerParser>();
    if (filename != null) {
      parsers.add(new JtlFileParser(filename));
      filename = null;
    }
    return this;
  }

//    public int getErrorFailedThreshold () {
//      return errorFailedThreshold;
//    }
//
//    public void setErrorFailedThreshold ( int errorFailedThreshold){
//      this.errorFailedThreshold = Math.max(0, Math.min(errorFailedThreshold, 100));
//    }
//
//    public int getErrorUnstableThreshold () {
//      return errorUnstableThreshold;
//    }
//
//    public void setErrorUnstableThreshold ( int errorUnstableThreshold){
//      this.errorUnstableThreshold = Math.max(0, Math.min(errorUnstableThreshold,
//              100));
//    }
//
//    public String getErrorUnstableResponseTimeThreshold () {
//      return this.errorUnstableResponseTimeThreshold;
//    }
//
//    public void setErrorUnstableResponseTimeThreshold (String errorUnstableResponseTimeThreshold){
//      this.errorUnstableResponseTimeThreshold = errorUnstableResponseTimeThreshold;
//    }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }


  public static File[] getPerformanceReportDirectory(AbstractBuild<?, ?> build,
                                                     String parserDisplayName, PrintStream logger) {
    File folder = new File(build.getRootDir() + "/" + JVisualizerReportMap.getPerformanceReportFileRelativePath(parserDisplayName, ""));
    File[] listOfFiles = folder.listFiles();
    return listOfFiles;
  }


  /**
   * Gets the Build object entered in the text box "Compare with nth Build"
   *
   * @param build, listener
   * @return build object
   * @throws IOException
   */

  // @psingh5 -
  public AbstractBuild<?, ?> getnthBuild(AbstractBuild<?, ?> build, BuildListener listener)
          throws IOException {
    AbstractBuild<?, ?> nthBuild = build;

    int nextBuildNumber = build.number - nthBuildNumber;

    for (int i = 1; i <= nextBuildNumber; i++) {
      nthBuild = (AbstractBuild<?, ?>) nthBuild.getPreviousBuild();
      if (nthBuild == null)
        return null;
    }
    return (nthBuildNumber == 0) ? null : nthBuild;
  }

  private List<File> getExistingReports(AbstractBuild<?, ?> build, PrintStream logger, String parserDisplayName)
          throws IOException, InterruptedException {
    List<File> localReports = new ArrayList<File>();
    final File localReport[] = getPerformanceReportDirectory(build, parserDisplayName, logger);

    for (int i = 0; i < localReport.length; i++) {

      String name = localReport[i].getName();
      String[] arr = name.split("\\.");

      //skip the serialized jmeter report file
      if (arr[arr.length - 1].equalsIgnoreCase("serialized"))
        continue;

      localReports.add(localReport[i]);
    }
    return localReports;
  }

  public int getNthBuildNumber() {
    return nthBuildNumber;
  }

  public void setNthBuildNumber(int nthBuildNumber) {
    this.nthBuildNumber = Math.max(0, Math.min(nthBuildNumber, Integer.MAX_VALUE));
  }
//
//    public boolean getModeOfThreshold () {
//      return modeOfThreshold;
//    }
//
//    public void setModeOfThreshold ( boolean modeOfThreshold){
//      this.modeOfThreshold = modeOfThreshold;
//    }

  public boolean getCompareBuildPrevious() {
    return compareBuildPrevious;
  }

  public void setCompareBuildPrevious(boolean compareBuildPrevious) {
    this.compareBuildPrevious = compareBuildPrevious;
  }

  public boolean isModeThroughput() {
    return modeThroughput;
  }

  public void setModeThroughput(boolean modeThroughput) {
    this.modeThroughput = modeThroughput;
  }
}





