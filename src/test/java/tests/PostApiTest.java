package tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.PropertyReader;

@Epic("GoRest API")
@Feature("Posts")
public class PostApiTest {

    private String postsEndpoint;
    private long maxTimeMs;

    private int postId;
    private String postTitle;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = PropertyReader.get("baseUrl");
        RestAssured.filters(new AllureRestAssured());
        postsEndpoint = PropertyReader.get("postsEndpoint");
        maxTimeMs = Long.parseLong(PropertyReader.get("maxResponseTimeMs"));
    }

    @Test(priority = 1)
    @Story("Get Post List")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Fetch all posts and verify the list is non-empty with valid ids and titles")
    public void getPostList() {
        Response response =
            given()
                .log().uri()
            .when()
                .get(postsEndpoint)
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("id", everyItem(notNullValue()))
                .body("title", everyItem(notNullValue()))
                .time(lessThan(maxTimeMs))
                .extract().response();

        postId = response.jsonPath().getInt("[0].id");
        postTitle = response.jsonPath().getString("[0].title");
        System.out.println("Using post id = " + postId + ", title = " + postTitle);
    }

    @Test(priority = 2, dependsOnMethods = "getPostList")
    @Story("Get Post Details")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Fetch a single post by id and verify its fields match")
    public void getPostDetails() {
        given()
            .log().uri()
        .when()
            .get(postsEndpoint + "/" + postId)
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))
            .body("title", equalTo(postTitle))
            .time(lessThan(maxTimeMs));
    }

    @Test(priority = 3, dependsOnMethods = "getPostList")
    @Story("Search Post")
    @Severity(SeverityLevel.NORMAL)
    @Description("Search posts by title keyword and verify matching results")
    public void searchPost() {
        String keyword = postTitle.split("\\s+")[0];

        Response response =
            given()
                .log().uri()
                .queryParam("title", keyword)
            .when()
                .get(postsEndpoint)
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .time(lessThan(maxTimeMs))
                .extract().response();

        for (String title : response.jsonPath().getList("title", String.class)) {
            Assert.assertTrue(title.toLowerCase().contains(keyword.toLowerCase()),
                "Title '" + title + "' does not contain keyword '" + keyword + "'");
        }
        Assert.assertTrue(response.jsonPath().getList("id").contains(postId),
            "Original post id " + postId + " not found in search results");
    }
}
