package de.otto.edison.mongo;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;

@Component
@ConditionalOnProperty(prefix = "edison.mongo.status", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MongoStatusDetailIndicator implements StatusDetailIndicator {

    private final MongoDatabase mongoDatabase;

    @Autowired
    public MongoStatusDetailIndicator(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public StatusDetail statusDetail() {
        String databaseStatusName = "MongoDB Status";
        Document document = new Document().append("ping", 1);
        Document answer;
        try {
            answer = mongoDatabase.runCommand(document);
        } catch (MongoTimeoutException e) {
            return StatusDetail.statusDetail(databaseStatusName, ERROR, "Mongo database check ran into timeout (" + e.getMessage() + ").");
        } catch (Exception other) {
            return StatusDetail.statusDetail(databaseStatusName, ERROR, "Exception during database check (" + other.getMessage() + ").");
        }

        if (answer != null && answer.get("ok") != null && (Double)answer.get("ok") == 1.0d) {
            return StatusDetail.statusDetail(databaseStatusName, OK, "Mongo database is reachable.");
        }

        return StatusDetail.statusDetail(databaseStatusName, ERROR, "Mongo database unreachable or ping command failed.");
    }
}
