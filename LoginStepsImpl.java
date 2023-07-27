package gov.irs.aoic.test.impl;

import gov.irs.aoic.test.pages.AOICMenuPage;
import gov.irs.aoic.test.pages.AreaOfficeMenuPage;
import gov.irs.aoic.test.pages.DecisionPointPage;
import gov.irs.aoic.test.pages.LoginPage;
import gov.irs.aoic.test.pages.MainPage;
import gov.irs.aoic.test.pages.OICUserPage;
import gov.irs.automationtest.fw.ConfUtils;

public class LoginStepsImpl {
	
	LoginPage loginPage = new LoginPage();
	MainPage mainPage = new MainPage();
	AOICMenuPage aoicMenuPage = new AOICMenuPage();
	OICUserPage oicUserPage = new OICUserPage();
	AreaOfficeMenuPage aoMenuPage = new AreaOfficeMenuPage();
	DecisionPointPage dpointPage = new DecisionPointPage();
	
	public void validlogin(String user, String pwd) throws InterruptedException {
		loginPage.login(ConfUtils.getEnvProperty(user), ConfUtils.getEnvProperty(pwd));
		loginPage.validateLogin();
	}
	
	public void validMain() throws InterruptedException {
		mainPage.validateMainPage();
	}
	
	public void clickAOICMenuLnk() throws InterruptedException {
		mainPage.clickAOICMenuLnk();
	}
	
    public void validLogonToAO() throws InterruptedException {
		aoicMenuPage.verifyElementPresentByXpath("//*[@id=\"plusImg\"]", "plugImg");
		aoicMenuPage.verifyElementPresentByXpath("//*[@id=\"minusImg\"]", "minusImg");
		aoicMenuPage.verifyElementPresentByXpath("//*[@id=\"resetImg\"]", "resetImg");
		
		aoicMenuPage.validateElementByXpath("//*[@id=\"plusImg\"]", "plusImg");
		aoicMenuPage.validateElementByXpath("//*[@id=\"plusImg\"]", "plusImg");
		aoicMenuPage.validateElementByXpath("//*[@id=\"plusImg\"]", "plusImg");
		aoicMenuPage.validateElementByXpath("//*[@id=\"plusImg\"]", "plusImg");
		
		aoicMenuPage.validateElementByXpath("//*[@id=\"minusImg\"]", "minusImg");
		aoicMenuPage.validateElementByXpath("//*[@id=\"minusImg\"]", "minusImg");
				
		aoicMenuPage.validateElementByXpath("//*[@id=\"resetImg\"]", "resetImg");
		
		aoicMenuPage.clickOICAreaOffice();
		
		aoMenuPage.clickOnOICUser();
		
		oicUserPage.checkUpdateRecordText();
		
		oicUserPage.checkOICRecordText();
		
		oicUserPage.updateOICUserLocation("Seleniao", "512", "123", "1234", "", "17 - Memphis Centralized");
    }
    
	public void clickOICAccess() throws InterruptedException {
		aoicMenuPage.clickOICAccess();
	}
	
	public void updateOICLocation() throws InterruptedException {
	    // click OIC Area Office
		aoicMenuPage.clickOICAreaOffice();
		
		aoMenuPage.clickOnOICUser();
		
		oicUserPage.checkUpdateRecordText();
		
		oicUserPage.checkOICRecordText();
		
		oicUserPage.updateOICUserLocation("Selenium User", "512", "867", "5309", "123", "18 - Brookhaven Centralized");
		
		mainPage.clickAOICMenuLnk();
	    // click OIC Area Office
		aoicMenuPage.clickOICAreaOffice();
	    
	    // check on Area OfficeText in AO Menu Page.
		aoMenuPage.chkAreaOfficeText();
		
	}
	
	public void validateDecisionPoint() throws InterruptedException {
	    // click OIC Area Office
		aoicMenuPage.clickOICAreaOffice();
		
		aoMenuPage.clickDecisionPoint();
		dpointPage.validateDecisionPoint();
	}
	
	public void validateDoClearLetters() throws InterruptedException {
		aoicMenuPage.clickOICAreaOffice();
		aoMenuPage.clickOnOICUser();
		aoicMenuPage.validateUpdateRecord();
		mainPage.clickAOICMenuLnk();
		aoicMenuPage.clickOICAreaOffice();
		aoicMenuPage.validateFormsAndLetters();
		aoicMenuPage.validateClearFormsAndLetters();
		aoicMenuPage.validateClearLetterFormText();
		aoicMenuPage.validateLetterFormsSelection();
		
		Thread.sleep(1000);
		
	}
	
}