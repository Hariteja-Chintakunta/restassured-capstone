package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.utils.TestDataUtil;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateUserTest extends ApiBase {

    @Test(description = "TC03 - Create user, then GET the same user")
    public void createUser() {
        String name = TestDataUtil.randomName();
        String email = TestDataUtil.randomEmail();

        // 1. create a user (POST) and 2. fetch the userId
        int id = given().spec(requestSpec)
                .body(TestDataUtil.userJson(name, email, "male", "active"))
                .when().post(USERS)
                .then().log().ifValidationFails()
                .statusCode(201)
                .time(lessThan(MAX_TIME_MS))
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("email", equalTo(email))
                .extract().path("id");

        // 3. get the same user with the same userId (GET)
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

        // clean up
        deleteUser(requestSpec, id);
    }
}