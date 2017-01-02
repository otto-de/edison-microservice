# Edison Core

Core library of edison-microservice.


# 1. Internal Pages

This library primarily contains some Thymeleaf template fragments that are commonly used to render the UI
of internal pages of Edison Microservices.

You can replace fragments of these pages by copying + modifying these fragments. For example, you can add
menu items to the main menu by overriding templates/fragments/navbar/main.html.

# 2. de.otto.edison.heath

TODO 

# 3. de.otto.edison.status

Edison Microservices are self-describing services: they expose information about the current state and about
the application, system, responsible team and dependencies to other services. The purpose of this library is
to expose such kind of information.

The /internal/status API contains information about the application status:
* A REST API to get the current status and information about the service as JSON
* An HTML representation / status page for human beeings.
* A possibility to indicate the status of jobs, repositories or other components
(see de.otto.edison.status.indicator.StatusDetailIndicator)
* A possibility to add information about required services (see de.otto.edison.about.spec.ServiceSpec)

## 3.2 Usage

The example-status shows how to use and configure the status library.
1. Add a dependency to edison-status (or directly add a dependency to edison-service)
2. Configure some properties (see below).
3. Optionally implement some Spring beans, implementing the StatusDetailIndicator (SDI) interface. Every SDI will
be automatically added to the status details section of the status page / JSON document.
4. Optionally override the behaviour of the StatusAggregator and/or schedulers (see 'Conditional Spring Beans'). This
should generally not be necessary.

## 3.3 Environment Properties

The following properties should be added to your application.properties or application.yml configuration.

Required:
* spring.application.name: The default Spring Boot property containing the name of the service.

Optional VCS information about the deployed application:
* edison.status.vcs.version: The version number (something like 1.5.2)
* edison.status.vcs.commit: The GIT commit hash
* edison.status.vcs.url-template: the URL template used to build an URL to the VCS
(e.g. https://github.com/otto-de/edison-microservice/commit/{commit})

Optional information about the application:
* edison.status.application.title: A short title that is used in the top navigation and the html title tag.
* edison.status.application.description: A human-readable short description of the application's purpose.
* edison.status.application.group: Information about the group of services this service is belonging to.
Example: 'order', 'user', 'campaign'
* edison.status.application.environment: The staging environment (like develop, prelive, live) of the service.

Optional information about the system:
* server.port: The port used to access the application
* server.hostname: The hostname of the server
* HOSTNAME: if server.hostname is not configured, the system environment's HOSTNAME is tried. If this is not available, 
the SystemInfoConfiguration is trying to get the hostname using InetAddress.getLocalHost().getHostName().

## 3.4 Conditional Spring Beans

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

# 4. de.otto.edison.metrics

TODO