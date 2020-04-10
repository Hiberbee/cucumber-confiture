Ability: Automate REST API testing

  Scenario: Test HTTP GET request
    Given base url is https://google.com
    And HTTP request method is GET
    When request is executed
    Then HTTP response status code is 200
    And Content-Type header is text/html; charset=ISO-8859-1
