package de.otto.edison.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;
import static java.util.Collections.singletonList;

@Repository
public class TestRepository extends AbstractDynamoRepository<TestObject> {

    private final AmazonDynamoDB dynamoClient;

    public TestRepository(final AmazonDynamoDB dynamoClient) {
        this.dynamoClient = dynamoClient;
    }

    void createTable() {
        if (!dynamoClient.listTables().getTableNames().contains(table().getTableName())) {
            final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
            provisionedThroughput.setReadCapacityUnits(1000L);
            provisionedThroughput.setWriteCapacityUnits(1000L);
            dynamoClient.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
                    singletonList(new KeySchemaElement(getKeyFieldName(), HASH)),
                    provisionedThroughput);
        }
    }

    @Override
    protected String tableName() {
        return "test";
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
