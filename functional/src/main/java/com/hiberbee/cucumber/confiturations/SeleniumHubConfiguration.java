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

package com.hiberbee.cucumber.confiturations;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class SeleniumHubConfiguration {

  @Bean
  public Capabilities capabilities() {
    return new ChromeOptions()
        .setHeadless(true)
        .addArguments(
            Arrays.stream(
                    "ignore-certificate-errors,headless,disable-ipv6,no-sandbox,disable-extensions,disable-gpu,disable-dev-shm-usage,proxy-bypass-list=localhost,window-size=1400x900"
                        .split(","))
                .map(a -> String.format("--%s", a))
                .collect(Collectors.toList()))
        .setExperimentalOption("useAutomationExtension", false);
  }

  @Bean
  public WebDriver webDriver(
      final Environment environment, final @NotNull Capabilities capabilities) {
    return RemoteWebDriver.builder()
        .url("https://selenium.hiberbee.dev/wd/hub")
        .addAlternative(capabilities)
        .build();
  }
}
