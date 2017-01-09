# Edison Mongo

MongoDB persistence for Edison Microservices.  

## Usage
 
 *PENDING*
 
## MongoJobRepository

If edison-mongo is configured appropriately and edison-jobs is in the classpath, a MongoDB implementation
of the JobRepository is automatically configured.

This provides persistence for Jobs, so job information can be gathered in clustered environments.

## MongoTogglzRepository

Similar to MongoJobRepository, a Mongo-implementation of a Togglz StateRepository is auto-configured, if
edison-togglz is used in addition to edison-mongo.
