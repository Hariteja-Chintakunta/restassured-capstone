package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.reporting.UserOperationReporter;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class GetAllUsersTest extends ApiBase {

    @Test(description = "TC01 - Get all users")
    public void getAllUsers() {
        Response response = given().spec(requestSpec)
                .when().get(USERS);

        UserOperationReporter.recordUsersRetrieved(
                response.getBody().asString(), response.getStatusCode());
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].email", containsString("@"));

        var users = response.jsonPath().<java.util.Map<String, Object>>getList("");
        UserOperationReporter.recordCheck("GET status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Response contains users", "more than 0", users.size());
        UserOperationReporter.recordCheck("First user ID is present", "not null", users.get(0).get("id"));
        UserOperationReporter.recordCheck("First user email is valid", "contains @", users.get(0).get("email"));
    }
}