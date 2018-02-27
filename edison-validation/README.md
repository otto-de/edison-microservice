# edison-validation

The validation module does two things:

1. It brings some new validation annotations, like `@IsEnum`,
`@IsInstant` and `@SafeId`.
2. It configures Spring in a way that it automatically turns a
violated validation into a `422 Unprocessable Entity` and a JSON
body corresponding to [spec.otto.de/profiles/error](http://spec.otto.de/profiles/error/).

## Usage

Annotate your domain objects with Hibernate / Spring validation
annotations, like:

```java
public class ApiObject {
        @Size(min = 3, max = 21)
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
```

Then use this as a parameter in controllers, together
with the `@Validated` annotation:

```java
@RestController
public class TestController {
    @RequestMapping(value = "/testing",
            method = RequestMethod.PUT,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public String doTest(@Validated @RequestBody ApiObject content) {
        return "bla";
    }
}
```

Then, if you put a value like `{"id":"aa"}` to this
endpoint `/testing`, you'll recieve a 422 and a response like:
```json
{
  "errorMessage": "Validation failed. 1 error(s)",
  "errors": {
    "id": [
      {
        "key": "list.invalid_size",
        "message": "Die Liste muss zwischen {min} und {max} Elemente enthalten.",
        "rejected": "aa"
      }
    ]
  }
}
```

You can customize the keys and messages used by providing a `ValidationMessages.properties`
file in the root of the classpath, similar to [the one in this project](/src/main/resources/ValidationMessages.properties).
