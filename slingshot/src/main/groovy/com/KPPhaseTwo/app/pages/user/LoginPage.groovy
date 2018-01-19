package com.KPPhaseTwo.app.pages.user

import com.KPPhaseTwo.model.FailureOutcome
import com.KPPhaseTwo.model.SuccessOutcome
import com.KPPhaseTwo.web.WebForm
import com.KPPhaseTwo.web.WebPage

import com.KPPhaseTwo.tools.Browser

import com.KPPhaseTwo.app.pages.KPCommonPage

/**
 * Created by Sandhya on 24/9/14
 */

final class LoginPage extends WebPage {

	//Override
	def populateData = { browser, formKey, formData ->
		new LoginForm().populateFields(browser, formData);
	}

	//Override
	def submit = { browser, formKey, formData ->
		new LoginForm().submit(browser, formData)
	}

	//To verify the logged in user is displayed
	def static correctUserLoggedIn = {browser, formData ->
		new LoginForm().correctUserLoggedIn browser, formData
	}

	//Try to login to the application with removed admin's credential
	def static loginAsAnAdmin = { browser, formData ->
		new LoginForm().loginAsAnAdmin browser, formData
	}

	def static loginWithInvitedUser = { browser,formData->
		new LoginForm().loginWithInvitedUser browser, formData
	}

	static final class LoginForm extends WebForm {

		//Login form elements
		private static final def  USERNAME = ".//input[@name='username']"

		private static final def PASSWORD = ".//input[@name='password']"

		private static final def SIGNIN = "//input[@class='button-primary'][@value='SIGN IN']"

		private static final def LOGGEDIN_EMAIL = ".//*[@id='emailId']"

		private static final def MENU_BTN = ".//*[@id='menu_btn']"

		private static final def EDITUSER_LINK = ".//a[@ui-sref='kp.editUserProfile']"

		private static final def VIEWPROFILE_LINK = ".//li[@ng-show='viewUserProfile']/a"

		private static final def EMAILON_EDITPROFILE = ".//h5[@class='p_5 text-center profile-text word-wr ng-binding']"

		//private static final def PROFILE_IMAGE = ""

		private static final def FIELDS = [USERNAME, PASSWORD]// the error fields.
		private static final def FORM_ERROR = "//div[@type='danger']/div/span"

		private static final def FIELD_ERROR = "//span[@class='error_message']/span"

		private static final def ERROR_MESSAGE_FIELDS = [FORM_ERROR, FIELD_ERROR]

		//error message map (Key-Value Pair)
		def static final loginPageErrorMessageMap = [
			uname_req:"Username is required",
			pass_req:"Password is required",
			invalid_credentials:"Invalid Credentials"
		]

		//To enter data
		def static final populateFields = { browser, formData ->
			def outcome = WebForm.checkFormFieldsData(formData, FIELDS)
			if(outcome.isSuccess()){
				for(int i = 0; i < FIELDS.size(); i++){
					if(FIELDS[i].equals(USERNAME)){
						KPCommonPage.userName = formData[i]
					}
				}
				outcome = WebForm.enterData(browser, formData, FIELDS, SIGNIN, WAIT_REQ_FIELDS)
			}
			return outcome
		}

		/**
		 * To submit the form
		 * @param browser browser instance
		 * @param data  array containing test data
		 */
		def final submit(browser, data) {
			def actualValidationMsg = submitForm browser, FIELDS, SIGNIN, data, ERROR_MESSAGE_FIELDS
			def actualValidationMsgKeys = getActualErrorMessageKeys(actualValidationMsg, loginPageErrorMessageMap)
			def outcome = new SuccessOutcome();
			outcome.setResultData(actualValidationMsgKeys)
			return outcome
		}

		//override submitForm
		def static submitForm = {browser, formFields, submitButton, data, errFields ->
			browser.click submitButton // submit the form.
			browser.delay(3000)
			browser.getValidationMessages errFields // get the validation messages from the current page.
		}

		//To verify the logged in user is displayed
		def static final correctUserLoggedIn = { browser, formData ->
			def userEmail
			if(browser.isDisplayed(LOGGEDIN_EMAIL)){
				if(KPCommonPage.userName.equalsIgnoreCase(browser.gettext(LOGGEDIN_EMAIL, "value"))){
					return new SuccessOutcome()
				}else{
					return KPCommonPage.returnFailureOutcome(browser, "UserNotMatching", "Logged in user is not matching the displayed user.")
				}
			}else if(browser.isDisplayed(EDITUSER_LINK)){
				browser.click(MENU_BTN)
				browser.delay(1000)
				browser.click(VIEWPROFILE_LINK)
				browser.delay(2000)
				userEmail = browser.gettext(EMAILON_EDITPROFILE).trim()
				if(userEmail.equalsIgnoreCase(KPCommonPage.invitedUserEmailId.trim())){
					return new SuccessOutcome()
				}else{
					return KPCommonPage.returnFailureOutcome(browser, "EmailMismatchIssue", "Email on view profile do not match with the email with which user is logged in")
				}
			}else{
				return KPCommonPage.returnFailureOutcome(browser, "UserDisplayIssue", "User is not displayed.")
			}
		}

		//Try to login to the application with removed admin's credential
		def static final loginAsAnAdmin = { browser, formData ->
			browser.populateField(USERNAME, KPCommonPage.adminEmailId[0])
			browser.populateField(PASSWORD, KPCommonPage.adminPassword)
			if(browser.checkEnabled(SIGNIN)){
				browser.click SIGNIN
				browser.delay(1000)
				return new SuccessOutcome()
			} else {
				return KPCommonPage.returnFailureOutcome(browser, "LoginButtonIssue", "Unable to click login submit button ")
			}
		}

		def static loginWithInvitedUser = { browser,formData->
			browser.populateField(USERNAME, KPCommonPage.invitedUserEmailId)
			browser.populateField(PASSWORD, KPCommonPage.invitedUserPassword)
			if(browser.checkEnabled(SIGNIN)){
				browser.click SIGNIN
				browser.delay(1000)
				return new SuccessOutcome()
			} else {
				return KPCommonPage.returnFailureOutcome(browser, "LoginButtonIssue", "Unable to click login button")
			}
		}
	}
}
