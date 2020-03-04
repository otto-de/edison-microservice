package de.otto.edison.jobs.repository.dynamo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

abstract class AbstractDynamoRepository {

    final DynamoDbClient dynamoDbClient;
    final String tableName;

    AbstractDynamoRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    void deleteEntriesPerBatch(List<WriteRequest> deleteRequests) {
        final int chunkSize = 25;
        final AtomicInteger counter = new AtomicInteger();
        final Collection<List<WriteRequest>> deleteRequestsSplittedByChunkSize = deleteRequests.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize))
                .values();

        deleteRequestsSplittedByChunkSize.forEach
                (currentDeleteRequests -> dynamoDbClient.batchWriteItem(
                        BatchWriteItemRequest.builder().requestItems(
                                ImmutableMap.of(tableName, currentDeleteRequests)).build()));

    }
}
