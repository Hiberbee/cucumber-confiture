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

import com.hiberbee.cucumber.functions.*;
import com.hiberbee.cucumber.gherkin.dsl.Maybe;
import com.hiberbee.cucumber.support.CucumberRun;
import io.cucumber.java.*;
import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.junit.platform.commons.function.Try;
import org.openqa.selenium.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;

import javax.annotation.Nonnull;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.*;

@Log
public class SeleniumStepDefinitions {

  private final Cache cache;
  private final WebDriver webDriver;

  public SeleniumStepDefinitions(
      final WebDriver webDriver, @Value("#{cacheManager.getCache('feature')}") final Cache cache) {
    this.webDriver = webDriver;
    this.cache = cache;
  }

  private @Nonnull Consumer<String> addToHistory() {
    return Objects.requireNonNull(this.cache.get(State.HISTORY, ArrayList<String>::new))::add;
  }

  private void makeScreenShot(final String name) {
    new ScreenShotGenerator()
        .accept(
            new ScreenShotNamer()
                .andThen(Paths.get("build/reports/screenshots")::resolve)
                .apply(String.valueOf(name).replace("\"", "")),
            this.webDriver);
  }

  @ParameterType(value = "(Chrome|Edge|Opera|Firefox)")
  public DriverManagerType browser(@Nonnull final String value) {
    return DriverManagerType.valueOf(value.toUpperCase());
  }

  @ParameterType("(maximized|minimized|fullscreen|normal)")
  public WindowState windowState(@Nonnull final String value) {
    return WindowState.valueOf(value.toUpperCase());
  }

  @Given("link with text {string} {maybe} present on the page")
  public void windowStateIs(final String text, @Nonnull final Maybe maybe) {
    Assertions.assertThat(this.webDriver.findElement(By.linkText(text)).isDisplayed())
        .matches(maybe.predicate());
  }

  @When("user click on link with text {string}")
  public void userClickOnLinkWithText(final String text) {
    this.webDriver.findElement(By.linkText(text)).click();
  }

  @Given("window state is {windowState}")
  public void windowStateIs(@Nonnull final WindowState state) {
    switch (state) {
      case MAXIMIZED:
        this.webDriver.manage().window().maximize();
        break;
      case MINIMIZED:
        this.webDriver.manage().window().setSize(new Dimension(0, 0));
        break;
      case FULLSCREEN:
        this.webDriver.manage().window().fullscreen();
        break;
      case NORMAL:
      default:
        break;
    }
  }

  @Given("user is opening {url} url")
  public void isOpeningUrl(@Nonnull final URL url) {
    Try.call(url::toURI)
        .andThenTry(URI::toString)
        .ifSuccess(this.webDriver::get)
        .ifSuccess(this.addToHistory())
        .ifFailure(CucumberRun::fail);
  }

  @Before("@ui")
  public void initBrowserHistory() {
    this.cache.putIfAbsent(State.HISTORY, new ArrayList<String>());
  }

  @After("@ui")
  public void quitWebDriver(final Scenario scenario) {
    if (this.webDriver != null) {
      if (scenario.isFailed()) {
        this.makeScreenShot(scenario.getName());
      }
    }
  }

  @ParameterType("(title|source|url)")
  public String pageAttribute(final String value) {
    return value;
  }

  @And("browser history should contain")
  public void browserHistoryShouldContain(final List<String> urls) {
    final var history = this.cache.get(State.HISTORY, ArrayList<String>::new);
    Assertions.assertThat(history).containsAll(urls);
  }

  @And("page {pageAttribute} {maybe} contain {string}")
  public void pageTitleShouldContain(
      final String location, final Maybe maybe, final String expected) {
    final Function<? super WebDriver, String> extractingFunction =
        webDriver -> {
          if (location.equals("title")) return webDriver.getTitle();
          if (location.equals("source")) return webDriver.getPageSource();
          if (location.equals("url")) return webDriver.getCurrentUrl();
          return "";
        };
    Assertions.assertThat(this.webDriver)
        .extracting(extractingFunction)
        .satisfies(
            actual ->
                Assertions.assertThat(actual)
                    .satisfies(it -> Assertions.assertThat(it).contains(expected)));
  }

  enum WindowState {
    MAXIMIZED,
    MINIMIZED,
    FULLSCREEN,
    NORMAL
  }

  enum State {
    HISTORY
  }
}
