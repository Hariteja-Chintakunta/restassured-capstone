package com.capstone.utils;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class UserApiHelper {

    public static final String USERS = "/public/v2/users";
    public static final long MAX_TIME_MS = 5000L;

    public static int createUser(RequestSpecification spec, String name, String email) {
        return given().spec(spec)
                .body(TestDataUtil.userJson(name, email, "male", "active"))
                .when().post(USERS)
                .then().statusCode(201)
                .extract().path("id");
    }

    public static void deleteUser(RequestSpecification spec, int id) {
        given().spec(spec)
                .when().delete(USERS + "/" + id)
                .then().statusCode(204);
    }
}