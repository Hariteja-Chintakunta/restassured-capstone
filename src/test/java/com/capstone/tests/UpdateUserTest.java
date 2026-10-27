package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.utils.TestDataUtil;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UpdateUserTest extends ApiBase {

    @Test(description = "TC04 - Update user, then GET the same user")
    public void updateUser() {
        String name = TestDataUtil.randomName();
        String email = TestDataUtil.randomEmail();

        // 1. create a user (POST) and 2. fetch the userId
        int id = createUser(requestSpec, name, email);
        String updatedName = name + " Updated";

        // 3. update the user with the same userId (PUT)
        given().spec(requestSpec)
                .body(TestDataUtil.userJson(updatedName, email, "male", "inactive"))
                .when().put(USERS + "/" + id)
                .then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(updatedName))
                .body("status", equalTo("inactive"));

        // 4. get the same user with the same userId (GET) and confirm the change was saved
        given().spec(requestSpec)
                .when().get(USERS + "/" + id)
                .then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(updatedName))
                .body("status", equalTo("inactive"));

        // clean up
        deleteUser(requestSpec, id);
    }
}