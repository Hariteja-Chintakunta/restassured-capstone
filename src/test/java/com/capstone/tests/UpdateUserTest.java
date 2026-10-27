package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReporter;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class UpdateUserTest extends ApiBase {

    @Test(description = "TC04 - Update user, then GET the same user")
    public void updateUser() {
        String name = TestDataUtil.randomName();
        String email = TestDataUtil.randomEmail();

        // 1. create a user (POST) and 2. fetch the userId
        int id = createUser(requestSpec, name, email);
        String updatedName = name + " Updated";

        com.capstone.utils.UserApiHelper.updateUser(
                requestSpec, id, updatedName, email, "male", "inactive");

        // 4. get the same user with the same userId (GET) and confirm the change was saved
        Response response = given().spec(requestSpec)
                .when().get(USERS + "/" + id);
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(updatedName))
                .body("status", equalTo("inactive"));

        UserOperationReporter.recordCheck("GET status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Returned user ID matches updated ID", id, response.jsonPath().getInt("id"));
        UserOperationReporter.recordCheck("Returned user name matches update", updatedName, response.jsonPath().getString("name"));
        UserOperationReporter.recordCheck(
                "Returned user status matches update", "inactive", response.jsonPath().getString("status"));

        // clean up
        deleteUser(requestSpec, id);
    }
}