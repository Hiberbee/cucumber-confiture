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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.*;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.fabric8.kubernetes.client.*;
import org.springframework.context.annotation.*;

import java.net.URI;

@Configuration
public class InfrastructureConfiguration {

  @Bean
  public KubernetesClient kubernetesClient() {
    return new DefaultKubernetesClient();
  }

  @Bean
  public DockerHttpClient dockerHttpClient() {
    return new JerseyDockerHttpClient.Builder()
        .dockerHost(URI.create("unix:///var/run/docker.sock"))
        .build();
  }

  @Bean
  public DockerClientConfig dockerClientConfig() {
    return DefaultDockerClientConfig.createDefaultConfigBuilder().build();
  }

  @Bean
  public DockerClient dockerClient(
      final DockerHttpClient httpClient, final DockerClientConfig config) {
    return DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();
  }
}
