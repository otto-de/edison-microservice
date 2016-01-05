# Edison Status

Every Edison microservice should provide access to it's internal status. This library provides the following
features to accomplish this:
* A REST API to get the current status of the service as JSON
* An HTML representation / status page for human beeings.
* A simple API to expose status information (see de.otto.edison.status.indicator.StatusDetailIndicator)

## Usage

The example-status shows how to use and configure the status library.
1. Add a dependency to edison-status (or directly add a dependency to edison-service)
2. Configure some properties (see below).
3. Optionally implement some Spring beans, implementing the StatusDetailIndicator (SDI) interface. Every SDI will
be automatically added to the status details section of the status page / JSON document.
4. Optionally override the behaviour of the StatusAggregator and/or schedulers (see 'Conditional Spring Beans'). This
should generally not be necessary.

## Environment Properties

The following properties should be added to your application.properties or application.yml configuration.

Required:
* spring.application.name: The default Spring Boot property containing the name of the service.

Optional VCS information about the deployed application:
* info.build.version: The version number (something like 1.5.2)
* info.build.commit: The GIT commit hash
* info.build.url-template: the URL template used to build an URL to the VCS
(e.g. https://github.com/otto-de/edison-microservice/commit/{commit})

Because these properties are beginning with "info.",
this information is automatically available by accessing Spring Boot's '/info' Endpoint

Optional information about the application:
* edison.status.application.description: A human-readable short description of the application's purpose.
* edison.status.application.group: Information about the group of services this service is belonging to.
Example: 'order', 'user', 'campaign'
* edison.status.application.environment: The staging environment (like develop, prelive, live) of the service.

## Conditional Spring Beans

By default, the status of the application is calculated every 10 seconds and cached in the meantime. You
can change this default behaviour in the following ways:

1. Override the @ConditionalOnMissingBean StatusAggregator. The default implementation is caching the status. You
could replace this default bean by providing an uncached implementation. If calculating the status is expensive
(for example, if you have to access a database), this might not be a good idea if the status ressource is retrieved
frequently.

2. Override the @ConditionalOnMissingBean named 'fixedDelayScheduler'. By default, this scheduler is updating the
StatusAggregator every 10 seconds.

3. Provide a 'cronScheduler' bean and configure edison.status.scheduler.cron in your application properties with
a valid cron expression. This way, the cron scheduler is used instead of the fixedDelayScheduler.
