Feature: Multi-module Gradle project

  Background:
    Given Chrome web browser
    And window is minimized

  Scenario: Included build depenencies
    Given I can open https://google.com web page
    And title should contain Google
    When I can open https://microsoft.com web page
    Then title should contain Microsoft
    But page source should not contain Google
