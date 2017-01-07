# Release History

## Release 1.0.0.RC1 (SNAPSHOT)

_**Beginning with 1.0.0, we will start using semantic versioning of releases.**_
 
**Breaking Changes:**
* **[edison]** Refactored module structure: 
  * moved `edison-status`, `edison-health`, `edison-metrics`, `edison-microservice` and `èdison-servicediscovery-client`
    into `edison-core`.
  * moved `edison-jobs-mongo` into `edison-mongo`
  * moved `edison-togglz-mongo` into `edison-mongo`
  * moved `edison-togglz-testsupport` into `edison-testsupport`
* **[edison]** Removed remaining dependencies to guava library.
* **[edison]** Graceful shutdown is now disabled by default. Enable it by setting `edison.gracefulshutdown.enabled=true`.
* **[edison-core]** Renamed package `de.otto.edison.discovery` to `de.otto.edison.registry`. The `DiscoveryClient` 
was renamed to `RegistryClient`.
* **[edison-core]** Properties `edison.servicediscovery.*` renamed to `edison.serviceregistry.*`
* **[edison-togglz]** Properties of `edison-togglz` including the togglz console and LDAP has changed. 
The new structure of the properties is like this:
  * `edison.togglz.cache-ttl=100` cache-ttl for feature toggles
  * `edison.togglz.console.enabled=true` Enable / Disable Togglz web console
  * `edison.togglz.console.ldap.enabled=true` Enable LDAP authentication for the web console
  * `edison.togglz.console.ldap.host=localhost` LDAP host
  * `edison.togglz.console.ldap.port=389` LDAP port
  * `edison.togglz.console.ldap.base-dn=test` LDAP base dn
  * `edison.togglz.console.ldap.rdn-identifier=test` LDAP rdn identifier

* **[edison-guava]** Removed the deprecated module `edison-guava`. This is now replaced by edison-cache.
* **[edison-cache]** Removed support for property `edison.cache.web.controller.enabled`. Because the main purpose of 
`edison-cache` is to provide cache statistics as HTML and/or JSON, it makes no sense to deactivate the controller.

**Bugfixes:**
* **[edison-jobs]** Fixed broken link from job messages to /jobdefinitions/<jobType>. `JobDefinitionService.getJobDefition(jobType)`
is now case insensitive.
* **[edison-mongo]** Using `primaryPreferred` instead of `primary` to increase availability during master election.

**New Features:**
* **[edison-core]** Added feature to configure the entries of the navigation bar of /internal/* pages. See
`de.otto.edison navigation` for details and have a look at the `NavigationConfiguration` in the examples. 
* **[edison-core]** Added support to get status information for service that deployed using green/blue deployments. See
`ClusterInfo`, `ClusterInfoProperties` and `ClusterInfoConfiguration` for details.
* **[edison-core]** Introduced `@ConfigurationProperties ApplicationInfoProperties`
* **[edison-core]** Introduced `@ConfigurationProperties TeamInfoProperties`
* **[edison-core]** Introduced `@ConfigurationProperties VersionInfoProperties`
* **[edison-core]** Introduced ``@ConfigurationProperties ClusterInfoProperties`
* **[edison-core]** Introduced `@ConfigurationProperties GracefulShutdownProperties`
* **[edison-core]** Introduced `@ConfigurationProperties MetricsLoadProperties`
* **[edison-core]** Introduced `@ConfigurationProperties ServiceRegistryProperties`
* **[edison-core]** Introduced `@ConfigurationProperties MongoProperties`
* **[edison-core]** Introduced `@ConfigurationProperties ToggzProperties`
* **[edison-jobs]** `JobEvents` not `@Beta` anymore.
* **[edison-mongo]** Added auto-configuration for `FeatureRepository` and `JobRepository`

## release 0.82.2
* ** [edision-mongo] use version of mongo driver to 3.4.1, fongo 2.0.11

## release 0.82.1
* **[edison-jobs]** add _primaryPreferred_ for jobRepository to avoid data loss while updating a jobState 

## Release 0.82.0
* **[edison-jobs]** Show only latest 10 jobs in Job Overview by default

## Release 0.81.0
* **[edison-mongo]** Allow configuration of mongo read preference by setting the property `edison.mongo.readPreference`. The default configuration is primary.

## Release 0.80.0
* **[edison-service]** Make edison-service independent of other edison packages
  - This might break your build because you did not write your project dependencies explicitly in
    your build script. Just add the missing edison packages and everything will be fine.
* **[edison-cache]** Allow CacheInfoController to be disabled

## Release 0.79.3
* **[edison-cache]** Allow registering custom built caches via `CacheRegistry` to gather cache metrics

## Release 0.79.2
* **[edison-mongo]** Bugfix: AbstractMongoRepository does not accept null as ID of objects anymore.

## Release 0.79.1
* **[edison]** Upgrade thymeleaf to version 3.0.2.RELEASE
* **[edison-jobs]** jobdetails page uses div tags instead of spans

## Release 0.79.0
* **[edison]** Upgrade spring boot to 1.4.2.RELEASE
* **[edison]** Upgrade spring version to 4.3.4.RELEASE
* **[edison]** Upgrade guava version to 20.0
* **[edison]** Upgrade caffeine version to 2.3.5
* **[edison]** Upgrade async http client to 1.9.40

## Release 0.78.0
* **[edison-mongo]** Support streaming for findAll methods. The old methods are deprecated now, so please use the new ones.

## Release 0.77.0
* **[edison-service]** Make edison-cache the default caching behaviour

## Release 0.76.1
* **[edison-jobs]** KeepLastJobs is not loading Job messages anymore, avoiding OutOfMemory errors
for lots of large jobs.

## Release 0.76.0
* **[edison-mongo]** Make AbstractRepository.byId() and AbstractRepository.matchAll() non final again
* **[edison-guava]** Make edison-guava deprecated in favor of the new and shiny edison-cache
* **[edison-cache]** Create edison-cache with support for Caffeine caches instead of Guava

## Release 0.75.0
* [edison-jobs],[edison-mongo] Add setJobStatus and setLastUpdate to JobRepository interface and both
  implementations - This fixes a race condition during keepalive and message.
  This is a **breaking change** if you wrote your own JobRepository. 

## Release 0.74.0
* Removed dependency to edison-hal (was introduced for testing purposes only).
* Removed usage of testng and replaced it by Junit

## Release 0.73.0
* [edison-mongo] Refactored AbstractRepository.update() which returns an boolean now.
* [edison-mongo] **Breaking change:** UpdateIfMatch returns an enum (UpdateIfMatchResult) instead of throwing undocumented exceptions.
                 To migrate, you have to remove the exception handling and evaluate the return code to handle it properly.
* [edison-mongo] Removed NotFoundException from edison-mongo
* [edison-jobs] Log job errors.

## Release 0.72.1
* Add some logging information

## Release 0.72.0
* Upgrade Spring Boot to 1.4.0-RELEASE
* Upgrade Spring to 4.3.2-RELEASE
* Add spring-boot-starter-test dependency for @SpringBootTest annotation

 * IMPORTANT: Please follow the Migration Guide on
 https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-1.4-Release-Notes
 when upgrading an existing service to this Edison Microservice version

## Release 0.71.0
* Upgrade mongodb-driver to 3.3.0
* Upgrade togglz to 2.3.0.Final
* Reduce default shutdown period from 30 to 25 seconds

## Release 0.70.0
* do not show distinct jobs if type is given
* optimize createOrUpdate in AbstractMongoRepository by using upsert

## Release 0.69.1
* Job Overview shows distinct jobs as default (old behaviour can be activated via distinct=false parameter)
* Add possibility to add slf4j markers to jobevents 

## Release 0.69.1
* Status page header and title configurable via property `edison.status.application.title`
* Add time of start/stop to job status detail

## Release 0.69.0
* Add JobEvents class. Use it to avoid handing a JobEventPublisher to helper classes of a Job.
* Indicate job errors with configured mapping in application status (default is ERROR).
  The property name is `edison.jobs.status.indicate-joberror-with-level`, possible values are `OK`, `WARNING` and `ERROR`

## Release 0.68.1
* Hide feature toggles menu entry if feature toggles UI is disabled

## Release 0.68.0
* Introduced dependency to github.com/otto-de/edison-hal
* Added media type application/hal+json for /internal/status
* indicate job errors as application error instead of warning
* Catch and log RuntimeExceptions that may occur while persisting JobMessages and StateChanges

## Release 0.67.0
* Fix circular dependencies in JobConfiguration
* Update to latest Spring Boot version
* Update outdated dependencies

## Release 0.66.2
* Fix NullPointerException in disableJobs Feature

## Release 0.66.1
* Add possibility to temporarily disable jobs via the jobdefinitions GUI
* Fix ClearDeadLocks Cleanup: Also clear a lock if the JobId does not exist anymore.

## Release 0.65.0
* Add `ClearDeadLocks` Cleanup Strategy. 
* *BREAKING CHANGE* : `JobRepository` Add method `runningJobsDocument`. Changed signature of `markJobAsRunningIfPossible`

## Release 0.64.0
* Add parameter distinct=true|false to jobcontroller to get only the latest of each jobs in the overview

## Release 0.63.0
* Cronexpressions of JobDefinitions are evaluated on construction of a JobDefinition
* Bugfix: JobLocks are now properly released when Jobs are marked dead.
* Optimization of JobLocking.
* Breaking Change: `JobRepository` interface: add methods `markAsRunningIfPossible` and `clearRunningMark`. Delete unused methods.
* Breaking Change: `StopDeadJobs` constructor now needs a `JobService` and a `JobRepository`. Those can be obtained by autowiring.

## Release 0.62.0
* JobInfo is immutable, this is a breaking change, use the JobInfo.builder() instead

## Release 0.61.0
* Modified Mutex Behavior. The new MutexHandler is now able to use a mongoJobLockProvider which persists the
  mutex-handling in the database. That prevents raceconditions if two jobs of the same type or in the same group should
  be started at the same time.
* Fixed representation of feature toggles to be consistent with
  status mediatype.
* Moved html representation of feature toggles from /internal/togglz
  to /internal/toggles/console
* Moved json representation of feature toggles from /internal/status/togglz
  to /internal/toggles

## Release 0.60.0
* added MetricsFilter to provide counters for http requests

## Release 0.59.0
* upgrade spring boot to 1.3.5.RELEASE
* upgrade spring to 4.2.6.RELEASE

## Release 0.58.1
* edison-togglz: Fixed a bug in the LDAP authentication filter
* edison-togglz-mongo: Log name of the user switching a toggle also in the mongo state repository

## Release 0.58.0
* edison-togglz: added optional LDAP authentication

## Release 0.57.2
* Small bugfix: Fix job detail URL in jobs template

## Release 0.57.1
* set the /internal/jobs/ in the URL of the Location header

## Release 0.57.0
* The JobIds do not contain slashes any more, (/internal/jobs/ is not part of the jobIds)
* Note that jobs which were persisted with older releases might not be accessible any more after the upgrade.
* Also note that some jobs might show up with a red status on your dashboard after the upgrade. Simply rerun the job to
  fix this.

## Release 0.56.3
* togglz: FilterRegistrationBean in TogglzWebConfiguration uses filter name "togglzFilter"

## Release 0.56.2
* edison-jobs-(mongo): Moved handling of persisting error status to PersistenceJobEventListener

## Release 0.56.1
+ Bugfix: Do not aggregate application status in constructor. Use @PostConstruct instead

## Release 0.56.0
* New Feature: Mutually exclusion of jobs using JobMutexGroups

## Release 0.55.2
* JobDefinition show fixedDelay in seconds if minutes would be zero

## Release 0.55.1
* Set job status to error after receiving an error message
* Make mongo codec registry configurable

## Release 0.55.0
* Optimized handling of persisting job messages
* add serverSelectionTimeout (default 30secs) to mongo config

## Release 0.54.1
* JobStatusDetailIndicator: Indicate error if job could not be retrieved from the repository 

## Release 0.54.0
* Keep last jobs strategy now keeps the last N jobs of each type
* Create indices in jobs collection
* Return DeleteResult of mongo delete queries

## Release 0.53.1
* edison-jobs-mongo: Create index on jobtype and started to sort jobs by date when all job documents are bigger then 32MB  

## Release 0.52.2
* edison-metrics: Refactored auto configuration of `LoadDetector` to allow strategy selection by `application.properties` as well as exposing your own bean the same time

## Release 0.52.1
* Refactored load indication (see 0.52.0) from edison-status to edison-metrics and introduced new endpoint `/internal/load` which returns the load status to be used by consumers (ie. auto-scaling drivers). See `example-metrics` for an usage example on average response time behaviour.

## Release 0.52.0
* Added status indicator allowing the application to signal overload (see `LoadStatusIndicator`), which enables ie. watchers to autoscale the application accordingly
* two default strategies to discover load, one making use of metrics library, allowing to leverage `@Timed` annotations on your classes  (see `application.properties` in `example-status`)
* Fixed behaviour of application status aggregation to provide instant calculation instead of waiting until first update has ran

## Release 0.51.1
* Updated to Spring Boot 1.3.3 (with Spring 4.2.5)

## Release 0.51.0
* edison-guava: Refactored edison-guava. Guava caches can now be configured using GuavaCacheConfig. These caches will
now expose cache statistics as /internal/metrics (JSON) and /internal/caches/statistics (HTML). 
See edison-guava/README.md for more details on how to use Guava caches.
* edison-metrics: Fixed package name (...metrics instead of ...health)

## Release 0.50.3
* Housekeeping: Updated Spring Boot to version 1.3.2, MongoDB driver to 3.2.2 and Logback to 1.1.5

## Release 0.50.2
* edison-status: Added status to /internal/status.html

## Release 0.50.1
* edison-jobs: Bug-Fix in find implementations to limit search result after sorting has happened
* edison-jobs: Added method to find job instances by type and status

## Release 0.50.0
* Added InternalController that is redirecting requests from /internal to /internal/status. This can be disabled
by setting edison.status.redirect-internal.enabled=false

## Release 0.49.4
* Reverted: "removed unnecessary bean from togglz configuration" because embedded containers in Spring Boot don't scan web-fragments

## Release 0.49.3
* removed unnecessary bean from togglz configuration
* Extended `JobService` to cover also synchronous job execution

## Release 0.49.2
* Fix error on job page for long-running jobs
* Job Lifecycle: set hostname via SystemInfo (to overcome problems on Mesos)

## Release 0.49.1
* Added field `hostname` to JobInfo, which allows to track on which server the job gets executed
* Updated dependencies: async-http-client (to 1.9.32) and mongodb-driver (to 3.2.1)

## Release 0.49.0
* Removed unneeded appId from ServiceSpec and ApplicationInfo
* Fixed path in navigation from /internal to /internal/status
* Using edison.status.application.environment + .group instead of edison.servicediscovery.environment + .group.
The properties edison.servicediscovery.environment and edison.servicediscovery.group 
can be removed from application.properties/.yml

## Release 0.48.0
* VCS information are now expected in edison.status.vcs.* instead of info.build.*. Change in build.gradle required
* Added ServiceSpecs to be able to specify dependencies to other services
* Added TeamInfo to the /internal/status
* Added environment, group and appId to /internal/status

## Release 0.47.2
* Added convenience methods to `JobEventPublisher` to make logging more fun again
* Updated spring-boot to version 1.3.1.
* Update to org.springframework.boot:spring-boot-starter-thymeleaf:1.3.1.RELEASE required


## Release 0.47.1
* Fixed bug in navigation: broken path to bootstrap css

## Release 0.47.0
* Added some more information to /internal/status 
* Fixed the format of the status JSON
* Update to com.github.fakemongo:fongo:2.0.4 required

## Release 0.46.2
* Removed StateChangeEvent.State.CREATE, replaced it by START
* Renamed State.STILL_ALIVE to KEEP_ALIVE
* Job HTML templates are now using the Thymeleaf navigation fragments so µServices have a common menu on all pages.

## Release 0.46.1
* Minor refactorings in handling of job events.

## Release 0.46.0
* Rebuild job state architecture due to introduce an event bus to separate the JobInfo and the
  JobRepository. From now on the JobRunner and the specific jobs will report changes (states and messages) to
  an EventPublisher, which will propagate the events to the JobEventListeners (e.g. persist them or log them).
  You can register your own JobEventListeners. The JobMonitor is removed.
* If your job has an error, you should throw a RuntimeException with an errorMessage. Then the job will be retriggered (if retry configured).

## Release 0.45.3
* Fixed default JobMonitor to save log messages in JobRepository for every message.

## Release 0.45.2
* Minor bugfixes
* Using Guava CacheStats in CacheStatistics

## Release 0.45.1

New Features:
* Show the runtime of the job to identify the complete exports with actions.
* Format details in status.json to camel case key names

## Release 0.45.0

New Features:
* Broken Jobs are automatically restarted according to the number of retries specified in the `JobDefintion`.
* `JobDefinition` now has a new field `restarts`, specifying how often a job is restarted if it failed because of an error.

Breaking Changes:
* Renamed libraries: all libs now have the prefix "edison-".
* The factory methods of the `DefaultJobDefinition` now have an additional parameter for the number of restarts.
* Every `JobRunnable` now needs to provide a `JobDefinition`
* Removed `JobRunnable#getJobType`. The job type is accessed by the `JobDefinition`
* Renamed to `DefaultJobDefinition#notTriggerableJobDefinition` to `manuallyTriggerableJobDefinition` to clarify purpose
* Removed hystrix module

## Release 0.44.0

New Features:
* MongoDB persistence: Allow to retrieve the object (including key) when it is created.
  Changed signature of `create` and  `createOrUpdate` in `AbstractMongoRepository`
  to return the object instead of void

Bug Fixes:
* Allow also a value without key to be given to `AbstractMongoRepository#createOrUpdate` without throwing a NPE
* `application.properties`: renamed ``edison.application.name`` (introduced in Release 0.35.0) back into `spring.application.name`
  (see [ContextIdApplicationContextInitializer](https://github.com/spring-projects/spring-boot/blob/v1.3.0.RELEASE/spring-boot/src/main/java/org/springframework/boot/context/ContextIdApplicationContextInitializer.java)
  for details on identifying your application)
* Added dummy feature toggle implementations (for `FeatureClassProvider`) to example projects, which can now
  be run on its own again (ie. `gradle example-jobs:bootRun`)


## Release 0.43.0
* Updated spring-boot to version [1.3.0](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-1.3-Release-Notes), hystrix to 1.4.21 and metrics library dependency to 3.1.3

## Release 0.40.0
* Update togglz library to 2.2.0

## Release 0.39.2
* Cleaned up link construction on internal status and job pages
* Timestamps in JSON status representation are now in ISO-8601 date format (including timezone)

## Release 0.39.1
* Fixed possible broken links on /internal pages

## Release 0.39.0
* Added servicediscovery-client to edison-microservice
* ServiceDiscovery is only used if edison.servicediscovery.* properties are available

## Release 0.38.0
* JobDefinitions extended to support jobs that are not triggered automatically.

## Release 0.37.0
* Fixed links on internal pages

## Release 0.35.0

New Features:
* New library servicediscovery-client introduced to provide basic ServiceDiscovery features for Edison Microservices.
This lib may be used to register services at an Edison JobTrigger.

Bug Fixes:
* Fixed possible ConcurrentModificationException if job messages are accessed from JobInfo.

Breaking Changes:
* application.properties is now using server.context-path instead of server.contextPath.
* application.properties is now using edison.application.name instead of application.name.


## Release 0.34.0
* Added ID to panel with job messages for identification in JobTrigger

## Release 0.33.0
* Added UIs for Status pages
* Nicer internal pages, including reworked jobs and job-definitions

## Release 0.29.0

New Features:
* Prepared integration with (upcoming) Edison JobTrigger
* Added Bootstrap UIs for Jobs and /internal
* Added edison-service for typical microservices, including jobs, health, status,
* Using webjars in edison-service to include bootstrap and jquery

Breaking Changes:
* The format of job definitions and jobs has changed.

## Release 0.28.2

Bugs fixed:
* Fixed critical bug that prevented the startup of the server in combination with usage of edison jobs-mongo

## Release 0.28.1

Bugs fixed:
* Fixed bugs that prevented the StatusDetails of jobs to be added to status page.

## Release 0.28.0

New Features:
* Jobs are rendered with absolute URLs in JSON and HTML representations.
* Introduced the (optional) concept of JobDefinitions to describe the expected frequency etc. to trigger jobs.
* Based on the JobDefinitions, for every Job a StatusDetailIndicator is registered automatically. This may be
disabled by either not providing a JobDefinition, or by setting property edison.jobs.status.enabled=false

Breaking Changes:
* Jobs in earlier version where identified by the URI of the job including the servlet context path. Starting with
this version, the context path is not part of the identifier anymore. You should DELETE all old jobs, otherwise
the URLs a broken.
* Job representations now contain the full URL of the jobs instead of relative URIs.


