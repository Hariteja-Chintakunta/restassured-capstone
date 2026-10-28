package com.capstone.tests;

import com.capstone.base.ApiBase;
import com.capstone.reporting.UserOperationReportListener;
import com.capstone.reporting.UserOperationReporter;
import com.capstone.utils.ApiSpecs;
import com.capstone.utils.ProductApiHelper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.capstone.utils.UserApiHelper.MAX_TIME_MS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;

@Listeners(UserOperationReportListener.class)
public class GetProductListTest extends ApiBase {

    @Test(description = "TC06 - Get product list; validate product ID, name, and status code")
    public void getProductListValidateProductIdNameAndStatusCode() {
        // Arrange
        var productSpec = ApiSpecs.productSpec();

        // Act
        Response response = given().spec(productSpec)
                .when().get(ProductApiHelper.PRODUCTS);

        // Assert
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS));

        List<Map<String, Object>> products = response.jsonPath().<Map<String, Object>>getList("products");
        Assert.assertNotNull(products, "Product list should be present in the response");
        Assert.assertFalse(products.isEmpty(), "Product list should contain products");

        for (var product : products) {
            Assert.assertNotNull(product.get("id"), "Product ID should not be null");
            Assert.assertNotNull(product.get("title"), "Product name should not be null");
            Assert.assertFalse(product.get("title").toString().isBlank(), "Product name should not be empty");
        }
        UserOperationReporter.recordCheck("GET status code", 200, response.getStatusCode());
        UserOperationReporter.recordCheck(
                "GET response time below limit", "< " + MAX_TIME_MS + " ms", response.getTime() + " ms");
        UserOperationReporter.recordCheck("Product list is not empty", "more than 0 products", products.size());
        UserOperationReporter.recordCheck(
                "Every product has an ID", "non-null IDs", products.size() + " products validated");
        UserOperationReporter.recordCheck(
                "Every product has a name", "non-empty names", products.size() + " products validated");
        ProductApiHelper.logProducts("TC06 - Products returned:", products);
    }
}
