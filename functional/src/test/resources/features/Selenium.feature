@ui
Feature: Automate Web UI testing with Cucumber

  Background: Browser is installed and configured
    Given web browser is Chrome

  Scenario: Simple url navigation
    When user is opening https://bndigital.co url
    And page title should contain 'BN Digital'
    And link with text 'Contact' should present on the page
    And link with text 'Behance' should present on the page
    When user is opening https://google.com url
    Then page title should contain 'Google'
    But page source should not contain 'BN Digital'
    And browser history should contain
      | https://bndigital.co |
      | https://google.com   |
