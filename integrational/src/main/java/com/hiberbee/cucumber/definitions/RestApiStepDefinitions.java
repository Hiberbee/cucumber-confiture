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

import com.hiberbee.cucumber.annotations.ScenarioState;
import com.hiberbee.cucumber.support.CucumberRun;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import static com.hiberbee.cucumber.definitions.StepDefinitions.State.BASE_URL;

public class RestApiStepDefinitions {

  @Value("#{cacheManager.getCache('feature')}")
  private Cache featureState;

  @Value("#{cacheManager.getCache('scenario')}")
  private Cache scenarioState;

  @Autowired private RestTemplate restTemplate;

  @ScenarioState("T(com.hiberbee.cucumber.definitions.RestApiStepDefinitions$State).REQUEST")
  @And("^HTTP request method is (GET|POST|PUT|PATCH|TRACE|DELETE|OPTIONS|HEAD)$")
  public ClientHttpRequest httpRequestMethodIs(final String httpMethod) {
    return Try.success(this.featureState.get(BASE_URL, URL.class))
        .andThenTry(URL::toURI)
        .andThenTry(
            it ->
                this.restTemplate
                    .getRequestFactory()
                    .createRequest(it, Objects.requireNonNull(HttpMethod.resolve(httpMethod))))
        .toOptional()
        .orElseThrow();
  }

  @ScenarioState("T(com.hiberbee.cucumber.definitions.RestApiStepDefinitions$State).RESPONSE")
  @When("request is executed")
  public ClientHttpResponse requestIsExecuted() {
    return Try.success(this.scenarioState.get(State.REQUEST, ClientHttpRequest.class))
        .andThenTry(ClientHttpRequest::execute)
        .toOptional()
        .orElseThrow();
  }

  @And("HTTP response status code is {int}")
  public void responseStatusCodeIs(final @NotNull Integer httpStatus) {
    Try.success(this.scenarioState.get(State.RESPONSE, ClientHttpResponse.class))
        .andThenTry(ClientHttpResponse::getRawStatusCode)
        .andThenTry(Assertions.assertThat(httpStatus)::isEqualTo)
        .ifFailure(CucumberRun::fail);
  }

  @And(
      "^HTTP request (Accept|AccessControlRequestMethod|Authorization|CacheControl|Connection|ContentEncoding|ContentLength|Cookie|ContentType|Date|Expect|Forwarded|From|Host|Origin|Pragma|ProxyAuthorization|Range|Referer|TransferEncoding|UserAgent) header is (.+)$")
  public void requestHeaderIs(final String headerName, final String expectedValue) {
    Try.success(this.scenarioState.get(State.REQUEST, ClientHttpRequest.class))
        .andThenTry(ClientHttpRequest::getHeaders);
  }

  @And("HTTP response body contains {string}")
  public void responseBodyContains(final String expected) {
    Try.success(this.scenarioState.get(State.RESPONSE, ClientHttpResponse.class))
        .andThenTry(ClientHttpResponse::getBody)
        .andThenTry(InputStream::readAllBytes)
        .andThenTry(String::new)
        .ifSuccess(it -> Assertions.assertThat(it).contains(expected))
        .ifFailure(CucumberRun::fail);
  }

  @And(
      "^HTTP response (CacheControl|ContentType|ETag|Expires|LastModified|Link|Location|Pragma|SetCookie|Vary) header is (.+)$")
  public void responseHeaderIs(final String headerName, final String expectedValue) {
    Try.success(this.scenarioState.get(State.RESPONSE, ClientHttpResponse.class))
        .andThenTry(ClientHttpResponse::getHeaders)
        .andThenTry(
            headers -> Assertions.assertThat(headers.get(headerName)).contains(expectedValue))
        .ifFailure(CucumberRun::fail);
  }

  enum State {
    REQUEST,
    RESPONSE
  }
}
