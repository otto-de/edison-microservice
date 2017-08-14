package de.otto.edison.dynamodb;

import static de.otto.edison.status.domain.Status.OK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;

@Component
@ConditionalOnProperty(prefix = "edison.dynamo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
@Beta
public class DynamoStatusDetailIndicator implements StatusDetailIndicator {

  private final AmazonDynamoDB dynamoDB;

  @Autowired
  public DynamoStatusDetailIndicator(final AmazonDynamoDB dynamoDB) {
    this.dynamoDB = dynamoDB;
  }

  @Override
  public StatusDetail statusDetail() {
    dynamoDB.listTables();
    final String databaseStatusName = "DynamoDB Status";
    return StatusDetail.statusDetail(databaseStatusName, OK, "Dynamo database is reachable.");
  }
}
