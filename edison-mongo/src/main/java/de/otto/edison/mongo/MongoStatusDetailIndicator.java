package de.otto.edison.mongo;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonList;

@Component
@ConditionalOnBean(MongoDatabase.class)
@ConditionalOnProperty(prefix = "edison.mongo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MongoStatusDetailIndicator implements StatusDetailIndicator {

    private final MongoDatabase mongoDatabase;

    @Autowired
    public MongoStatusDetailIndicator(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public List<StatusDetail> statusDetails() {
        String databaseStatusName = "MongoDB Status";
        Document document = new Document().append("ping", 1);
        Document answer;
        try {
            answer = mongoDatabase.runCommand(document);
        } catch (MongoTimeoutException e) {
            return singletonList(
                    statusDetail(databaseStatusName, ERROR, "Mongo database check ran into timeout (" + e.getMessage() + ").")
            );
        } catch (Exception other) {
            return singletonList(
                    statusDetail(databaseStatusName, ERROR, "Exception during database check (" + other.getMessage() + ").")
            );
        }

        if (answer != null && answer.get("ok") != null && (Double)answer.get("ok") == 1.0d) {
            return singletonList(
                    statusDetail(databaseStatusName, OK, "Mongo database is reachable.")
            );
        }

        return singletonList(
                statusDetail(databaseStatusName, ERROR, "Mongo database unreachable or ping command failed.")
        );
    }
}
