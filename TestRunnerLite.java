package gov.irs.aoic.test.automation.runners;
import org.junit.runner.RunWith;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		glue = {"gov.irs.aoic.test.steps"},
		features = {"src/test/resources/features"},
		strict=true,
		plugin = {"json:target/cucumber.json"},
		tags = {" @AddUpdateMFT"}
		)

public class TestRunnerLite {

}

