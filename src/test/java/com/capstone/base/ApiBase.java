package com.capstone.base;

import com.capstone.utils.PropertyReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public class ApiBase {

    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setUp() {
        String token = PropertyReader.get("token");
        if (token.isBlank() || token.contains("PASTE_YOUR_TOKEN")) {
            throw new IllegalStateException("Set a real token in " + PropertyReader.getEnv() + ".properties");
        }
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(PropertyReader.get("baseUrl"))
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .addFilter((req, res, ctx) -> {
                    io.restassured.response.Response response = ctx.next(req, res);
                    System.out.println("API CALL  : " + req.getMethod() + " " + req.getURI());
                    System.out.println("STATUS    : " + response.getStatusCode());
                    System.out.println("TIME      : " + response.getTime() + " ms");
                    String body = response.getBody().asString();
                    if (!body.isBlank()) {
                        System.out.println("DETAILS   : " + body);
                    }
                    System.out.println("------------------------------------------------");
                    return response;
                })
                .build();
    }
}