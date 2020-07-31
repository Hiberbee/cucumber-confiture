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

import com.hiberbee.cucumber.configurations.CucumberConfiguration;
import com.hiberbee.cucumber.definitions.StepDefinitions.State;
import com.hiberbee.cucumber.gherkin.dsl.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;

import javax.annotation.Resource;
import java.net.*;

@SpringBootTest(classes = {CucumberConfiguration.class, StepDefinitions.class})
class StepDefinitionsTests {

  @Resource private StepDefinitions stepDefinitions;

  @Value("#{cacheManager.getCache('feature')}")
  private Cache featureState;

  @Test
  void testCacheInitialized() {
    Assertions.assertThat(this.featureState).isNotNull();
  }

  @Test
  void testUrlParameter() throws Exception {
    final var actual = "https://hiberbee.dev";
    final var expected = this.stepDefinitions.url(actual);
    Assertions.assertThat(actual).isEqualTo(expected.toExternalForm());
  }

  @Test
  void testBaseUrlIs() throws MalformedURLException {
    final var actual = new URL("https://hiberbee.dev");
    Assertions.assertThat(this.featureState.get(State.BASE_URL, URL.class)).isNull();

    final var expected = this.stepDefinitions.baseUrlIs(Maybe.IS, new URL("https://hiberbee.dev"));
    Assertions.assertThat(actual)
        .isEqualTo(expected)
        .isEqualTo(this.featureState.get(State.BASE_URL, URL.class));
    this.stepDefinitions.clearFeatureHookCache();
    Assertions.assertThat(this.featureState.get(State.BASE_URL, URL.class)).isNull();
  }
}
