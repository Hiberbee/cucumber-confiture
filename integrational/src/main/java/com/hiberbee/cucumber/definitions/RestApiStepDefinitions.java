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

import com.hiberbee.cucumber.annotations.EnableCucumberState;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@EnableCucumberState
public class RestApiStepDefinitions {

  @Value("#{cacheManager.getCache('feature')}")
  private Cache feature;

  @Value("#{cacheManager.getCache('scenario')}")
  private Cache scenario;

  @Autowired private RestTemplate restTemplate;

  @ParameterType("(GET|POST|PUT|PATCH|TRACE|DELETE|OPTIONS|HEAD)")
  public HttpMethod httpMethod(final String httpMethod) {
    return HttpMethod.resolve(httpMethod);
  }

  @ParameterType("(.+)")
  public HttpStatus httpStatus(final String httpStatus) {
    return HttpStatus.resolve(Integer.parseInt(httpStatus));
  }

  @When("I make {httpMethod} request")
  public void makeRequest(final HttpMethod httpMethod) {
    final var response =
        Try.call(() -> this.feature.get("baseUrl", URL.class))
            .andThenTry(URL::toURI)
            .andThenTry(
                uri ->
                    this.restTemplate.getRequestFactory().createRequest(uri, httpMethod).execute())
            .toOptional()
            .orElseThrow();
    this.scenario.put("response", response);
  }

  @Then("response status code is {httpStatus}")
  public void responseStatusCodeIs(final @NotNull HttpStatus httpStatus) throws IOException {
    Try.success(this.scenario.get("response", ClientHttpResponse.class))
        .andThenTry(ClientHttpResponse::getRawStatusCode)
        .andThenTry(Assertions.assertThat(httpStatus.value())::isEqualTo)
        .ifFailure(e -> Assertions.fail(e.getMessage()));
  }

  @Then("^(Accept|Content-Type) header is(?:(|not)) (.+)$")
  public void responseHeaderIs(final String key, final String not, final String value) {
    Try.success(this.scenario.get("response", ClientHttpResponse.class))
        .andThenTry(ClientHttpResponse::getHeaders)
        .andThenTry(headers -> Assertions.assertThat(headers).containsEntry(key, List.of(value)));
  }
}
