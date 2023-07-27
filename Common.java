package gov.irs.automationtest.fw;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.Select;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class Common {
	
	private static ExtentTest test;
	private static ExtentReports report;
	private static WebDriver driver;
	private static Properties localConf;
	private static final String userDir = System.getProperty("user.dir");
	private static Scenario testScenario;
	
	@Before
	public void setUp(Scenario scenario) throws Exception {
		testScenario = scenario;
		localConf = ConfUtils.loadConf();
		report = new ExtentReports(userDir + ConfUtils.getProperty("extentRptPath") + "_" + getDate() + ".html", false);
		String scenarioName = scenario.getName();
		test = report.startTest(scenarioName);
		getWebDriver().get(ConfUtils.getEnvProperty("url"));
		test.log(LogStatus.INFO, "AOIC Test Started");
	}
	
	@After
	public void tearDown() throws Exception {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
		report.endTest(test);
		report.flush();
		report.close();
	}
	
	/**
	 * Returns WebDriver
	 * @return WebDriver
	 */
	public static WebDriver getWebDriver() {
		if (driver==null) {

			System.setProperty("webdriver.edge.driver", userDir + ConfUtils.getProperty("msEdgeDvrPath"));
			EdgeOptions edgeOptions = new EdgeOptions();
			String usrDataPath = "user-data-dir="+System.getProperty("user.home") + ConfUtils.getProperty("usrDataPath");
			edgeOptions.addArguments(usrDataPath);
			edgeOptions.addArguments("--remote-allow-origins=*");
			driver=new EdgeDriver(edgeOptions);
			driver.manage().window().maximize();
		}
		return driver;
	}
	
	/**
	 * Returns the current date in MM-dd-yyyy format
	 * @return String date
	 * 
	 */
	public String getDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");  		
		Date curDate = new Date();
		return formatter.format(curDate);
	}
	
	public static String AddScreenShot(String fileName) {
		TakesScreenshot scrShot = ((TakesScreenshot)driver);
		try {
			File src = scrShot.getScreenshotAs(OutputType.FILE);
			File destFile = new File( userDir + ConfUtils.getProperty("screenshotPath")+testScenario.getName()+"_"+fileName+Calendar.getInstance().getTimeInMillis()+".png");
			FileUtils.copyFile(src, destFile);
			return test.addScreenCapture(destFile.getAbsolutePath());
		} catch(Exception e) {
			e.printStackTrace();
		} 
		return null;
		
	}
	
	public void cleanupScreenshots() {
		try {
			File destFile = new File( userDir + ConfUtils.getProperty("screenshotPath"));
			FileUtils.cleanDirectory(destFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void LogFailedTestScreenCapture(String message) {
		test.log(LogStatus.FAIL, message +AddScreenShot("Exception"));
	}
	
	public static void LogFailedTestScreenCapture(String message, String fileName) {
		test.log(LogStatus.FAIL, message +AddScreenShot(fileName));
	}
	
	public static void LogTestScreenCapture(String message) {
		test.log(LogStatus.PASS, AddScreenShot(message));
	}
	
	/**
	 * Returns the WebElement by xpath
	 * @param xpath xpath of the webelement in webpage
	 * @return WebElement
	 * 
	 */
	public static WebElement FindElementByXpath(String xpath) {
		return getWebDriver().findElement(By.xpath(xpath));
	}
	
	/**
	 * Returns the List of WebElements by xpath
	 * @param xpath xpath of the webelements in webpage
	 * @return List of WebElements
	 * 
	 */
	public static List<WebElement> FindElementsByXpath(String xpath) {
		return getWebDriver().findElements(By.xpath(xpath));
	}
	
	public static WebElement FindElementByName(String name) {
		return getWebDriver().findElement(By.name(name));
	}
	
	public static WebElement FindElementByLinkText(String linkText) {
		return getWebDriver().findElement(By.linkText(linkText));
	}
	
	public static List<WebElement> FindElementsByName(String name) {
		return getWebDriver().findElements(By.name(name));
	}
	
	public static List<WebElement> FindElementsByLinkText(String linkText) {
		return getWebDriver().findElements(By.linkText(linkText));
	}
	
	public static WebElement FindElementById(String id) {
		return getWebDriver().findElement(By.id(id));
	}
	
	public static WebElement FindElementByClassName(String className) {
		return getWebDriver().findElement(By.className(className));
	}
	
	public static List<WebElement> FindElementsByClassName(String className) {
		return getWebDriver().findElements(By.className(className));
	}
	
	public static boolean IsTextPresent(String text) {
		return getWebDriver().getPageSource().contains(text);
	}
	
	public static void LogSuccessfulTest(String message) {
		test.log(LogStatus.PASS, message);
	}
	
	public static void ScrollDownBy(int num) { 
		JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
		js.executeScript("window.scrollBy(0,"+num+")", "");
		
	}
	
	public static void SelectByValue(String elementName, String value) { 
		Select st = new Select(FindElementByName(elementName));
		st.selectByValue(value);
	}
	
	public static void SelectByValues(String elementName, String[] values) { 
		Select pghs = new Select(FindElementByName(elementName));
		List<WebElement> options = pghs.getOptions();
		for (WebElement i : options) {
			if (Arrays.asList(values).contains(i.getText())) {
				pghs.selectByValue(i.getText());
			}
		}
	}
	
	/**
	 * Returns list of values in dropdown
	 * @param fieldName dropdown field name in webpage
	 * @return list of options
	 */
	public static List<String> GetListOfValuesFromSelect(String fieldName) {
		Select select = new Select(FindElementByName(fieldName));
		List<WebElement> allOptions = select.getOptions();
		List<String> options = new ArrayList<String>();
		
		for(WebElement webElement : allOptions) {
			options.add(webElement.getAttribute("value"));
		}
		return options;
	}

	/**
	 * Calls a script by the ScriptKey returned by pin(String). This can be thought of as inlining the pinned script and simply calling executeScript(String, Object).
	 * @param element Webelement
	 */	
	public static void scrollToElementAndClick(WebElement element) {
		
		((JavascriptExecutor)getWebDriver()).executeScript("arguments[0].scrollIntoView(true);",element); 
		element.click();
	}
	
	/**
	 * Switches to the currently active modal dialog for this particular driver instance.
	 * @return A handle to the dialog.
	 */	
	public static Alert SwitchToAlert() {
		return getWebDriver().switchTo().alert();
	}
	
	/**
	 * Returns the current date in MM/dd/yyyy format
	 * @return String date
	 */
	public static String getCurrentDate() {
		LocalDate dateObj = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return dateObj.format(formatter);
	}
	
	public static String getCurrentDatePlusDays(int days) {
		LocalDate dateObj = LocalDate.now().plusDays(days);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return dateObj.format(formatter);
	}
	public static String getCurrentDateMinusDays(int days) {
		LocalDate dateObj = LocalDate.now().minusDays(days);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return dateObj.format(formatter);
	}
}
