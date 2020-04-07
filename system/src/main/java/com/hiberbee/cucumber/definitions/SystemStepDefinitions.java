package com.hiberbee.cucumber.definitions;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.function.Try;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class SystemStepDefinitions {

  @Contract(pure = true)
  private @NotNull Callable<String> getOutputFromCommandExecution(final String... command) {
    return () ->
        new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A").next();
  }

  @ParameterType("(.+)")
  private String env(final @NotNull String value) {
    return value.startsWith("$") ? System.getenv(value.substring(1)) : value;
  }

  @Given("version of {string} is {string}")
  public void iHaveBinaryInstalled(final String binary, final String version) {
    Try.call(this.getOutputFromCommandExecution(binary, "version"))
        .orElseTry(this.getOutputFromCommandExecution(binary, "-v"))
        .andThenTry(s -> new StringAssert(s).contains(version.replace(".*", "")))
        .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @Given("^I have (.+) (file|directory|executable) in (.+) path$")
  public void subPathExists(final String subpath, final String type, final String path) {
    Assertions.assertThat(Arrays.stream(this.env(path).split(":")))
        .extracting(Paths::get)
        .extracting(it -> it.resolve(subpath))
        .anyMatch(
            it ->
                type.equals("executable")
                    ? Files.isExecutable(it)
                    : (type.equals("directory") ? Files.isDirectory(it) : Files.isRegularFile(it)));
  }

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
