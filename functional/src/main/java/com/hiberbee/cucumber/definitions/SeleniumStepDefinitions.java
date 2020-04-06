/*
 * MIT License
 *
 * Copyright (c) 2020 Hiberbee
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hiberbee.cucumber.definitions;

import io.cucumber.java.After;
import io.cucumber.java.ParameterType;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.browser.model.WindowState;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

public class SeleniumStepDefinitions {

  private WebDriver driver;

  @ParameterType(value = "(Chrome|Edge|Opera|Firefox)", name = "browser")
  public DriverManagerType browser(@NotNull final String value) {
    return DriverManagerType.valueOf(value.toUpperCase());
  }

  @ParameterType(value = "(maximized|minimized|fullscreen|normal)", name = "windowState")
  public WindowState windowState(@NotNull final String value) {
    return WindowState.fromString(value.toUpperCase());
  }

  @ParameterType(value = "(.+)", name = "url")
  public URL url(final String value) {
    final var url = Try.call(() -> URI.create(value)).andThenTry(URI::toURL).toOptional();
    return url.orElseThrow();
  }

  @After
  public void maximizeWindow(@NotNull final Scenario scenario) {
    if (scenario.isFailed()) this.driver.manage().window().maximize();
  }

  @Given("window state is {windowState}")
  public void windowStateIs(@NotNull final WindowState state) {
    switch (state) {
      case NORMAL:
      default:
        break;
      case MAXIMIZED:
        this.driver.manage().window().maximize();
        break;
      case MINIMIZED:
        this.driver.manage().window().minimize();
        break;
      case FULLSCREEN:
        this.driver.manage().window().fullscreen();
        break;
    }
  }

  @Given("I can open {url} web page")
  public void iCanOpenWebPageWithWebDriver(@NotNull final URL url) {
    this.driver.get(url.toExternalForm());
  }

  @After
  public void quitWebDriver() {
    if (this.driver != null) this.driver.quit();
  }


  @Given("{browser} web browser")
  public void webBrowser(@NotNull final DriverManagerType browser) {
    WebDriverManager.getInstance(browser).setup();
    @SuppressWarnings("unchecked") final var maybeDriver = (Optional<WebDriver>) ReflectionUtils
      .tryToLoadClass(browser.browserClass())
      .andThenTry(ReflectionUtils::newInstance).toOptional();
    this.driver = maybeDriver.orElseGet(ChromeDriver::new);
  }

  @And("^(page source|title) should(?:(| not)) contain (.+)$")
  public void pageTitleShouldContain(final String location, final String not, final String expected) {
    final Function<? super WebDriver, String> extractingFunction = webDriver -> {
      if (location.equals("title")) return webDriver.getTitle();
      return webDriver.getPageSource();
    };
    Assertions.assertThat(this.driver).extracting(extractingFunction).matches(actual -> not.isBlank() == actual.contains(expected));
  }
}
