# Edison Dynamodb

DynamoDB persistence for Edison Microservices.  

## Usage
 
 *PENDING*

## DynamodbStatusDetailIndicator

A StatusDetailIndicator is autoconfigured and regularly checks the availability of the DynamoDB. The indicator can be
disabled by setting the property `edison.dynamodb.status.enabled=false`.

## DynamodbJobRepository

If edison-dynamodb is configured appropriately and edison-jobs is in the classpath, a dynamoDB implementation
of the JobRepository is automatically configured.

This provides persistence for Jobs, so job information can be gathered in clustered environments.

## DynamodbTogglzRepository

Similar to dynamodbJobRepository, a dynamodb-implementation of a Togglz StateRepository is auto-configured, if
edison-togglz is used in addition to edison-dynamodb.
