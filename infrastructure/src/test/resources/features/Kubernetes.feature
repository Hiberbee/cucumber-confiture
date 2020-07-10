Feature: Automate Kubernets cluster testing with Cucumber

  Background: Dependencies are installed
    Given 'kubectl' command is executable
    And 'minikube' command is executable

  Scenario: Kubernetes connection
    Given kubernetes master url is 'https://b30cad24-92e5-48b3-9ec8-b640b9abb7dc.k8s.ondigitalocean.com'
    And namespace is 'kube-system'
    When I get secrets
    Then list size is greater then 15
    When I get pods
    Then list size is greater then 10
    When I get services
    Then list size is greater then 1
    When I get ingresses
    Then list size is greater then 0
    When I get deployments
    Then list size is greater then 0
    When I get daemon sets
    Then list size is greater then 0
