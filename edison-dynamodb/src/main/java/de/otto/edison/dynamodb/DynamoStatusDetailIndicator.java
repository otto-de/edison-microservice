package de.otto.edison.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static de.otto.edison.status.domain.Status.OK;

@Component
@ConditionalOnProperty(prefix = "edison.dynamo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
@Beta
public class DynamoStatusDetailIndicator implements StatusDetailIndicator {

    private final AmazonDynamoDB dynamoClient;

    @Autowired
    public DynamoStatusDetailIndicator(final AmazonDynamoDB dynamoClient) {
        this.dynamoClient = dynamoClient;
    }

    @Override
    public StatusDetail statusDetail() {
        dynamoClient.listTables();
        final String databaseStatusName = "DynamoDB Status";
        return StatusDetail.statusDetail(databaseStatusName, OK, "Dynamo database is reachable.");
    }
}
