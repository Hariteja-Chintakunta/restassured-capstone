package com.capstone.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.restassured.path.json.JsonPath;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class UserOperationReporter {

    private static final Path REPORT_DIRECTORY = Path.of("target", "reports");
    private static final List<OperationEvent> EVENTS = new ArrayList<>();
    private static final List<TestResultEvent> TEST_RESULTS = new ArrayList<>();
    private static final ThreadLocal<String> CURRENT_TEST =
            ThreadLocal.withInitial(() -> "Unattributed API operation");
    private static final ThreadLocal<List<String>> CURRENT_CHECKS =
            ThreadLocal.withInitial(ArrayList::new);
    private static ExtentReports extent;
    private static boolean started;
    private static boolean finished;

    private UserOperationReporter() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        try {
            Files.createDirectories(REPORT_DIRECTORY);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the user-operation report directory", e);
        }
        ExtentSparkReporter sparkReporter =
                new ExtentSparkReporter(REPORT_DIRECTORY.resolve("extent-report.html").toString());
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        started = true;
    }

    public static void setCurrentTest(String testName) {
        CURRENT_TEST.set(testName);
        CURRENT_CHECKS.set(new ArrayList<>());
    }

    public static void recordCheck(String validation, Object expected, Object actual) {
        CURRENT_CHECKS.get().add("PASS - " + validation
                + " | Expected: " + expected
                + " | Actual: " + actual);
    }

    static List<String> currentChecks() {
        return List.copyOf(CURRENT_CHECKS.get());
    }

    public static void clearCurrentTest() {
        CURRENT_TEST.remove();
        CURRENT_CHECKS.remove();
    }

    public static synchronized void recordTestResult(ITestResult result, String status) {
        String testName = testName(result);
        if (result.getTestClass().getName().endsWith(".EnvConfigTest")) {
            return;
        }
        String description = result.getMethod().getDescription();
        long duration = result.getEndMillis() - result.getStartMillis();
        Throwable throwable = result.getThrowable();
        String details = throwable == null ? "" : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        TestResultEvent testResult = new TestResultEvent(
                testName, description == null ? "" : description, status, duration, details,
                currentChecks());
        TEST_RESULTS.add(testResult);

        ExtentTest extentTest = extent.createTest(testName);
        if (testName.contains("GetAllUsersTest.getAllUsers")) {
            extentTest.assignCategory("GET ALL USERS");
        }
        extentTest.info("Status: " + status);
        extentTest.info("Duration: " + duration + " ms");
        if (!testResult.description.isBlank()) {
            extentTest.info("Description: " + testResult.description);
        }
        testResult.checks.forEach(extentTest::info);
        if (!details.isBlank()) {
            extentTest.info("Details: " + details);
        }
        if ("FAIL".equals(status)) {
            extentTest.fail("Test failed");
        } else if ("SKIP".equals(status)) {
            extentTest.skip("Test skipped");
        } else {
            extentTest.pass("Test passed");
        }
    }

    public static synchronized void recordCreated(String requestPayload, String responsePayload, int statusCode) {
        Map<String, Object> user = parsePayload(responsePayload);
        Object userId = user.get("id");
        String username = displayValue(user.get("username"), "Not returned by API");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", userId);
        summary.put("name", user.get("name"));
        summary.put("email", user.get("email"));
        summary.put("username", username);

        Map<String, String> details = new LinkedHashMap<>();
        details.put("User ID", displayValue(userId, "Not returned"));
        details.put("Name", displayValue(user.get("name"), "Not returned"));
        details.put("Email", displayValue(user.get("email"), "Not returned"));
        details.put("Username", username);
        details.put("Request Payload", requestPayload);
        details.put("Response Payload", responsePayload);
        details.put("Status Code", String.valueOf(statusCode));
        record("USER CREATED", "Created User", summary, details);
    }

    public static synchronized void recordUsersRetrieved(String responsePayload, int statusCode) {
        Object users = parseJsonPayload(responsePayload);
        Object totalUsers = users instanceof List<?> userList ? userList.size() : "Response is not a user list";

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("statusCode", statusCode);
        summary.put("totalUsers", totalUsers);
        summary.put("users", users);

        Map<String, String> details = new LinkedHashMap<>();
        details.put("Status Code", String.valueOf(statusCode));
        details.put("Users Returned", String.valueOf(totalUsers));
        details.put("Response Payload", responsePayload);
        record("USERS RETRIEVED", "Get all users", summary, details);
    }

    public static synchronized void recordUpdated(
            int userId, String oldUserPayload, String requestPayload, String responsePayload, int statusCode) {
        Map<String, Object> oldUser = parsePayload(oldUserPayload);
        Map<String, Object> updatedUser = parsePayload(responsePayload);
        List<String> changedFields = fieldsChanged(oldUser, updatedUser);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", userId);
        summary.put("oldUserDetails", oldUser);
        summary.put("updatedUserDetails", updatedUser);
        summary.put("fieldsChanged", changedFields);

        Map<String, String> details = new LinkedHashMap<>();
        details.put("User ID", String.valueOf(userId));
        details.put("Old User Details", oldUserPayload);
        details.put("Updated User Details", responsePayload);
        details.put("Fields Changed", String.join(", ", changedFields));
        details.put("Request Payload", requestPayload);
        details.put("Response Payload", responsePayload);
        details.put("Status Code", String.valueOf(statusCode));
        record("USER UPDATED", "Updated User", summary, details);
    }

    public static synchronized void recordDeleted(
            int userId, String userBeforeDeletion, String responsePayload, int statusCode) {
        Map<String, Object> user = parsePayload(userBeforeDeletion);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", userId);
        summary.put("name", user.get("name"));
        summary.put("email", user.get("email"));
        summary.put("username", displayValue(user.get("username"), "Not returned by API"));

        Map<String, String> details = new LinkedHashMap<>();
        details.put("User ID", String.valueOf(userId));
        details.put("User Details Before Deletion", userBeforeDeletion);
        details.put("Delete Response", responsePayload.isBlank() ? "(empty response body)" : responsePayload);
        details.put("Status Code", String.valueOf(statusCode));
        record("USER DELETED", "Deleted User", summary, details);
    }

    static List<String> fieldsChanged(Map<String, Object> oldDetails, Map<String, Object> updatedDetails) {
        Set<String> fields = new LinkedHashSet<>(oldDetails.keySet());
        fields.addAll(updatedDetails.keySet());
        return fields.stream()
                .filter(field -> !Objects.equals(oldDetails.get(field), updatedDetails.get(field)))
                .toList();
    }

    public static synchronized void finish() {
        if (!started || finished) {
            return;
        }
        String summary = buildSummary();
        System.out.println(summary);
        writeHtmlReport(summary);
        extent.createTest("TEST EXECUTION SUMMARY").info(summary);
        extent.flush();
        finished = true;
    }

    private static void record(
            String operation, String summaryLabel, Map<String, Object> summary, Map<String, String> details) {
        start();
        String testName = CURRENT_TEST.get();
        OperationEvent event = new OperationEvent(operation, summaryLabel, testName, summary, details);
        EVENTS.add(event);

        System.out.println();
        System.out.println(operation);
        System.out.println("Test Case: " + testName);
        details.forEach((label, value) -> System.out.println(label + ": " + value));
        System.out.println("------------------------------------------------");

        ExtentTest extentTest = extent.createTest(testName + " - " + operation);
        details.forEach((label, value) -> extentTest.info(label + ": " + value));
    }

    private static Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return new LinkedHashMap<>();
        }
        return JsonPath.from(payload).getMap("");
    }

    static Object parseJsonPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "";
        }
        return JsonPath.from(payload).get();
    }

    private static String displayValue(Object value, String missingValue) {
        return value == null ? missingValue : String.valueOf(value);
    }

    private static String buildSummary() {
        long created = EVENTS.stream().filter(event -> event.operation.equals("USER CREATED")).count();
        long updated = EVENTS.stream().filter(event -> event.operation.equals("USER UPDATED")).count();
        long deleted = EVENTS.stream().filter(event -> event.operation.equals("USER DELETED")).count();
        StringBuilder report = new StringBuilder()
                .append("=================================================\n")
                .append("TEST EXECUTION SUMMARY\n")
                .append("=================================================\n");

        for (int i = 0; i < EVENTS.size(); i++) {
            OperationEvent event = EVENTS.get(i);
            report.append("\nTest Case ").append(i + 1).append(":\n")
                    .append(event.summaryLabel).append(":\n")
                    .append(toJson(event.summary, 0)).append('\n')
                    .append("Test Method: ").append(event.testName).append('\n');
        }

        report.append("\nTEST CASE RESULTS\n");
        for (int i = 0; i < TEST_RESULTS.size(); i++) {
            TestResultEvent result = TEST_RESULTS.get(i);
            report.append("\nTest ").append(i + 1).append(": ")
                    .append(result.testName).append('\n')
                    .append("Status: ").append(result.status).append('\n')
                    .append("Duration: ").append(result.durationMs).append(" ms\n");
            if (!result.description.isBlank()) {
                report.append("Description: ").append(result.description).append('\n');
            }
            for (String check : result.checks) {
                report.append("  ").append(check).append('\n');
            }
            if (!result.details.isBlank()) {
                report.append("Details: ").append(result.details).append('\n');
            }
        }

        report.append("\n=================================================\n")
                .append("TOTAL USERS CREATED : ").append(created).append('\n')
                .append("TOTAL USERS UPDATED : ").append(updated).append('\n')
                .append("TOTAL USERS DELETED : ").append(deleted).append('\n')
                .append("=================================================");
        return report.toString();
    }

    private static void writeHtmlReport(String summary) {
        StringBuilder html = new StringBuilder()
                .append("<!doctype html><html><head><meta charset=\"utf-8\">")
                .append("<title>User Operation Summary</title>")
                .append("<style>body{font-family:Arial,sans-serif;margin:2rem;color:#222}")
                .append("pre{white-space:pre-wrap;background:#f4f4f4;padding:1rem}")
                .append("section{border-bottom:1px solid #ccc;padding:1rem 0}")
                .append("table{border-collapse:collapse;width:100%}th,td{border:1px solid #ccc;padding:.6rem;text-align:left}")
                .append(".highlighted-test{background:#e8f4ff;font-weight:600}")
                .append(".test-badge{display:inline-block;margin-left:.5rem;padding:.2rem .5rem;background:#1769aa;color:#fff;border-radius:.25rem}")
                .append("ul{margin:.25rem 0;padding-left:1.25rem}li{margin:.25rem 0}")
                .append("</style></head><body><h1>TEST EXECUTION SUMMARY</h1>")
                .append("<h2>Test Case Results</h2><table><thead><tr>")
                .append("<th>Test case</th><th>Description</th><th>Status</th><th>Duration</th>")
                .append("<th>Passed checks</th><th>Failure details</th>")
                .append("</tr></thead><tbody>");
        for (TestResultEvent result : TEST_RESULTS) {
            boolean highlighted = result.testName.contains("GetAllUsersTest.getAllUsers");
            html.append(highlighted ? "<tr class=\"highlighted-test\"><td>" : "<tr><td>")
                    .append(escapeHtml(result.testName));
            if (highlighted) {
                html.append("<strong class=\"test-badge\">GET ALL USERS</strong>");
            }
            html.append("</td><td>")
                    .append(escapeHtml(result.description))
                    .append("</td><td>").append(escapeHtml(result.status))
                    .append("</td><td>").append(result.durationMs).append(" ms</td><td>");
            if (result.checks.isEmpty()) {
                html.append("No API checks recorded");
            } else {
                html.append("<ul>");
                result.checks.forEach(check -> html.append("<li>")
                        .append(escapeHtml(check)).append("</li>"));
                html.append("</ul>");
            }
            html.append("</td><td>")
                    .append(escapeHtml(result.details)).append("</td></tr>");
        }
        html.append("</tbody></table><h2>User Operations</h2>");
        for (int i = 0; i < EVENTS.size(); i++) {
            OperationEvent event = EVENTS.get(i);
            html.append("<section><h2>Test Case ").append(i + 1).append(": ")
                    .append(escapeHtml(event.summaryLabel)).append("</h2><p>")
                    .append(escapeHtml(event.testName)).append("</p><pre>")
                    .append(escapeHtml(toJson(event.summary, 0))).append("</pre><dl>");
            event.details.forEach((label, value) -> html.append("<dt><strong>")
                    .append(escapeHtml(label)).append("</strong></dt><dd><pre>")
                    .append(escapeHtml(value)).append("</pre></dd>"));
            html.append("</dl></section>");
        }
        int totalsStart = summary.indexOf("TOTAL USERS CREATED :");
        int totalsSectionStart = summary.lastIndexOf("=================================================", totalsStart);
        html.append("<pre>").append(escapeHtml(summary.substring(totalsSectionStart)))
                .append("</pre></body></html>");

        try {
            Files.writeString(REPORT_DIRECTORY.resolve("user-operation-summary.html"), html);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write the user-operation HTML report", e);
        }
    }

    private static String toJson(Object value, int depth) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return "\"" + escapeJson(text) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        String indentation = "  ".repeat(depth);
        String childIndentation = "  ".repeat(depth + 1);
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                return "{}";
            }
            StringBuilder json = new StringBuilder("{\n");
            map.forEach((key, item) -> json.append(childIndentation)
                    .append(toJson(String.valueOf(key), depth + 1)).append(": ")
                    .append(toJson(item, depth + 1)).append(",\n"));
            json.setLength(json.length() - 2);
            return json.append('\n').append(indentation).append('}').toString();
        }
        if (value instanceof Iterable<?> values) {
            List<String> items = new ArrayList<>();
            values.forEach(item -> items.add(childIndentation + toJson(item, depth + 1)));
            return items.isEmpty() ? "[]" : "[\n" + String.join(",\n", items) + "\n" + indentation + "]";
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char character : value.toCharArray()) {
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String testName(ITestResult result) {
        String name = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        return description == null || description.isBlank()
                ? name
                : name + " (" + description + ")";
    }

    private record OperationEvent(
            String operation,
            String summaryLabel,
            String testName,
            Map<String, Object> summary,
            Map<String, String> details) {
    }

    private record TestResultEvent(
            String testName,
            String description,
            String status,
            long durationMs,
            String details,
            List<String> checks) {
    }
}
