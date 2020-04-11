/*
 * Copyright (c) 2019. Hiberbee https://hiberbee.github.io/cucumber-k8s
 *
 * This file is part of the Hiberbee OSS. Licensed under the MIT License
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.hiberbee.cucumber.definitions;

import io.cucumber.java.en.Given;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import static io.fabric8.kubernetes.assertions.Assertions.assertThat;

public class KubernetesStepDefinitions {

  @Autowired private KubernetesClient kubernetesClient;

  @Given("kubernetes is running on {string}")
  public void kubernetesIsRunningOn(final String host) {
    assertThat(this.kubernetesClient).matches(it -> it.getMasterUrl().getHost().contains(host));
  }

  @Given("namespace is {string}")
  public void namespaceIs(final String expected) {
    Assertions.assertThat(this.kubernetesClient.namespaces().withName(expected).get()).isNotNull();
  }
}
