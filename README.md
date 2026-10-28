# Rest Assured Capstone

This Java, Maven, Rest Assured, and TestNG project runs API tests against an embedded local fake API server. No GoRest, DummyJSON, ReqRes, or other public API is contacted by the test suite.

## Run the tests

```powershell
mvn test
```

The server starts automatically before the TestNG suite, binds to an available loopback port, and shuts down when the suite finishes. Its in-memory data is reset at startup, so test data is deterministic and does not persist between runs. A GoRest token is not needed.

## Local API endpoints

| Method | Endpoint | Behavior |
|---|---|---|
| GET | `/public/v2/users` | List seeded and created users |
| POST | `/public/v2/users` | Create a user |
| GET | `/public/v2/users/{id}` | Retrieve a user |
| PUT/PATCH | `/public/v2/users/{id}` | Update a user |
| DELETE | `/public/v2/users/{id}` | Delete a user |
| GET | `/public/v2/posts` | List seeded and created posts |
| POST | `/public/v2/users/{id}/posts` | Create a post for an existing user |
| DELETE | `/public/v2/posts/{id}` | Delete a post |
| GET | `/public/v2/posts/{id}/comments` | Retrieve seeded comments for a post |
| GET | `/products` | Retrieve seeded products |
| GET | `/products/{id}` | Retrieve a product |
| GET | `/products/search?q={term}` | Search seeded products by title |

The same user and post endpoints also accept the shorter `/users` and `/posts` prefixes. The server is implemented in `com.capstone.server.LocalApiServer`; `ApiBase` starts and stops it for the TestNG suite.

## Reports

After running the suite, open:

- `target\reports\extent-report.html`
- `target\reports\user-operation-summary.html`
- `target\surefire-reports\`
