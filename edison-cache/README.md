# Edison Cache

Optional add-on for Edison Microservices, adding support for Caffeine caching statistics. 

* The current release of Spring Boot has no possibility to configure two or more Caffeine caches differently. Using
`edison-cache`, you can not only do this, but also configure loading caches that can be used together with
methods annotated with `@Cacheable`.
* For every cache you can retrieve cache statistics and configuration info in HTML or JSON formats.

## Usage

**Include edison-cache:**
 
```gradle
    dependencies {
        compile "de.otto.edison:edison-cache:1.0.0"
        ...
    }
```

This will provide you with a transitive dependency to com.github.ben-manes.caffeine:caffeine:<version>.
 
**Configure your Caffeine caches:**

```java
    @Configuration
    public class CacheConfiguration {
    
        @Bean
        public CaffeineCacheConfig customerCache() {
            return new CaffeineCacheConfig(
                    "CustomerCache",
                    "initialCapacity=100,maximumSize=500,expireAfterAccess=10m,expireAfterWrite=10m,recordStats"
            );
        }
    
        @Bean
        public CaffeineCacheConfig yetAnotherCache() {
            return new CaffeineCacheConfig(
                    "YetAnotherCache",
                    "initialCapacity=10,maximumSize=50,expireAfterAccess=5s,expireAfterWrite=5s,recordStats"
            );
        }
    }
```

If you need more control about the configuration of you Caffeine caches, you can also expose a `CaffeineCache`as
a Spring Bean:

```java
    @Configuration
    public class CacheConfiguration {

        @Bean
        CaffeineCache loadingCustomerCache(final CustomerRepository repository) {
            return new CaffeineCache(
                    "CustomerCache",
                    Caffeine.from("expireAfterWrite=10m,maximumSize=1,recordStats")
                            .build(key -> repository.findBy(key))
            );
        }

    }
```

**Add `@Cacheable` annotations to methods of your Spring beans:**

```java
    @Cacheable(value="CustomerCache", key = "#customer.name")
    public String getMessage(final Customer customer) {
    ...
    }
```

**Cache Configuration and Statistics:**

After configuring at least one CaffeineCacheConfig, you can access information about your caches via 
HTTP GET /internal/cacheinfos in application/json and text/html format.

The cache information is automatically added to the right navbar (see fragment right.html in edison-core).

## Examples

Example-cache contains an example for edison-cache usage.

