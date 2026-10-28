// ABOUTME: Data-driven test for user creation using TestNG DataProvider and User POJO.
// ABOUTME: Each DataProvider row creates a user, verifies all fields via GET, then cleans up.
package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.data.UserDataProvider;
import com.capstone.models.User;
import com.capstone.reporting.UserOperationReporter;
import com.capstone.reporting.UserOperationReportListener;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class CreateUserDataProviderTest extends ApiBase {

    @Test(dataProvider = "createUserData", dataProviderClass = UserDataProvider.class,
            description = "TC12 - Create user with varied data via DataProvider")
    public void createUserWithDataProvider(User user) {
        int id = createUser(requestSpec, user);

        Response response = given().spec(requestSpec)
                .when().get(USERS + "/" + id);
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(user.getName()))
                .body("email", equalTo(user.getEmail()))
                .body("gender", equalTo(user.getGender()))
                .body("status", equalTo(user.getStatus()));

        UserOperationReporter.recordCheck("GET status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Returned user ID matches created ID", id, response.jsonPath().getInt("id"));
        UserOperationReporter.recordCheck("Returned user name", user.getName(), response.jsonPath().getString("name"));
        UserOperationReporter.recordCheck("Returned user email", user.getEmail(), response.jsonPath().getString("email"));
        UserOperationReporter.recordCheck("Returned user gender", user.getGender(), response.jsonPath().getString("gender"));
        UserOperationReporter.recordCheck("Returned user status", user.getStatus(), response.jsonPath().getString("status"));

        deleteUser(requestSpec, id);
    }
}
