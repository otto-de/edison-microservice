# Release History

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


