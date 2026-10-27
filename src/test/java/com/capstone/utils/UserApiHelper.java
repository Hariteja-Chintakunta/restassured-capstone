package com.capstone.utils;

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

    public static int createUser(RequestSpecification spec, String name, String email) {
        String requestPayload = TestDataUtil.userJson(name, email, "male", "active");
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
        return response.jsonPath().getInt("id");
    }

    public static void updateUser(
            RequestSpecification spec, int id, String name, String email, String gender, String status) {
        String oldUserPayload = given().spec(spec)
                .when().get(USERS + "/" + id)
                .then().statusCode(200)
                .extract().asString();
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
    }

    public static void deleteUser(RequestSpecification spec, int id) {
        String userBeforeDeletion = given().spec(spec)
                .when().get(USERS + "/" + id)
                .then().statusCode(200)
                .extract().asString();
        Response response = given().spec(spec)
                .when().delete(USERS + "/" + id);

        UserOperationReporter.recordDeleted(
                id, userBeforeDeletion, response.getBody().asString(), response.getStatusCode());
        response.then().log().ifValidationFails()
                .statusCode(204)
                .time(lessThan(MAX_TIME_MS));
    }
}