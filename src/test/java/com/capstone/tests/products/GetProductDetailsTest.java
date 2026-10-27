package com.capstone.tests;

import com.capstone.reporting.UserOperationReportListener;
import com.capstone.utils.ApiSpecs;
import com.capstone.utils.ProductApiHelper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static com.capstone.utils.UserApiHelper.MAX_TIME_MS;

@Listeners(UserOperationReportListener.class)
public class GetProductDetailsTest {

    private static final int PRODUCT_ID = 1;

    @Test(description = "TC07 - Get product details; validate product ID, name, and status code")
    public void getProductDetailsValidateProductIdNameAndStatusCode() {
        // Arrange
        var productSpec = ApiSpecs.productSpec();

        // Act
        Response response = given().spec(productSpec)
                .when().get(ProductApiHelper.PRODUCTS + "/" + PRODUCT_ID);

        // Assert
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS));

        var product = response.jsonPath().<String, Object>getMap("");
        Assert.assertEquals(product.get("id"), PRODUCT_ID, "Returned product ID should match the requested ID");
        Assert.assertNotNull(product.get("title"), "Product name should not be null");
        Assert.assertFalse(product.get("title").toString().isBlank(), "Product name should not be empty");
        ProductApiHelper.logProduct("TC07 - Product details returned:", product);
    }
}
