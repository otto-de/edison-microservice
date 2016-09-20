package de.otto.edison.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void postConstruct() {
        ensureIndexes();
    }

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
        Document doc = encode(value);
        collection().replaceOne(byId(key), doc, new UpdateOptions().upsert(true));
        return decode(doc);
    }

    public V create(final V value) {
        Document doc = encode(value);
        collection().insertOne(doc);
        return decode(doc);
    }

    /**
     * Updates the document if it is already present in the repository.
     *
     * @param value the new value
     * @return true, if the document was updated, false otherwise.
     */
    public boolean update(final V value) {
        final K key = keyOf(value);
        return collection()
                .replaceOne(byId(key), encode(value))
                .getModifiedCount() == 1;
    }

    /**
     * Updates the document if the document's ETAG is matching the given etag (conditional put).
     * <p>
     *     Using this method requires that the document contains an "etag" field that is updated if
     *     the document is changed.
     * </p>
     *
     * @param value the new value
     * @param eTag the etag used for conditional update
     * @return true, if the document was updated, false otherwise.
     */
    public boolean updateIfMatch(final V value, final String eTag) {
        final Bson query = and(eq(AbstractMongoRepository.ID, keyOf(value)), eq(ETAG, eTag));
        final Document updatedETaggable = collection().findOneAndReplace(query, encode(value), new FindOneAndReplaceOptions().returnDocument(AFTER));
        return updatedETaggable != null;
    }

    public long size() {
        return collection().count();
    }

    /**
     * Deletes the document identified by key.
     * @param key the identifier of the deleted document
     * @return DeleteResult
     */
    public DeleteResult delete(final K key) {
        return collection().deleteOne(byId(key));
    }

    /**
     * Deletes all documents from this repository.
     *
     * @return DeleteResult
     */
    public DeleteResult deleteAll() {
        return collection().deleteMany(matchAll());
    }

    /**
     * Returns a query that is selecting documents by ID.
     *
     * @param key the document's key
     * @return query Document
     */
    protected final Document byId(final K key) {
        return key != null ? new Document(ID, key.toString()) : new Document();
    }

    /**
     * Returns a query that is selecting all documents.
     *
     * @return query Document
     */
    protected final Document matchAll() {
        return new Document();
    }

    /**
     * @return the MongoCollection used by this repository to store {@link Document documents}
     */
    protected abstract MongoCollection<Document> collection();

    /**
     * Returns the key / identifier from the given value.
     *
     * @param value the value
     * @return key
     */
    protected abstract K keyOf(final V value);

    /**
     * Encode a value into a MongoDB {@link Document}.
     *
     * @param value the value
     * @return Document
     */
    protected abstract Document encode(final V value);

    /**
     * Decode a MongoDB {@link Document} into a value.
     *
     * @param document the Document
     * @return V
     */
    protected abstract V decode(final Document document);

    /**
     * Ensure that the MongoDB indexes required by the repository do exist.
     * <p>
     *     This method is called once after startup of the application.
     * </p>
     */
    protected abstract void ensureIndexes();
}
