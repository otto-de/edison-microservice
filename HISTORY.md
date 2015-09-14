# Release History

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


