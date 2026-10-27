package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.utils.TestDataUtil;
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

        given().spec(requestSpec)
                .when().get(USERS + "/" + id)
                .then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(name))
                .body("email", equalTo(email))
                .body("gender", equalTo("male"))
                .body("status", equalTo("active"));

        deleteUser(requestSpec, id);
    }
}