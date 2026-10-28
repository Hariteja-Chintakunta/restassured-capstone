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
public class GetPostCommentsTest extends ApiBase {

    @Test(description = "TC11 - Get comments for a seeded local post")
    public void getCommentsForPost() {
        // Arrange
        int postId = 1;

        // Act
        Response response = given().spec(requestSpec)
                .when().get("/public/v2/posts/" + postId + "/comments");

        // Assert
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS));

        List<Map<String, Object>> comments = response.jsonPath().getList("");
        Assert.assertNotNull(comments, "Comment list should be present");
        Assert.assertFalse(comments.isEmpty(), "Seeded post should have a comment");
        for (Map<String, Object> comment : comments) {
            Assert.assertNotNull(comment.get("id"), "Comment ID should be present");
            Assert.assertEquals(((Number) comment.get("post_id")).intValue(), postId);
            Assert.assertFalse(comment.get("body").toString().isBlank(), "Comment body should not be empty");
        }

        UserOperationReporter.recordCheck("GET comments status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET comments response time below limit",
                "< " + MAX_TIME_MS + " ms",
                response.getTime() + " ms");
        UserOperationReporter.recordCheck("Comments are returned", "more than 0 comments", comments.size());
        UserOperationReporter.recordCheck(
                "Every comment belongs to requested post",
                "post_id=" + postId,
                comments.size() + " comments validated");
        System.out.println("Comments for local post " + postId + ": " + response.asString());
    }
}
