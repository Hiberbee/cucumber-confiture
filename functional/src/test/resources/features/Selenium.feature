@ui
Feature: Automate Web UI testing with Cucumber

  Scenario: Index page
    When user is opening https://bndigital.co url
    Then page source should contain 'The full-service partner for digital solutions'
    And page title should contain 'BN Digital'
    And link with text 'Contact' should present on the page
    And link with text 'Behance' should present on the page
    And link with text 'Linkedin' should present on the page
    And link with text 'Facebook' should present on the page
    And browser history should contain
      | https://bndigital.co |
