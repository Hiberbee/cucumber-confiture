@ui
Feature: Multi-module Gradle project

  Background:
    Given Chrome web browser
    And window state is minimized

  Scenario: Included build depenencies
    Given https://google.com base url
    When I go to '/' url
    And title should contain Google
    When I go to 'https://microsoft.com' url
    Then title should contain Microsoft
    But page source should not contain Google
