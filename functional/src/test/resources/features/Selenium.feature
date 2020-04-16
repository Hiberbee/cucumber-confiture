@ui
Feature: Automate Web UI testing with Cucumber

  Background: Browser is installed and configured
    Given web browser is Chrome

  Scenario: Simple url navigation
    When user is opening https://google.com url
    And title should contain Google
    When user is opening https://microsoft.com url
    Then title should contain Microsoft
    But page source should not contain Google
    And browser history should contain
      | https://google.com    |
      | https://microsoft.com |
