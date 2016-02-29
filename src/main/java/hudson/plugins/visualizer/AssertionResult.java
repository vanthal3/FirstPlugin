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
    public String Name;
    public String Failure;
    public String Error;
    public String FailureMessage;
    private int id;


    public AssertionResult(int myid){
        this.id = myid;
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

    public int getId(){return id;}

    public void setFailureMessage(String fm){
        this.FailureMessage= fm;
    }

    public String getFailureMessage(){
        if (FailureMessage==null){
            String s=new String();
            s="no failure msg";
            return s;
        }
        return FailureMessage;
    }

}
