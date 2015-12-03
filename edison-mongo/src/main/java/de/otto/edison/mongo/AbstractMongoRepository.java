package de.otto.edison.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

public abstract class AbstractMongoRepository<K, V> {

    protected static final String ID = "_id";
    protected static final String ETAG = "etag";

    public Optional<V> findOne(final K key) {
        return ofNullable(collection()
                .find(byId(key))
                .map(this::decode)
                .first());
    }

    public List<V> findAll() {
        return collection()
                .find()
                .map(this::decode)
                .into(new ArrayList<>());
    }

    public List<V> findAll(int skip, int limit) {
        return collection()
                .find()
                .skip(skip)
                .limit(limit)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    public V createOrUpdate(final V value) {
        final K key = keyOf(value);
        final Document existing = collection().find(byId(key)).first();
        Document doc = encode(value);
        if (existing != null) {
            collection().replaceOne(byId(key), doc);
        } else {
            collection().insertOne(doc);
        }
        return decode(doc);
    }

    public V create(final V value) {
        Document doc = encode(value);
        collection().insertOne(doc);
        return decode(doc);
    }

    public void update(final V value) {
        final K key = keyOf(value);
        collection().replaceOne(byId(key), encode(value));
    }

    public void updateIfMatch(final V value, final String eTag) {
        Bson query = and(eq(AbstractMongoRepository.ID, keyOf(value)), eq(ETAG, eTag));

        Document updatedETaggable = collection().findOneAndReplace(query, encode(value), new FindOneAndReplaceOptions().returnDocument(AFTER));
        if (isNull(updatedETaggable)) {
            Optional<V> findById = findOne(keyOf(value));
            if (findById.isPresent()) {
                throw new ConcurrentModificationException("Entity concurrently modified: " + keyOf(value));
            }

            throw new NotFoundException("Entity does not exist: " + keyOf(value));
        }
    }

    public long size() {
        return collection().count();
    }

    public void delete(final K key) {
        collection().deleteOne(byId(key));
    }

    public void deleteAll() {
        collection().deleteMany(matchAll());
    }

    protected Document byId(final K key) {
        return key != null ? new Document(ID, key.toString()) : new Document();
    }

    protected Document matchAll() {
        return new Document();
    }

    protected abstract MongoCollection<Document> collection();

    protected abstract K keyOf(final V value);

    protected abstract Document encode(final V value);

    protected abstract V decode(final Document document);

    protected abstract void ensureIndexes();
}
