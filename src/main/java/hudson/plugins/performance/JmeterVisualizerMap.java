package hudson.plugins.performance;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import hudson.model.TaskListener;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import java.io.FilenameFilter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Root object of a performance report.
 */
public class JmeterVisualizerMap implements ModelObject {

  /**
   * The {@link JmeterVisualizerBuildAction} that this report belongs to.
   */
  private transient JmeterVisualizerBuildAction buildAction;
  /**
   * {@link JmeterVisualizer}s are keyed by
   * {@link JmeterVisualizer#reportFileName}
   * 
   * Test names are arbitrary human-readable and URL-safe string that identifies
   * an individual report.
   */
  private Map<String, JmeterVisualizer> performanceReportMap = new LinkedHashMap<String, JmeterVisualizer>();
  private static final String PERFORMANCE_REPORTS_DIRECTORY = "performance-reports";
  private static final String PLUGIN_NAME = "performance";
  private static final String TRENDREPORT_LINK = "trendReport";

  private static AbstractBuild<?, ?> currentBuild = null;

  /**
   * Parses the reports and build a {@link JmeterVisualizerMap}.
   * 
   * @throws IOException
   *           If a report fails to parse.
   */
  JmeterVisualizerMap(final JmeterVisualizerBuildAction buildAction,
                      TaskListener listener) throws IOException {
    this.buildAction = buildAction;
    parseReports(getBuild(), listener, new PerformanceReportCollector() {

      public void addAll(Collection<JmeterVisualizer> reports) {
        for (JmeterVisualizer r : reports) {
          r.setBuildAction(buildAction);
          performanceReportMap.put(r.getReportFileName(), r);
          System.out.println("NAME OF REPORT "+r.getReportFileName());
        }
      }
    }, null);
  }

  private void addAll(Collection<JmeterVisualizer> reports) {
    for (JmeterVisualizer r : reports) {
      r.setBuildAction(buildAction);
      performanceReportMap.put(r.getReportFileName(), r);
    }
  }

  public AbstractBuild<?, ?> getBuild() {
    return buildAction.getBuild();
  }

  JmeterVisualizerBuildAction getBuildAction() {
    return buildAction;
  }

  public String getDisplayName() {
    return Messages.Report_DisplayName();
  }

  public List<JmeterVisualizer> getPerformanceListOrdered() {
    List<JmeterVisualizer> listPerformance = new ArrayList<JmeterVisualizer>(
        getPerformanceReportMap().values());
    Collections.sort(listPerformance);
    return listPerformance;
  }

  public Map<String, JmeterVisualizer> getPerformanceReportMap() {
    return performanceReportMap;
  }

  /**
   * <p>
   * Give the Performance report with the parameter for name in Bean
   * </p>
   * 
   * @param performanceReportName
   * @return
   */
  public JmeterVisualizer getPerformanceReport(String performanceReportName) {
    return performanceReportMap.get(performanceReportName);
  }
//
//  /**
//   * Get a URI report within a Performance report file
//   *
//   * @param uriReport
//   *          "Performance report file name";"URI name"
//   * @return
//   */
////  public UriReport getUriReport(String uriReport) {
////    if (uriReport != null) {
////      String uriReportDecoded;
////      try {
////        uriReportDecoded = URLDecoder
////            .decode(uriReport.replace(UriReport.END_PERFORMANCE_PARAMETER, ""),
////                "UTF-8");
////      } catch (UnsupportedEncodingException e) {
////        e.printStackTrace();
////        return null;
////      }
////      StringTokenizer st = new StringTokenizer(uriReportDecoded,
////          GraphConfigurationDetail.SEPARATOR);
////      return getPerformanceReportMap().get(st.nextToken()).getUriReportMap()
//          .get(st.nextToken());
//    } else {
//      return null;
//    }
//  }

  public String getUrlName() {
    return PLUGIN_NAME;
  }

  void setBuildAction(JmeterVisualizerBuildAction buildAction) {
    this.buildAction = buildAction;
  }

  public void setPerformanceReportMap(
      Map<String, JmeterVisualizer> performanceReportMap) {
    this.performanceReportMap = performanceReportMap;
  }

  public static String getPerformanceReportFileRelativePath(
      String parserDisplayName, String reportFileName) {
    return getRelativePath(parserDisplayName, reportFileName);
  }

  public static String getPerformanceReportDirRelativePath() {
    return getRelativePath();
  }

  private static String getRelativePath(String... suffixes) {
    StringBuilder sb = new StringBuilder(100);
    sb.append(PERFORMANCE_REPORTS_DIRECTORY);
    for (String suffix : suffixes) {
      sb.append(File.separator).append(suffix);
    }
    return sb.toString();
  }

  /**
   * <p>
   * Verify if the JmeterVisualizer exist the performanceReportName must to be
   * like it is in the build
   * </p>
   * 
   * @param performanceReportName
   * @return boolean
   */
  public boolean isFailed(String performanceReportName) {
    return getPerformanceReport(performanceReportName) == null;
  }

  public void doRespondingTimeGraph(StaplerRequest request,
      StaplerResponse response) throws IOException {
    String parameter = request.getParameter("performanceReportPosition");
    AbstractBuild<?, ?> previousBuild = getBuild();
    final Map<AbstractBuild<?, ?>, Map<String, JmeterVisualizer>> buildReports = new LinkedHashMap<AbstractBuild<?, ?>, Map<String, JmeterVisualizer>>();
    while (previousBuild != null) {
      final AbstractBuild<?, ?> currentBuild = previousBuild;
      parseReports(currentBuild, TaskListener.NULL,
          new PerformanceReportCollector() {

            public void addAll(Collection<JmeterVisualizer> parse) {
              for (JmeterVisualizer jmeterVisualizer : parse) {
                if (buildReports.get(currentBuild) == null) {
                  Map<String, JmeterVisualizer> map = new LinkedHashMap<String, JmeterVisualizer>();
                  buildReports.put(currentBuild, map);
                }
                buildReports.get(currentBuild).put(
                    jmeterVisualizer.getReportFileName(), jmeterVisualizer);
              }
            }
          }, parameter);
      previousBuild = previousBuild.getPreviousCompletedBuild();
    }
    // Now we should have the data necessary to generate the graphs!
    DataSetBuilder<String, NumberOnlyBuildLabel> dataSetBuilderAverage = new DataSetBuilder<String, NumberOnlyBuildLabel>();
    for (AbstractBuild<?, ?> currentBuild : buildReports.keySet()) {
      NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(currentBuild);
      JmeterVisualizer report = buildReports.get(currentBuild).get(parameter);
      dataSetBuilderAverage.add(report.getAverage(),
          Messages.ProjectAction_Average(), label);
    }
    ChartUtil.generateGraph(request, response, JmeterVisualizerProjectAction
        .createRespondingTimeChart(dataSetBuilderAverage.build()), 400, 200);
  }

  public void doSummarizerGraph(StaplerRequest request, StaplerResponse response)
      throws IOException {
    String parameter = request.getParameter("performanceReportPosition");
    AbstractBuild<?, ?> previousBuild = getBuild();
    final Map<AbstractBuild<?, ?>, Map<String, JmeterVisualizer>> buildReports = new LinkedHashMap<AbstractBuild<?, ?>, Map<String, JmeterVisualizer>>();

    while (previousBuild != null) {
      final AbstractBuild<?, ?> currentBuild = previousBuild;
      parseReports(currentBuild, TaskListener.NULL,
          new PerformanceReportCollector() {

            public void addAll(Collection<JmeterVisualizer> parse) {
              for (JmeterVisualizer jmeterVisualizer : parse) {
                if (buildReports.get(currentBuild) == null) {
                  Map<String, JmeterVisualizer> map = new LinkedHashMap<String, JmeterVisualizer>();
                  buildReports.put(currentBuild, map);
                }
                buildReports.get(currentBuild).put(
                    jmeterVisualizer.getReportFileName(), jmeterVisualizer);
              }
            }
          }, parameter);
      previousBuild = previousBuild.getPreviousCompletedBuild();
    }
    DataSetBuilder<NumberOnlyBuildLabel, String> dataSetBuilderSummarizer = new DataSetBuilder<NumberOnlyBuildLabel, String>();
    for (AbstractBuild<?, ?> currentBuild : buildReports.keySet()) {
      NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(currentBuild);
      JmeterVisualizer report = buildReports.get(currentBuild).get(parameter);

      // Now we should have the data necessary to generate the graphs!
//      for (Integer key : report.getUriReportMap().keySet()) {
//        Long methodAvg = report.getUriReportMap().get(key).getAverage();
//       // dataSetBuilderSummarizer.add(methodAvg, label, key);
//      }
      ;
    }
    ChartUtil.generateGraph(
        request,
        response,
        JmeterVisualizerProjectAction.createSummarizerChart(
            dataSetBuilderSummarizer.build(), "ms",
            Messages.ProjectAction_RespondingTime()), 400, 200);
  }

  private void parseReports(AbstractBuild<?, ?> build, TaskListener listener,
      PerformanceReportCollector collector, final String filename)
      throws IOException {
    File repo = new File(build.getRootDir(),
        JmeterVisualizerMap.getPerformanceReportDirRelativePath());

    // files directly under the directory are for JMeter, for compatibility
    // reasons.
    File[] files = repo.listFiles(new FileFilter() {

      public boolean accept(File f) {
        return !f.isDirectory() && !f.getName().endsWith(".serialized");
      }
    });
    // this may fail, if the build itself failed, we need to recover gracefully
    if (files != null) {
      addAll(new JtlFileParser("").parse(build, Arrays.asList(files), listener));
    }

    // otherwise subdirectory name designates the parser ID.
    File[] dirs = repo.listFiles(new FileFilter() {

      public boolean accept(File f) {
        return f.isDirectory();
      }
    });
    // this may fail, if the build itself failed, we need to recover gracefully
    if (dirs != null) {
      for (File dir : dirs) {
        JmeterVisualizerParser p = buildAction.getParserByDisplayName(dir
            .getName());
        if (p != null) {
          File[] listFiles = dir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
              if (filename == null && !name.endsWith(".serialized")) {
                return true;
              }
              if (name.equals(filename)) {
                return true;
              }
              return false;
            }
          });
          try {
            collector.addAll(p.parse(build, Arrays.asList(listFiles), listener));
          } catch (IOException ex) {
            listener.getLogger().println("Unable to process directory '"+ dir+"'.");
            ex.printStackTrace(listener.getLogger());
          }
        }
      }
    }

    addPreviousBuildReports();
  }

  private void addPreviousBuildReports() {

    // Avoid parsing all builds.
    if (JmeterVisualizerMap.currentBuild == null) {
      JmeterVisualizerMap.currentBuild = getBuild();
    } else {
      if (JmeterVisualizerMap.currentBuild != getBuild()) {
        JmeterVisualizerMap.currentBuild = null;
        return;
      }
    }

    AbstractBuild<?, ?> previousBuild = getBuild().getPreviousCompletedBuild();
    if (previousBuild == null) {
      return;
    }

    JmeterVisualizerBuildAction previousPerformanceAction = previousBuild
        .getAction(JmeterVisualizerBuildAction.class);
    if (previousPerformanceAction == null) {
      return;
    }

    JmeterVisualizerMap previousJmeterVisualizerMap = previousPerformanceAction
        .getPerformanceReportMap();
    if (previousJmeterVisualizerMap == null) {
      return;
    }

    for (Map.Entry<String, JmeterVisualizer> item : getPerformanceReportMap()
        .entrySet()) {
      JmeterVisualizer lastReport = previousJmeterVisualizerMap
          .getPerformanceReportMap().get(item.getKey());
      if (lastReport != null) {
        item.getValue().setLastBuildReport(lastReport);
      }
    }
  }

  private interface PerformanceReportCollector {

    public void addAll(Collection<JmeterVisualizer> parse);
  }

//  public Object getDynamic(final String link, final StaplerRequest request,
//      final StaplerRequest response) {
//    if (TRENDREPORT_LINK.equals(link)) {
//      return createTrendReportGraphs(request);
//    } else {
//      return null;
//    }
//  }
//
////  public Object createTrendReportGraphs(final StaplerRequest request) {
//    String filename = getTrendReportFilename(request);
//    JmeterVisualizer report = performanceReportMap.get(filename);
//    AbstractBuild<?, ?> build = getBuild();
//
//    TrendReportGraphs trendReport = new TrendReportGraphs(build.getProject(),
//        build, request, filename, report);
//
//    return trendReport;
//  }

  private String getTrendReportFilename(final StaplerRequest request) {
    JmeterVisualizerPosition jmeterVisualizerPosition = new JmeterVisualizerPosition();
    request.bindParameters(jmeterVisualizerPosition);
    return jmeterVisualizerPosition.getPerformanceReportPosition();
  }
}
