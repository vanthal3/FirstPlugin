package hudson.plugins.visualizer;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.AbstractProject;

import java.util.*;
import org.kohsuke.stapler.StaplerRequest;
import java.util.List;


public class FailedTestCases implements ModelObject {
    public static Map<String, ArrayList<JVisualizerBuildAction>> failedTests = new HashMap<String, ArrayList<JVisualizerBuildAction>>();

    //BUILD# ON FILE:

    AbstractProject<?, ?> project;
    StaplerRequest request;
    String xmlDir=null;

    FailedTestCases(final AbstractProject<?, ?> p,
                    final StaplerRequest r){

        this.project=p;
        this.request=r;

    }


    public ArrayList<JVisualizerBuildAction> getAllBuildActions(){
        ArrayList<JVisualizerBuildAction> builds=new ArrayList<JVisualizerBuildAction>();

        //System.out.println("in get all build actions");

        List<? extends AbstractBuild<?, ?>> buildsFromProject = project.getBuilds();
        int c=0;
        //iterate over each build
        for (AbstractBuild<?, ?> currentBuild : buildsFromProject) {
            //EnvVars env = currentBuild.getEnvironment(manager.listner);

            JVisualizerBuildAction JVBuildAction = currentBuild
                    .getAction(JVisualizerBuildAction.class);
            if (JVBuildAction == null) {
                continue;
            }

            // System.out.println("for currentBuild: "+JVBuildAction.getMyBuildNum());

            builds.add(JVBuildAction);
            //System.out.println("build num: "+JVBuildAction.getMyBuildNum()+" build Date: "+ JVBuildAction.getMyDate());
//           for(JVisualizerReport jv: JVBuildAction.getJVisualizerReportMap().getPerformanceListOrdered()){
//               builds.add(jv);
//
//           }
            //iterate over each report in each build
//            for(JVisualizerReport jvb: JVBuildAction.getJVisualizerReportMap().getPerformanceListOrdered() ){
//                //System.out.println("NAME: "+ jvb.getReportFileName());
//                System.out.println("BUILD#: "+JVBuildAction.getMyBuildNum() + " ON: "+JVBuildAction.getMyDate()+" FILE: "+ jvb.getReportFileName()
//                        +" get my FailedTestMap: "+ jvb.getFailedTests().size());
////                //failedTestsMap.put(JVBuildAction.getMyBuildNum(), JVBuildAction);
//

//
//                //historyInfo.put(jvb.getReportFileName(),builds);
//                // for( : jvb)
//
//            }
//
//        }
        }
        //System.out.println("SIZE OF BUILDS: "+ builds.size());
        //forLoopIt(historyInfo);
        return builds;

    }

//    public void forLoopIt(Map<String, JVisualizerBuildAction> info){
//        System.out.println("for looping it");
//
//        for(Map.Entry<String, JVisualizerBuildAction> e: info.entrySet()){
//            System.out.println("BUILD# "+e.getValue().getMyBuildNum()+" ON: "+e.getValue().getMyDate()+" FILE: "+ e.getKey());
//        }
//    }



    public String getDisplayName() {
        return "Failed Test Cases";
    }


}
