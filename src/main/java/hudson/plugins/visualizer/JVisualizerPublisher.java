package hudson.plugins.visualizer;

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
      return "/plugin/visualizer/help.html";
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
  public JVisualizerPublisher(
                              int nthBuildNumber,
                              boolean compareBuildPrevious,
                              List<? extends JVisualizerParser> parsers
                            ) {


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
   * look for visualizer reports based in the configured parameter includes.
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
    Result failedResult = Result.FAILURE;
    EnvVars env = build.getEnvironment(listener);

    System.out.println("BUILD NUM: "+env.get("BUILD_NUMBER"));

      // add the report to the build object.
      JVisualizerBuildAction a = new JVisualizerBuildAction(env,build, logger, parsers );
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
        for (File f : localReports) {
          //System.out.println("name of file: "+f.getName());
        }
        Collection<JVisualizerReport> parsedReports = parser.parse(build, localReports, listener);

        for (JVisualizerReport r : parsedReports) {
          if (r.getReportStatus()) {
            build.setResult(Result.FAILURE);
          }
        }
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


  public boolean getCompareBuildPrevious() {
    return compareBuildPrevious;
  }

  public void setCompareBuildPrevious(boolean compareBuildPrevious) {
    this.compareBuildPrevious = compareBuildPrevious;
  }

}





