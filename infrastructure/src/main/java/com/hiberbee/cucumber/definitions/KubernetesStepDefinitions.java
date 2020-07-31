/*
 * Copyright (c) 2019. Hiberbee https://hiberbee.github.io/cucumber-k8s
 *
 * This file is part of the Hiberbee OSS. Licensed under the MIT License
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.hiberbee.cucumber.definitions;

import com.hiberbee.cucumber.annotations.ScenarioState;
import com.hiberbee.cucumber.gherkin.dsl.Maybe;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.*;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class KubernetesStepDefinitions {

  private final KubernetesClient kubernetesClient;

  private final Cache cache;

  public KubernetesStepDefinitions(
      final KubernetesClient kubernetesClient,
      @Value("#{cacheManager.getCache('scenario')}") final Cache cache) {
    this.kubernetesClient = kubernetesClient;
    this.cache = cache;
  }

  @ParameterType(
      "(pods|services|ingresses|deployments|replica sets|daemon sets|stateful sets|secrets|config maps)")
  public Supplier<MixedOperation<?, ? extends KubernetesResourceList<?>, ?, ?>> kubernetesResource(
      @Nonnull final String value) {
    switch (value) {
      case "services":
        return this.kubernetesClient::services;
      case "ingresses":
        return this.kubernetesClient.network()::ingress;
      case "config maps":
        return this.kubernetesClient::configMaps;
      case "secrets":
        return this.kubernetesClient::secrets;
      case "deployments":
        return this.kubernetesClient.apps()::deployments;
      case "daemon sets":
        return this.kubernetesClient.apps()::daemonSets;
      case "replica sets":
        return this.kubernetesClient.apps()::replicaSets;
      case "pods":
      default:
        return this.kubernetesClient::pods;
    }
  }

  @Given("{maybe} resource with {string} {maybe} equal to {string}")
  public void resourceWithPathMaybeExist(
      @Nonnull final Maybe maybeHas,
      @Nonnull final String path,
      @Nonnull final Maybe maybeEqualTo,
      @Nonnull final String value) {
    final var resources = this.cache.get("resources", ArrayList<HasMetadata>::new);
    Assertions.assertThat(resources).extracting(path).anyMatch(it -> it.toString().contains(value));
  }

  @Given("kubernetes master url {maybe} {string}")
  public void kubernetesIsRunningOn(@Nonnull final Maybe maybe, final String host) {
    Assertions.assertThat(this.kubernetesClient.getMasterUrl().toString().contains(host))
        .isEqualTo(maybe.yes());
  }

  @Given("namespace is {string}")
  @ScenarioState
  public Namespace namespace(final String expected) {
    final var namespace = this.kubernetesClient.namespaces().withName(expected).get();
    Assertions.assertThat(namespace).isNotNull();
    return namespace;
  }

  @Given("context is {string}")
  public void context(final String expected) {
    final var context = new NamedContext();
    context.setName(expected);
    this.kubernetesClient.getConfiguration().setCurrentContext(context);
  }

  @When("I get {kubernetesResource}")
  @ScenarioState
  public <T extends HasMetadata> List<T> resources(
      @Nonnull
          final Supplier<MixedOperation<T, KubernetesResourceList<T>, ?, ?>>
              kubernetesResourceSupplier) {
    final var namespace = this.cache.get("namespace", Namespace.class);
    final var operation = kubernetesResourceSupplier.get();
    return (namespace == null)
        ? operation.inAnyNamespace().list().getItems()
        : operation.inNamespace(namespace.getMetadata().getNamespace()).list().getItems();
  }

  @Then("list size {maybe} greater then {int}")
  public void listSize(final Maybe maybe, final Integer size) {
    Assertions.assertThat(this.cache.get("resources", List.class))
        .hasSizeGreaterThanOrEqualTo(size);
  }
}
