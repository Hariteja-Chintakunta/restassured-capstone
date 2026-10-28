package com.capstone.utils;

import com.capstone.models.User;
import com.capstone.reporting.UserOperationReporter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

public class UserApiHelper {

    public static final String USERS = "/public/v2/users";
    public static final long MAX_TIME_MS = 5000L;

    public static int createUser(RequestSpecification spec, User user) {
        return createUser(spec, user.getName(), user.getEmail(), user.getGender(), user.getStatus());
    }

    public static int createUser(RequestSpecification spec, String name, String email) {
        return createUser(spec, name, email, "male", "active");
    }

    public static int createUser(
            RequestSpecification spec, String name, String email, String gender, String status) {
        String requestPayload = TestDataUtil.userJson(name, email, gender, status);
        Response response = given().spec(spec)
                .body(requestPayload)
                .when().post(USERS);

        UserOperationReporter.recordCreated(
                requestPayload, response.getBody().asString(), response.getStatusCode());
        response.then().log().ifValidationFails()
                .statusCode(201)
                .time(lessThan(MAX_TIME_MS))
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("email", equalTo(email));
        UserOperationReporter.recordCheck("POST status code", 201, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "POST response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Created user ID", "not null", response.jsonPath().get("id"));
        UserOperationReporter.recordCheck("Created user name", name, response.jsonPath().getString("name"));
        UserOperationReporter.recordCheck("Created user email", email, response.jsonPath().getString("email"));
        return response.jsonPath().getInt("id");
    }

    public static void updateUser(
            RequestSpecification spec, int id, String name, String email, String gender, String status) {
        Response oldUserResponse = given().spec(spec)
                .when().get(USERS + "/" + id)
                .then().statusCode(200)
                .extract().response();
        String oldUserPayload = oldUserResponse.asString();
        UserOperationReporter.recordCheck("GET user before update status code", 200, oldUserResponse.getStatusCode());
        String requestPayload = TestDataUtil.userJson(name, email, gender, status);
        Response response = given().spec(spec)
                .body(requestPayload)
                .when().put(USERS + "/" + id);

        UserOperationReporter.recordUpdated(
                id, oldUserPayload, requestPayload, response.getBody().asString(), response.getStatusCode());
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("id", equalTo(id))
                .body("name", equalTo(name))
                .body("status", equalTo(status));
        UserOperationReporter.recordCheck("PUT status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "PUT response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Updated user ID", id, response.jsonPath().getInt("id"));
        UserOperationReporter.recordCheck("Updated user name", name, response.jsonPath().getString("name"));
        UserOperationReporter.recordCheck("Updated user status", status, response.jsonPath().getString("status"));
    }

    public static void deleteUser(RequestSpecification spec, int id) {
        Response userResponse = given().spec(spec)
                .when().get(USERS + "/" + id)
                .then().statusCode(200)
                .extract().response();
        String userBeforeDeletion = userResponse.asString();
        UserOperationReporter.recordCheck(
                "GET user before deletion status code", 200, userResponse.getStatusCode());
        Response response = given().spec(spec)
                .when().delete(USERS + "/" + id);

        UserOperationReporter.recordDeleted(
                id, userBeforeDeletion, response.getBody().asString(), response.getStatusCode());
        response.then().log().ifValidationFails()
                .statusCode(204)
                .time(lessThan(MAX_TIME_MS));
        UserOperationReporter.recordCheck("DELETE status code", 204, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "DELETE response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
    }
}