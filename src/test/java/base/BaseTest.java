package base;

import com.aventstack.extentreports.ExtentTest;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
//import utils.JiraReportUploadListener;
//import utils.ExtentReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.lang.reflect.Method;
public class BaseTest implements ISuiteListener {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

//    @BeforeMethod
//    public void startTest(Method method) {
//        ExtentTest extentTest = ExtentReportManager.getExtentReports().createTest(method.getName());
//        test.set(extentTest);
//    }
//
//    @AfterMethod
//    public void endTest(ITestResult result) {
//        if (result.getStatus() == ITestResult.FAILURE) {
//            test.get().fail(result.getThrowable());
//        } else if (result.getStatus() == ITestResult.SUCCESS) {
//            test.get().pass("Test passed");
//        } else if (result.getStatus() == ITestResult.SKIP) {
//            test.get().skip("Test skipped");
//        }
//
//    }
//    @BeforeSuite(alwaysRun = true)
//    public void initializeReport() {
//        logger.info("Initializing ExtentReports before the suite starts...");
//        ExtentReportManager.getExtentReports();
//        logger.info("ExtentReports initialized successfully before the suite.");
//    }
//
//    @Override
//    public void onStart(ISuite suite) {
//        logger.info("Initializing test suite...");
//        System.out.println("ENV: " + System.getProperty("env", "qa"));
//
//        // Ensure ExtentReports is initialized
//        ExtentReportManager.getExtentReports(); // Ensure ExtentReports is initialized
//        logger.info("ExtentReports initialized successfully.");
//
//        // Debug log for report path
//        String reportPath = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
//        logger.info("Expected report path: {}", reportPath);
//
//        // Retrieve Jira parameters from testng.xml
//        String jiraUrl = suite.getParameter("jiraUrl");
//        String jiraEmail = suite.getParameter("jiraEmail");
//        String jiraApiToken = suite.getParameter("jiraApiToken");
//        String jiraIssueKey = suite.getParameter("jiraIssueKey");
//
//        if (jiraUrl == null || jiraEmail == null || jiraApiToken == null || jiraIssueKey == null) {
//            logger.error("Jira parameters are missing. Please set jiraUrl, jiraEmail, jiraApiToken, and jiraIssueKey in testng.xml.");
//            System.err.println("Jira parameters are missing. Please set jiraUrl, jiraEmail, jiraApiToken, and jiraIssueKey in testng.xml.");
//            return;
//        }
//
//        logger.info("Jira parameters loaded successfully.");
//        logger.info("Jira URL: {}", jiraUrl);
//        logger.info("Jira Issue Key: {}", jiraIssueKey);
//
//        try {
//            // Add JiraReportUploadListener setup
//            JiraReportUploadListener jiraListener = new JiraReportUploadListener();
//            jiraListener.onStart(suite); // Initialize the listener with the suite
//            logger.info("JiraReportUploadListener initialized successfully.");
//        } catch (Exception e) {
//            logger.error("Failed to initialize JiraReportUploadListener: {}", e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void onFinish(ISuite suite) {
//        logger.info("Finalizing test suite...");
//        logger.info("Flushing ExtentReports after all tests are executed...");
//        ExtentReportManager.flushReports();
//        logger.info("ExtentReports flushed and saved successfully.");
//
//        // Confirm the ExtentReports file is saved with executed results
//        String reportPath = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
//        File reportFile = new File(reportPath);
//        if (reportFile.exists()) {
//            logger.info("ExtentReports saved successfully at: {}", reportFile.getAbsolutePath());
//            System.out.println("ExtentReports saved successfully at: " + reportFile.getAbsolutePath());
//        } else {
//            logger.error("Failed to save ExtentReports at: {}", reportFile.getAbsolutePath());
//            System.err.println("Failed to save ExtentReports at: " + reportFile.getAbsolutePath());
//        }
//    }
//
//    @AfterSuite(alwaysRun = true)
//    public void saveReport() {
//        ExtentReportManager.flushReports();
//    //    ExtentReportManager.uploadReportToJira();
//
//        // Removed redundant flush call to avoid premature flushing
//    }
}
