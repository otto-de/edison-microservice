# Release Notes

## 2.2.4
* **[edison-jobs]**: DynamoDb Support
    * fix ETag handling for DynamoJobRepository

## 2.2.3
* **[edison-jobs]**: DynamoDb Support
    * creation of tables removed
    * Fixes for Enabling Jobs and ClearRunningJob
    * Clear table when calling deleteAll instead of deleting and recreating table

## 2.2.2
* **[edison-jobs]**: add DynamoDb Support for Edison-Jobs
    * Properties for enabling DynamoDb:
        * **edison.jobs.dynamo.enabled**: Enable DynamoDb (disabled by default)
        * **edison.jobs.mongo.enabled**: MongoDb needs to be disabled
        * **edison.jobs.dynamo.jobinfo.tableName**: Name for JobInfo table (gets created if non-existent)
        * **edison.jobs.dynamo.jobinfo.pageSize**: PageSize for scan-requests against JobInfo table
        * **edison.jobs.dynamo.jobmeta.tableName**: Name for JobMeta table (gets created if non-existent)

## 2.2.1
* **[general]**: upgrade aws sdk
* **[edison-core]**: Fix basic auth credentials retrieval on wrong format

## 2.2.0
* **[general]**: Update to Spring Boot 2.2

See https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2-Release-Notes for a migration guide 

## 2.1.3
* **[general]**: Update dependencies

## 2.1.2
* **[general]**: increase gradle version

## 2.1.1
* **[edison-togglz]**: 
  * Make S3TogglzRepository cache dependent from String instead of Feature in order to support kotlin togglz
  * Add `getFeatureFromName(String name)`- function to be able to retrieve current feature by its name

## 2.1.0
* **[edison-togglz]**: 
  * Make FeatureManager @ConditionalOnMissingBean to allow overriding
  * Get @Label and other annotations from FeatureManager.getMetaData so that this works with Features that are not enums, too
  * Add methods to FeatureManagerSupport that get the FeatureManager as parameter instead of from the FeatureContext

## 2.0.1
* **[edison-validation]**: Make error profile configurable via application property 'edison.validation.error-profile'

## 2.0.0
* **[edison-validation]**: Add EnumListValidator to be able to validate a list of enums

## 2.0.0-rc9
* **[edison-togglz]**: 
If `edison.togglz.s3.check-bucket` is enabled, the s3 bucket has to be available in order to let the service startup correctly.
This prevents the in-memory repository to instantiate.


## 2.0.0-rc8
* **[general]**: Bugfix: Use async http client version that fits AWS SDK

## 2.0.0-rc7
* **[edison-oauth]**: 
Suppress unnecessary warning on startup

* **[general]**: Various dependency upgrades

## 2.0.0-rc6
* **[edison-oauth]**: 
Block query of public keys until keys have been initially fetched from server 

## 2.0.0-rc5

* **[edison-jobs]**: 
    - Reimplement the JobEventPublisher from Edison 1.x for backwards compatibility

* **[edison-oauth]**: 
    - Add dependency to org.springframework.security:spring-security-web:5.1.4.RELEASE
    - Fixes thread-safety issue in OAuthPublicKeyInMemoryRepository

## 2.0.0-rc4

* **[edison-togglz]**:
Re-Enable Prefetch-Feature for `S3TogglzRepository` in newer Spring versions again

## 2.0.0-rc3

* **[edison-togglz]**: 
`edison.togglz.mongo.enabled` is now true by default

* **[edison-jobs]**: 
`edison.jobs.mongo.enabled` is now true by default

## 2.0.0-rc2
* **[general]**: Updated to Spring Boot 2.1.2

## 2.0.0-rc1

* **[general]**: Updated to Gradle 5.0
* **[general]**: Updated to AWS SDK 2.2.0

## 2.0.0-m5

**New Features:**
* **[edison-core]** Enhanced LDAP configuration to require a specific role to access secured paths. To achieve this you 
                    can set the property `edison.ldap.required-role`. As usual, all whitelisted paths will be ignored.
* **[edison-core]** Enhanced LDAP configuration to use more than one prefix path with the property `edison.ldap.prefixes`.  
                    The old property `prefix` is now deprecated, but still usable and will be appended to `prefixes`.
                    
**Deprecation:**
* **[edison-core]** The single value property `edison.ldap.prefix` is now deprecated and replaced by the multi value 
                    property `edison.ldap.prefixes`.  
                    The old property `prefix` is now deprecated, but still usable and will be appended to `prefixes`.

**Breaking Changes:**
* **[edison-aws]**  AWS-related stuff now requires an AwsCredentialsProvider to be exposed as a Spring bean. No default
                    bean available anymore.
* **[edison-aws]**  S3 togglz repository is automatically used if the property `edison.togglz.s3.bucket-name` is set, 
                    `edison-togglz.s3.enabled` is not false, and if the S3Client.class is in the classpath of your 
                    Edison service. A S3Client bean is required in the ApplicationContext.                     
* **[edison-aws]**  S3TogglzRepository is now part of **[edison-togglz]** and is automatically used if the property 
                    `edison.togglz.s3.bucket-name` is set, `edison.togglz.s3.enabled` is true and a bean of type 
                    `software.amazon.awssdk.services.s3.S3Client` is provided.
* **[edison-aws]** ParamStorePropertySourcePostProcessor moved to **[edison-core]**.
                    The properties used to configure the paramstore have changed from `edison.aws.config.*` to 
                    `edison.env.paramstore.*`                        
* **[edison-mongo]** MongoTogglzRepository is now part of **[edison-togglz]** and will be autoconfigured if `edison.togglz.mongo.enabled`
                    is true and a bean of type `com.mongodb.MongoClient` is provided.
* **[edison-mongo]** MongoJobRepository is now part of **[edison-jobs]** and will be autoconfigured if `edison.jobs.mongo.enabled`
                    is true and a bean of type `com.mongodb.MongoClient` is provided.
* **[edison-core]** ParamStorePropertySourcePostProcessor moved to **[edison-core]** and removed project 
                    **[edison-paramstore]**.
                    The properties used to configure the paramstore have changed from `edison.aws.config.*` to 
                    `edison.env.paramstore.*`
                    
**Maintenance:**
* **[general]**     Tests have been moved from JUnit4 to JUnit5 in all projects.

## 2.0.0-m4

* **[general]**: You need Java >9 to compile, but target compatibility is set back to 1.8
* **[edison-mongo]** create compound index of `type` and `started` in job repository

## 2.0.0-m3

* **[edison-togglz]**: TogglzStateRepositories are now always Cache-Repositories with a TTL of 60 Seconds. 
  If you need a non-Cached repo or any other way of handling these states, feel free to write a custom `TogglzConfig` spring bean configuration.

* **[edison-aws]**: There is now a `S3TogglzRepository` generally available. It will store Togglz-State in S3, caches the state and refreshes it every 60 seconds
  from S3.

## 2.0.0-m2

* **[general]**: Updated to Spring Boot 2.0.4.RELEASE

* **[general]**: Updated to Spring Framework 5.0.8.RELEASE

* **[general]**: Updated to Gradle 4.10

* **[general]**: Changed to Java 10 SDK

* **[general]**: Updated dependencies to latest versions

* **[edison-togglz]**: Added Testconfiguration for TogglzTest

* **[edison-core]**: remove codahale metrics and use builtin micrometer in Spring Boot 2

* **[edison-aws]**: Migrate modules `edisown-aws-s3` and `edison-aws-config` into submodule `edison-aws`

* **[edison-aws]**: Use `testcontainers`-library for local testing with `localstack` in `edison-aws`

## 2.0.0-m1

**Breaking Changes:**

* Updated to Spring Boot 2.0.0.M7
  * This also requires gradle 4.x for building edison-microservice.
 
* upgrade asyncHttpClient version to 2.1.0-RC1

* Refactored edison-jobs:
  * **[edison-jobs]**: The JobEventPublisher from Edison 1.x is removed now. Messages are attached to JobInfo by simply
  writing a (Logback-) Log message with a `JobMarker` like this: 
  ```LOG.error(JobMarker.JOB, "Some random error occured"); ```
  * **[edison-jobs]**: `JobRunnable.execute()` is now returning a boolean that is used to indicate, whether the job
  was executed (true) or skipped (false).
  
* Removed @Beta code:
  * **[edison-core]** Removed prototype code to support dynamic scaling with load detection.  

* Removed @Deprecated stuff:
  * **[edison-cache]** Removed edison-cache because Spring Boot 2.0 does not support this anymore.
  * **[edison-core]** Removed ServiceSpec. Replaced by ServiceDependency.
  * **[edison-mongo]** Default constructor from AbstractMongoRepository is replaced with 
  AbstractMongoRepository(MongoProperties)
  * **[edison-mongo]** Removed property `passwd` from MongoProperties which was replaced with `password` in order to 
  prevent exposure
  * **[edison-mongo]** Removed property `socketTimeout` from MongoProperties. Use `defaultReadTimeout` and 
  `defaultWriteTimeout` instead.
  * **[edison-mongo]** Removed property `socketTimeoutForHighTimeoutClient` from MongoProperties, as well as Spring 
  Beans `mongoClientWithHighSocketTimeout` and `mongoDatabaseWithHighSocketTimeout`. Use custom timeouts 
  on read and write operations instead. 
  * **[edison-togglz]** LDAP support from edison-togglz is replaced by edison-core

### Migrating from 1.2.3:

The Spring Boot docs on how to upgrade can be found here:
* [Spring Boot 2.0.0-M1 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M1-Release-Notes)
* [Spring Boot 2.0.0-M2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M2-Release-Notes)
* [Spring Boot 2.0.0-M3 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M3-Release-Notes)
* [Spring Boot 2.0.0-M4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M4-Release-Notes)
* [Spring Boot 2.0.0-M5 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M5-Release-Notes)
* [Spring Boot 2.0.0-M6 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M6-Release-Notes)
* [Spring Boot 2.0.0-M7 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-M7-Release-Notes)
* [Spring Boot 2.0.0-RC1 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0.0-RC1-Release-Notes)

#### a) Management Context-Path

In Spring Boot 1.x, the `management.context-path` property is used to configure the path of endpoints relative to the
application's `server.context-path`. Beginning with Spring Boot 2.0.0 / Edison 2.0.0, the following configuration must
be changed:
```yaml
server:
    context-path: /my-app
management:
    context-path: /internal
```
will become:
```yaml
server:
    servlet:
        context-path: /my-app
management:
    endpoints:
        web:
            base-path: /internal
```

#### b) Properties

Edison's ApplicationInfoProperties have been renamed to EdisonApplicationProperties and moved to 
`de.otto.edison.configuration`.
The property names have changed, too:

| Old                      | New                                    | Default Value     |
|--------------------------|----------------------------------------|-------------------|
|edison.status.title       | edison.application.title               | "Edison µService" |
|edison.status.group       | edison.application.group               | ""                |
|edison.status.environment | edison.application.environment         | "unknown"         |
|edison.status.description | edison.application.description         | ""                |
| -                        | edison.application.management.base-path| "/internal"       |

#### c) Status, /internal and Actuator Endpoints

Most Edison-Microservices will currently use `/internal` for Endpoints, as well as Edison-specific admin pages like
`/internal/loggers` or `/internal/status`  

While Edison is currently by default redirecting /internal to /internal/status (see 
`de.otto.edison.status.controller.InternalController` and the usage of `edison.status.redirect-internal.enabled`, 
Spring Boot is rendering some kind of "Home Document", obviously in application/hal+json format, for requests to 
${management.endpoints.web.base-path} (that is, requests to `/internal`). Right now, I can not see how to disable this.

There are two recommended options for Edison Services:

1) Disable /internal Redirection and use /internal as a base-path for Spring Boot Actuator endpoints:

Configure `edison.status.redirect-internal.enabled=false`: This will disable the redirection of `/internal` requests.

Now you can configure Edison and Actuator to use the same base-path, which is the same behaviour as in 
Edison 1.x, only that `/internal` is not redirecting to `/status` anymore.

```yaml
management.endpoints.web.base-path=/internal
edison.application.management.base-path=/internal 
```

2) Separate Spring Boot Actuator Endpoints from Edison:

In order to avoid future hassle with conflicting Actuator endpoints, we should possibly separate endpoints from Edison:
Configure `management.endpoints.web.base-path=/actuator` (which is the new default for Spring Boot Actuator endpoints) 
and the new `edison.application.management.base-path=/internal`. This way, all actuator Endpoints will be available 
under different URLs than Edison admin pages. 

#### d) Actuator Endpoints

Because of changes in Spring Boot 2.0.0, the actuator endpoints must now be enabled explicitly.
```yaml
endpoints:
    env:
        enabled: true
    health:
        enabled: true
    loggers:
        enabled: false
    metrics:
        enabled: true
    trace:
        enabled: true
    # ...
    # ... etc. pp.
    # ...
    status:
        enabled: false
```

## 1.2.22-SNAPSHOT
* **[edison-mongo]** Replace fongo tests with embedded mongo tests, because fongo doesn't work with recent mongo drivers
* **[edison-mongo]** Updated mongo client library to latest version 3.8.1                       

## 1.2.21
* **[edison-jobs]** Fix Error Status for deactivated jobs

## 1.2.13
* **[edison-validation]** Fix deserialization of ErrorHalRepresentation

## 1.2.12
* **[edison-validation]** Fix validation helper to use correct message interpolator.

## 1.2.11
* **[edison-validation]** InstantValidator accepts null values. 

## 1.2.10
**New Features:**
* **[edison-validation]** Add this new module for common validation concerns.

## 1.2.9
**Bug fixes**
* **[edison-core]** Fix server error for empty username/password and allow colons in passwords.

## 1.2.8

**New Features:**
* **[edison-core]** Add property `edison.ldap.encryptionType` which can be configured to `StartTLS` (default) or `SSL` 

## 1.2.7

**Maintenance:**
* **[edison-core]** Limit global model attributes to edison packages

## 1.2.6
* upgrade asyncHttpClient version to 2.2.0 for edison-aws (conflict netty-client with amazon sdk 2.0.0)

## 1.2.5

**Maintenance:**

* **[edison-core]** Don't expose user names at successful login as this is a security issue

## 1.2.3

**New Features:**

* **[edison-core]** Add support for ldaps with SSLLdapConnectionFactory

**Breaking Changes:**

* **[edison-dynamodb]** __(Beta)__ Moved to [edison-aws](https://github.com/otto-de/edison-aws) repository.

## 1.2.2

**Bugfixes:**
  
* **[edison-core]** Fix accidential usage of a jdk.nashorn @Immutable annotation, which prevents the usage of edison
                    with Java 9
* **[edison-jobs]** Timestamps in JobStatus and JobMessage are truncated to milliseconds. This adds compatibility of
                    our current persistence with Java 9, which has a new default clock precision of nanoseconds.

## 1.2.1

**Maintenance:**

* **[edison-core][edison-jobs]** Update of AsyncHttpClient from 1.9.x to 2.0.x.
    To not break existing services when upgrading, it was changed to a `compile` dependency instead of a `compileOnly`
    dependency.
    **Important:** Please upgrade or remove the AsyncHttpClient dependency in your project. The group name has also
    changed from `com.ning` to `org.asynchttpclient`.

**Bugfixes:**

* **[edison-core]** `LdapAuthenticationFilter` only throws a warning when authentication
    against *all* baseDN's fails. 

## 1.2.0

**New Features:**

* **[edison-mongo]** Instead of using a socket timeout for all reads and writes,
    you can now use specific timeouts for each operation. Also you can specify a
    `defaultReadTimeout` and a `defaultWriteTimeout`. These will be used for all
    operations in `AbstractMongoRepository` when you do not specify a timeout explicitly.

    **Important:** When upgrading to this release, you should remove the `socketTimeout`
    option in your configs or set it to a very high value. It should be higher than 
    any expected long running query or batch write will take. The new default socket
    timeout is 0, which means that there is no socket timeout. This is the default in 
    the mongo driver. 

* **[edison-jobs]** When a service is shutting down gracefully, it writes a
    notification into all currently running job logs.

## 1.1.6

**New Features:**

* **[edison-mongo]** Add new property: `edison.mongo.sslEnabled`

    Set this property to true to use ssl for connection.
    This is needed when using Mongodb Atlas.
    
**Bugfixes:**

* **[edison-core]** Fix user role determination in LDAP authentication filter.

## 1.1.5

**New Features:**

* **[edison-dynamodb]** __(Beta)__ DynamoDB persistence.
* **[edison-mongo]** Add new property: `edison.mongo.authenticationDb`

    Set this property to use different db for user authentication then for data.
    This is needed when using Mongodb Atlas. Use "admin" db.
* **[edison-mongo]** edison-mongo can create a second mongodb client with different socket timeout now.

    This can be useful outside of controllers with small response time limits. One use case is a maintenance job
    with a query that takes some seconds.
    
    The second client is only created if `edison.mongo.socket-timeout-for-high-timeout-client` is defined in your
    application properties.
    
    You can wire the beans like this in your code: 
    
    ```
    @Autowired
    public ExampleRepository(final @Qualifier("mongoDatabaseWithHighSocketTimeout") MongoDatabase mongoDatabaseWithHighSocketTimeout) {...
    ```
    or 
    ```
    @Autowired
    public ExampleRepository(final @Qualifier("mongoClientWithHighSocketTimeout") MongoClient mongoClientWithHighSocketTimeout) {...
    ```
    
    Side note: The concept of a small socket timeout for the mongodb client to set small query limits is deprecated. We
    should go for timeouts on DBCursors that result from find operations and for timeouts on WriteConcerns for writing
    operations. The "second client concept" is more of a workaround for existing services that rely on the current
    concept of edison-mongo.
         
**Maintenance:**
  
* **[edison-core]** log ldap auth errors as warning  

## 1.1.4

**New Features**

* **[edison-core]** Allow to configure multiple BaseDN names to bind against LDAP for authentication.

    If you use the property file format, you need to add one entry for each BaseDN name.
    E.g.:
    ```
        edison.ldap.baseDn[0]=ou=otto,ou=people..
        edison.ldap.baseDn[1]=ou=tools,ou=people..
    ```

## 1.1.3

**New Features:**

* **[edison-core]** Add thread information to default metrics reporting

## 1.1.2

**Dependency Updates:**

* Updated to Spring Boot 1.5.4.RELEASE
* Updated to Spring Framework 4.3.8.RELEASE

**Bugfixes:**

* Fixed issue #93: SAXParseException when requesting /internal/loggers

**New Features:**

* Add ServiceDependency for service registry

## 1.1.1

**Bugfixes:**

* **[edison-core]** Fix broken navbar on internal pages when using Thymeleaf 2

## 1.1.0

**Dependency Updates:**
* Updated to Spring Boot 1.5.3.RELEASE
* Updated to Spring Framework 4.3.7.RELEASE
* Updated to Mockito Core 2.8.9

**Bugfixes:**

* **[edison-core]** Added /internal/js to LDAP whitelist, so the JS can be loaded w/o authentication in case of
whilelisted /internal/jobs or /internal/jobdefinitions
* **[edison-core]** Renamed Spring Bean `authenticationFilter` (see `LdapConfiguration`) to `ldapAuthenticationFilter`
to prevent naming collisions with `authenticationFilter` bean registered by
`de.otto.hmac.authentication.AuthenticationFilter` from `hmac-auth-server` library.

**Deprecations:**

* **[edison-core]** Deprecated the (Beta) ServiceSpecs.
* **[edison-togglz]** Deprecated the usage of TogglzLdapProperties and TogglzLdapConfiguration. Instead of using this,
you should migrate to the LDAP authentication introduced with 1.0.1.RELEASE.

**New Features:**

* **[edison-core]** Added a UI to configure the log levels of the service. The UI is added to the right nav bar and is
available under `/internal/loggers`
* **[edison-core]** Introduced the configuration of external dependencies, which is replacing the now
deprecated ServiceSpecs. External dependencies include datasources like databases or queues, as well as
dependencies to REST or other services.
* **[edison-core]** Introduced the configuration of information about the criticality of a service.
* **[edison-core]** Enhanced /internal/status JSON and HTML to return criticality and external dependencies.
* **[edison-core]** Introduced support to add HTTP request headers to the Slf4j MDC. By default, X-Origin headers are
added, so log messages can automatically be enhanced by the value of this header.
* **[edison-core]** Added support for `git.properties` generated by gradle plugin "com.gorylenko.gradle-git-properties".
Added more properties to VersionInfoProperties (for example, user info and commit messages) and added these
information to the status page.
* **[edison-metrics]** Added support to filter metrics sent to Graphite.
By default, metrics with names having a postfix of`.m5_rate`, `.m15_rate`, `.min`, `.max`, `.mean_rate`,
 `.p50`, `.p75`, `.p98` or `.stddev` are filtered and not reported to graphite.
You may configure the filtering by overriding
the default `GraphiteReporterConfiguration.graphiteFilterPredicate` bean definition.
* **[edison-metrics]** Add property `edison.metrics.graphite.addHostToPrefix:true` to switch off the hostname in
graphite prefix
* **[edison-mongo]** The new MongoStatusDetailIndicator is autoconfigured and regularly checks the availability of the
MongoDB. The indicator can be disabled by setting the property `edison.mongo.status.enabled=false`.
* **[edison-jobs]** JobEvents can broadcast a log message to all running jobs. 

## 1.0.1.RELEASE

**Bugfixes:**

* **[edison-core]** Fix reporting of http request count and time to Graphite
* **[edison-mongo]** Fixed display of mongo password in /internal/env

**New Features:**

* **[edison-core]** Allow LDAP authentication for user-defined paths.
* **[edison-mongo]** Add @ConditionalOnMissingBean to MongoTogglzRepository
* **[edison-mongo]** Make ID and ETAG constants public to be able to access them from outside
* **[edison-cache]** It is now possible to configure `CaffeineCache` instances as Spring beans and
use them using `@Cacheable`. For example, this way it is possible, to configure loading caches.
The new `example-cache` contains a showcase for this. This feature makes the 'old' `CacheRegistry`
interface obsolete.

**Deprecations:**

* **[edison-cache]** The `CacheRegistry` is now deprecated and will be removed in release 2.0.0.
* **[edison-mongo]** Deprecation of `edison.mongo.passwd`. Use `edison.mongo.password` instead to sanitize value in environment. 
The support for `edison.mongo.passwd` will be removed in 2.0.0

## 1.0.0.RC9

**New Features:**

* **[edison-jobs]** Add the cleanup strategy `DeleteSkippedJobs` to remove skipped jobs first. Configure with property
`edison.jobs.cleanup.number-of-skipped-jobs-to-keep`
* **[edison-mongo]** Extend @ConditionalOnMissingBean for mongoClient & mongoDatabase by name matcher

**Maintenance:**

* **[edison-*]** Add gradle task to determine possible dependency updates (see [README.md](README.md##dependency-update))
* **[edison-*]** Gradle test tasks now will generate a coverage report 
* **[edison-*]** Minor dependency updates

## 1.0.0.RC8

**Bugfixes:**

* **[edison-jobs]** Catching duplicate key exceptions in `JobMetaRepository.createValue`
* **[edison-jobs]** Fixed `@Value` annotation to configure the Mongo collections in `MongoJobsConfiguration`

**New Features:**

* **[edison-jobs]** Add a button to show the last runs of a job type

## 1.0.0.RC7

**New Features:**

* **[edison-jobs]** Introduced possibility to change the collection names used in MongoJob*Repositories using properties:
  * `edison.jobs.collection.jobinfo`: name of the collection used to store job information. Default value is `jobinfo`.
  * `edison.jobs.collection.jobmeta`: name of the collection used to store job meta information. Default value is `jobmeta`.

## 1.0.0.RC6

**Bugfixes:**

* **[edison-jobs]** Fixed: JobDefinitions-Page throws error with RC5

## 1.0.0.RC5

**Bugfixes:**

* **[edison-jobs]** Fixed bug in configuration of JobMutexGroups.
* **[edison-jobs]** Fixed missing `deleteAll()` in JobRepository.

**Breaking Changes:**

* **[edison-jobs]** Refactored JobRepository.

**New Features:**

* **[edison-jobs]** Introduced JobMetaService and JobMetaRepository to store meta data about jobs.
* **[edison-jobs]** Introduced MetaJobRunnable to make it easy to implement jobs having metadata like,
for example, import jobs that are keeping track of their last read position.
* **[edison-jobs]** Added the possibility to provide a comment and show it in the UI when disabling jobs.

## 1.0.0.RC4

**Bugfixes:**

* **[edison-core]** Fixed version conflict for Thymeleaf; Updated dependencies to Thymeleaf 3.
* **[edison-core]** Fixed broken links to jobs in status details JSON.
* **[edison-jobs]** Fix property name numberOfToKeep => numberOfJobsToKeep
* **[edison-jobs]** Removed MessageEvent.Level and replaced it by de.otto.edison.jobs.domain.Level.
* **[edison-jobs]** Sleep with retry delay before retry job execution.

**New Features:**

* **[edison-jobs]** Added new JobStatus SKIPPED for jobs that have been skipped because there was nothing to do.
JobRunnables can not call `jobEventPublisher.skipped()` to announce skipped jobs.
* **[edison-jobs]** Added buttons to retrigger jobs in the job UI

**Breaking Changes:**

* **[edison-jobs]** Refactored JobRepository.

## 1.0.0.RC3

**Bugfixes:**

* **[edison-mongo]** Fixed issue Unable to use MongoJobRepository


## 1.0.0.RC2

**Bugfixes:**

* **[edison-core]** Fixed issue Status page should always render `vcs.url`
* **[edison-jobs]** Fixed issue add `JobRepository.deleteAll()`

**New Features:**

* **[edison-core]** Added `@ConfigurationProperties MetricsProperties`
* **[edison-core]** Added `StatusDetail.getLinks()` and rendering hyperlinks on status pages. Job details are now using
links; this way you can directly jump from the status page to the job messages.
* **[edison-*]** Added annotations for Java Bean Validation to configuration properties.

## 1.0.0.RC1a

* **[edison-jobs]** Fixed bug that prevented the configuration of `edison.jobs.status.calculator`. 

## 1.0.0.RC1

First Release Candidate for Edison 1.0.0.

_**Beginning with 1.0.0, we will start using semantic versioning of releases.**_

_Because a couple of modules have been removed in this release, you should probably delete your existing project and clone the current version from scratch_

**Breaking Changes:**

* **[edison-*]** Refactored module structure: 
  * moved `edison-status`, `edison-health`, `edison-metrics`, `edison-microservice` and `?dison-servicediscovery-client`
    into `edison-core`.
  * moved `edison-jobs-mongo` into `edison-mongo`
  * moved `edison-togglz-mongo` into `edison-mongo`
  * moved `edison-togglz-testsupport` into `edison-testsupport`
* **[edison.*]** Removed remaining dependencies to guava library.
* **[edison.*]** Graceful shutdown is now disabled by default. Enable it by setting `edison.gracefulshutdown.enabled=true`.
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
* **[edison-jobs]** Renamed `edison.jobs.*` properties:
  * `edison.jobs.web.controller.enabled:true` -> `edison.jobs.external-trigger:true`
  * `edison.jobs.scheduler.thread-count:10` -> `edison.jobs.thread-count:10`
* **[edison-jobs]** The previous, now unsupported property `edison.jobs.status.indicate-joberror-with-level:ERROR` 
is replaced by setting  `edison.jobs.status.calculator.default=errorOnLastJobFailed`.

**Bugfixes:**

* **[edison-jobs]** Fixed broken link from job messages to /jobdefinitions/<jobType>. `JobDefinitionService.getJobDefition(jobType)`
is now case insensitive.
* **[edison-mongo]** Using `primaryPreferred` instead of `primary` to increase availability during master election.

**New Features:**

* **[edison-core]** Added feature to configure the entries of the navigation bar of /internal/* pages. See
`de.otto.edison navigation` for details and have a look at the `NavigationConfiguration` in the examples. 
* **[edison-core]** Added support to get status information for service that deployed using green/blue deployments. See
`ClusterInfo`, `ClusterInfoProperties` and `ClusterInfoConfiguration` for details.
* **[edison-core]** Introduced `@ConfigurationProperties and annotation-processor to provide auto-completion
 in application.property files.
  * ApplicationInfoProperties`
  * `TeamInfoProperties`
  * `VersionInfoProperties`
  * `ClusterInfoProperties`
  * `GracefulShutdownProperties`
  * `MetricsLoadProperties`
  * `ServiceRegistryProperties`
  * `MongoProperties`
  * `ToggzProperties`
  * `JobsProperties`
* **[edison-jobs]** `JobEvents` not `@Beta` anymore.
* **[edison-jobs]** Refactored `JobStatusDetailIndicator` to use a configurable `JobStatusCalculator` to map failed 
jobs to `StatusDetails`. 

  The application property `edison.jobs.status.calculator.default` is used to select one of the following 
  calculator strategies:
  * `warningOnLastJobFailed` the default, if nothing is configured. Reports a failed job as `Status.WARNING`
  * `errorOnLastJobFailed` Reports a failed job as `Status.ERROR`
  * `errorOnLastThreeJobsFailed` Reports a failed job as `Status.WARNING`, or `Status.ERROR` if the last three jobs 
  were failing. 
  * `errorOnLastTenJobsFailed` Reports a failed job as `Status.WARNING`, or `Status.ERROR` if the last ten jobs 
  were failing. 
* **[edison-jobs]** The new property-map `edison.jobs.status.calculator:` is used to configure the new 
JobStatusCalculator strategies (see above) for single job types (-> `JobDefinition.jobType()`). The job types are
case-insensitive, blanks are converted to `-`. 
  
  **Example**: A job type named `Delta Import` should use `errorOnLastThreeJobsFailed`, while all other jobs 
  should use `errorOnLastJobFailed`:

   * `edison.jobs.status.calculator.default = errorOnLastJobFailed`
   * `edison.jobs.status.calculator.delta-import = errorOnLastThreeJobsFailed`
* **[edison-mongo]** Added auto-configuration for `FeatureRepository` and `JobRepository`

## 0.82.2
* **[edision-mongo]** use version of mongo driver to 3.4.1, fongo 2.0.11

## 0.82.1
* **[edison-jobs]** add _primaryPreferred_ for jobRepository to avoid data loss while updating a jobState 

## 0.82.0
* **[edison-jobs]** Show only latest 10 jobs in Job Overview by default

## 0.81.0
* **[edison-mongo]** Allow configuration of mongo read preference by setting the property `edison.mongo.readPreference`. The default configuration is primary.

## 0.80.0
* **[edison-service]** Make edison-service independent of other edison packages
  - This might break your build because you did not write your project dependencies explicitly in
    your build script. Just add the missing edison packages and everything will be fine.
* **[edison-cache]** Allow CacheInfoController to be disabled

## 0.79.3
* **[edison-cache]** Allow registering custom built caches via `CacheRegistry` to gather cache metrics

## 0.79.2
* **[edison-mongo]** Bugfix: AbstractMongoRepository does not accept null as ID of objects anymore.

## 0.79.1
* **[edison.*]** Upgrade thymeleaf to version 3.0.2.RELEASE
* **[edison-jobs]** jobdetails page uses div tags instead of spans

## 0.79.0
* **[edison.*]** Upgrade spring boot to 1.4.2.RELEASE
* **[edison.*]** Upgrade spring version to 4.3.4.RELEASE
* **[edison.*]** Upgrade guava version to 20.0
* **[edison.*]** Upgrade caffeine version to 2.3.5
* **[edison.*]** Upgrade async http client to 1.9.40

## 0.78.0
* **[edison-mongo]** Support streaming for findAll methods. The old methods are deprecated now, so please use the new ones.

## 0.77.0
* **[edison-service]** Make edison-cache the default caching behaviour

## 0.76.1
* **[edison-jobs]** KeepLastJobs is not loading Job messages anymore, avoiding OutOfMemory errors
for lots of large jobs.

## 0.76.0
* **[edison-mongo]** Make AbstractRepository.byId() and AbstractRepository.matchAll() non final again
* **[edison-guava]** Make edison-guava deprecated in favor of the new and shiny edison-cache
* **[edison-cache]** Create edison-cache with support for Caffeine caches instead of Guava

## 0.75.0
* **[edison-jobs]**, **[edison-mongo]** Add setJobStatus and setLastUpdate to JobRepository interface and both
  implementations - This fixes a race condition during keepalive and message.
  This is a **breaking change** if you wrote your own JobRepository. 

## 0.74.0
* Removed dependency to edison-hal (was introduced for testing purposes only).
* Removed usage of testng and replaced it by Junit

## 0.73.0
* **[edison-mongo]** Refactored AbstractRepository.update() which returns an boolean now.
* **[edison-mongo]** **Breaking change:** UpdateIfMatch returns an enum (UpdateIfMatchResult) instead of throwing undocumented exceptions. To migrate, you have to remove the exception handling and evaluate the return code to handle it properly.
* **[edison-mongo]** Removed NotFoundException from edison-mongo
* **[edison-jobs]** Log job errors.

## 0.72.1
* Add some logging information

## 0.72.0
* Upgrade Spring Boot to 1.4.0-RELEASE
* Upgrade Spring to 4.3.2-RELEASE
* Add spring-boot-starter-test dependency for @SpringBootTest annotation

* IMPORTANT: Please follow the Migration Guide on
https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-1.4-Release-Notes
when upgrading an existing service to this Edison Microservice version

## 0.71.0
* Upgrade mongodb-driver to 3.3.0
* Upgrade togglz to 2.3.0.Final
* Reduce default shutdown period from 30 to 25 seconds

## 0.70.0
* do not show distinct jobs if type is given
* optimize createOrUpdate in AbstractMongoRepository by using upsert

## 0.69.1
* Job Overview shows distinct jobs as default (old behaviour can be activated via distinct=false parameter)
* Add possibility to add slf4j markers to jobevents 

## 0.69.1
* Status page header and title configurable via property `edison.status.application.title`
* Add time of start/stop to job status detail

## 0.69.0
* Add JobEvents class. Use it to avoid handing a JobEventPublisher to helper classes of a Job.
* Indicate job errors with configured mapping in application status (default is ERROR).
  The property name is `edison.jobs.status.indicate-joberror-with-level`, possible values are `OK`, `WARNING` and `ERROR`

## 0.68.1
* Hide feature toggles menu entry if feature toggles UI is disabled

## 0.68.0
* Introduced dependency to github.com/otto-de/edison-hal
* Added media type application/hal+json for /internal/status
* indicate job errors as application error instead of warning
* Catch and log RuntimeExceptions that may occur while persisting JobMessages and StateChanges

## 0.67.0
* Fix circular dependencies in JobConfiguration
* Update to latest Spring Boot version
* Update outdated dependencies

## 0.66.2
* Fix NullPointerException in disableJobs Feature

## 0.66.1
* Add possibility to temporarily disable jobs via the jobdefinitions GUI
* Fix ClearDeadLocks Cleanup: Also clear a lock if the JobId does not exist anymore.

## 0.65.0
* Add `ClearDeadLocks` Cleanup Strategy. 
* *BREAKING CHANGE* : `JobRepository` Add method `runningJobsDocument`. Changed signature of `markJobAsRunningIfPossible`

## 0.64.0
* Add parameter distinct=true|false to jobcontroller to get only the latest of each jobs in the overview

## 0.63.0
* Cronexpressions of JobDefinitions are evaluated on construction of a JobDefinition
* Bugfix: JobLocks are now properly released when Jobs are marked dead.
* Optimization of JobLocking.
* Breaking Change: `JobRepository` interface: add methods `markAsRunningIfPossible` and `clearRunningMark`. Delete unused methods.
* Breaking Change: `StopDeadJobs` constructor now needs a `JobService` and a `JobRepository`. Those can be obtained by autowiring.

## 0.62.0
* JobInfo is immutable, this is a breaking change, use the JobInfo.builder() instead

## 0.61.0
* Modified Mutex Behavior. The new MutexHandler is now able to use a mongoJobLockProvider which persists the
  mutex-handling in the database. That prevents raceconditions if two jobs of the same type or in the same group should
  be started at the same time.
* Fixed representation of feature toggles to be consistent with
  status mediatype.
* Moved html representation of feature toggles from /internal/togglz
  to /internal/toggles/console
* Moved json representation of feature toggles from /internal/status/togglz
  to /internal/toggles

## 0.60.0
* added MetricsFilter to provide counters for http requests

## 0.59.0
* upgrade spring boot to 1.3.5.RELEASE
* upgrade spring to 4.2.6.RELEASE

## 0.58.1
* edison-togglz: Fixed a bug in the LDAP authentication filter
* edison-togglz-mongo: Log name of the user switching a toggle also in the mongo state repository

## 0.58.0
* edison-togglz: added optional LDAP authentication

## 0.57.2
* Small bugfix: Fix job detail URL in jobs template

## 0.57.1
* set the /internal/jobs/ in the URL of the Location header

## 0.57.0
* The JobIds do not contain slashes any more, (/internal/jobs/ is not part of the jobIds)
* Note that jobs which were persisted with older releases might not be accessible any more after the upgrade.
* Also note that some jobs might show up with a red status on your dashboard after the upgrade. Simply rerun the job to
  fix this.

## 0.56.3
* togglz: FilterRegistrationBean in TogglzWebConfiguration uses filter name "togglzFilter"

## 0.56.2
* edison-jobs-(mongo): Moved handling of persisting error status to PersistenceJobEventListener

## 0.56.1
+ Bugfix: Do not aggregate application status in constructor. Use @PostConstruct instead

## 0.56.0
* New Feature: Mutually exclusion of jobs using JobMutexGroups

## 0.55.2
* JobDefinition show fixedDelay in seconds if minutes would be zero

## 0.55.1
* Set job status to error after receiving an error message
* Make mongo codec registry configurable

## 0.55.0
* Optimized handling of persisting job messages
* add serverSelectionTimeout (default 30secs) to mongo config

## 0.54.1
* JobStatusDetailIndicator: Indicate error if job could not be retrieved from the repository 

## 0.54.0
* Keep last jobs strategy now keeps the last N jobs of each type
* Create indices in jobs collection
* Return DeleteResult of mongo delete queries

## 0.53.1
* edison-jobs-mongo: Create index on jobtype and started to sort jobs by date when all job documents are bigger then 32MB  

## 0.52.2
* edison-metrics: Refactored auto configuration of `LoadDetector` to allow strategy selection by `application.properties` as well as exposing your own bean the same time

## 0.52.1
* Refactored load indication (see 0.52.0) from edison-status to edison-metrics and introduced new endpoint `/internal/load` which returns the load status to be used by consumers (ie. auto-scaling drivers). See `example-metrics` for an usage example on average response time behaviour.

## 0.52.0
* Added status indicator allowing the application to signal overload (see `LoadStatusIndicator`), which enables ie. watchers to autoscale the application accordingly
* two default strategies to discover load, one making use of metrics library, allowing to leverage `@Timed` annotations on your classes  (see `application.properties` in `example-status`)
* Fixed behaviour of application status aggregation to provide instant calculation instead of waiting until first update has ran

## 0.51.1
* Updated to Spring Boot 1.3.3 (with Spring 4.2.5)

## 0.51.0
* edison-guava: Refactored edison-guava. Guava caches can now be configured using GuavaCacheConfig. These caches will
now expose cache statistics as /internal/metrics (JSON) and /internal/caches/statistics (HTML). 
See edison-guava/README.md for more details on how to use Guava caches.
* edison-metrics: Fixed package name (...metrics instead of ...health)

## 0.50.3
* Housekeeping: Updated Spring Boot to version 1.3.2, MongoDB driver to 3.2.2 and Logback to 1.1.5

## 0.50.2
* edison-status: Added status to /internal/status.html

## 0.50.1
* edison-jobs: Bug-Fix in find implementations to limit search result after sorting has happened
* edison-jobs: Added method to find job instances by type and status

## 0.50.0
* Added InternalController that is redirecting requests from /internal to /internal/status. This can be disabled
by setting edison.status.redirect-internal.enabled=false

## 0.49.4
* Reverted: "removed unnecessary bean from togglz configuration" because embedded containers in Spring Boot don't scan web-fragments

## 0.49.3
* removed unnecessary bean from togglz configuration
* Extended `JobService` to cover also synchronous job execution

## 0.49.2
* Fix error on job page for long-running jobs
* Job Lifecycle: set hostname via SystemInfo (to overcome problems on Mesos)

## 0.49.1
* Added field `hostname` to JobInfo, which allows to track on which server the job gets executed
* Updated dependencies: async-http-client (to 1.9.32) and mongodb-driver (to 3.2.1)

## 0.49.0
* Removed unneeded appId from ServiceSpec and ApplicationInfo
* Fixed path in navigation from /internal to /internal/status
* Using edison.status.application.environment + .group instead of edison.servicediscovery.environment + .group.
The properties edison.servicediscovery.environment and edison.servicediscovery.group 
can be removed from application.properties/.yml

## 0.48.0
* VCS information are now expected in edison.status.vcs.* instead of info.build.*. Change in build.gradle required
* Added ServiceSpecs to be able to specify dependencies to other services
* Added TeamInfo to the /internal/status
* Added environment, group and appId to /internal/status

## 0.47.2
* Added convenience methods to `JobEventPublisher` to make logging more fun again
* Updated spring-boot to version 1.3.1.
* Update to org.springframework.boot:spring-boot-starter-thymeleaf:1.3.1.RELEASE required


## 0.47.1
* Fixed bug in navigation: broken path to bootstrap css

## 0.47.0
* Added some more information to /internal/status 
* Fixed the format of the status JSON
* Update to com.github.fakemongo:fongo:2.0.4 required

## 0.46.2
* Removed StateChangeEvent.State.CREATE, replaced it by START
* Renamed State.STILL_ALIVE to KEEP_ALIVE
* Job HTML templates are now using the Thymeleaf navigation fragments so ?Services have a common menu on all pages.

## 0.46.1
* Minor refactorings in handling of job events.

## 0.46.0
* Rebuild job state architecture due to introduce an event bus to separate the JobInfo and the
  JobRepository. From now on the JobRunner and the specific jobs will report changes (states and messages) to
  an EventPublisher, which will propagate the events to the JobEventListeners (e.g. persist them or log them).
  You can register your own JobEventListeners. The JobMonitor is removed.
* If your job has an error, you should throw a RuntimeException with an errorMessage. Then the job will be retriggered (if retry configured).

## 0.45.3
* Fixed default JobMonitor to save log messages in JobRepository for every message.

## 0.45.2
* Minor bugfixes
* Using Guava CacheStats in CacheStatistics

## 0.45.1

**New Features:**
* Show the runtime of the job to identify the complete exports with actions.
* Format details in status.json to camel case key names

## 0.45.0

**New Features:**
* Broken Jobs are automatically restarted according to the number of retries specified in the `JobDefintion`.
* `JobDefinition` now has a new field `restarts`, specifying how often a job is restarted if it failed because of an error.

**Breaking Changes:**
* Renamed libraries: all libs now have the prefix "edison-".
* The factory methods of the `DefaultJobDefinition` now have an additional parameter for the number of restarts.
* Every `JobRunnable` now needs to provide a `JobDefinition`
* Removed `JobRunnable#getJobType`. The job type is accessed by the `JobDefinition`
* Renamed to `DefaultJobDefinition#notTriggerableJobDefinition` to `manuallyTriggerableJobDefinition` to clarify purpose
* Removed hystrix module

## 0.44.0

**New Features:**
* MongoDB persistence: Allow to retrieve the object (including key) when it is created.
  Changed signature of `create` and  `createOrUpdate` in `AbstractMongoRepository`
  to return the object instead of void

**Bugfixes:**
* Allow also a value without key to be given to `AbstractMongoRepository#createOrUpdate` without throwing a NPE
* `application.properties`: renamed ``edison.application.name`` (introduced in Release 0.35.0) back into `spring.application.name`
  (see [ContextIdApplicationContextInitializer](https://github.com/spring-projects/spring-boot/blob/v1.3.0.RELEASE/spring-boot/src/main/java/org/springframework/boot/context/ContextIdApplicationContextInitializer.java)
  for details on identifying your application)
* Added dummy feature toggle implementations (for `FeatureClassProvider`) to example projects, which can now
  be run on its own again (ie. `gradle example-jobs:bootRun`)


## 0.43.0
* Updated spring-boot to version [1.3.0](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-1.3-Release-Notes), hystrix to 1.4.21 and metrics library dependency to 3.1.3

## 0.40.0
* Update togglz library to 2.2.0

## 0.39.2
* Cleaned up link construction on internal status and job pages
* Timestamps in JSON status representation are now in ISO-8601 date format (including timezone)

## 0.39.1
* Fixed possible broken links on /internal pages

## 0.39.0
* Added servicediscovery-client to edison-microservice
* ServiceDiscovery is only used if edison.servicediscovery.* properties are available

## 0.38.0
* JobDefinitions extended to support jobs that are not triggered automatically.

## 0.37.0
* Fixed links on internal pages

## 0.35.0

**New Features:**
* New library servicediscovery-client introduced to provide basic ServiceDiscovery features for Edison Microservices.
This lib may be used to register services at an Edison JobTrigger.

**Bugfixes:**
* Fixed possible ConcurrentModificationException if job messages are accessed from JobInfo.

**Breaking Changes:**
* application.properties is now using server.context-path instead of server.contextPath.
* application.properties is now using edison.application.name instead of application.name.


## 0.34.0
* Added ID to panel with job messages for identification in JobTrigger

## 0.33.0
* Added UIs for Status pages
* Nicer internal pages, including reworked jobs and job-definitions

## 0.29.0

**New Features:**
* Prepared integration with (upcoming) Edison JobTrigger
* Added Bootstrap UIs for Jobs and /internal
* Added edison-service for typical microservices, including jobs, health, status,
* Using webjars in edison-service to include bootstrap and jquery

**Breaking Changes:**
* The format of job definitions and jobs has changed.

## 0.28.2

**Bugfixes:**
* Fixed critical bug that prevented the startup of the server in combination with usage of edison jobs-mongo

## 0.28.1

**Bugfixes:**
* Fixed bugs that prevented the StatusDetails of jobs to be added to status page.

## 0.28.0

**New Features:**
* Jobs are rendered with absolute URLs in JSON and HTML representations.
* Introduced the (optional) concept of JobDefinitions to describe the expected frequency etc. to trigger jobs.
* Based on the JobDefinitions, for every Job a StatusDetailIndicator is registered automatically. This may be
disabled by either not providing a JobDefinition, or by setting property edison.jobs.status.enabled=false

**Breaking Changes:**
* Jobs in earlier version where identified by the URI of the job including the servlet context path. Starting with
this version, the context path is not part of the identifier anymore. You should DELETE all old jobs, otherwise
the URLs a broken.
* Job representations now contain the full URL of the jobs instead of relative URIs.
