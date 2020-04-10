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

import com.hiberbee.cucumber.functions.ScreenShotGenerator;
import com.hiberbee.cucumber.functions.ScreenShotNamer;
import com.hiberbee.cucumber.support.CucumberRun;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.github.bonigarcia.wdm.DriverManagerType;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.ReflectionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.browser.model.WindowState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Log
public class SeleniumStepDefinitions {

  private WebDriver driver;

  @Value("#{cacheManager.getCache('feature')}")
  private Cache featureState;

  private @NotNull Consumer<String> addToHistory() {
    return Objects.requireNonNull(this.featureState.get(State.HISTORY, ArrayList<String>::new))
        ::add;
  }

  private void makeScreenShot(final String name) {
    new ScreenShotGenerator()
        .accept(
            new ScreenShotNamer()
                .andThen(Paths.get("build/reports/screenshots")::resolve)
                .apply(String.valueOf(name).replace("\"", "")),
            this.driver);
  }

  @ParameterType(value = "(Chrome|Edge|Opera|Firefox)")
  public DriverManagerType browser(@NotNull final String value) {
    return DriverManagerType.valueOf(value.toUpperCase());
  }

  @ParameterType("(maximized|minimized|fullscreen|normal)")
  public WindowState windowState(@NotNull final String value) {
    return WindowState.fromString(value.toUpperCase());
  }

  @Given("window state is {windowState}")
  public void windowStateIs(@NotNull final WindowState state) {
    switch (state) {
      case MAXIMIZED:
        this.driver.manage().window().maximize();
        break;
      case MINIMIZED:
        this.driver.manage().window().minimize();
        break;
      case FULLSCREEN:
        this.driver.manage().window().fullscreen();
        break;
      case NORMAL:
      default:
        break;
    }
  }

  @Given("user is opening {url} url")
  public void isOpeningUrl(@NotNull final URL url) {
    Try.call(url::toURI)
        .andThenTry(URI::toString)
        .ifSuccess(this.driver::get)
        .ifSuccess(this.addToHistory())
        .ifFailure(CucumberRun::fail);
  }

  @Before("@ui")
  public void initBrowserHistory() {
    this.featureState.putIfAbsent(State.HISTORY, new ArrayList<String>());
  }

  @After("@ui")
  public void quitWebDriver(final Scenario scenario) {
    if (this.driver != null) {
      if (scenario.isFailed()) {
        this.makeScreenShot(scenario.getName());
      }
      this.driver.quit();
    }
  }

  @Given("web browser is {browser}")
  public void webBrowserIs(@NotNull final DriverManagerType browser) {
    WebDriverManager.getInstance(browser).setup();
    @SuppressWarnings("unchecked")
    final var maybeDriver =
        (Optional<WebDriver>)
            ReflectionUtils.tryToLoadClass(browser.browserClass())
                .andThenTry(ReflectionUtils::newInstance)
                .toOptional();
    this.driver = maybeDriver.orElseGet(ChromeDriver::new);
  }

  @And("browser history should contain")
  public void browserHistoryShouldContain(final List<String> urls) {
    final var history = this.featureState.get(State.HISTORY, ArrayList<String>::new);
    Assertions.assertThat(history).containsAll(urls);
  }

  @And("^(page source|title) should(?:(| not)) contain (.+)$")
  public void pageTitleShouldContain(
      final String location, final String not, final String expected) {
    final Function<? super WebDriver, String> extractingFunction =
        webDriver -> {
          if (location.equals("title")) return webDriver.getTitle();
          return webDriver.getPageSource();
        };
    Assertions.assertThat(this.driver)
        .extracting(extractingFunction)
        .matches(actual -> not.isBlank() == actual.contains(expected));
  }

  enum State {
    HISTORY
  }
}
