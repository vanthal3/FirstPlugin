package hudson.plugins.performance;

import java.util.Date;
import java.io.Serializable;
import java.util.Map;
 import java.util.HashMap;
import java.util.Random;

/**
 * Information about a particular HTTP request such as
 * its duration, success, and/or list of assertions.
 * 
 * This object belongs under {@link UriReport}.
 */
public class HttpSample implements Serializable, Cloneable {

	private static final long serialVersionUID = -3531990216789038711L;

	private Map<Integer, AssertionResult> assertions;

	private String httpCode = "";

	public Map<String, Map<Integer, AssertionResult>> ar = new HashMap<String, Map<Integer, AssertionResult>>();

	private Integer httpId;

	public HttpSample(Integer myid){
		this.httpId=myid;

	}

	public HttpSample(){}

	private long duration;

	private boolean successful;

	public String responseMessage;
	private boolean errorObtained;

	private Date date;

	private String uri;

	public String threadname;

	private double sizeInKb;

	// Summarizer fields
	private long summarizerMin;

	private long summarizerMax;

	public String errorCount;

	private float summarizerErrors;

	private long summarizerSamples;

	public Integer id=101;

	//private ArrayList<AssertionResult> assertions= new ArrayList<AssertionResult>();

//	public int addAr(String parentUri, String name) {
//		AssertionResult e = new AssertionResult();
//		if (ar.containsKey(parentUri)) {
//			id++;
//			//update assertion Map
//			ar.get(parentUri).put(id, e);
//
//		} else {
//			assertions = new HashMap<Integer, AssertionResult>();
//			assertions.put(id, e); //update assertion MAP
//			ar.put(parentUri, assertions); //update entire map
//		}
//
////		System.out.println("the id befor put and before incrementing"+ id);
////		id++;
////		assertions=new HashMap<Integer, AssertionResult>();
////		assertions.put(id, e);
////		System.out.println("the id befor put and AFTER incrementing"+ id);
////
////		System.out.println("added in assertions ar NAME "+ assertions.size() + " with ID: " + id +" NAME: "+ assertions.containsKey(id) +" and value "+ assertions.containsValue(e));
////		System.out.println("GET THE DAMN VALUE" + assertions.get(id));
////		ar.put(parentUri, assertions);
////		System.out.println("before I return, the id is: "+ id);
//		return id;
//	}


	public void setHttpId(Integer id){
		this.httpId=id;
	}

	public int addAr(){

		if(assertions ==null){
			assertions=new HashMap<Integer, AssertionResult>();
			int arId=createID();
			AssertionResult ar =new AssertionResult(arId);
			assertions.put(arId, ar);

		}
		int arId=createID();
		AssertionResult ar =new AssertionResult(arId);
		assertions.put(arId, ar);
		//System.out.println("just inserted: "+arId+" with: "+ ar.getName()+" ==========");

		return arId;

	}

	public Map<Integer, AssertionResult> getAssertions(){
		return assertions;

	}
	@Override
	public Object clone()throws CloneNotSupportedException{
		return super.clone();
	}

	public void setErrorCount(String ec){
		this.errorCount=ec;
	}

	public String getErrorCount(){
		return errorCount;
	}

	public void setThreadName(String threadn){
		this.threadname=threadn;
	}

	public String getThreadname(){
		return threadname;
	}

	public String getResponseMessage(){
		return responseMessage;
	}

	public AssertionResult getArObject(Integer id){
//		System.out.println("Id of object I want to get "+ id);
//		System.out.println("size of assertions: "+assertions.size());
//		System.out.println("getting Ar's Name "+ assertions.get(id));
		return assertions.get(id);
	}

//	public AssertionResult getArObject(String aRname){
//		for (AssertionResult aR : assertions){
//			if (aR.getName().equals(aRname)){
//				return aR;
//			}
//		}
//	}

	public long getDuration() {
		return duration;
	}

	public void setResponseMessage(String rm){
		this.responseMessage=rm;
	}

	public Date getDate() {
		return date;
	}

	public String getUri() {
		return uri;
	}

	public long getSummarizerSamples() {
		return summarizerSamples;
	}

	public long getSummarizerMin() {
		return summarizerMin;
	}

	public long getSummarizerMax() {
		return summarizerMax;
	}

	public float getSummarizerErrors() {
		return summarizerErrors;
	}

	public boolean isFailed() {
		return !isSuccessful();
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public void setErrorObtained(boolean errorObtained) {
		this.errorObtained = errorObtained;
	}

	public boolean hasError() {
		return errorObtained;
	}

	public void setDate(Date time) {
		this.date = time;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getHttpId(){
		return httpId;
	}

	public String getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(String httpCode) {
		this.httpCode = httpCode;
	}

	public void setSummarizerSamples(long summarizerSamples) {
		this.summarizerSamples = summarizerSamples;
	}

	public void setSummarizerMin(long summarizerMin) {
		this.summarizerMin = summarizerMin;
	}

	public void setSummarizerMax(long summarizerMax) {
		this.summarizerMax = summarizerMax;
	}

	public void setSummarizerErrors(float summarizerErrors) {
		this.summarizerErrors = summarizerErrors;
	}

	public int compareTo(HttpSample o) {
		return (int) (getDuration() - o.getDuration());
	}

	public double getSizeInKb() {
		return sizeInKb;
	}

	public void setSizeInKb(double d) {
		this.sizeInKb = d;
	}

	public boolean isErrorObtained() {
		return errorObtained;
	}

	public Integer createID(){

		Random r = new Random();
		Integer id = r.nextInt(9999);
		return id;
	}
}
