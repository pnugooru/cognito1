#Author: your.email@your.domain.com
#Keywords Summary :
#Feature: List of scenarios.
#Scenario: Business rule through list of steps with arguments.
#Given: Some precondition step
#When: Some key actions
#Then: To observe outcomes or validation
#And,But: To enumerate more Given,When,Then steps
#Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
#Examples: Container for s table
#Background: List of steps run before each of the scenarios
#""" (Doc Strings)
#| (Data Tables)
#@ (Tags/Labels):To group Scenarios
#<> (placeholder)
#""
## (Comments)
#Sample Feature Definition Template
@sample
Feature: Sample Feature
  I want to use this template for my feature file

  @GoodLogOn @RunAll
  Scenario: GoodLogOn
    Given User1 logs into AOIC as a valid user
    Then Main page is displayed

  @UpdateLocation @RunAll
  Scenario: Update Location
    Given User1 logs into AOIC as a valid user
    Then Main page is displayed and update Location
    
  @DecisionPoint @RunAll
  Scenario: DecisionPoint
    Given User1 logs into AOIC as a valid user
    Then DecisionPoint page is validated
    
  @LogonToAo @RunAll
  Scenario: LogonToAO
    Given User2 logs into AOIC as a valid user
    Then Main page is displayed and LogonToAO
    
  @DoClearLetters @RunAll
  Scenario: DoClearLetters
    Given User2 logs into AOIC as a valid user
    Then Main page is displayed and DoClearLetters
  
  @ValidateAddUpdateDeleteRemarks @RunAll
  Scenario: ValidateAddUpdateDeleteRemarks
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and ValidateAddUpdateDeleteRemarks
    
  @AddUpdatePOA @RunAll
  Scenario: AddUpdatePOA
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and AddUpdatePOA  
    
  @CheckCCCUpdateEntity @RunAll
  Scenario: CheckCCCUpdateEntity
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and CheckCCCUpdateEntity
    
  @GenerateRejectLetter @RunAll
  Scenario: GenerateRejectLetter
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and GenerateRejectLetter   
    
 
  @ReportsAndListings @RunAll
  Scenario: ReportsAndListings
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and ReportsAndListings
  
  @AddUpdateMFT @RunAll
  Scenario: AddUpdateMFT
	Given User2 logs into AOIC as a valid user
    Then Main page is displayed and AddUpdateMFT     