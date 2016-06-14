# edison-microservice:edison-jobs

jobs library of edison-microservice.

## Status

UNSTABLE

## About

This library offers the possibility to execute jobs.
These jobs my be persisted if you use edison-jobs-mongo. Otherwise they are only persisted in memory.
Beside starting a job via the Job service you can also use the graphical user interface which comes with this framework
The scheduling of the jobs is not part of this framework.

For the usage of edison-jobs take a look at example-jobs.

## Documentation

### JobMutexHandler
You can define JobMutex-Groups to define, that certain jobs may not be executed, while other specific jobs are running.

To define a mutex group you need to define a bean of type JobMutexGroup