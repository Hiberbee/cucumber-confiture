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

import com.hiberbee.cucumber.support.CucumberRun;
import org.junit.platform.commons.function.Try;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.nio.file.*;
import java.util.function.BiConsumer;
import java.util.stream.*;

@Configuration
public class SeleniumHubConfiguration {

  @Bean
  public Capabilities capabilities() {
    final var args =
        Stream.of(
                "ignore-certificate-errors",
                "disable-ipv6",
                "no-sandbox",
                "disable-extensions",
                "disable-gpu",
                "disable-dev-shm-usage",
                "proxy-bypass-list=localhost",
                "window-size=1400x900")
            .map(a -> String.format("--%s", a))
            .collect(Collectors.toList());

    return new ChromeOptions()
        .addArguments(args)
        .setExperimentalOption("useAutomationExtension", false);
  }

  @Bean
  public BiConsumer<Path, WebDriver> screenshotGenerator() {
    return (path, driver) -> {
      final var screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      Try.success(path)
          .andThenTry(Files::createDirectories)
          .andThenTry(it -> Files.createFile(it.resolve(path)))
          .andThenTry(it -> Files.write(it, screenshot))
          .ifFailure(CucumberRun::fail);
    };
  }

  @Bean(destroyMethod = "quit")
  public WebDriver webDriver(final Environment environment, final Capabilities capabilities) {
    return new ChromeDriver();
  }
}
