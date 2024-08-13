# edison-microservice

Collection of independent libraries on top of Spring Boot to provide a faster setup of jvm microservices.

> "I never did anything by accident, nor did any of my inventions come by accident; they came by work." - Thomas Edison

## Status

[![Build](https://github.com/otto-de/edison-microservice/workflows/Build/badge.svg?branch=master)](https://github.com/otto-de/edison-microservice/actions?query=workflow%3ABuild)
[![codecov](https://codecov.io/gh/otto-de/edison-microservice/branch/master/graph/badge.svg)](https://codecov.io/gh/otto-de/edison-microservice)
[![Known Vulnerabilities](https://snyk.io/test/github/otto-de/edison-microservice/badge.svg)](https://snyk.io/test/github/otto-de/edison-microservice)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/otto-de/edison-microservice/badge)](https://securityscorecards.dev/viewer/?uri=github.com/otto-de/edison-microservice)
[![Maven Central](https://img.shields.io/maven-central/v/de.otto.edison/edison-core?label=maven-central)](https://search.maven.org/search?q=g:de.otto%20a:edison-core%20v:RELEASE%20p:jar)
[![license](https://img.shields.io/github/license/otto-de/edison-microservice.svg)](./LICENSE)

Have a look at the [release notes](CHANGELOG.md) for details about updates and changes.

## About

This project contains a number of independent libraries on top of Spring Boot to provide a faster setup of jvm microservices.
The libraries are used in different projects at OTTO.
It's purpose is to provide a common implementation for cross-cutting requirements like:

* Health checks that are used to tell the load balancer or mesos platform whether or not a service is healthy.
* A [status page/document](https://github.com/otto-de/edison-microservice/tree/master/edison-core) that is used to give information about the current state of the service. Status information also include details about sub-components, background jobs like imports, and so on.
* A simple job handling library that is used to run asynchronous background jobs, which for example can be used to run data imports from other systems.
* An optional MongoDB-based implementation of a JobRepository
* Support for MongoDB-based repositories in case you do not like Spring Data
* Support for feature toggles based on [Togglz](https://www.togglz.org/)

... plus all the features of [Spring Boot](http://projects.spring.io/spring-boot/).


## Releases

[Semantic Versioning v2.0.0](http://semver.org/spec/v2.0.0.html) is used to specify the version numbers.

This project maintains its roadmap with [issues](https://github.com/otto-de/edison-microservice/issues) and [milestones](https://github.com/otto-de/edison-microservice/milestones).

**2.7.x**: Edison Microservices for Spring Boot 2.7.x &#10004; - Compatible with Java 11 and greater

**3.2.x**: Edison Microservices for Spring Boot 3.2.x &#10004; - Compatible with Java 17 and greater

## Migration from Edison 2 to Edison 3

In edison-ldap, whitelisted-paths was replaced with allowlisted-paths.
Everything else should be ok if you follow the Spring Boot 2 -> 3 migration guide:
https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide

## Migration from Edison 1.x to Edison 2

Edison 2 has several breaking changes that will make a refactoring of your current application necessary. For a list of
the actual changes, please take a look at the [Changelog](CHANGELOG.md).

When migrating, take care of the following adjustments:

* Follow the [Spring Boot 2.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide)
  to fix the most common problems.
    * If you want to use the behaviour of Edison 1.x, which hosts all management endpoints below `/internal`, you have to 
      configure `management.endpoints.web.base-path=/internal` in your `application.yml`
* Remove dependencies to the [edison-aws Project](https://github.com/otto-de/edison-aws), which will be deprecated some time in the future. 
  Necessary functionality was transferred to a submodule of `edison-microservice` (named `edison-aws`).
* If you have used `gradlew bootRepackage` for packaging your application so far, you have to migrate this to `gradlew bootJar`.
* Refactor calls made through the AWS SDK, which got updated in the process of the new major version of edison and this
  will most probably break prior code that relied on the AWS SDK.
* To use `@Timed`-Annotations, you need to configure Micrometer accordingly. See the following Example for a configuration that
  covers the annotation and naming of all metrics:
  
  ```java
    @Configuration
    @EnableAspectJAutoProxy
    public class MicrometerConfiguration {
    
        @Bean
        public PrometheusNamingConvention prometheusNamingConvention() {
            return new PrometheusNamingConvention();
        }
    
        @Bean
        public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(@Value("${service.vertical}") final String vertical,
                                                                        @Value("${service.name}") final String serviceName,
                                                                        final PrometheusNamingConvention prometheusNamingConvention) {
            return registry -> registry
                    .config()
                    .namingConvention(new NamingConvention() { 
                        // Set naming convention that gets applied to all metrics, in this  example explicitly using a Prometheus naming convention
                        @Override
                        public String name(final String name, final Meter.Type type, final String baseUnit) {
                            return prometheusNamingConvention.name(String.format("%s.%s.%s", vertical, serviceName, name), type, baseUnit);
                        }
                    })
                    .meterFilter(new MeterFilter() { // Configure generally applicable configurations, like percentiles
                        @Override
                        public DistributionStatisticConfig configure(final Meter.Id id,
                                                                     final DistributionStatisticConfig config) {
                            return config.merge(DistributionStatisticConfig.builder()
                                    .percentiles(0.5, 0.9, 0.95, 0.98, 0.99, 0.999)
                                    .build());
                        }
                    });
        }
    
        // Create a `TimedAspect` to enable `@Timed`-Annotations
        @Bean
        public TimedAspect timedAspect(final MeterRegistry registry) {
            return new TimedAspect(registry);
        }
    }

  ```


## Documentation

Edison Modules:
* [`edison-aws`](edison-auth-aws/README.md): AWS related configuration and togglz settings
* [`edison-core`](edison-core/README.md): Main library of Edison microservices.
* [`edison-jobs`](edison-jobs/README.md): Optional module providing a simple job library.
* [`edison-mongo`](edison-mongo/README.md): Auto-configuration for MongoDB repositories plus implementation of MongoJobRepository and Togglz StateRepository.
* [`edison-oauth`](edison-oauth/README.md): Auto-configuration for OAuth Public Key repositories with autofetching and a simple JWT Token Validation.
* [`edison-togglz`](edison-togglz/README.md): Optional support for feature toggles for Edison microservices based on [Togglz](https://www.togglz.org/).
* `edison-testsupport`: Test support for feature toggles plus utilities.
* [`edison-validation`](edison-validation/README.md): Optional module for validation in Spring with a specific response format.

Examples:
* [`example-status`](examples/example-status): Service only relying on `edison-core` to show the usage of health and status features. 
* [`example-jobs`](examples/example-jobs): Edison service using edison-jobs to run background tasks. 
* [`example-togglz`](examples/example-togglz): Example using `edison-togglz´ to implement feature toggles.
* [`example-togglz-mongo`](examples/example-togglz-mongo): Same `edison-toggz`, but with a MongoDB configuration to auto-configure persistence of feature toggles.


## Setup

Make sure you have Java 11 or later and gradle 6.x installed on your computer.

### Testing

Test and create coverage report

    gradle check

### Dependency Update

Determine possible dependency updates

    gradle dependencyUpdates -Drevision=release

### Publishing

#### Publish new releases to sonatype

    ./release.sh

#### Create a release in github
Click on "Releases" -> "Draft a new release". Create a tag and copy&paste the relevant info from the changelog.<br/>
Don't publish packages to github. They are published to sonatype.

## Examples

There are a few examples that may help you to start your first microservice based
on Edison and Spring Boot. Because Spring Boot itself has some complexity, it is
recommended to first read its documentation before starting with Edison.

The examples can be started with gradle:

    gradle examples:example-status:bootRun
    gradle examples:example-jobs:bootRun
    gradle examples:example-togglz:bootRun
    gradle examples:example-togglz-mongo:bootRun

Open in your browser [http://localhost:8080/](http://localhost:8080/)

*Note:* Every example is configured to use port 8080, so make sure to run only one example at a time or to reconfigure
the ports.


## Contributing

Have a look at our [contribution guidelines](CONTRIBUTING.md).
