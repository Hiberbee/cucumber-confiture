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
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class KubernetesStepDefinitions {

  @Autowired private KubernetesClient kubernetesClient;

  @Value("#{cacheManager.getCache('scenario')}")
  private Cache scenarioState;

  @ParameterType("(pods|services|ingresses|deployments|daemon sets|secrets|config maps)")
  public Supplier<MixedOperation<?, ? extends KubernetesResourceList<?>, ?, ?>> kubernetesResource(
    final String value) {
    switch (value) {
      case "pods":
      default:
        return this.kubernetesClient::pods;
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
    }
  }

  @Given("kubernetes master url {maybe} {string}")
  public void kubernetesIsRunningOn(final Maybe maybe, final String host) {
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

  @When("I get {kubernetesResource}")
  @ScenarioState
  public List<? extends HasMetadata> kubernetesResourceList(
    final Supplier<MixedOperation<?, ? extends KubernetesResourceList<?>, ?, ?>>
          kubernetesResourceSupplier) {
    final var namespace = this.scenarioState.get("namespace", Namespace.class);
    final var operation = kubernetesResourceSupplier.get();
    return (namespace == null)
        ? operation.inAnyNamespace().list().getItems()
        : operation.inNamespace(namespace.getMetadata().getNamespace()).list().getItems();
  }

  @Then("list size {maybe} greater then {int}")
  public void listSize(final Maybe maybe, final Integer size) {
    Assertions.assertThat(this.scenarioState.get("kubernetesResourceList", List.class))
        .hasSizeGreaterThanOrEqualTo(size);
  }
}
