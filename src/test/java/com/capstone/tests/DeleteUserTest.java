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
public class DeleteUserTest extends ApiBase {

    @Test(description = "TC05 - Delete user")
    public void deleteUser() {
        int id = createUser(requestSpec, TestDataUtil.randomName(), TestDataUtil.randomEmail());

        com.capstone.utils.UserApiHelper.deleteUser(requestSpec, id);

        // confirm the user is really gone
        given().spec(requestSpec)
                .when().get(USERS + "/" + id)
                .then().statusCode(404);
    }
}