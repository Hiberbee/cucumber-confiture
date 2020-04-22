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

import com.hiberbee.cucumber.annotations.FeatureState;
import com.hiberbee.cucumber.configurations.CucumberConfiguration;
import com.hiberbee.cucumber.support.CucumberRun;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import lombok.extern.java.Log;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

@Log
@SpringBootTest(classes = CucumberConfiguration.class)
public class StepDefinitions {

  @Value("#{cacheManager.getCache('feature')}")
  private Cache featureState;

  @Value("#{cacheManager.getCache('hook')}")
  private Cache hookState;

  @Value("#{cacheManager.getCache('scenario')}")
  private Cache scenarioState;

  @Value("${cucumber.slack.icon}")
  private String icon;

  @Value("${cucumber.slack.enabled}")
  private Boolean slackEnabled;

  @Autowired private MethodsClient slackClient;

  @ParameterType("(.+)")
  private String env(final @NotNull String value) {
    return value.startsWith("$") ? System.getenv(value.substring(1)) : value;
  }

  @ParameterType(value = "(is|is not)")
  public Boolean condition(final String value) {
    return "is".equals(value);
  }

  @ParameterType("(.+)")
  public URL url(final String value) {
    return Try.success(value)
        .andThenTry(URL::new)
        .orElseTry(URI.create(value)::toURL)
        .getOrThrow(CucumberException::new);
  }

  @Given("version of {string} is {string}")
  public void iHaveBinaryInstalled(final String binary, final String version) {
    Try.call(this.getOutputFromCommandExecution(binary, "version"))
        .orElseTry(this.getOutputFromCommandExecution(binary, "-v"))
        .andThenTry(s -> new StringAssert(s).contains(version.replace(".*", "")))
        .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @Contract(pure = true)
  private @NotNull Callable<String> getOutputFromCommandExecution(final String... command) {
    return () ->
        new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A").next();
  }

  @Given("^(.+) (file|directory|executable) is in (.+) path$")
  public void subPathExists(final String subpath, final @NotNull String type, final String path) {
    final Predicate<Path> condition;
    switch (type) {
      case "directory":
        condition = Files::isRegularFile;
        break;
      case "executable":
        condition = Files::isExecutable;
        break;
      case "file":
      default:
        condition = Files::isDirectory;
        break;
    }

    Assertions.assertThat(Arrays.stream(this.env(path).split(":")))
        .extracting(Paths::get)
        .extracting(it -> it.resolve(subpath))
        .anyMatch(condition);
  }

  @FeatureState("T(com.hiberbee.cucumber.definitions.StepDefinitions$State).BASE_URL")
  @Given("base url {condition} {url}")
  public URL baseUrlIs(final Boolean is, final URL url) {
    return Try.success(this.featureState.get(State.BASE_URL, URL.class))
        .andThenTry(Objects::requireNonNull)
        .orElseTry(() -> url)
        .getOrThrow(CucumberException::new);
  }

  @After()
  public void sendSlackNotifications(final @NotNull Scenario scenario) {
    if (scenario.isFailed() && this.slackEnabled) {
      final var message =
          ChatPostMessageRequest.builder()
              .iconUrl(this.icon)
              .channel("cucumber")
              .text(scenario.getName())
              .build();
      Try.call(() -> this.slackClient.chatPostMessage(message)).ifFailure(CucumberRun::fail);
    }
  }

  @Before
  public void clearFeatureHookCache() {
    this.featureState.clear();
    this.hookState.clear();
  }

  @After
  public void clearScenarioCache() {
    this.scenarioState.clear();
  }

  public enum State {
    BASE_URL
  }
}
