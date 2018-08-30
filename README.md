# edison-microservice

Collection of independent libraries on top of Spring Boot to provide a faster setup of jvm microservices.

> "I never did anything by accident, nor did any of my inventions come by accident; they came by work." - Thomas Edison


## Status

[![Next Selected Stories](https://badge.waffle.io/otto-de/edison-microservice.svg?label=Ready&title=Selected)](http://waffle.io/otto-de/edison-microservice)
[![Active Stories](https://badge.waffle.io/otto-de/edison-microservice.svg?label=In%20Progress&title=Doing)](http://waffle.io/otto-de/edison-microservice)

[![build](https://travis-ci.org/otto-de/edison-microservice.svg)](https://travis-ci.org/otto-de/edison-microservice) 
[![codecov](https://codecov.io/gh/otto-de/edison-microservice/branch/master/graph/badge.svg)](https://codecov.io/gh/otto-de/edison-microservice)
[![dependencies](https://www.versioneye.com/user/projects/58b16b4a7b9e15004a98c400/badge.svg?style=flat)](https://www.versioneye.com/user/projects/58b16b4a7b9e15004a98c400)
[![release](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core)
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


## Future Releases aka Roadmap

[Semantic Versioning v2.0.0](http://semver.org/spec/v2.0.0.html) is used to specify the version numbers.

This project maintains its roadmap with [issues](https://github.com/otto-de/edison-microservice/issues) and [milestones](https://github.com/otto-de/edison-microservice/milestones).

**[1.0.0](https://github.com/otto-de/edison-microservice/milestone/1)**: Edison Microservices for Spring Boot 1.4 &#10004;

**[1.x.0](https://github.com/otto-de/edison-microservice/milestone/2)**: Edison Microservices for Spring Boot 1.5 &#10004;

**[2.0.0](https://github.com/otto-de/edison-microservice/milestone/3)**: Edison Microservices for Spring Boot 2.0


## Migration from Edison 1.x to Edison 2

Edison 2 has several breaking changes that will make a refactoring of your current application necessary. For a list of
the actual changes, please take a look at the [Changelog](CHANGELOG.md).

When migrating, take care of the following adjustments:

* Starting with Edison 2.0, the minimal Java version will be Java10. So install and configure your application and 
  CI-Systems to use Java10 for running and building.
* Follow the [Spring Boot 2.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide)
  to fix the most common problems.
    * If you want to use the behaviour of Edison 1.x, which hosts all management endpoints below `/internal`, you have to 
      configure `management.endpoints.web.base-path=/internal` in your `application.yml`
* Remove dependencies to the [edison-aws Project](https://github.com/otto-de/edison-aws), which will be deprecated some time in the future. 
  Necessary functionality was transferred to a submodule of `edison-microservice` (named `edison-aws`).
* Refactor calls made through the AWS SDK, which got updated in the process of the new major version of edison and this
  will most probably break prior code that relied on the AWS SDK.

## Documentation

Edison Modules:
* [`edison-core`](edison-core/README.md): Main library of Edison microservices.
* [`edison-jobs`](edison-jobs/README.md): Optional module providing a simple job library.
* [`edison-mongo`](edison-mongo/README.md): Auto-configuration for MongoDB repositories plus implementation of MongoJobRepository and
 Togglz StateRepository.
* [`edison-togglz`](edison-togglz/README.md): Optional support for feature toggles for Edison microservices based on [Togglz](https://www.togglz.org/).
* `edison-testsupport`: Test support for feature toggles plus utilities.
* [`edison-validation`](edison-validation/README.md): Optional module for validation in Spring with a specific response format.

Examples:
* [`example-status`](examples/example-status): Service only relying on `edison-core` to show the usage of health and status features. 
* [`example-jobs`](examples/example-jobs): Edison service using edison-jobs to run background tasks. 
* [`example-togglz`](examples/example-togglz): Example using `edison-togglz´ to implement feature toggles.
* [`example-togglz-mongo`](examples/example-togglz-mongo): Same `edison-toggz`, but with a MongoDB configuration to auto-configure persistence of 
feature toggles.


## Setup

Make sure you have Java 10 or later and gradle 4.x installed on your computer.

### Testing

Test and create coverage report

    gradle check

### Dependency Update

Determine possible dependency updates

    gradle dependencyUpdates -Drevision=release

### Publishing

Publish new releases

    gradle uploadArchives


## Examples

There are a few examples that may help you to start your first microservice based
on Edison and Spring Boot. Because Spring Boot itself has some complexity, it is
recommended to first read it's documentation before starting with Edison.

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
