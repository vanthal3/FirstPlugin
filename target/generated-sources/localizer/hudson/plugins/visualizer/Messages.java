// CHECKSTYLE:OFF

package hudson.plugins.visualizer;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

@SuppressWarnings({
    "",
    "PMD"
})
public class Messages {

    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);

    /**
     * Run Jmeter Visualizer
     * 
     */
    public static String Publisher_DisplayName() {
        return holder.format("Publisher.DisplayName");
    }

    /**
     * Run Jmeter Visualizer
     * 
     */
    public static Localizable _Publisher_DisplayName() {
        return new Localizable(holder, "Publisher.DisplayName");
    }

    /**
     * Configure
     * 
     */
    public static String GraphConfigurationDetail_DisplayName() {
        return holder.format("GraphConfigurationDetail.DisplayName");
    }

    /**
     * Configure
     * 
     */
    public static Localizable _GraphConfigurationDetail_DisplayName() {
        return new Localizable(holder, "GraphConfigurationDetail.DisplayName");
    }

    /**
     * Missing required fields
     * 
     */
    public static String CsvParser_validation_MissingFields() {
        return holder.format("CsvParser.validation.MissingFields");
    }

    /**
     * Missing required fields
     * 
     */
    public static Localizable _CsvParser_validation_MissingFields() {
        return new Localizable(holder, "CsvParser.validation.MissingFields");
    }

    /**
     * average
     * 
     */
    public static String ProjectAction_Average() {
        return holder.format("ProjectAction.Average");
    }

    /**
     * average
     * 
     */
    public static Localizable _ProjectAction_Average() {
        return new Localizable(holder, "ProjectAction.Average");
    }

    /**
     * average kb
     * 
     */
    public static String ProjectAction_AverageKB() {
        return holder.format("ProjectAction.AverageKB");
    }

    /**
     * average kb
     * 
     */
    public static Localizable _ProjectAction_AverageKB() {
        return new Localizable(holder, "ProjectAction.AverageKB");
    }

    /**
     * min
     * 
     */
    public static String ProjectAction_Minimum() {
        return holder.format("ProjectAction.Minimum");
    }

    /**
     * min
     * 
     */
    public static Localizable _ProjectAction_Minimum() {
        return new Localizable(holder, "ProjectAction.Minimum");
    }

    /**
     * Trend report
     * 
     */
    public static String TrendReportDetail_DisplayName() {
        return holder.format("TrendReportDetail.DisplayName");
    }

    /**
     * Trend report
     * 
     */
    public static Localizable _TrendReportDetail_DisplayName() {
        return new Localizable(holder, "TrendReportDetail.DisplayName");
    }

    /**
     * Performance Trend Action
     * 
     */
    public static String ProjectAction_DisplayName() {
        return holder.format("ProjectAction.DisplayName");
    }

    /**
     * Performance Trend Action
     * 
     */
    public static Localizable _ProjectAction_DisplayName() {
        return new Localizable(holder, "ProjectAction.DisplayName");
    }

    /**
     * Jmeter Visualizer
     * 
     */
    public static String Report_DisplayName() {
        return holder.format("Report.DisplayName");
    }

    /**
     * Jmeter Visualizer
     * 
     */
    public static Localizable _Report_DisplayName() {
        return new Localizable(holder, "Report.DisplayName");
    }

    /**
     * total kb
     * 
     */
    public static String ProjectAction_TotalTrafficKB() {
        return holder.format("ProjectAction.TotalTrafficKB");
    }

    /**
     * total kb
     * 
     */
    public static Localizable _ProjectAction_TotalTrafficKB() {
        return new Localizable(holder, "ProjectAction.TotalTrafficKB");
    }

    /**
     * max
     * 
     */
    public static String ProjectAction_Maximum() {
        return holder.format("ProjectAction.Maximum");
    }

    /**
     * max
     * 
     */
    public static Localizable _ProjectAction_Maximum() {
        return new Localizable(holder, "ProjectAction.Maximum");
    }

    /**
     * Requests Per Seconds
     * 
     */
    public static String ProjectAction_RequestsPerSeconds() {
        return holder.format("ProjectAction.RequestsPerSeconds");
    }

    /**
     * Requests Per Seconds
     * 
     */
    public static Localizable _ProjectAction_RequestsPerSeconds() {
        return new Localizable(holder, "ProjectAction.RequestsPerSeconds");
    }

    /**
     * Throughput
     * 
     */
    public static String ProjectAction_Throughput() {
        return holder.format("ProjectAction.Throughput");
    }

    /**
     * Throughput
     * 
     */
    public static Localizable _ProjectAction_Throughput() {
        return new Localizable(holder, "ProjectAction.Throughput");
    }

    /**
     * Performance Report
     * 
     */
    public static String BuildAction_DisplayName() {
        return holder.format("BuildAction.DisplayName");
    }

    /**
     * Performance Report
     * 
     */
    public static Localizable _BuildAction_DisplayName() {
        return new Localizable(holder, "BuildAction.DisplayName");
    }

    /**
     * Delimier can't be empty
     * 
     */
    public static String CsvParser_validation_delimiterEmpty() {
        return holder.format("CsvParser.validation.delimiterEmpty");
    }

    /**
     * Delimier can't be empty
     * 
     */
    public static Localizable _CsvParser_validation_delimiterEmpty() {
        return new Localizable(holder, "CsvParser.validation.delimiterEmpty");
    }

    /**
     * median
     * 
     */
    public static String ProjectAction_Median() {
        return holder.format("ProjectAction.Median");
    }

    /**
     * median
     * 
     */
    public static Localizable _ProjectAction_Median() {
        return new Localizable(holder, "ProjectAction.Median");
    }

    /**
     * errors
     * 
     */
    public static String ProjectAction_Errors() {
        return holder.format("ProjectAction.Errors");
    }

    /**
     * errors
     * 
     */
    public static Localizable _ProjectAction_Errors() {
        return new Localizable(holder, "ProjectAction.Errors");
    }

    /**
     * Pattern is required
     * 
     */
    public static String CsvParser_validation_patternEmpty() {
        return holder.format("CsvParser.validation.patternEmpty");
    }

    /**
     * Pattern is required
     * 
     */
    public static Localizable _CsvParser_validation_patternEmpty() {
        return new Localizable(holder, "CsvParser.validation.patternEmpty");
    }

    /**
     * Iago
     * 
     */
    public static String Iago_DisplayName() {
        return holder.format("Iago.DisplayName");
    }

    /**
     * Iago
     * 
     */
    public static Localizable _Iago_DisplayName() {
        return new Localizable(holder, "Iago.DisplayName");
    }

    /**
     * Test Suite report
     * 
     */
    public static String TestSuiteReportDetail_DisplayName() {
        return holder.format("TestSuiteReportDetail.DisplayName");
    }

    /**
     * Test Suite report
     * 
     */
    public static Localizable _TestSuiteReportDetail_DisplayName() {
        return new Localizable(holder, "TestSuiteReportDetail.DisplayName");
    }

    /**
     *  90% line
     * 
     */
    public static String ProjectAction_Line90() {
        return holder.format("ProjectAction.Line90");
    }

    /**
     *  90% line
     * 
     */
    public static Localizable _ProjectAction_Line90() {
        return new Localizable(holder, "ProjectAction.Line90");
    }

    /**
     * Percentage of failed tests
     * 
     */
    public static String ProjectAction_PercentageOfFailedTests() {
        return holder.format("ProjectAction.PercentageOfFailedTests");
    }

    /**
     * Percentage of failed tests
     * 
     */
    public static Localizable _ProjectAction_PercentageOfFailedTests() {
        return new Localizable(holder, "ProjectAction.PercentageOfFailedTests");
    }

    /**
     * Percentage of errors
     * 
     */
    public static String ProjectAction_PercentageOfErrors() {
        return holder.format("ProjectAction.PercentageOfErrors");
    }

    /**
     * Percentage of errors
     * 
     */
    public static Localizable _ProjectAction_PercentageOfErrors() {
        return new Localizable(holder, "ProjectAction.PercentageOfErrors");
    }

    /**
     * Responding time
     * 
     */
    public static String ProjectAction_RespondingTime() {
        return holder.format("ProjectAction.RespondingTime");
    }

    /**
     * Responding time
     * 
     */
    public static Localizable _ProjectAction_RespondingTime() {
        return new Localizable(holder, "ProjectAction.RespondingTime");
    }

}
