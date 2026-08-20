//package utils;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testng.ISuite;
//import org.testng.ISuiteListener;
//import utils.JiraUploadUtil;
//
//import java.io.File;
//
//public class JiraReportUploadListener implements ISuiteListener {
//
//    private static final Logger logger =
//            LoggerFactory.getLogger(JiraReportUploadListener.class);
//
//    @Override
//    public void onFinish(ISuite suite) {
//
//        try {
//            // ✅ Use SAME report path as ExtentReportManager
//            String reportPath = ExtentReportManager.getReportPath();
//            File reportFile = new File(reportPath);
//
//            if (!reportFile.exists() || reportFile.length() == 0) {
//                logger.error("Extent report not found or empty: {}", reportPath);
//                return;
//            }
//
//            String jiraUrl = suite.getParameter("jiraUrl");
//            String jiraEmail = suite.getParameter("jiraEmail");
//            String jiraApiToken = suite.getParameter("jiraApiToken");
//            String jiraIssueKey = suite.getParameter("jiraIssueKey");
//
//            if (jiraUrl == null || jiraEmail == null ||
//                    jiraApiToken == null || jiraIssueKey == null) {
//
//                logger.error("Jira parameters missing in testng.xml");
//                return;
//            }
//
//            JiraUploadUtil.uploadReportToJira(
//                    jiraUrl,
//                    jiraEmail,
//                    jiraApiToken,
//                    jiraIssueKey,
//                    reportPath
//            );
//
//            logger.info("✅ Extent report uploaded to Jira issue: {}",
//                    jiraIssueKey);
//
//        } catch (Exception e) {
//            logger.error("❌ Failed to upload Extent report to Jira", e);
//        }
//    }
//}