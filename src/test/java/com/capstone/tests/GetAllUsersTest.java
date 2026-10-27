package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class GetAllUsersTest extends ApiBase {

    @Test(description = "TC01 - Get all users")
    public void getAllUsers() {
        given().spec(requestSpec)
                .when().get(USERS)
                .then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].email", containsString("@"));
    }
}