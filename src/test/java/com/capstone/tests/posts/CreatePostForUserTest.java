package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.reporting.UserOperationReporter;
import com.capstone.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.UUID;

import static com.capstone.utils.UserApiHelper.MAX_TIME_MS;
import static com.capstone.utils.UserApiHelper.USERS;
import static com.capstone.utils.UserApiHelper.createUser;
import static com.capstone.utils.UserApiHelper.deleteUser;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

@Listeners(UserOperationReportListener.class)
public class CreatePostForUserTest extends ApiBase {

    @Test(description = "TC10 - Create a post for a user in the local API")
    public void createPostForUser() {
        // Arrange
        int userId = createUser(requestSpec, TestDataUtil.randomName(), TestDataUtil.randomEmail());
        String title = "Local QA Post " + UUID.randomUUID().toString().substring(0, 8);
        String body = "Post created by the local API automation test";
        String requestPayload = """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(title, body);
        Integer postId = null;

        try {
            // Act
            Response response = given().spec(requestSpec)
                    .body(requestPayload)
                    .when().post(USERS + "/" + userId + "/posts");

            // Assert
            response.then().log().ifValidationFails()
                    .statusCode(201)
                    .time(lessThan(MAX_TIME_MS));

            postId = response.jsonPath().getInt("id");
            Assert.assertTrue(postId > 0, "Created post ID should be positive");
            Assert.assertEquals(response.jsonPath().getInt("user_id"), userId);
            Assert.assertEquals(response.jsonPath().getString("title"), title);
            Assert.assertEquals(response.jsonPath().getString("body"), body);

            UserOperationReporter.recordCheck("POST post status code", 201, response.getStatusCode());
            UserOperationReporter.recordCheck(
                    "POST post response time below limit",
                    "< " + MAX_TIME_MS + " ms",
                    response.getTime() + " ms");
            UserOperationReporter.recordCheck("Created post ID", "positive integer", postId);
            UserOperationReporter.recordCheck("Post belongs to user", userId,
                    response.jsonPath().getInt("user_id"));
            UserOperationReporter.recordCheck("Created post title", title, response.jsonPath().getString("title"));
            UserOperationReporter.recordCheck("Created post body", body, response.jsonPath().getString("body"));
            System.out.println("Created local post: " + response.asString());
        } finally {
            if (postId != null) {
                given().spec(requestSpec).when().delete("/public/v2/posts/" + postId).then().statusCode(204);
            }
            deleteUser(requestSpec, userId);
        }
    }
}
