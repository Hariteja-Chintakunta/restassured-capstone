package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.utils.TestDataUtil;
import org.testng.annotations.Test;

import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class DeleteUserTest extends ApiBase {

    @Test(description = "TC05 - Delete user")
    public void deleteUser() {
        int id = createUser(requestSpec, TestDataUtil.randomName(), TestDataUtil.randomEmail());

        given().spec(requestSpec)
                .when().delete(USERS + "/" + id)
                .then().log().ifValidationFails()
                .statusCode(204)
                .time(lessThan(MAX_TIME_MS));

        // confirm the user is really gone
        given().spec(requestSpec)
                .when().get(USERS + "/" + id)
                .then().statusCode(404);
    }
}