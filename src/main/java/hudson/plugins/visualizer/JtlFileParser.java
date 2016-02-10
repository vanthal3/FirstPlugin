package hudson.plugins.visualizer;

import hudson.Extension;
import java.util.Random;

import java.io.*;
import java.util.Date;
import javax.xml.parsers.SAXParserFactory;

import org.kohsuke.stapler.DataBoundConstructor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for JMeter.
 *
 * @author Kohsuke Kawaguchi
 */
public class JtlFileParser extends AbstractParser {

  private  Integer sampleID;


  @Extension
  public static class DescriptorImpl extends JVisualizerParserDescriptor {
    @Override
    public String getDisplayName() {
      return "JTL File";
    }
  }

  @DataBoundConstructor
  public JtlFileParser(String glob) {
    super(glob);
  }


  @Override
  public String getDefaultGlobPattern() {
    return "**/*.jtl";
  }

  JVisualizerReport parse(File reportFile) throws Exception
  {
    // JMeter stores either CSV or XML in .JTL files.
    final boolean isXml = isXmlFile(reportFile);
    return parseXml(reportFile);

  }

  /**
   * Utility method that checks if the provided file has XML content.
   *
   * This implementation looks for the first non-empty file. If an XML prolog appears there, this method returns <code>true</code>, otherwise <code>false</code> is returned.
   *
   * @param file File from which the content is to e analyzed. Cannot be null.
   * @return <code>true</code> if the file content has been determined to be XML, otherwise <code>false</code>.
   */
  public static boolean isXmlFile(File file) throws IOException {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String firstLine;
      while ((firstLine = reader.readLine()) != null ) {
        if (firstLine.trim().length() == 0) continue; // skip empty lines.
        return firstLine != null && firstLine.toLowerCase().trim().startsWith("<?xml ");
      }
      return false;
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  /**
   * A delegate for {@link #parse(File)} that can process XML data.
   */
  JVisualizerReport parseXml(File reportFile) throws Exception
  {
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);

    final JVisualizerReport report = new JVisualizerReport();

    report.setReportFileName(reportFile.getName());
    System.out.println("==========================entering parsexml, new parseReport"+ report.getReportFileName()+" created with hash: "+System.identityHashCode(report));

    factory.newSAXParser().parse(reportFile, new DefaultHandler() {

      public HttpSample sample;

      public HttpSample currentSample;

      public int counter=0;


      StringBuilder sb = new StringBuilder();

      boolean phttp=false;
      boolean pName=false;
      boolean pFailure=false;
      boolean pFailureMessage=false;
      boolean pError=false;
      boolean pAssertion=false;
      Integer arID;

      /**
       * Performance XML log format is in http://jakarta.apache.org/jmeter/usermanual/listeners.html
       *
       * There are two different tags which delimit jmeter samples:
       * - httpSample for http samples
       * - sample for non http samples
       *
       * There are also two different XML formats which we have to handle:
       * v2.0 = "label", "timeStamp", "time", "success"
       * v2.1 = "lb", "ts", "t", "s"
       */
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        sample = new HttpSample(createSampleID());
        if("httpSample".equalsIgnoreCase(qName) || "sample".equalsIgnoreCase(qName)){
          phttp=true;
          //System.out.println("phttp: "+ phttp);
        final String dateValue;
        if (attributes.getValue("ts") != null) {
          dateValue = attributes.getValue("ts");
        } else {
          dateValue = attributes.getValue("timeStamp");
        }
        sample.setDate( new Date(Long.valueOf(dateValue)) );
        //System.out.println("set date: "+ sample.getDate());
         // System.out.println("=======set current date: "+ currentSample.getDate());


          final String durationValue;
        if (attributes.getValue("t") != null) {
          durationValue = attributes.getValue("t");
        } else {
          durationValue = attributes.getValue("time");
        }
        sample.setDuration(Long.valueOf(durationValue));
          //System.out.println("set Duration: "+ sample.getDuration());
          //System.out.println("=======set current duration: "+ currentSample.getDuration());


          final String successfulValue;
        if (attributes.getValue("s") != null) {
          successfulValue = attributes.getValue("s");
        } else {
          successfulValue = attributes.getValue("success");
        }
        sample.setSuccessful(Boolean.parseBoolean(successfulValue));
          //System.out.println("set setSucessful: "+ sample.isSuccessful());
//          System.out.println("=======set current successs: "+ currentSample.isSuccessful());



          final String uriValue;
        if (attributes.getValue("lb") != null) {
          uriValue = attributes.getValue("lb");
        } else {
          uriValue = attributes.getValue("label");
        }
        sample.setUri(uriValue);
          //System.out.println("set uri: "+ sample.getUri());
          //System.out.println("======= set uri: "+ currentSample.getUri());



          final String threadname;
        if (attributes.getValue("tn") != null) {
          threadname = attributes.getValue("tn");
        } else {
          threadname = attributes.getValue("threadname");
        }
        sample.setThreadName(threadname);
         // System.out.println("set threadname: "+ sample.getThreadname());
          //System.out.println("=======set current threadname: "+ currentSample.getThreadname());



          final String errorCount;
        if (attributes.getValue("ec") != null) {
          errorCount = attributes.getValue("ec");
        } else {
          errorCount = attributes.getValue("errorCount");
        }
        sample.setErrorCount(errorCount);
          //System.out.println("set errorCount: "+ sample.getErrorCount());
          //System.out.println("=======set current errorCount: "+ currentSample.getErrorCount());




          final String responseMessage;
        if (attributes.getValue("rm") != null) {
          responseMessage = attributes.getValue("rm");
        } else {
          responseMessage = attributes.getValue("responseMessage");
        }
        sample.setResponseMessage(responseMessage);
          //System.out.println("set response Message: "+ sample.getResponseMessage());
          //System.out.println("=======set current response Message: "+ currentSample.getResponseMessage());



          final String httpCodeValue;
        if (attributes.getValue("rc") != null && attributes.getValue("rc").length() <= 3) {
          httpCodeValue = attributes.getValue("rc");
        } else {
          httpCodeValue = "0";
        }
        sample.setHttpCode(httpCodeValue);
          //System.out.println("set httpcode: "+ sample.getHttpCode());
          //System.out.println("=======set current httpcode: "+ currentSample.getHttpCode());


          final String sizeInKbValue;
        if (attributes.getValue("by") != null) {
          sizeInKbValue = attributes.getValue("by");
        } else {
          sizeInKbValue = "0";
        }
        sample.setSizeInKb(Double.valueOf(sizeInKbValue) / 1024d);
        } else if ("AssertionResult".equalsIgnoreCase(qName)) {
          pAssertion = true;
          //System.out.println("set pAssertion: "+ pAssertion);

        }else if ("name".equalsIgnoreCase(qName)) {
          pName = true;
         // System.out.println("set pName: "+ pName);

        }else if ("failure".equalsIgnoreCase(qName)) {
          pFailure = true;
          //System.out.println("set pFailure: "+ pFailure);

        }else if ("error".equalsIgnoreCase(qName)) {
          pError = true;
          //System.out.println("set pError: "+ pAssertion);

        }else if("failureMessage".equalsIgnoreCase(qName)){
          pFailureMessage=true;
          //System.out.println("set pFailureMessage: "+ pFailureMessage);

        }
      }

              @Override
              public void characters(char[] ch, int start, int length) throws SAXException {
                sb.append(new String(ch, start, length));

//                if(phttp){
//                }
//                if(pAssertion){
//                  arID =sample.addAr();
//                  //System.out.println("the ID in pNAMe:  "+ sample.getUri()+" ------ "+arID);
//
//                  pAssertion=false;
//
//                }
//                if(pName){
//                  sample.getArObject(arID).setName(new String(ch, start, length));
//                  //System.out.println("Name: " +sample.getAr().getName());
//                  pName=false;
//                }
//                if(pFailure){
//                  sample.getArObject(arID).setFailure(new String(ch, start, length));
//                  //System.out.println("failure: " +sample.getAr().isFailure());
//                  pFailure=false;
//                }
//                if(pError){
//                  sample.getArObject(arID).setError(new String(ch, start, length));
//                  //System.out.println("isError: " +sample.getAr().isError());
//                  pError=false;
//                }
//                if(pFailureMessage){
//                  //System.out.println("==============getFailureMessage: sb " +sb.toString()+" ========");
//                  pFailureMessage=false;
//                }
            }


      @Override
      public void endElement(String uri, String localName, String qName) {
        //sample.getArObject(arID).setFailureMessage(sb.toString());
        //System.out.println("failure msg: "+ sb.toString());
        try {
          currentSample =(HttpSample) sample.clone();
//          System.out.println("current sample: "+ currentSample.getUri());
//          for(int arId: currentSample.getAssertions().keySet()){
//
//            System.out.println("Assertion # "+arId+" and my name is: "+currentSample.getAssertions().get(arId).getName()+" ========");
//
//          }


        }catch(CloneNotSupportedException c){

        }

        if ("httpSample".equalsIgnoreCase(qName) || "sample".equalsIgnoreCase(qName)) {
          //counter++;
          //System.out.println("counter is "+ counter +" and hash for report is: "+ System.identityHashCode(report));
          //System.out.println("currentSample now is: "+ currentSample.getUri()+" and sample is: "+ sample.getUri());
          System.out.println("3");

          //System.out.println("Counter: "+counter+" Sample ID: "+ sample.getHttpId()+ " and currentSample ID: "+ currentSample.getHttpId());
            try {
              System.out.println("4 sample: "+ sample.getHttpId()+" hasCode: "+ System.identityHashCode(sample));

              System.out.println("4 currentSample: "+ currentSample.getHttpId()+" hasCode: "+ System.identityHashCode(currentSample));

              report.addSample(currentSample);
            } catch (SAXException e) {
              e.printStackTrace();
            }

        }
      }
    });

    return report;
  }
//
//  public static String generateString(Random rng, String characters, int length)
//  {
//    char[] text = new char[length];
//    for (int i = 0; i < length; i++)
//    {
//      text[i] = characters.charAt(rng.nextInt(characters.length()));
//    }
//    return new String(text);
//  }

  public Integer createSampleID(){

    Random r = new Random();
    Integer id = r.nextInt(9999);
    sampleID=id;


    return sampleID;
  }





}
