package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.reporting.UserOperationReporter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.capstone.utils.UserApiHelper.MAX_TIME_MS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

@Listeners(UserOperationReportListener.class)
public class GetAllPostsTest extends ApiBase {

    @Test(description = "TC09 - Get all posts from the local API")
    public void getAllPosts() {
        // Arrange

        // Act
        Response response = given().spec(requestSpec)
                .when().get("/public/v2/posts");

        // Assert
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS));

        List<Map<String, Object>> posts = response.jsonPath().getList("");
        Assert.assertNotNull(posts, "Post list should be present");
        Assert.assertFalse(posts.isEmpty(), "Post list should not be empty");
        for (Map<String, Object> post : posts) {
            Assert.assertNotNull(post.get("id"), "Post ID should be present");
            Assert.assertNotNull(post.get("user_id"), "Post user ID should be present");
            Assert.assertFalse(post.get("title").toString().isBlank(), "Post title should not be empty");
        }

        UserOperationReporter.recordCheck("GET posts status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET posts response time below limit",
                "< " + MAX_TIME_MS + " ms",
                response.getTime() + " ms");
        UserOperationReporter.recordCheck("Post list is not empty", "more than 0 posts", posts.size());
        UserOperationReporter.recordCheck(
                "Every post has an ID, user ID, and title",
                "all fields present",
                posts.size() + " posts validated");
        System.out.println("Local posts: " + response.asString());
    }
}
