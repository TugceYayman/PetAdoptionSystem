Feature: Test User Registration Endpoint

  Background:
    * url 'http://localhost:8081'

Scenario: Register with an existing email
* def email = 'user' + java.util.UUID.randomUUID() + '@example.com'

# First registration
Given path '/auth/register'
And request
"""
{
  "name": "First User",
  "email": "#(email)",
  "password": "password123"
}
"""
When method POST
Then status 201
And match response.message == "User registered successfully"

# Second registration with the same email
Given path '/auth/register'
And request
"""
{
  "name": "Duplicate User",
  "email": "#(email)",
  "password": "password123"
}
"""
When method POST
Then status 400
And match response.message == "Email already exists"
