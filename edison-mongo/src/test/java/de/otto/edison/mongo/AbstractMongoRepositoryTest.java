package de.otto.edison.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

@Test
public class AbstractMongoRepositoryTest {

    private TestRepository testee;

    @BeforeMethod
    public void setUp() throws Exception {
        Fongo fongo = new Fongo("inmemory-mongodb");
        MongoDatabase database = fongo.getDatabase("db");

        testee = new TestRepository(database);
    }

    @Test
    public void shouldUpdateExistingDocument() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");
        testee.create(testObject);

        TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue");

        // when
        final boolean updated = testee.update(testObjectToUpdate);

        // then
        assertThat(updated, is(true));
        final TestObject updatedTestObject = testee.findOne("someId").get();
        assertThat(updatedTestObject.eTag, notNullValue());
        assertThat(updatedTestObject.eTag, is(not(testObject.eTag)));
        assertThat(updatedTestObject.id, is("someId"));
        assertThat(updatedTestObject.value, is("someUpdatedValue"));
    }

    @Test
    public void shouldNotUpdateMissingDocument() throws Exception {
        // given

        TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue");

        // when
        final boolean updated = testee.update(testObjectToUpdate);

        // then
        assertThat(updated, is(false));
        assertThat(testee.findOne("someId").isPresent(), is(false));
    }

    @Test
    public void shouldUpdateIfETagMatch() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");
        testee.create(testObject);

        String etagFromCreated = testee.findOne("someId").get().eTag;
        TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue", etagFromCreated);

        // when
        final boolean updated = testee.updateIfMatch(testObjectToUpdate, etagFromCreated);
        TestObject updatedTestObject = testee.findOne("someId").get();

        // then
        assertThat(updated, is(true));
        assertThat(updatedTestObject.eTag, notNullValue());
        assertThat(updatedTestObject.eTag, is(not(etagFromCreated)));
        assertThat(updatedTestObject.id, is("someId"));
        assertThat(updatedTestObject.value, is("someUpdatedValue"));
    }

    @Test
    public void shouldNotUpdateIfEtagNotMatch() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue", "someEtagWhichIsNotInTheDb");
        testee.create(testObject);

        // when
        final boolean updated = testee.updateIfMatch(testObject, "someOtherETag");

        // then
        assertThat(updated, is(false));
    }

    @Test
    public void shouldNotUpdateIfEtagNotExists() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");

        // when
        final boolean updated = testee.updateIfMatch(testObject, "someETag");

        // then
        assertThat(updated, is(false));
    }

    @Test
    public void shouldCreateOrUpdateWithMissingId() throws Exception {
        // given
        TestObject testObject = new TestObject(null, "someValue");

        // when
        TestObject resultingObject = testee.createOrUpdate(testObject);

        // then
        assertThat(resultingObject, notNullValue());
        assertThat(resultingObject.eTag, notNullValue());
    }

    @Test
    public void shouldCreateWithMissingId() throws Exception {
        // given
        TestObject testObject = new TestObject(null, "someValue");

        // when
        TestObject resultingObject = testee.create(testObject);

        // then
        assertThat(resultingObject, notNullValue());
        assertThat(resultingObject.eTag, notNullValue());
    }


    // ~~

    public class TestRepository extends AbstractMongoRepository<String, TestObject> {

        private final MongoCollection<Document> collection;

        public TestRepository(MongoDatabase database) {
            this.collection = database.getCollection("test");
        }

        @Override
        protected MongoCollection<Document> collection() {
            return collection;
        }

        @Override
        protected String keyOf(TestObject value) {
            return value.id;
        }

        @Override
        protected Document encode(TestObject value) {
            Document document = new Document();

            document.append("_id", value.id);
            document.append("etag", UUID.randomUUID().toString());
            document.append("value", value.value);

            return document;
        }

        @Override
        protected TestObject decode(Document document) {
            return new TestObject(
                    document.getString("_id"),
                    document.getString("value"),
                    document.getString("etag")
            );
        }

        @Override
        protected void ensureIndexes() {
        }
    }

    public class TestObject {

        private String id;
        private String value;

        private String eTag;

        protected TestObject(String id, String value, String eTag) {
            this.eTag = eTag;
            this.id = id;
            this.value = value;
        }

        protected TestObject(String id, String value) {
            this(id, value, null);
        }

    }
}
