# edison-microservice

Common basis for some of otto.de's micro-services using Spring Boot.


>"I never did anything by accident, nor did any of my inventions come by accident; they came by work."

>Thomas Edison


## Status

[![Build Status](https://travis-ci.org/otto-de/edison-microservice.svg)](https://travis-ci.org/otto-de/edison-microservice) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-service/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-service)
[![Dependency Status](https://www.versioneye.com/user/projects/55ba6f016537620017001905/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55ba6f016537620017001905)

Have a look at the [release history](HISTORY.md) for details about updates and changes.


## About

This project contains a number of independent libraries that may be used to create microservices on top of Spring Boot. The libraries are used in different projects at OTTO. It's purpose is to provide a common implementation for cross-cutting requirements like:

* Health checks that are used to tell the load balancer or mesos platform whether or not a service is healthy.
* A [Status page/document](https://github.com/otto-de/edison-microservice/tree/master/edison-status) that may be used to give information about the current state of the service. Status information may also include details about sub-components, background jobs like imports, and so on.
* A simple job handling library that is used to run asynchronous background jobs, which for example can be used to run data imports from other systems.
* An optional MongoDB-based implementation of a JobRepository
* Support for MongoDB-based repositories in case you do not like Spring Data
* Reporting of metrics to Graphite
* Support for [Guava caches](https://github.com/otto-de/edison-microservice/tree/master/edison-guava)
* Logging of messages to Kafka queues
* Support for feature toggles based on the Togglz library

... plus all the features of [Spring Boot](http://projects.spring.io/spring-boot/).


## Getting started

Make sure you have Java 1.8 and gradle 2.x and installed on your computer.
To run all tests for all edison modules execute in the base directory
where you have checked out this project the shell and give it a spin:

    gradle clean check


## Examples

There are a few examples that may help you to start your first microservice based
on Edison and Spring Boot. Because Spring Boot itself has some complexity, it is
recommended to first read it's documentation before starting with Edison.

The examples can be started with gradle:

    gradle clean example-jobs:bootRun
    gradle clean example-metrics:bootRun
    gradle clean example-status:bootRun
    gradle clean example-layout:bootRun

Open in your browser [http://localhost:8080/example/](http://localhost:8080/example/)

*Note:* Every example is configured to use port 8080, so make sure to run only one example at a time or to reconfigure
the ports.

## Contributing

So, you want to contribute to this project! That's awesome. However, before doing so, 
please read the following simple steps how to contribute.

### Discuss the changes before doing them

There is a [edison-microservice Google Group](https://groups.google.com/forum/#!forum/edison-microservice) where you 
can get help or open a discussion about missing features or other issues. 

You can also use the GitHub issue tracker, to [open an issue](https://github.com/otto-de/edison-microservice/issues), 
describing the contribution you would like to make, the bug you found or any other ideas you have. 
This will help us to get you started on the right foot.

It is recommended to wait for feedback before continuing to next steps. However, if 
the issue is clear (e.g. a typo) and the fix is simple, you can continue and fix it.

### Fixing issues

Fork the project in your account and create a branch with your fix: some-great-feature or some-issue-fix.

Commit your changes in that branch, writing the code following the code style. Please do not forget to
add some tests and documentation

### Creating a pull request

Open a pull request, and reference the initial issue in the pull request message (e.g. fixes #). 
Write a good description and title, so everybody will know what is fixed/improved.

### Wait for feedback

Before accepting your contributions, we will review them. You may get feedback about what should be 
fixed in your modified code. If so, just keep committing in your branch and the pull request will be 
updated automatically.

### Everyone is happy!

Finally, your contributions will be merged, and we will publish a new release. Contributions are more than welcome!
