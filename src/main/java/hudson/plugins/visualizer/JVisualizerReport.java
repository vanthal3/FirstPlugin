package hudson.plugins.visualizer;

import hudson.model.AbstractBuild;
import java.io.IOException;

import java.io.Serializable;
import java.util.*;

import org.xml.sax.SAXException;

/**
 * Represents the core of JmeterVisualizer plugin. It contains all JTL files tested

 *
 * This object belongs under {@link JVisualizerReportMap}.
 */

public class JVisualizerReport extends AbstractReport implements Serializable,
        Comparable<JVisualizerReport> {

  private static final long serialVersionUID = 675698410989941826L;

  private transient JVisualizerBuildAction buildAction;

  private String reportFileName = null;

  /**

   */
  private final Map<Integer, HttpSample> httpSampleMap = new LinkedHashMap<Integer, HttpSample>();


  private JVisualizerReport lastBuildReport;

  /**
   * A lazy cache of all duration values of all HTTP samples in all UriReports, ordered by duration.
   */
  private transient List<Long> durationsSortedBySize = null;

  /**
   * A lazy cache of all UriReports, reverse-ordered.
   */
  private transient List<HttpSample> httpSampleOrdered = null;

  /**
   * The amount of http samples that are not successful.
   */
  private int nbError = 0;

  /**
   * The sum of summarizerErrors values from all samples;
   */
  private float summarizerErrors = 0;

  /**
   * The amount of samples in all uriReports combined.
   */
  private int size;

  /**
   * The duration of all samples combined, in milliseconds.
   */
  private long totalDuration = 0;

  /**
   * The size of all samples combined, in kilobytes.
   */
  private double totalSizeInKB = 0;

  /**
   * The longest duration from all samples, or Long.MIN_VALUE when no samples where processed.
   */
  private long max = Long.MIN_VALUE;

  /**
   * The shortest duration from all samples, or Long.MAX_VALUE when no samples where processed.
   */
  private long min = Long.MAX_VALUE;

  public HttpSample httpSample;

  public int counter=0;

  public static String asStaplerURI(String uri) {
    return uri.replace("http:", "").replaceAll("/", "_");
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  public void addSample(HttpSample pHttpSample) throws SAXException {
    String uri = pHttpSample.getUri();

    //Integer httpID = createID();

    System.out.println("current sample: "+ pHttpSample.getUri() + "CurrentSample HAASH: "+System.identityHashCode(pHttpSample)+
    "SIZE: "+pHttpSample.getAssertions().size());





    //pHttpSample.setHttpId(httpID);
    //System.out.println("but now currentSample/pHttpSample Id is: "+ pHttpSample.getHttpId());



    for(Integer arId : pHttpSample.getAssertions().keySet()){
      System.out.println("Assertion # "+arId+" and my name is: "+pHttpSample.getAssertions().get(arId).getName()+" ========");
    }





    //System.out.println(pHttpSample.getUri()+"/"+pHttpSample.getAssertions().size());


    //Integer uriID=createID();

    //System.out.println("================================== Uri id is: "+ uriID);

//    if (uri == null) {
//      buildAction
//          .getHudsonConsoleWriter()
//          .println("label cannot be empty, please ensure your jmx file specifies "
//              + "name properly for each http sample: skipping httpsample");
//      return;
//    }
    //String staplerUri = JmeterVisualizer.asStaplerURI(uri);
    //setStaplerUri(staplerUri);
    synchronized (httpSampleMap) {
      //httpSample = httpSampleMap.get(httpID);
//      if (httpSampleMap == null) {
//        System.out.println(" httpSample is null, so new, so insert into report Map");
////        System.out.println("getting urireport name: "+ httpSample.getUri() +" from file: "+ getReportFileName()+ " size: "+ httpSampleMap.size()
////                +" and hascode: "+ System.identityHashCode(httpSampleMap));
//
//
//        //httpSample.setHttpSample(pHttpSample);
//
//        httpSampleMap.put(httpID, pHttpSample);
//        System.out.println("I now have: "+ httpSampleMap.size() + " and inserted "+ httpSampleMap.get(httpID).getUri());
//
//        //System.out.println("================================== abt to put into map: "+ httpSample.getUri()+" with ID: "+ httpSample.getUriID());
//
//        //System.out.println("================================== just inserterted "+httpSampleMap.get(uriID)+ " and size: "+httpSampleMap.size());
//        //httpSample.setHttpSample(pHttpSample);
//        //getUriListOrdered();
//       // httpSample.recieved();
//
//      }



      httpSampleMap.put(pHttpSample.getHttpId(), pHttpSample);
//      System.out.println("I now have: "+ httpSampleMap.size() + " and inserted "+ httpSampleMap.get(httpID).getUri()+
//      " and hashcode is: "+ System.identityHashCode(pHttpSample)+ " my HTTP ID IS: "+httpID);
//

      //getUriListOrdered();

//      System.out.println("NAME: "+ pHttpSample.getUri()+ " SIZE: "+pHttpSample.getAssertions().size()+
//      " ID: "+ pHttpSample.getHttpId());
//
//
//      for(Integer arId : pHttpSample.getAssertions().keySet()){
//        System.out.println("Assertion # "+arId+" and my failmsg is: "+pHttpSample.getAssertions().get(arId).getFailureMessage()+" ========");
//      }



      //httpSample.setHttpSample(pHttpSample);

      //uriReport2.setHttpSample(pHttpSample);
      durationsSortedBySize = null;
      httpSampleOrdered = null;

      //System.out.println("getUriListOrdered...");
      //getUriListOrdered();

    }

    if (!pHttpSample.isSuccessful()) {
      nbError++;
    }
    summarizerErrors += pHttpSample.getSummarizerErrors();
    size++;
    totalDuration += pHttpSample.getDuration();
    totalSizeInKB += pHttpSample.getSizeInKb();
    max = Math.max(pHttpSample.getDuration(), max);
    min = Math.min(pHttpSample.getDuration(), min);
  }

  public List<HttpSample> getUriListOrdered() {
    counter++;
    // System.out.println("counter in getlist: "+getReportFileName()+" counter # "+ counter + " size of map: "+ httpSampleMap.size());
//    for (Map.Entry<String, UriReport> testEntry: httpSampleMap.entrySet()){
//
//      System.out.println("TEST MAP: "+"#: "+counter+" "+testEntry.getKey()+"  MY HTTPCODE: "+ testEntry.getValue().getHttpSample().getHttpCode());
//  }
    synchronized (httpSampleMap) {
      if (httpSampleOrdered == null) {
        httpSampleOrdered = new ArrayList<HttpSample>(httpSampleMap.values());
        //Collections.sort(httpSampleOrdered, Collections.reverseOrder());
      }

      // runforLoop(httpSampleOrdered);

      return httpSampleOrdered;
    }

  }


//  public void runforLoop(List<HttpSample> li){
//    for(HttpSample hs: li){
//      System.out.println("======= HS id: "+ System.identityHashCode(hs)+" get code: "+ hs.getHttpCode());
//
//      for(Integer arId : hs.getAssertions().keySet()){
//        System.out.println("Assertion # "+arId+" and my name is: "+hs.getAssertions().get(arId).getName()+" ========");
//      }
//    }
//  }

  public int compareTo(JVisualizerReport jmReport) {
    if (this == jmReport) {
      return 0;
    }
    return getReportFileName().compareTo(jmReport.getReportFileName());
  }
//
//  public void setStaplerUri(String sUri){
//    this.staplerUri=sUri;
//  }
//
//  public String getStaplerUri(){
//    return staplerUri;
//  }

  public int countErrors() {
    return nbError;
  }

  public double errorPercent() {
    if (ifSummarizerParserUsed(reportFileName)) {
      if (httpSampleMap.size() == 0) return 0;
      return summarizerErrors / httpSampleMap.size();
    } else {
      return size() == 0 ? 0 : ((double) countErrors()) / size() * 100;
    }
  }

  public long getAverage() {
    if (size == 0) {
      return 0;
    }

    return totalDuration / size;
  }

  public double getAverageSizeInKb() {
    if (size == 0) {
      return 0;
    }
    return roundTwoDecimals(totalSizeInKB / size);
  }

  private long getDurationAt(double percentage) {
    if (percentage < 0 || percentage > 1) {
      throw new IllegalArgumentException("Argument 'percentage' must be a value between 0 and 1 (inclusive)");
    }

    if (size == 0) {
      return 0;
    }
//
//    synchronized (httpSampleMap) {
//      if (durationsSortedBySize == null) {
//        durationsSortedBySize = new ArrayList<Long>();
//        for (HttpSample currentReport : httpSampleMap.values()) {
//          durationsSortedBySize.addAll(currentReport.getDurations());
//        }
//        Collections.sort(durationsSortedBySize);
//      }
//      return durationsSortedBySize.get((int) (durationsSortedBySize.size() * percentage));
//    }
    return 1;
  }

  public long get90Line() {
    return getDurationAt(.9);
  }

  public long getMedian() {
    return getDurationAt(.5);
  }

  public String getHttpCode() {
    return "";
  }

  public AbstractBuild<?, ?> getBuild() {
    return buildAction.getBuild();
  }

  JVisualizerBuildAction getBuildAction() {
    return buildAction;
  }

  public String getDisplayName() {
    return Messages.Report_DisplayName();
  }

  public HttpSample getDynamic(String token) throws IOException {
    return getHttpSampleMap().get(token);
  }

  public long getMax() {
    return max;
  }

  public double getTotalTrafficInKb() {
    return roundTwoDecimals(totalSizeInKB);
  }

  public long getMin() {
    return min;
  }

  public String getReportFileName() {
    return reportFileName;
  }



  public Map<Integer, HttpSample> getHttpSampleMap() {
    return httpSampleMap;
  }

  void setBuildAction(JVisualizerBuildAction buildAction) {
    this.buildAction = buildAction;
  }

  public void setReportFileName(String reportFileName) {
    this.reportFileName = reportFileName;
  }

  public int size() {
    return size;
  }

  public void setLastBuildReport(JVisualizerReport lastBuildReport) {
    Map<Integer, HttpSample> lastBuildUriReportMap = lastBuildReport
            .getHttpSampleMap();
    for (Map.Entry<Integer, HttpSample> item : httpSampleMap.entrySet()) {
      HttpSample lastBuildUri = lastBuildUriReportMap.get(item.getKey());
//      if (lastBuildUri != null) {
//        item.getValue().addLastBuildUriReport(lastBuildUri);
//      }
    }
    this.lastBuildReport = lastBuildReport;
  }

  public long getAverageDiff() {
    if (lastBuildReport == null) {
      return 0;
    }
    return getAverage() - lastBuildReport.getAverage();
  }

  public long getMedianDiff() {
    if (lastBuildReport == null) {
      return 0;
    }
    return getMedian() - lastBuildReport.getMedian();
  }

  public double getErrorPercentDiff() {
    if (lastBuildReport == null) {
      return 0;
    }
    return errorPercent() - lastBuildReport.errorPercent();
  }


  public Integer createID(){

    Random r = new Random();
    Integer id = r.nextInt(9999);
    return id;
  }

  public String getLastBuildHttpCodeIfChanged() {
    return "";
  }

  public int getSizeDiff() {
    if (lastBuildReport == null) {
      return 0;
    }
    return size() - lastBuildReport.size();
  }

  /**
   * Check if the filename of the file being parsed is being parsed by a
   * summarized parser (JMeterSummarizer).
   *
   * @param filename
   *          name of the file being parsed
   * @return boolean indicating usage of summarized parser
   */
  public boolean ifSummarizerParserUsed(String filename) {
    List<JVisualizerParser> list = buildAction.getBuild().getProject()
            .getPublishersList().get(JVisualizerPublisher.class).getParsers();

    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).getDescriptor().getDisplayName()
              .equals("JmeterSummarizer")) {
        String fileExt = list.get(i).glob;
        String parts[] = fileExt.split("\\s*[;:,]+\\s*");
        for (String path : parts) {
          if (filename.endsWith(path.substring(5))) {
            return true;
          }
        }
      } else if (list.get(i).getDescriptor().getDisplayName()
              .equals("Iago")) {
        return true;
      }

    }
    return false;
  }

  private double roundTwoDecimals(double d) {
    synchronized (twoDForm) {
      return Double.valueOf(twoDForm.format(d));
    }
  }
}
