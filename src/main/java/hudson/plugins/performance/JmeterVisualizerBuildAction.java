package hudson.plugins.performance;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.StreamTaskListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class JmeterVisualizerBuildAction implements Action, StaplerProxy {
  private final AbstractBuild<?, ?> build;

  /**
   * Configured parsers used to parse reports in this build.
   * For compatibility reasons, this can be null.
   */
  private final List<JmeterVisualizerParser> parsers;

  private transient final PrintStream hudsonConsoleWriter;

  private transient WeakReference<JmeterVisualizerMap> performanceReportMap;

  private static final Logger logger = Logger.getLogger(JmeterVisualizerBuildAction.class.getName());


  public JmeterVisualizerBuildAction(AbstractBuild<?, ?> pBuild, PrintStream logger,
                                     List<JmeterVisualizerParser> parsers) {
    build = pBuild;
    hudsonConsoleWriter = logger;
    this.parsers = parsers;
  }

  public JmeterVisualizerParser getParserByDisplayName(String displayName) {
    if (parsers != null)
      for (JmeterVisualizerParser parser : parsers)
        if (parser.getDescriptor().getDisplayName().equals(displayName))
          return parser;
    return null;
  }

  public String getDisplayName() {
     return "Individual Build";
  }

  public String getIconFileName() {
    return "graph.gif";
  }

  public String getUrlName() {
    return "visualizerBA";
  }

  public JmeterVisualizerMap getTarget() {
    return getPerformanceReportMap();
  }

  public AbstractBuild<?, ?> getBuild() {
    return build;
  }

  PrintStream getHudsonConsoleWriter() {
    return hudsonConsoleWriter;
  }

  public JmeterVisualizerMap getPerformanceReportMap() {
    JmeterVisualizerMap reportMap = null;
    WeakReference<JmeterVisualizerMap> wr = this.performanceReportMap;
    if (wr != null) {
      reportMap = wr.get();
      if (reportMap != null)
        return reportMap;
    }

    try {
      reportMap = new JmeterVisualizerMap(this, StreamTaskListener.fromStderr());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error creating new JmeterVisualizerMap()", e);
    }
    this.performanceReportMap = new WeakReference<JmeterVisualizerMap>(
        reportMap);
    return reportMap;
  }

  public void setPerformanceReportMap(
      WeakReference<JmeterVisualizerMap> performanceReportMap) {
    this.performanceReportMap = performanceReportMap;
  }
}
