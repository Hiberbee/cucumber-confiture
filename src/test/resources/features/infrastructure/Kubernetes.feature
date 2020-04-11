Feature: Automate Kubernets cluster testing with Cucumber

  Scenario: Kubernetes connection
    Given I have kubectl executable in $PATH path
    And version of 'kubectl' is '1.18'
    And namespace is 'default'
