import com.capstone.reporting.UserOperationReportListener;
import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(UserOperationReportListener.class)
public class BuildCheckTest {
    @Test
    public void buildWorks() {
        Assert.assertNotNull(RestAssured.class);
    }
}