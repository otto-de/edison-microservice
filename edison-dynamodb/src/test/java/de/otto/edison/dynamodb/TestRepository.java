package de.otto.edison.dynamodb;

import static java.util.Collections.singletonList;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

@Repository
public class TestRepository extends AbstractDynamoRepository<TestObject> {

  private final AmazonDynamoDB dynamoDB;
  private final Table table;

  public TestRepository(final AmazonDynamoDB dynamoDB) {
    this.dynamoDB = dynamoDB;
    this.table = new DynamoDB(dynamoDB).getTable("test");
  }

  public void createTable() {
    if (!dynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
      provisionedThroughput.setReadCapacityUnits(1000L);
      provisionedThroughput.setWriteCapacityUnits(1000L);
      dynamoDB.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
        singletonList(new KeySchemaElement(getKeyFieldName(), HASH)),
        provisionedThroughput);
    }
  }

  public void deleteTable() {
    if (dynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      dynamoDB.deleteTable(table().getTableName());
    }
  }

  @Override
  protected Table table() {
    return this.table;
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
