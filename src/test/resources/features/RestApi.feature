@rest
Ability: Automate REST API testing

  Scenario: Test HTTP GET request
    Given https://google.com base url
    When I make GET request
    Then response status code is 200
    And Content-Type header is text/html; charset=ISO-8859-1
