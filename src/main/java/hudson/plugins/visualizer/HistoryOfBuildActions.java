package hudson.plugins.visualizer;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.AbstractProject;

import java.util.*;
import org.kohsuke.stapler.StaplerRequest;
import java.util.List;


/**
 *
 *
 *
 */

public class HistoryOfBuildActions implements ModelObject {

    AbstractProject<?, ?> project;
    StaplerRequest request;

    HistoryOfBuildActions(final AbstractProject<?, ?> p,
                          final StaplerRequest r){

        this.project=p;
        this.request=r;

    }

    public ArrayList<JVisualizerBuildAction> getBuildHistory(){
        ArrayList<JVisualizerBuildAction> builds=new ArrayList<JVisualizerBuildAction>();


        List<? extends AbstractBuild<?, ?>> buildsFromProject = project.getBuilds();
        //iterate over each build
        for (AbstractBuild<?, ?> currentBuild : buildsFromProject) {

            JVisualizerBuildAction JVBuildAction = currentBuild
                    .getAction(JVisualizerBuildAction.class);
            if (JVBuildAction == null) {
                continue;
            }


            builds.add(JVBuildAction);

        }
        return builds;

    }


    public String getDisplayName() {
        return "Failed Test Cases";
    }


}
