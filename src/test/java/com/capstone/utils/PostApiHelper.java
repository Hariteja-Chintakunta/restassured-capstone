package com.capstone.utils;

import io.restassured.specification.RequestSpecification;

import static com.capstone.utils.UserApiHelper.USERS;
import static io.restassured.RestAssured.given;

public class PostApiHelper {

    public static final String POSTS = "/public/v2/posts";

    public static String postJson(String title, String body) {
        return """
                {
                  "title": "%s",
                  "body": "%s"
                }
                """.formatted(title, body);
    }

    public static int createPostForUser(RequestSpecification spec, int userId, String title) {
        return given().spec(spec)
                .body(postJson(title, "Body text for " + title))
                .when().post(USERS + "/" + userId + "/posts")
                .then().statusCode(201)
                .extract().path("id");
    }

    public static void deletePost(RequestSpecification spec, int postId) {
        given().spec(spec)
                .when().delete(POSTS + "/" + postId)
                .then().statusCode(204);
    }
}
