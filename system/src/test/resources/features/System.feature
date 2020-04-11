Ability: Automate System testing with Cucumber

  Scenario: Check PATH
    Given javac executable is in $PATH path
    And gradle executable is in $PATH path
    And . directory is in $PWD path
