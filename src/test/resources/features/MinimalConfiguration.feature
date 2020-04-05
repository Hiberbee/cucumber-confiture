@Chrome @Edge
Feature: Multi-module Gradle project

  Background:
    Given Chrome web browser

  Scenario: Included build depenencies
    Given I can open https://google.com web page in Chrome
    And page title should contain Google
    When I can open https://microsoft.com web page in Chrome
    Then page title should contain Microsoft
    But page title should not contain Google
