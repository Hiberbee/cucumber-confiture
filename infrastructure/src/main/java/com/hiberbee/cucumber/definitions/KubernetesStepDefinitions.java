/*
 * Copyright (c) 2019. Hiberbee https://hiberbee.github.io/cucumber-k8s
 *
 * This file is part of the Hiberbee OSS. Licensed under the MIT License
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.hiberbee.cucumber.definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.assertj.core.api.Assertions;

public class KubernetesStepDefinitions {

  private KubernetesClient kubernetesClient;

  @Before
  public void initKubernetesClient() {
    this.kubernetesClient = new DefaultKubernetesClient();
  }

  @Given("kubernetes is running on {string}")
  public void kubernetesIsRunningOn(final String host) {
    Assertions.assertThat(host).isEqualTo(this.kubernetesClient.getMasterUrl().getHost());
  }

  @And("kubernetes version is greater than {float}")
  public void kubernetesVersionIsGreaterThan(final Float version) {
    Assertions.assertThat(this.kubernetesClient.getApiVersion()).isEqualTo(version.toString());
  }
}
