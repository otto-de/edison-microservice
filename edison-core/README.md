# Edison Core

Core library of edison-microservice. All Edison Microservices have to include this.

# 1. Internal Pages

This library primarily contains some Thymeleaf template fragments that are commonly used to render the UI
of internal pages of Edison Microservices.

You can replace fragments of these pages by copying + modifying these fragments. For example, you can add
menu items to the main menu by overriding templates/fragments/navbar/main.html.

## 1.1 Customizing the Service Name

The main navigation of /internal pages contains the Name of the Edison service. The name can be configured
by setting `edison.status.application.title` in the application properties.

## 1.2 Customizing menu items

Beside of replacing fragment HTMLs, it is easy to add more menu items to the navigation of /internal pages using
the two `NavBar` beans configured in the `NavBarConfiguration`:
* **`mainNavBar`:** Spring that is used to configure the main navigation bar at the top.
* **`rightNavBar`:** Spring that is used to configure the "Admin" navigation bar at the right side of the menu.
 
The NavBars can be configured like this:
```java
@Component
class MyClass {
    @Autowired NavBar mainNavBar;
    
    @PostConstruct
    public void postConstruct() {
        mainNavBar.register(navBarItem(bottom(), "My Page", "/my/page"));
    }
}
```

# 2. de.otto.edison.health

The health package is relying on Spring Boot HealthIndicators, but with a slightly different focus. While

In contrast to Spring Boot, Edison is using health to indicate that a single instance of a service is healthy
or unhealthy. Load Balancers are using health checks to determine, whether or not a service should get load, or not.

## 2.1 Endpoint /internal/health  

The /internal/health endpoint is used by load balancers to identify if a service
is able to handle requests. It responds with HTTP 200, if the service is available,
or with some HTTP server error (5xx), if not.

The response body contains health information from the different HealthIndicators
(see next section):

```
GET /internal/health
```
Response Body:
```
{
    "status": "UP",
    "application": {
        "status": "UP"
    }
}
```
## 2.2 Health Indicators

ApplicationHealthIndicator is a bean that is used to signal application health for /internal/health.

```java
@Component
class MyClass {
    @Autowired 
    final ApplicationHealthIndicator healthIndicator;

    public void someMethod() {
        try {
            doSomething();
        } catch (final Error err) {    
            healthIndicator.indicateHealth(
                    down().withException(err).build()
            );
        }
    }
}
```
The health check is used by load balancers and/or clients to determine, which service is currently 
able to handle requests.

## 2.3 Adding more Health Indicators

All available Spring Boot Actuator `HealthIndicator` beans are automatically used to determine the current 
application health. The `/internal/health` endpoint is returning information for all registered health indicators. 

## 2.3 Graceful Shutdown

Graceful shutdown is a feature that helps to shutdown services without interrupting client requests in execution.
After getting a signal to shutdown the service, there are two phases:
1. The Service is continuing to stay healty for a few seconds.
2. After this, the service is starting to respond with HTTP server errors, so clients will start to
take the service out of load balancing. After this period, the service actually stops.

Graceful shutdown of services can be configured as follows:

* `edison.gracefulshutdown.enabled:false` Enable/Disable graceful shutdown.
* `edison.gracefulshutdown.indicate-error-after:5000` Number of millis to wait before the health check 
is starting to respond with HTTP server errors.
* `edison.gracefulshutdown.phase-out-after:20000` Number of millis to send server errors, before the 
service is finally shutting down.

# 3. de.otto.edison.status

Edison Microservices are self-describing services: they expose information about the current state and about
the application, system, responsible team and dependencies to other services. The purpose of this library is
to expose such kind of information.

The /internal/status API contains information about the application status:
* A REST API to get the current status and information about the service as JSON
* An HTML representation / status page for human beeings.
* A possibility to indicate the status of jobs, repositories or other components
(see `de.otto.edison.status.indicator.StatusDetailIndicator`)
* A possibility to add information about required services and datasources:
(see `de.otto.edison.status.domain.ExternalDependency` and associated classes).

## 3.1 Usage

The example-status shows how to use and configure the status library.
1. Add a dependency to edison-status (or directly add a dependency to edison-service)
2. Configure some properties (see below).
3. Optionally implement some Spring beans, implementing the StatusDetailIndicator (SDI) interface. Every SDI will
be automatically added to the status details section of the status page / JSON document.
4. Optionally override the behaviour of the StatusAggregator and/or schedulers (see 'Conditional Spring Beans'). This
should generally not be necessary.

## 3.2 Endpoint /internal/status

The Status endpoint is available in HTML and JSON format. It provides information about the application's configuration
as well as the dynamic status.

### 3.3 Application Info

The application section of the status page contains general information about the application that is configured using
`de.edison.status.configuration.ApplicationInfoProperties`:

  * `edison.status.application.title` A short title that is used in the top navigation and the html title tag.
  * `edison.status.application.description` A human-readable short description of the application's purpose.
  * `edison.status.application.group` Information about the group of services this service is belonging to.
  Example: 'order', 'user', 'campaign'
  * `edison.status.application.environment` The stage environment (like develop, prelive, live) of the service.

### 3.4 Application Status

In addition to the static application info, there is a dynamically calculated status, that can be used to monitor
whether or not the application is behaving as expected.

Similar to the HealthIndicators, Edison provides StatusDetailIndicators to determine the application status. For
example, for every job Edison is auto-configuring an indicator that is monitoring the execution of jobs.

By default, the status of the application is calculated every 10 seconds and cached in the meantime. You
can change this default behaviour in the following ways:

1. Override the @ConditionalOnMissingBean StatusAggregator. The default implementation is caching the status. You
could replace this default bean by providing an uncached implementation. If calculating the status is expensive
(for example, if you have to access a database), this might not be a good idea if the status ressource is retrieved
frequently.

2. Override the `@ConditionalOnMissingBean` named `fixedDelayScheduler`. By default, this scheduler is updating the
StatusAggregator every 10 seconds.

3. Provide a `cronScheduler` bean and configure `edison.status.scheduler.cron` in your application properties with
a valid cron expression. This way, the cron scheduler is used instead of the fixedDelayScheduler.

### 3.5 System Information

### 3.6 Version Information

The application can be configured to provide information about the currently deployed version. Version info is displayed
in both HTML and JSON format.

There are two ways to configure the data:

### 3.6.1. Using Edison Properties

A) Edison is supporting a number of properties (see `de.edison.status.configuration.VersionInfoProperties`) to
configure the VCS information:

* `edison.status.vcs.version` The version number (something like 1.5.2)
* `edison.status.vcs.commit:` The GIT commit hash
* `edison.status.vcs.url-template:` The URL template used to build an URL to the VCS
(e.g. https://github.com/otto-de/edison-microservice/commit/{commit})
* `edison.status.vcs.commit-time:` Time of the commit
* `edison.status.vcs.user-name:` Name of the committer
* `edison.status.vcs.user-email:` Email of the committer
* `edison.status.vcs.message-short:` Short commit message
* `edison.status.vcs.message-full:` Full commit message
* `edison.status.vcs.branch:` The branch of the commit

### 3.6.2 Using Spring Boot GitProperties

B) Spring Boot is auto-configuring a `GitProperties` bean, if a `git.properties` resource is found in the classpath. See
(http://docs.spring.io/spring-boot/docs/1.5.2.RELEASE/reference/htmlsingle/#production-ready-application-info-git)[] for
more information about the usage of GitProperties and how to generate the properties in your Gradle or Maven build.

Because GitProperties do not contain fields for VCS url or version, you may provide these two properties as in A), while
using the Spring Boot GitProperties for all the other information.

### 3.7 Team Information

### 3.8 Criticality

### 3.9 External Dependencies

## 3.3 Environment Properties

The following properties should be added to your application.properties or application.yml configuration.

**Required:**
* `spring.application.name` The default Spring Boot property containing the name of the service.

Optional information about the system:
* `server.port` The port used to access the application
* `server.hostname` The hostname of the server
* `HOSTNAME` if server.hostname is not configured, the system environment's HOSTNAME is tried. If this is not available, 
the SystemInfoConfiguration is trying to get the hostname using InetAddress.getLocalHost().getHostName().

* `edison.status.redirect-internal.enabled:true` Redirect /internal to /internal/status

**de.edison.status.configuration.TeamInfoProperties:** Optional information about the team responsible for the service:
* `edison.status.team.name`
* `edison.status.team.technical-contact`
* `edison.status.team.business-contact`

**de.edison.status.configuration.ClusterInfoProperties:** Optional properties about how to access information about
 the current cluster state in green/blue deployment scenarios:
* `edison.status.cluster.enabled:false`
* `edison.status.cluster.color-header:X-Color`
* `edison.status.cluster.color-state-header:X-Staging`

# 4. de.otto.edison.metrics

TODO