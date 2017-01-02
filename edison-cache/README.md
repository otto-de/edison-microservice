# edison-microservice:edison-cache

Optional add-on for Edison Microservices, adding support for Caffeine caching statistics. 

Many of otto.de's microservices make use of the [Caffeine](https://github.com/ben-manes/caffeine) cache. 

## Usage

*Include edison-cache*:
 
```gradle
    dependencies {
        compile "de.otto.edison:edison-cache:1.0.0"
        ...
    }
```

This will provided you with a transitive dependency to com.github.ben-manes.caffeine:caffeine:<version>.
 
*Configure your Caffeine caches*:

```java
    @Configuration
    public class CacheConfiguration {
    
        @Bean
        public CaffeineCacheConfig customerCacheConfig() {
            return new CaffeineCacheConfig(
                    "CustomerCache",
                    "initialCapacity=100,maximumSize=500,expireAfterAccess=10m,expireAfterWrite=10m,recordStats"
            );
        }
    
        @Bean
        public CaffeineCacheConfig yetAnotherCacheConfig() {
            return new CaffeineCacheConfig(
                    "YetAnotherCache",
                    "initialCapacity=10,maximumSize=50,expireAfterAccess=5s,expireAfterWrite=5s,recordStats"
            );
        }
    }
```

*Add `@Cacheable` annotations to methods of your Spring beans*:

```java
    @Cacheable(value="CustomerCache", key = "#customer.name")
    public String getMessage(final Customer customer) {
    ...
    }
```

After configuring at least one CaffeineCacheConfig, you can access information about your caches via 
HTTP GET /internal/cacheinfos in application/json and text/html format.

The cache information is automatically added to the right navbar (see fragment right.html in edison-core).

## Examples

Example-metrics contains an example for edison-cache usage.

