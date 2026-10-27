package com.capstone.reporting;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class UserOperationReportListener implements ISuiteListener, ITestListener {

    @Override
    public void onStart(ISuite suite) {
        UserOperationReporter.start();
    }

    @Override
    public void onFinish(ISuite suite) {
        UserOperationReporter.finish();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String description = result.getMethod().getDescription();
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        if (description != null && !description.isBlank()) {
            testName += " (" + description + ")";
        }
        UserOperationReporter.setCurrentTest(testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        UserOperationReporter.clearCurrentTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        UserOperationReporter.clearCurrentTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        UserOperationReporter.clearCurrentTest();
    }
}
