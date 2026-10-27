package com.capstone.reporting;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Listeners(UserOperationReportListener.class)
public class UserOperationReporterTest {

    @Test
    public void fieldsChangedReturnsOnlyValuesThatDiffer() {
        Map<String, Object> oldDetails = new LinkedHashMap<>();
        oldDetails.put("name", "John");
        oldDetails.put("email", "john@test.com");
        oldDetails.put("status", "active");

        Map<String, Object> updatedDetails = new LinkedHashMap<>();
        updatedDetails.put("name", "John Updated");
        updatedDetails.put("email", "john@test.com");
        updatedDetails.put("status", "inactive");

        Assert.assertEquals(
                UserOperationReporter.fieldsChanged(oldDetails, updatedDetails),
                List.of("name", "status"));
    }

    @Test
    public void fieldsChangedIncludesAddedAndRemovedFields() {
        Map<String, Object> oldDetails = Map.of("name", "John", "username", "john");
        Map<String, Object> updatedDetails = Map.of("name", "John");

        Assert.assertEquals(
                UserOperationReporter.fieldsChanged(oldDetails, updatedDetails),
                List.of("username"));
    }

    @Test
    public void parsesGetAllUsersPayloadAsUserList() {
        Object users = UserOperationReporter.parseJsonPayload(
                "[{\"id\":101,\"name\":\"John\"},{\"id\":102,\"name\":\"Jane\"}]");

        Assert.assertTrue(users instanceof List<?>);
        Assert.assertEquals(((List<?>) users).size(), 2);
        Assert.assertEquals(((Map<?, ?>) ((List<?>) users).get(0)).get("name"), "John");
    }
}
