package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.utils.TestDataUtil;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.UUID;

import static com.capstone.utils.PostApiHelper.*;
import static com.capstone.utils.UserApiHelper.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class GetProductDetailsTest extends ApiBase {

    @Test(description = "TC07 - Get product (post) details by ID")
    public void getProductDetails() {
        int userId = createUser(requestSpec, TestDataUtil.randomName(), TestDataUtil.randomEmail());
        Integer postId = null;
        try {
            String title = "QA Post " + UUID.randomUUID().toString().substring(0, 6);
            postId = createPostForUser(requestSpec, userId, title);

            given().spec(requestSpec)
                    .when().get(POSTS + "/" + postId)
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .time(lessThan(MAX_TIME_MS))
                    .body("id", equalTo(postId))
                    .body("title", equalTo(title))
                    .body("user_id", equalTo(userId));
        } finally {
            if (postId != null) {
                deletePost(requestSpec, postId);
            }
            deleteUser(requestSpec, userId);
        }
    }
}
