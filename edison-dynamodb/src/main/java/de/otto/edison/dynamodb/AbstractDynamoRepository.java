package de.otto.edison.dynamodb;

import de.otto.edison.annotations.Beta;
import software.amazon.awssdk.services.dynamodb.document.Item;
import software.amazon.awssdk.services.dynamodb.document.ItemCollection;
import software.amazon.awssdk.services.dynamodb.document.ScanOutcome;
import software.amazon.awssdk.services.dynamodb.document.Table;
import software.amazon.awssdk.services.dynamodb.document.spec.PutItemSpec;
import software.amazon.awssdk.services.dynamodb.document.spec.ScanSpec;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Beta
public abstract class AbstractDynamoRepository<V> {

    private static final boolean DISABLE_PARALLEL_STREAM_PROCESSING = false;

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

    public Optional<V> findOne(final String key) {
        final Item item = table().getItem(getKeyFieldName(), key);
        if (item != null) {
            return Optional.of(decode(item));
        }
        return Optional.empty();
    }

    public List<V> findAll() {
        return findAllAsStream().collect(toList());
    }

    public Stream<V> findAllAsStream(final String lastKey) {
        final ItemCollection<ScanOutcome> scanResult = table().scan(new ScanSpec().withExclusiveStartKey(getKeyFieldName(), lastKey));
        return toStream(scanResult).map(this::decode);
    }

    public Stream<V> findAllAsStream() {
        return toStream(table().scan()).map(this::decode);
    }

    public V createOrUpdate(final V value) {
        table().putItem(encode(value));
        return value;
    }

    public boolean update(final V value) {
        final PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(encode(value))
                .withConditionExpression("attribute_exists(" + getKeyFieldName() + ")");

        try {
            table().putItem(putItemSpec);
        } catch (final ConditionalCheckFailedException conditionalCheckFailedException) {
            return false;
        }
        return true;
    }

    public V create(final V value) {
        final PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(encode(value))
                .withConditionExpression("attribute_not_exists(" + getKeyFieldName() + ")");

        table().putItem(putItemSpec);
        return value;
    }

    public long size() {
        return findAllAsStream().count();
    }

    /**
     * Deletes the document identified by key.
     *
     * @param key the identifier of the deleted document
     */
    public void delete(final String key) {
        table().deleteItem(getKeyFieldName(), key);
    }

    /**
     * Deletes all documents from this repository.
     */
    public void deleteAll() {
        findAllAsStream().forEach(item -> delete(keyOf(item)));
    }

    protected abstract Table table();

    /**
     * Returns the key / identifier from the given value.
     * <p>
     * The key of a document must never be null.
     * </p>
     *
     * @param value the value
     * @return key
     */
    protected abstract String keyOf(final V value);

    protected abstract Item encode(final V value);

    protected abstract V decode(final Item item);

    protected abstract String getKeyFieldName();
}
