package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.capstone.utils.PostApiHelper.*;
import static com.capstone.utils.UserApiHelper.MAX_TIME_MS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Listeners(UserOperationReportListener.class)
public class GetProductListTest extends ApiBase {

    @Test(description = "TC06 - Get product (post) list")
    public void getProductList() {
        given().spec(requestSpec)
                .when().get(POSTS)
                .then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS))
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].title", not(emptyOrNullString()));
    }
}
