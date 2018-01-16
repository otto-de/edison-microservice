# Edison Jobs

Simple addon library to support background jobs for Edison Microservices.

## About

This library offers the possibility to execute background jobs like, for example, importing data from other services
on a regular basis.

Job information can automatically be persisted in a MongoDB if you use edison-mongo. Otherwise they are only persisted 
in memory. In clustered environments (multiple instances of a single service) it is generally a good idea to use some
kind of persistence. 

It is possible to persist job information in different data stores. In this case, a `JobRepository` must be implemented 
and exposed as a Spring Bean.

Beside of starting a job programmatically via the `Jobservice` you can also use the graphical user interface which 
comes with this library. Links to this UI are automatically added to the service´s /internal pages menu bar if
edison-jobs is added to the classpath.

The scheduling of the jobs is not part of this framework. External triggering is implemented by 
[Edison JobTrigger](https://github.com/otto-de/edison-jobtrigger), but also internal triggers are easy to implement
using Spring's @EnableScheduling and @Scheduled annotations. 

For the usage of edison-jobs take a look at example-jobs.

## Usage

*PENDING*

### JobMutexHandler
You can define JobMutex-Groups to define, that certain jobs may not be executed, while other specific jobs are running.

To define a mutex group you need to define a bean of type JobMutexGroup