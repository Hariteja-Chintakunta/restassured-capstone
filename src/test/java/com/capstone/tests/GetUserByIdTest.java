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
public class GetUserByIdTest extends ApiBase {

    @Test(description = "TC02 - Get user by ID")
    public void getUserById() {
        String name = TestDataUtil.randomName();
        String email = TestDataUtil.randomEmail();
        int id = createUser(requestSpec, name, email);

        Response response = given().spec(requestSpec)
                .when().get(USERS + "/" + id);
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(name))
                .body("email", equalTo(email))
                .body("gender", equalTo("male"))
                .body("status", equalTo("active"));

        UserOperationReporter.recordCheck("GET status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Returned user ID matches requested ID", id, response.jsonPath().getInt("id"));
        UserOperationReporter.recordCheck("Returned user name", name, response.jsonPath().getString("name"));
        UserOperationReporter.recordCheck("Returned user email", email, response.jsonPath().getString("email"));
        UserOperationReporter.recordCheck("Returned user gender", "male", response.jsonPath().getString("gender"));
        UserOperationReporter.recordCheck("Returned user status", "active", response.jsonPath().getString("status"));

        deleteUser(requestSpec, id);
    }
}