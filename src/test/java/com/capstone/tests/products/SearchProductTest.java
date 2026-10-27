package com.capstone.tests;

import com.capstone.reporting.UserOperationReportListener;
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
public class SearchProductTest {

    private static final String SEARCH_KEYWORD = "phone";

    @Test(description = "TC08 - Search product; validate product ID, name, and status code")
    public void searchProductValidateProductIdNameAndStatusCode() {
        // Arrange
        var productSpec = ApiSpecs.productSpec();

        // Act
        Response response = given().spec(productSpec)
                .queryParam("q", SEARCH_KEYWORD)
                .when().get(ProductApiHelper.PRODUCT_SEARCH);

        // Assert
        response.then().log().ifValidationFails()
                .statusCode(200)
                .time(lessThan(MAX_TIME_MS));

        List<Map<String, Object>> products = response.jsonPath().getList("products");
        Assert.assertNotNull(products, "Search results should be present");
        Assert.assertFalse(products.isEmpty(), "Search should return at least one product");

        Map<String, Object> matchingProduct = products.stream()
                .filter(product -> product.get("title") != null)
                .filter(product -> product.get("title").toString()
                        .toLowerCase().contains(SEARCH_KEYWORD))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No product name matched search keyword: " + SEARCH_KEYWORD));
        Assert.assertNotNull(matchingProduct.get("id"), "Searched product ID should be present");
        ProductApiHelper.logProducts("TC08 - Products matching '" + SEARCH_KEYWORD + "':", products);
    }
}
