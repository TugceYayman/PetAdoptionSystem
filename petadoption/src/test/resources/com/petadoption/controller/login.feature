Feature: Authenticate and retrieve Bearer token

Background:
  * url 'http://localhost:8081'
  * configure headers = { 'Content-Type': 'application/json' }

Scenario: Get authentication token
  Given path '/auth/login'
  * def credentials = { email: 'admin@petadoption.com', password: 'admin123' }
  * print 'Sending request:', credentials
  And request credentials
  When method POST
  * print 'Received response:', response
  Then status 200
  And match response.token != null
  * def authToken = response.token
  * print 'Generated Token:', authToken