package hudson.plugins.visualizer;

import hudson.model.AbstractProject;
import hudson.model.Action;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public final class JVisualizerProjectAction implements Action {

  private static final String BUILD_HISTORY = "BuildHistoryVi";

  private static final String PLUGIN_NAME = "visualizer";

  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1L;

  /** Logger. */
  private static final Logger LOGGER = Logger
      .getLogger(JVisualizerProjectAction.class.getName());

  public final AbstractProject<?, ?> project;

  private transient List<String> performanceReportList;

  public String getDisplayName() {
    //return "yolo";
    return Messages.ProjectAction_DisplayName();
  }

  public String getIconFileName() {
    return "graph.gif";
  }

  public String getUrlName() {
    return PLUGIN_NAME;
  }

  public JVisualizerProjectAction(AbstractProject<?, ?> project) {
    this.project = project;
  }



    private String getPerformanceReportNameFile(StaplerRequest request) {
        JVisualizerPosition jVisualizerPosition = new JVisualizerPosition();
        request.bindParameters(jVisualizerPosition);
        return getPerformanceReportNameFile(jVisualizerPosition);
    }

    private String getPerformanceReportNameFile(final JVisualizerPosition jVisualizerPosition) {
        String performanceReportNameFile = jVisualizerPosition.getPerformanceReportPosition();
      System.out.println("================= report position: "+ jVisualizerPosition);
        if (performanceReportNameFile == null) {
            if (getPerformanceReportList().size() == 1) {
                performanceReportNameFile = getPerformanceReportList().get(0);
            }
        }
        return performanceReportNameFile;
    }


  public AbstractProject<?, ?> getProject() {
    return project;
  }

  public List<String> getPerformanceReportList() {
    this.performanceReportList = new ArrayList<String>(0);
    if (null == this.project) {
      return performanceReportList;
    }
    if (null == this.project.getSomeBuildWithWorkspace()) {
      return performanceReportList;
    }
    File file = new File(this.project.getSomeBuildWithWorkspace().getRootDir(),
        JVisualizerReportMap.getPerformanceReportDirRelativePath());
    if (!file.isDirectory()) {
      return performanceReportList;
    }

    for (File entry : file.listFiles()) {
      if (entry.isDirectory()) {
        for (File e : entry.listFiles()) {
          if (!e.getName().endsWith(".serialized") && !e.getName().endsWith(".serialized-v2")) {
            this.performanceReportList.add(e.getName());
          }
        }
      } else {
        if (!entry.getName().endsWith(".serialized") && !entry.getName().endsWith(".serialized-v2")) {
          this.performanceReportList.add(entry.getName());
        }
      }

    }

    Collections.sort(performanceReportList);

    return this.performanceReportList;
  }

  public void setPerformanceReportList(List<String> performanceReportList) {
    this.performanceReportList = performanceReportList;
  }



  /**
   * Returns the graph configuration for this project.
   *
   * @param link
   *          not used
   * @param request
   *          Stapler request
   * @param response
   *          Stapler response
   * @return the dynamic result of the analysis (detail page).
   */
  public Object getDynamic(final String link, final StaplerRequest request,
      final StaplerResponse response) {
     if (BUILD_HISTORY.equals(link)){
      return showHistoryFailedTC(request);
    } else {
      return null;
    }
  }

  /**
   * Creates a view for the  history visualizer for the current user.
   *
   * @param request
   *          Stapler request
   * @return a view to configure the result visualizer for the current user
   */

  private Object showHistoryFailedTC(final StaplerRequest request){
   HistoryOfBuildActions ft= new HistoryOfBuildActions(project,request);

    return ft;

  }

}
