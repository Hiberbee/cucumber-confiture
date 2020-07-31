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

package com.hiberbee.cucumber.configurations;

import com.google.common.base.Converter;
import io.cucumber.junit.platform.engine.Cucumber;
import lombok.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;

import javax.annotation.Nonnull;

@Cucumber
@EnableAutoConfiguration
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@EnableCaching
@ComponentScan("com.hiberbee")
public class CucumberConfiguration {

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager(Caches.getNames());
  }

  @Bean
  public Converter<String, String> dslToSnakeCaseConverter() {
    return new Converter<>() {
      @Override
      protected String doForward(@Nonnull String s) {
        return s.toUpperCase().replace(' ', '_');
      }

      @Override
      protected String doBackward(@Nonnull String s) {
        return s.toLowerCase().replace('_', ' ');
      }
    };
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Caches {

    public static final String FEATURE = "feature";
    public static final String SUITE = "suite";
    public static final String HOOK = "hook";
    public static final String SCENARIO = "scenario";

    static String @Nonnull [] getNames() {
      return new String[] {Caches.SUITE, Caches.HOOK, Caches.FEATURE, Caches.SCENARIO};
    }
  }
}
