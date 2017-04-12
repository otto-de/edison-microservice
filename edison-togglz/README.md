# Edison Togglz

Feature toggles for Edison Microservices.

## Usage

There is an example showing how to use the `edison-togglz` library: see `examples/example-togglz`.  

Basically, you have to:
1. Add `de.otto.edison:edison-togglz:<version>` to you project.
2. In clustered environments, also add `de.otto.edison:edison-mongo:<version>` and configure MongoDB's
 properties as described in [Edison Mongo README](https://github.com/otto-de/edison-microservice/tree/master/edison-mongo).
3. Implement your Features enum:
 ```java
 public enum Features implements Feature {
 
     @Label("Toggles the 'Hello Edison' message displayed on http://localhost/8080/example page")
     HELLO_TOGGLE;
 
     public boolean isActive() {
         return FeatureContext.getFeatureManager().isActive(this);
     }
 }
 ```
 4. Override the default FeatureClassProvider as a Spring Bean:
 ```java
@Component
public class FeatureClassProvider implements de.otto.edison.togglz.FeatureClassProvider {
    @Override
    public Class<? extends Feature> getFeatureClass() {
        return Features.class;
    }
}
```


### Persisting Feature State

In clustered environments it is necessary to persist the feature state, otherwise developers will have
to manually take care, that all service instances have the same state of the toggles.

By simply adding (and configuring) `edison-mongo` to your project, you will automatically get a `MongoTogglzRepository`,
implementing `org.togglz.core.repository.StateRepository` interface. 

### Caching Feature State

Features are accessed frequently, often many times during a single request. In order to improve performance, it is 
important to cache the feature state, if persistence is used. By default, the feature state is cached for five seconds.
This period can be configured by application property `edison.togglz.cache-ttl` (values in millis).

### Togglz Console

By default, the Togglz web console is configured and added to the 'Admin' navigation bar of the /internal pages. 

You can disable the console by setting `edison.togglz.console.enabled=false` in your application.properties.

### LDAP Authentication for Togglz Console

Authentication can be enabled for the console by configuring an LDAP server:
* `edison.togglz.console.ldap.enabled=true` Enables LDAP authentication. Default value is `false`.

If enabled, the following properties must be provided:
* `edison.togglz.console.ldap.host=<host>` Host name of the LDAP server.
* `edison.togglz.console.ldap.base-dn=<base dn>` The base distinguished name (base DN)
* `edison.togglz.console.ldap.rdn-identifier=<rdn>` The relative DN (RDN)

The port can be changed, too:
* `edison.togglz.console.ldap.port=<port>` Port of the LDAP server. Default value is `389`.

If this is not sufficient, Spring Security might be an alternative.

### Using Features

Nothing special about that: just use it as [documented](https://www.togglz.org):

```java
class Foo {
    public void doSomethingUseful() {
        if (HELLO_TOGGLE.isActive()) {
            sayHello();
        } else {
            saySomethingElse();
        }
    }
}
```
### Features in Thymeleaf

Sometimes it is necessary to use feature toggles in the frontend. Because most Edison Microservices are using
Thymeleaf as a template engine, a support for Thymeleaf templates would be nice. 

Fortunately, there already is a solution: a [Thymeleaf Togglz Dialect](https://github.com/heneke/thymeleaf-extras-togglz).

Example:
```xhtml
<html lang="en"
    xmlns:th="http://www.thymeleaf.org"
    xmlns:togglz="https://github.com/heneke/thymeleaf-extras-togglz">
    <body>
        <div togglz:active="YOUR_FEATURE_NAME">
            content only visible if feature is active
        </div>
        <div togglz:inactive="YOUR_FEATURE_NAME">
            content only visible if feature is <b>inactive</b>
        </div>
    </body>
</html>

```
 
## Alternatives

Since Togglz 2.3.0, there is a [`togglz-spring-boot-starter](https://www.togglz.org/documentation/spring-boot-starter.html) 
that is richer in functionality, but not necessarily easier to configure. You can use this instead of 
`edison-togglz`, but don't forget about persisting feature state if you have more than one instance of 
your service.
 
Please give me some feedback about your experiences, if you do so!


