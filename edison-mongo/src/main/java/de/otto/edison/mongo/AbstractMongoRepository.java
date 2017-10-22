package de.otto.edison.mongo;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

import static de.otto.edison.mongo.UpdateIfMatchResult.CONCURRENTLY_MODIFIED;
import static de.otto.edison.mongo.UpdateIfMatchResult.NOT_FOUND;
import static de.otto.edison.mongo.UpdateIfMatchResult.OK;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.CountOptions;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.UpdateOptions;

public abstract class AbstractMongoRepository<K, V> {

    public static final String ID = "_id";
    public static final String ETAG = "etag";

    private static final boolean DISABLE_PARALLEL_STREAM_PROCESSING = false;
    protected final MongoProperties mongoProperties;

    public AbstractMongoRepository(final MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @PostConstruct
    public void postConstruct() {
        ensureIndexes();
    }

    /**
     * Find a single value with the specified key, if existing.
     *
     * @param key the key to search for
     * @return an Optional containing the requested value, or {@code Optional.empty()} if no value with this key exists
     */
    public Optional<V> findOne(final K key) {
        return findOne(key, mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * Find a single value with the specified key, if existing.
     *
     * @param key      the key to search for
     * @param maxTime  the maximum time for this request
     * @param timeUnit the time unit in which {@code maxTime} is specified
     * @return an Optional containing the requested value, or {@code Optional.empty()} if no value with this key exists
     */
    public Optional<V> findOne(final K key, final long maxTime, final TimeUnit timeUnit) {
        return ofNullable(collection()
                .find(byId(key))
                .maxTime(maxTime, timeUnit)
                .map(this::decode)
                .first());
    }

    /**
     * Convert given {@link Iterable} to a standard Java8-{@link Stream}.
     * The {@link Stream} requests elements from the iterable in a lazy fashion as they will usually,
     * so p.e. passing <code>collection().find()</code> as parameter will not result in the
     * whole collection being read into memory.
     * <p>
     * Parallel processing of the iterable is not used.
     *
     * @param iterable any {@link Iterable}
     * @param <T>      the type of elements returned by the iterator
     * @return a {@link Stream} wrapping the given {@link Iterable}
     */
    protected static <T> Stream<T> toStream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), DISABLE_PARALLEL_STREAM_PROCESSING);
    }

    public Stream<V> findAllAsStream() {
        return findAllAsStream(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    public Stream<V> findAllAsStream(final long maxTime, final TimeUnit timeUnit) {
        return toStream(
                collection().find()
                        .maxTime(maxTime, timeUnit))
                .map(this::decode);
    }

    public List<V> findAll() {
        return findAll(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    public List<V> findAll(final long maxTime, final TimeUnit timeUnit) {
        return findAllAsStream(maxTime, timeUnit).collect(toList());
    }

    public Stream<V> findAllAsStream(final int skip, final int limit) {
        return findAllAsStream(skip, limit, mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    public Stream<V> findAllAsStream(final int skip, final int limit, final long maxTime, final TimeUnit timeUnit) {
        return toStream(
                getFindIterable(skip, limit)
                        .maxTime(maxTime, timeUnit))
                .map(this::decode);
    }

    private FindIterable<Document> getFindIterable(int skip, int limit) {
        return collection()
                .find()
                .skip(skip)
                .limit(limit);
    }

    public List<V> findAll(final int skip, final int limit) {
        return findAll(skip, limit, mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    public List<V> findAll(final int skip, final int limit, final long maxTime, final TimeUnit timeUnit) {
        return findAllAsStream(skip, limit, maxTime, timeUnit).collect(toList());
    }

    public V createOrUpdate(final V value) {
        return createOrUpdate(value, mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    public V createOrUpdate(final V value, final long maxTime, final TimeUnit timeUnit) {
        final Document doc = encode(value);
        collectionWithWriteTimeout(maxTime, timeUnit)
                .replaceOne(byId(keyOf(value)), doc, new UpdateOptions().upsert(true));
        return decode(doc);
    }

    protected MongoCollection<Document> collectionWithWriteTimeout(long maxTime, TimeUnit timeUnit) {
        return collection()
                .withWriteConcern(collection().getWriteConcern().withWTimeout(maxTime, timeUnit));
    }

    public V create(final V value) {
        return create(value, mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    public V create(final V value, final long maxTime, final TimeUnit timeUnit) {
        final K key = keyOf(value);
        if (key != null) {
            final Document doc = encode(value);
            collectionWithWriteTimeout(maxTime, timeUnit).insertOne(doc);
            return decode(doc);
        } else {
            throw new NullPointerException("Key must not be null");
        }
    }

    /**
     * Updates the document if it is already present in the repository.
     *
     * @param value the new value
     * @return true, if the document was updated, false otherwise.
     */
    public boolean update(final V value) {
        return update(value, mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * Updates the document if it is already present in the repository.
     *
     * @param value    the new value
     * @param maxTime  max time for the update
     * @param timeUnit the time unit for the maxTime value
     * @return true, if the document was updated, false otherwise.
     */
    public boolean update(final V value, final long maxTime, final TimeUnit timeUnit) {
        final K key = keyOf(value);
        if (key != null) {
            return collectionWithWriteTimeout(maxTime, timeUnit)
                    .replaceOne(byId(key), encode(value))
                    .getModifiedCount() == 1;
        } else {
            throw new IllegalArgumentException("Key must not be null");
        }
    }

    /**
     * Updates the document if the document's ETAG is matching the given etag (conditional put).
     * <p>
     * Using this method requires that the document contains an "etag" field that is updated if
     * the document is changed.
     * </p>
     *
     * @param value the new value
     * @param eTag  the etag used for conditional update
     * @return {@link UpdateIfMatchResult}
     */
    public UpdateIfMatchResult updateIfMatch(final V value, final String eTag) {
        return updateIfMatch(value, eTag, mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * Updates the document if the document's ETAG is matching the given etag (conditional put).
     * <p>
     * Using this method requires that the document contains an "etag" field that is updated if
     * the document is changed.
     * </p>
     *
     * @param value    the new value
     * @param eTag     the etag used for conditional update
     * @param maxTime  max time for the update
     * @param timeUnit the time unit for the maxTime value
     * @return {@link UpdateIfMatchResult}
     */
    public UpdateIfMatchResult updateIfMatch(final V value,
                                             final String eTag,
                                             final long maxTime,
                                             final TimeUnit timeUnit) {
        final K key = keyOf(value);
        if (key != null) {
            final Bson query = and(eq(AbstractMongoRepository.ID, key), eq(ETAG, eTag));

            final Document updatedETaggable = collectionWithWriteTimeout(maxTime, timeUnit).findOneAndReplace(query, encode(value), new FindOneAndReplaceOptions().returnDocument(AFTER));
            if (isNull(updatedETaggable)) {
                final boolean documentExists = collection()
                        .count(eq(AbstractMongoRepository.ID, key), new CountOptions().maxTime(maxTime, timeUnit)) != 0;
                if (documentExists) {
                    return CONCURRENTLY_MODIFIED;
                }

                return NOT_FOUND;
            }

            return OK;
        } else {
            throw new IllegalArgumentException("Key must not be null");
        }
    }

    public long size() {
        return size(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS);
    }

    public long size(final long maxTime, final TimeUnit timeUnit) {
        return collection().count(new BsonDocument(), new CountOptions().maxTime(maxTime, timeUnit));
    }

    /**
     * Deletes the document identified by key.
     *
     * @param key the identifier of the deleted document
     */
    public void delete(final K key) {
        delete(key, mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * Deletes the document identified by key.
     *
     * @param key      the identifier of the deleted document
     * @param maxTime  max time for the operation
     * @param timeUnit the time unit for the maxTime value
     */
    public void delete(final K key, final long maxTime, final TimeUnit timeUnit) {
        collectionWithWriteTimeout(maxTime, timeUnit).deleteOne(byId(key));
    }

    /**
     * Deletes all documents from this repository.
     */
    public void deleteAll() {
        deleteAll(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * Deletes all documents from this repository.
     * @param maxTime  max time for the operation
     * @param timeUnit the time unit for the maxTime value
     */
    public void deleteAll(final long maxTime, final TimeUnit timeUnit) {
        collectionWithWriteTimeout(maxTime, timeUnit).deleteMany(matchAll());
    }

    /**
     * Returns a query that is selecting documents by ID.
     *
     * @param key the document's key
     * @return query Document
     */
    protected Document byId(final K key) {
        if (key != null) {
            return new Document(ID, key.toString());
        } else {
            throw new NullPointerException("Key must not be null");
        }
    }

    /**
     * Returns a query that is selecting all documents.
     *
     * @return query Document
     */
    protected Document matchAll() {
        return new Document();
    }

    /**
     * @return the MongoCollection used by this repository to store {@link Document documents}
     */
    protected abstract MongoCollection<Document> collection();

    /**
     * Returns the key / identifier from the given value.
     * <p>
     * The key of a document must never be null.
     * </p>
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
     * This method is called once after startup of the application.
     * </p>
     */
    protected abstract void ensureIndexes();
}
