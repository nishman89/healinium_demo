package com.sparta.nam;


import net.serenitybdd.core.pages.WebElementFacade;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Title;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@ExtendWith(SerenityJUnit5Extension.class)
public class LoginTestSelenium {

    @Managed
    WebDriver webDriver;


    private static final String BASE_URL =
        new File("src/test/resources/webapp/index.html").toURI().toString();

    @BeforeEach
    public void setup(){
        webDriver.get(BASE_URL);
    }
    @Test
    @Title("Check the page title is correct")
    public void checkPageTitle() {
        MatcherAssert.assertThat(webDriver.getTitle(), is("Sparta Global — Sign In"));
    }


    @Test
    @Title("Check that all login form elements are visible on the page")
    public void checkLoginFormElementsArePresent() {
        WebElement usernameField = webDriver.findElement(By.name("user-name"));
        WebElement passwordField = webDriver.findElement(By.name("password"));
        WebElement loginButton   = webDriver.findElement(By.id("login-button"));

        MatcherAssert.assertThat(usernameField.isDisplayed(), is(true));
        MatcherAssert.assertThat(passwordField.isDisplayed(), is(true));
        MatcherAssert.assertThat(loginButton.isDisplayed(),   is(true));
    }


    @Test
    @Title("Given valid credentials, when I click Login, then the success popup appears")
    public void successfulLoginTest() {
        // Arrange
        WebElement usernameField = webDriver.findElement(By.name("user-name"));
        WebElement passwordField = webDriver.findElement(By.name("password"));
        WebElement loginButton   = webDriver.findElement(By.id("login-button"));

        // Act
        usernameField.sendKeys("sparta");
        passwordField.sendKeys("correct");
        loginButton.click();

        // Assert
        WebElement successPopup = new WebDriverWait(webDriver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));

        MatcherAssert.assertThat( successPopup.getText(), containsString("You have signed in successfully"));
    }


    @Test
    @Title("Given an invalid password, when I click Login, then the error popup appears")
    public void unsuccessfulLoginTest_InvalidPassword() {
        // Arrange
        WebElement usernameField = webDriver.findElement(By.name("user-name"));
        WebElement passwordField = webDriver.findElement(By.name("password"));
        WebElement loginButton   = webDriver.findElement(By.id("login-button"));

        // Act
        usernameField.sendKeys("sparta");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        // Assert
        WebElement errorPopup = new WebDriverWait(webDriver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-test='error']")));

        MatcherAssert.assertThat(errorPopup.getText(), containsString("Incorrect details"));
    }



}