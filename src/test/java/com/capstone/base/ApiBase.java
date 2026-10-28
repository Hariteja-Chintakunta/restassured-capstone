package com.capstone.base;

import com.capstone.server.LocalApiServer;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public class ApiBase {

    protected RequestSpecification requestSpec;

    @BeforeSuite(alwaysRun = true)
    public void startLocalApiServer() {
        LocalApiServer.start();
    }

    @AfterSuite(alwaysRun = true)
    public void stopLocalApiServer() {
        LocalApiServer.stop();
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(LocalApiServer.baseUri())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
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