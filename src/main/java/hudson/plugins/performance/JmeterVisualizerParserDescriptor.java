package hudson.plugins.performance;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class JmeterVisualizerParserDescriptor extends
    Descriptor<JmeterVisualizerParser> {

  /**
   * Internal unique ID that distinguishes a parser.
   */
  public final String getId() {
    return getClass().getName();
  }

  /**
   * Returns all the registered {@link JmeterVisualizerParserDescriptor}s.
   */
  public static DescriptorExtensionList<JmeterVisualizerParser, JmeterVisualizerParserDescriptor> all() {
    return Hudson.getInstance().<JmeterVisualizerParser, JmeterVisualizerParserDescriptor>getDescriptorList(JmeterVisualizerParser.class);
  }

  public static JmeterVisualizerParserDescriptor getById(String id) {
    for (JmeterVisualizerParserDescriptor d : all())
      if (d.getId().equals(id))
        return d;
    return null;
  }
}
