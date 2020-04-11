Ability: Automate RESTful APIs testing with Cucumber

  Scenario: Test HTTP GET request
    Given base url is http://google.com
    And HTTP request method is GET
    And HTTP request ContentType header is text/html; charset=ISO-8859-1
    When request is executed
    Then HTTP response status code is 200
    And HTTP request ContentType header is text/html; charset=ISO-8859-1
    And HTTP response Expires header is -1
    And HTTP response body contains "http://schema.org/WebPage"
