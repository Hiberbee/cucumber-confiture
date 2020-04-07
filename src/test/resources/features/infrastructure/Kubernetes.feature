Feature: Kubernetes client

  Scenario: Kubernetes connection
    Given I have kubectl executable in $PATH path
    And version of 'kubectl' is '1.18'
