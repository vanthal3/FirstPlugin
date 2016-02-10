package hudson.plugins.performance;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.*;
import java.util.Collection;

/**
 * Parses performance result files into {@link JmeterVisualizer}s. This object
 * is persisted with {@link JmeterVisualizerPublisher} into the project
 * configuration.
 * 
 * <p>
 * Subtypes can define additional parser-specific parameters as instance fields.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class JmeterVisualizerParser implements
    Describable<JmeterVisualizerParser>, ExtensionPoint {
  /**
   * GLOB patterns that specify the performance report.
   */
  public final String glob;

  @DataBoundConstructor
  protected JmeterVisualizerParser(String glob) {
    this.glob = (glob == null || glob.length() == 0) ? getDefaultGlobPattern()
        : glob;
  }

  public JmeterVisualizerParserDescriptor getDescriptor() {
    return (JmeterVisualizerParserDescriptor) Hudson.getInstance()
        .getDescriptorOrDie(getClass());
  }

  /**
   * Parses the specified reports into {@link JmeterVisualizer}s.
   */
  public abstract Collection<JmeterVisualizer> parse(
      AbstractBuild<?, ?> build, Collection<File> reports, TaskListener listener)
      throws IOException;

  public abstract String getDefaultGlobPattern();

  /**
   * All registered implementations.
   */
  public static ExtensionList<JmeterVisualizerParser> all() {
    return Hudson.getInstance().getExtensionList(JmeterVisualizerParser.class);
  }

  public String getReportName() {
    return this.getClass().getName().replaceAll("^.*\\.(\\w+)Parser.*$", "$1");
  }

}
