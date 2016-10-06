# edison-microservice:edison-cache

Support for Caffine caches in Edison microservices.

## Status

BETA - work in progress.

## About

Many of otto.de's microservices make use of the [Caffeine](https://github.com/ben-manes/caffeine) cache. 
Edison-cache is providing some code to integrate the cache statistics of Caffeine caches into the Edison internal pages.

## Usage

*Include edison-cache*:
 
```gradle
    dependencies {
        compile "de.otto.edison:edison-service:0.77.0",
        compile "de.otto.edison:edison-cache:0.77.0"
        ...
    }
```
 
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

You can access information about your caches via HTTP GET /internal/cacheinfos.

## Examples

Have a look at example-metrics.

