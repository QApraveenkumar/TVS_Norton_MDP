//package utils;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Base64;
//
//public class JiraUploadUtil {
//    private static final Logger logger = LoggerFactory.getLogger(JiraUploadUtil.class);
//
//    public static void uploadReportToJira(String jiraUrl, String jiraEmail, String jiraApiToken, String jiraIssueKey, String reportPath) {
//        try {
//            File file = new File(reportPath);
//            if (!file.exists() || file.length() == 0) {
//                logger.error("Report file not found or empty: {}", reportPath);
//                System.out.println("[JiraUploadUtil] Report file not found or empty: " + reportPath);
//                return;
//            }
//            System.out.println("[JiraUploadUtil] Uploading report file to Jira: " + file.getAbsolutePath());
//
//            String uploadUrl = jiraUrl + "/rest/api/2/issue/" + jiraIssueKey + "/attachments";
//            String auth = jiraEmail + ":" + jiraApiToken;
//            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
//
//            HttpURLConnection connection = (HttpURLConnection) new URL(uploadUrl).openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
//            connection.setRequestProperty("X-Atlassian-Token", "no-check");
//            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//            OutputStream outputStream = connection.getOutputStream();
//            String filePartHeader = "--" + boundary + "\r\n" +
//                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
//                    "Content-Type: text/html\r\n\r\n";
//            outputStream.write(filePartHeader.getBytes());
//
//            FileInputStream inputStream = new FileInputStream(file);
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//            inputStream.close();
//
//            String filePartFooter = "\r\n--" + boundary + "--\r\n";
//            outputStream.write(filePartFooter.getBytes());
//            outputStream.flush();
//            outputStream.close();
//
//            int responseCode = connection.getResponseCode();
//            StringBuilder response = new StringBuilder();
//            try (java.io.InputStream is = (responseCode >= 200 && responseCode < 400) ? connection.getInputStream() : connection.getErrorStream()) {
//                if (is != null) {
//                    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
//                    response.append(s.hasNext() ? s.next() : "");
//                }
//            }
//            if (responseCode == 200 || responseCode == 201) {
//                logger.info("Report uploaded to Jira successfully. Response code: {}", responseCode);
//                System.out.println("[JiraUploadUtil] Report uploaded to Jira successfully. Jira response: " + response.toString());
//                // Try to extract and print the attachment URL(s)
//                try {
//                    String resp = response.toString();
//                    // Jira returns a JSON array for attachments
//                    int urlIndex = resp.indexOf("\"content\":");
//                    if (urlIndex != -1) {
//                        int start = resp.indexOf('"', urlIndex + 10) + 1;
//                        int end = resp.indexOf('"', start);
//                        if (start > 0 && end > start) {
//                            String attachmentUrl = resp.substring(start, end);
//                            System.out.println("[JiraUploadUtil] Direct attachment URL: " + attachmentUrl);
//                        }
//                    }
//                } catch (Exception ex) {
//                    System.out.println("[JiraUploadUtil] Could not extract attachment URL from Jira response.");
//                }
//            } else {
//                logger.error("Failed to upload report to Jira. Response code: {}", responseCode);
//                System.err.println("[JiraUploadUtil] Failed to upload report to Jira. Jira response: " + response.toString());
//            }
//        } catch (Exception e) {
//            logger.error("Exception while uploading report to Jira", e);
//        }
//    }
//}
