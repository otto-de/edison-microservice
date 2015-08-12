package de.otto.edison.mongo.etag;

import com.mongodb.client.model.FindOneAndReplaceOptions;
import de.otto.edison.mongo.AbstractMongoRepository;
import de.otto.edison.mongo.NotFoundException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.util.StringUtils;

import java.util.ConcurrentModificationException;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static java.util.Objects.isNull;

public abstract class AbstractEtaggableMongoRepository<K, V extends ETaggable> extends AbstractMongoRepository<K, V> {

    protected static final String ETAG = "etag";

    public V updateIfETagMatch(final V value) {
        Bson query = queryWithOrWithoutETag(value);

        Document updatedETaggable = collection().findOneAndReplace(query, encode(value.copyAndAddETag()), new FindOneAndReplaceOptions().returnDocument(AFTER));
        if (isNull(updatedETaggable)) {
            Optional<V> findById = findOne(keyOf(value));
            if (findById.isPresent()) {
                throw new ConcurrentModificationException("Entity concurrently modified: " + keyOf(value));
            }

            throw new NotFoundException("Entity does not exist: " + keyOf(value));
        }

        return decode(updatedETaggable);
    }

    private Bson queryWithOrWithoutETag(final V value) {
        if (!StringUtils.isEmpty(value.getETag())) {
            return and(eq("_id", keyOf(value)), eq(ETAG, value.getETag()));
        }
        return eq("_id", keyOf(value));
    }

    public V createWithETag(final V value) {
        super.create(value.copyAndAddETag());
        return findOne(keyOf(value)).get();
    }

    @Override
    public void create(final V value) {
        throw new UnsupportedOperationException("this is not supported if you use eTags: Please use createWithETag instead");
    }

    @Override
    public void createOrUpdate(final V value) {
        throw new UnsupportedOperationException("this is not supported if you use eTags: Please use updateIfETagMatch or createWithETag instead");
    }

    @Override
    public void update(final V value) {
        throw new UnsupportedOperationException("this is not supported if you use eTags: Please use updateIfETagMatch instead");
    }
}
