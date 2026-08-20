//package utils;
//
//import com.aventstack.extentreports.ExtentReports;
//import com.aventstack.extentreports.reporter.ExtentSparkReporter;
//
//import java.io.File;
//
//public class ExtentReportManager {
//
//    private static ExtentReports extent;
//    private static final String REPORT_PATH =
//            System.getProperty("user.dir") + "/target/extent-report.html";
//
//    private ExtentReportManager() {
//        // Prevent instantiation
//    }
//
//    public static synchronized ExtentReports getExtentReports() {
//
//        if (extent == null) {
//
//            File reportFile = new File(REPORT_PATH);
//            File reportDir = reportFile.getParentFile();
//
//            if (!reportDir.exists() && !reportDir.mkdirs()) {
//                throw new RuntimeException(
//                        "Failed to create report directory: " + reportDir
//                );
//            }
//
//            ExtentSparkReporter sparkReporter =
//                    new ExtentSparkReporter(REPORT_PATH);
//            sparkReporter.config().setReportName("Automation Test Results");
//            sparkReporter.config().setDocumentTitle("Extent Report");
//
//            extent = new ExtentReports();
//            extent.attachReporter(sparkReporter);
//
//            extent.setSystemInfo("Tester",
//                    System.getProperty("user.name"));
//            extent.setSystemInfo("Environment",
//                    System.getProperty("env", "QA"));
//
//            System.out.println("✅ ExtentReports initialized at: " + REPORT_PATH);
//        }
//        return extent;
//    }
//
//    public static synchronized void flushReports() {
//        if (extent != null) {
//            extent.flush();
//            System.out.println("ExtentReport saved at: " + REPORT_PATH);
//        } else {
//            System.out.println("ExtentReports is null. Nothing to flush.");
//        }
//    }
//
//    public static String getReportPath() {
//        return REPORT_PATH;
//    }
//}