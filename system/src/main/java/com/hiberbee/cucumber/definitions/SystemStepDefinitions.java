package com.hiberbee.cucumber.definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.junit.platform.commons.function.Try;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SystemStepDefinitions {

  @When("{string} added and installed")
  public void dependencyAddedAndInstalled(final String dependency) {
    Try.call(() -> Files.readAllBytes(Paths.get(System.getenv("HOME"), ".Brewfile.lock.json")))
        .ifSuccess(it -> Assertions.assertThat(new String(it)).contains(dependency))
        .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @And("dependency {string}")
  public void dependency(final String dependency) {
    Try.call(() -> Files.readAllBytes(Paths.get(System.getenv("HOME"), ".Brewfile")))
        .ifSuccess(it -> Assertions.assertThat(new String(it)).contains(dependency))
        .ifFailure(it -> Assertions.fail(it.getMessage()));
  }
}
