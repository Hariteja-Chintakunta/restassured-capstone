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
public class SearchProductTest extends ApiBase {

    @Test(description = "TC08 - Search product (post) by title")
    public void searchProduct() {
        int userId = createUser(requestSpec, TestDataUtil.randomName(), TestDataUtil.randomEmail());
        Integer postId = null;
        try {
            String title = "qapost" + UUID.randomUUID().toString().substring(0, 8);
            postId = createPostForUser(requestSpec, userId, title);

            given().spec(requestSpec)
                    .queryParam("title", title)
                    .when().get(POSTS)
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .time(lessThan(MAX_TIME_MS))
                    .body("size()", greaterThan(0))
                    .body("id", hasItem(postId))
                    .body("title", hasItem(title));
        } finally {
            if (postId != null) {
                deletePost(requestSpec, postId);
            }
            deleteUser(requestSpec, userId);
        }
    }
}
