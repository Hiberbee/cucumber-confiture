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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.*;
import com.hiberbee.cucumber.annotations.FeatureState;
import com.hiberbee.cucumber.configurations.CucumberConfiguration;
import com.hiberbee.cucumber.gherkin.dsl.*;
import com.hiberbee.cucumber.support.CucumberRun;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.java.*;
import io.cucumber.java.en.Given;
import io.cucumber.spring.CucumberContextConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.core.io.UrlResource;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.net.*;
import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberConfiguration.class)
public class StepDefinitions {

  @Value("#{cacheManager.getCache('feature')}")
  private Cache featureState;

  @Value("#{cacheManager.getCache('hook')}")
  private Cache hookState;

  @Value("#{cacheManager.getCache('scenario')}")
  private Cache scenarioState;

  @Autowired private Environment environment;

  @Autowired private Converter<String, String> dslToSnakeCaseConverter;
  @Autowired private ObjectMapper objectMapper;

  @ParameterType("\\$(.+)")
  public String env(final @Nonnull String value) {
    return Optional.of(value).map(this.environment::getProperty).orElse(value);
  }

  @DefaultDataTableEntryTransformer
  @DefaultDataTableCellTransformer
  @DefaultParameterTransformer
  public Object defaultTransformer(final Object fromValue, final Type toValueType) {
    return this.objectMapper.convertValue(fromValue, this.objectMapper.constructType(toValueType));
  }

  /**
   * @param value
   *     <p>One of: "is" | "is not" | "are" | "are not" | "has" | "has not" | "have" | "have not" |
   *     "should" | "should not".
   *     <p>Case insensitive
   * @return {@link Maybe}
   */
  @ParameterType("(is|is not|are|are not|has|has not|have|have not|should|should not)")
  public Maybe maybe(final String value) {
    return Enums.stringConverter(Maybe.class)
        .compose(this.dslToSnakeCaseConverter::convert)
        .apply(value);
  }

  @ParameterType("(.+)")
  public URL url(final String value) throws Exception {
    return Try.call(() -> new UrlResource(value))
        .andThenTry(UrlResource::getURL)
        .orElse(() -> Try.call(URI.create(value)::toURL))
        .ifFailure(CucumberRun::fail)
        .get();
  }

  @ParameterType("(file|directory|executable)")
  public Inode inode(final String value) {
    return Enums.stringConverter(Inode.class)
        .compose(this.dslToSnakeCaseConverter::convert)
        .apply(value);
  }

  @Given("version of {string} is {string}")
  public void iHaveBinaryInstalled(final String binary, final String version) {
    Try.call(this.getOutputFromCommandExecution(binary, "version"))
        .orElseTry(this.getOutputFromCommandExecution(binary, "-v"))
        .andThenTry(it -> it.contains(version.replace(".*", "")))
        .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  private @Nonnull Callable<String> getOutputFromCommandExecution(final String... command) {
    return () ->
        new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A").next();
  }

  @Given("{string} command {maybe} executable")
  public void subPathExists(final String command, @Nonnull final Maybe maybe) {
    Try.call(() -> Runtime.getRuntime().exec(command)).ifFailure(CucumberRun::fail);
  }

  @FeatureState("T(com.hiberbee.cucumber.definitions.StepDefinitions$State).BASE_URL")
  @Given("base url {maybe} {url}")
  public URL baseUrlIs(final Maybe maybe, final URL url) {
    return Try.success(this.featureState.get(State.BASE_URL, URL.class))
        .andThenTry(Objects::requireNonNull)
        .orElseTry(() -> url)
        .getOrThrow(CucumberException::new);
  }

  @Before
  public void clearFeatureHookCache() {
    this.featureState.clear();
    this.scenarioState.clear();
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
