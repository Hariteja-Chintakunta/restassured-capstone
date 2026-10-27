import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BuildCheckTest {
    @Test
    public void buildWorks() {
        Assert.assertNotNull(RestAssured.class);
    }
}