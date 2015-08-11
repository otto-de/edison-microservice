package de.otto.edison.mongo.etag;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.NotFoundException;
import org.bson.Document;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

@Test
public class AbstractEtaggableMongoRepositoryTest {

    private TestRepository testee;

    @BeforeMethod
    public void setUp() throws Exception {
        Fongo fongo = new Fongo("inmemory-mongodb");
        MongoDatabase database = fongo.getDatabase("db");

        testee = new TestRepository(database);
    }

    @Test
    public void shouldUpdateIfETagMatch() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");
        testee.createWithETag(testObject);

        String etagFromCreated = testee.findOne("someId").get().getETag();
        TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue", etagFromCreated);

        // when
        TestObject updatedTestObject = testee.updateIfETagMatch(testObjectToUpdate);

        // then
        assertThat(updatedTestObject.eTag, notNullValue());
        assertThat(updatedTestObject.eTag, is(not(etagFromCreated)));
        assertThat(updatedTestObject.id, is("someId"));
        assertThat(updatedTestObject.value, is("someUpdatedValue"));
    }

    @Test
    public void shouldUpdateWithoutEtagButGenerateIt() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");
        TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue");
        testee.createWithETag(testObject);

        // when
        TestObject updatedTestObject = testee.updateIfETagMatch(testObjectToUpdate);
        Optional<TestObject> campaignAfterUpdate = testee.findOne("someId");

        // then
        assertThat(updatedTestObject.eTag, notNullValue());
        assertThat(updatedTestObject.id, is("someId"));
        assertThat(updatedTestObject.value, is("someUpdatedValue"));
    }

    @Test(expectedExceptions = ConcurrentModificationException.class)
    public void shouldNotUpdateIfEtagNotMatch() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue", "someEtagWhichIsNotInTheDb");
        testee.createWithETag(testObject);

        // when
        try {
            testee.updateIfETagMatch(testObject);
            Assert.fail();
        } catch (ConcurrentModificationException e) {
            // then
            throw e;
        }
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldNotUpdateIfCampaignNotExists() throws Exception {
        // given
        TestObject testObject = new TestObject("someId", "someValue");

        // when
        try {
            testee.updateIfETagMatch(testObject);
            Assert.fail();
        } catch (NotFoundException e) {
            // then
            throw e;
        }
    }

    public class TestRepository extends AbstractEtaggableMongoRepository<String, TestObject> {

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
            document.append("etag", value.eTag);
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

    public class TestObject extends ETaggable {

        private String id;
        private String value;

        protected TestObject(String id, String value, String eTag) {
            super(eTag);
            this.id = id;
            this.value = value;
        }

        protected TestObject(String id, String value) {
            this(id, value, null);
        }

        @Override
        public TestObject copyAndAddETag() {
            return new TestObject(id, value, createETag());
        }
    }
}
