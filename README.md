# GoRest API Test Suite

REST Assured + Java + TestNG API automation suite for the GoRest API (https://gorest.co.in/public/v2/posts).

## Stack

- Java 11, Maven, TestNG 7.10.2
- REST Assured 5.5.0, Allure 2.25.0

## Tests — 3 chained tests in PostApiTest

| # | Test | What it checks |
|---|------|----------------|
| 1 | getPostList (CRITICAL) | GET /posts returns 200, a non-empty list, every id and title present, response under 5 seconds. Saves the first post's id and title. |
| 2 | getPostDetails (CRITICAL, depends on 1) | GET /posts/{id} returns the same id and title. |
| 3 | searchPost (NORMAL, depends on 1) | GET /posts?title= using the first word of the saved title returns matching posts, including the original post. |

Tests 2 and 3 depend on test 1 — if test 1 fails, they skip.

## How to run

mvn test

Switch environment (qa is the default):

mvn test -Denv=uat

## Reports

Allure: mvn allure:serve

## Notes

- Starter suite: GET requests only — no POST, PUT, DELETE, auth, or schema validation yet.
- The qa.properties and uat.properties files currently hold the same values.
