package com.hiberbee.cucumber.definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.StringAssert;
import org.junit.platform.commons.function.Try;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class SystemStepDefinitions {

  private static Callable<String> getOutputFromCommandExecution(final String... command) {
    return () ->
      new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A").next();
  }

  private static String resolveEnvVar(final String value) {
    return value.startsWith("$") ? System.getenv(value.substring(1)) : value;
  }

  @And("^dependency (.*)$")
  public static void dependency(final String dependency) {
    Try.call(() -> Files.readAllBytes(Paths.get(System.getenv("HOME"), ".Brewfile")))
      .ifSuccess(it -> Assertions.assertThat(new String(it)).contains(dependency))
      .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @When("^(.+) added and installed$")
  public static void dependencyAddedAndInstalled(final String dependency) {
    Try.call(() -> Files.readAllBytes(Paths.get(System.getenv("HOME"), ".Brewfile.lock.json")))
      .ifSuccess(it -> Assertions.assertThat(new String(it)).contains(dependency))
      .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @Given("^version of (.+) is (.+)$")
  public static void iHaveBinaryInstalled(final String binary, final String version) {
    Try.call(SystemStepDefinitions.getOutputFromCommandExecution(binary, "version"))
      .orElseTry(SystemStepDefinitions.getOutputFromCommandExecution(binary, "-v"))
      .andThenTry(s -> new StringAssert(s).contains(version.replace(".*", "")))
      .ifFailure(it -> Assertions.fail(it.getMessage()));
  }

  @Given("^I have (.+) (file|directory|executable) in (.+) path")
  public static void subPathExists(final String subpath, final String type, final String path) {
    Assertions.assertThat(Arrays.stream(SystemStepDefinitions.resolveEnvVar(path).split(":")))
      .extracting(Paths::get)
      .extracting(it -> it.resolve(subpath))
      .anyMatch(
        it ->
          type.equals("executable")
            ? Files.isExecutable(it)
            : (type.equals("directory") ? Files.isDirectory(it) : Files.isRegularFile(it)));
  }
}
