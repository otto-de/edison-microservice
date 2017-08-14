package de.otto.edison.dynamodb;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.DynamoDb;
import software.amazon.awssdk.services.dynamodb.document.Item;
import software.amazon.awssdk.services.dynamodb.document.Table;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import java.util.UUID;

import static software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMapperFieldModel.DynamoDbAttributeType.S;
import static software.amazon.awssdk.services.dynamodb.model.KeyType.HASH;

@Repository
public class TestRepository extends AbstractDynamoRepository<TestObject> {

    private final DynamoDBClient dynamoClient;
    private final Table table;

    public TestRepository(final DynamoDBClient dynamoClient) {
        this.dynamoClient = dynamoClient;
        table = new DynamoDb(dynamoClient).getTable("test");
    }

    void createTable() {
        if (!dynamoClient.listTables().tableNames().contains(table().getTableName())) {
            dynamoClient.createTable(CreateTableRequest.builder()
                    .tableName(table().getTableName())
                    .keySchema(KeySchemaElement.builder().attributeName(getKeyFieldName()).keyType(HASH).build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(1000L)
                            .writeCapacityUnits(1000L)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(getKeyFieldName())
                            .attributeType(S.name())
                            .build())
                    .build());
        }
    }

    @Override
    protected Table table() {
        return table;
    }

    @Override
    protected String keyOf(final TestObject value) {
        return value.getId();
    }

    @Override
    protected Item encode(final TestObject testObject) {
        return new Item()
                .withPrimaryKey("id", testObject.getId())
                .withString("eTag", UUID.randomUUID().toString())
                .with("value", testObject.getValue());
    }

    @Override
    protected TestObject decode(final Item item) {
        return new TestObject(item.getString("id"), item.getString("value"), item.getString("eTag"));
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }
}
