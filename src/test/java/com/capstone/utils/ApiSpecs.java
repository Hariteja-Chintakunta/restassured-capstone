package com.capstone.utils;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class ApiSpecs {

    public static RequestSpecification productSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(PropertyReader.get("productBaseUrl"))
                .setContentType(ContentType.JSON)
                .build();
    }

    public static long maxResponseTime() {
        return Long.parseLong(PropertyReader.get("maxResponseTimeMs"));
    }
}