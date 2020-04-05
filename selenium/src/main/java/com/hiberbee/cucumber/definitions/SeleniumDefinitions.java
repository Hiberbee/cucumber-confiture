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

import com.hiberbee.cucumber.types.Browser;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.openqa.selenium.WebDriver;

import java.net.URI;
import java.net.URL;

public class SeleniumDefinitions {

  private WebDriver driver;

  @Before("@Edge")
  public static void setupEdge() {
    WebDriverManager.edgedriver().useBetaVersions().setup();
  }

  @Before("@Chrome")
  public static void setupChrome() {
    WebDriverManager.chromedriver().useBetaVersions().setup();
  }

  @ParameterType(value = "(Chrome|Edge)", name = "browser")
  public static Browser browser(@NotNull final String value) {
    return Browser.valueOf(value.toUpperCase());
  }

  @ParameterType(value = "(.+)", name = "url")
  public static URL url(@NotNull final String value) {
    final var url = Try.call(() -> URI.create(value)).andThenTry(URI::toURL).toOptional();
    return url.orElseThrow();
  }

  @Given("I can open {url} web page in {browser}")
  public void iCanOpenWebPageWithWebDriver(@NotNull final URL url, @NotNull final Browser browser) {
    this.driver = browser.create();
    this.driver.get(url.toExternalForm());
  }

  @After
  public void quitWebDriver() {
    if (this.driver != null) this.driver.quit();
  }

  @Given("{browser} web browser")
  public void webBrowser(@NotNull final Browser browser) {
    this.driver = browser.create();
  }

  @And("^page (title|content) should(?:(| not)) contain (.+)$")
  public void pageTitleShouldContain(final String location, @NotNull final String not, final String text) {
    final var assertion = Assertions.assertThat(this.driver.getTitle());
    if (not.isBlank()) assertion.contains(text);
    else assertion.doesNotContain(text);
  }
}
