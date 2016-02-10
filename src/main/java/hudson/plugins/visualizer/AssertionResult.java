package hudson.plugins.visualizer;


import java.io.Serializable;


/**
 * An HttpSample may send AssertionResults which holds information
 * about the success of the assertions.
 *
 * This object belongs under {@link HttpSample}.
 *
 */


public class AssertionResult implements Serializable {
    private String Name;
    public String Failure;
    public String Error;
    public String FailureMessage;
    public HttpSample Parent;
    public int id;


    public AssertionResult(int myid){
        this.id = myid;
    }


    public void setParent(HttpSample parent){
        this.Parent=parent;
    }

    public HttpSample getParent(){
        return Parent;
    }

    public void setName(String name){
        this.Name=name;
    }

    public String getName(){
        return this.Name;
    }

    public void setFailure(String failure){
        this.Failure=failure;
    }

    public String isFailure(){
        return this.Failure;
    }

    public void setError(String error){
        this.Error=error;
    }

    public String isError(){
        return Error;
    }

    public void setFailureMessage(String fm){
        this.FailureMessage= fm;
    }

    public String getFailureMessage(){
        return FailureMessage;
    }
}
