Ability: Automate System testing with Cucumber

  Scenario: Check PATH
    Given I have javac executable in $PATH path
    And I have gradle executable in $PATH path
    And I have . directory in $PWD path
