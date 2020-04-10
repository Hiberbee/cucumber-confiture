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
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;

import java.net.URI;
import java.net.URL;

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

  @ParameterType("(.*)")
  public URL url(final String value) {
    return Try.call(() -> URI.create(value))
        .andThenTry(URI::toURL)
        .getOrThrow(CucumberException::new);
  }

  @FeatureState("T(com.hiberbee.cucumber.definitions.StepDefinitions$State).BASE_URL")
  @Given("base url is {url}")
  public URL baseUrlIs(final URL url) {
    return url;
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

  enum State {
    BASE_URL
  }
}
