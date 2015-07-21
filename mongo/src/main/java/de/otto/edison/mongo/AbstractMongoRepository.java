package de.otto.edison.mongo;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class AbstractMongoRepository<K, V> {

    private static final String ID = "_id";

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

    public void createOrUpdate(final V value) {
        final K key = keyOf(value);
        final Document existing = collection().find(byId(key)).first();
        if (existing != null) {
            collection().replaceOne(byId(key), encode(value));
        } else {
            collection().insertOne(encode(value));
        }
    }

    public void create(final V value) {
        collection().insertOne(encode(value));
    }

    public void update(final V value) {
        final K key = keyOf(value);
        collection().replaceOne(byId(key), encode(value));
    }

    public long size() {
        return collection().count();
    }

    public void clear() {
        collection().deleteMany(matchAll());
    }

    protected Document byId(final K key) {
        return new Document(ID, key.toString());
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
