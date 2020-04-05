Feature: Multi-module Gradle project

  Background:
    Given Chrome web browser

  Scenario: Included build depenencies
    Given I can open https://google.com web page in browser
    And page title should contain Google
