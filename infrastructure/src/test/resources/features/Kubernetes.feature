Feature: Automate Kubernets cluster testing with Cucumber

  Scenario: Kubernetes connection
    Given kubernetes is running on 'https://localhost:8443'
