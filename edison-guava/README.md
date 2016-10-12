# edison-microservice:edison-guava

Support for Guava caches in Edison microservices.

## Status

BETA - work in progress.

DEPRECATED - will be removed soon. Use edison-cache instead.

## About

Many of otto.de's microservices make use of the [Google Guava](https://github.com/google/guava) libraries. 
Edison-guava is providing some code to integrate the cache statistics of Guava caches into the Edison internal pages.

## Usage

*Include edison-guava*:
 
```gradle
    dependencies {
        compile "de.otto.edison:edison-service:0.51.0",
        compile "de.otto.edison:edison-guava:0.51.0"
        ...
    }
```
 
*Configure your Guava caches*:

```java
    @Configuration
    public class CacheConfiguration {
    
        @Bean
        public GuavaCacheConfig customerCacheConfig() {
            return new GuavaCacheConfig(
                    "CustomerCache",
                    "initialCapacity=100,maximumSize=500,expireAfterAccess=10m,expireAfterWrite=10m,recordStats"
            );
        }
    
        @Bean
        public GuavaCacheConfig yetAnotherCacheConfig() {
            return new GuavaCacheConfig(
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

You can access information about your caches via HTTP GET /internal/cacheinfos.

## Examples

Have a look at example-metrics.

