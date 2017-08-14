package de.otto.edison.dynamodb;

import static java.util.stream.Collectors.toList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"de.otto.edison.dynamodb"})
@EnableAutoConfiguration
@ActiveProfiles("test")
public class AbstractDynamoRepositoryIntegrationTest {

  @Autowired
  private TestRepository testRepository;

  @Before
  public void setUp() {
    testRepository.createTable();
    testRepository.deleteAll();
  }

  @Test
  public void shouldUpdateExistingDocument() throws Exception {
    // given
    final String testId = "someId";
    final String updatedValue = "someUpdatedValue";

    final TestObject testObject = new TestObject(testId, "someValue");
    testRepository.createOrUpdate(testObject);

    final TestObject testObjectToUpdate = new TestObject(testId, updatedValue);

    // when
    final TestObject updatedObject = testRepository.createOrUpdate(testObjectToUpdate);

    // then
    assertThat(updatedObject, is(testObjectToUpdate));

    final TestObject foundObject = testRepository.findOne(testId).get();
    assertThat(foundObject.geteTag(), notNullValue());
    assertThat(foundObject.geteTag(), is(not(testObject.geteTag())));
    assertThat(foundObject.getId(), is(testId));
    assertThat(foundObject.getValue(), is(updatedValue));
  }

  @Test
  public void shouldNotUpdateMissingDocument() throws Exception {
    // given
    final TestObject testObjectToUpdate = new TestObject("someId", "someUpdatedValue");

    // when
    final boolean result = testRepository.update(testObjectToUpdate);

    // then
    assertThat(result, is(false));
    assertThat(testRepository.findOne("someId").isPresent(), is(false));
  }

  @Test(expected = AmazonDynamoDBException.class)
  public void shouldNotCreateOrUpdateWithMissingId() throws Exception {
    // given
    final TestObject testObject = new TestObject(null, "someValue");

    // when
    testRepository.createOrUpdate(testObject);
  }

  @Test(expected = AmazonDynamoDBException.class)
  public void shouldNotCreateWithMissingId() throws Exception {
    // given
    final TestObject testObject = new TestObject(null, "someValue");

    // when
    testRepository.create(testObject);
  }

  @Test(expected = AmazonDynamoDBException.class)
  public void shouldNotFindOneWithMissingId() throws Exception {
    // when
    testRepository.findOne(null);
  }

  private void createTestObjects(final String... values) {
    Arrays.stream(values)
      .map(value -> new TestObject(value, value))
      .forEach(testRepository::create);
  }

  @Test
  public void shouldFindAllEntries() {
    final String[] inputValues = {"testObject01", "testObject02", "testObject03", "testObject04", "testObject05", "testObject06"};
    createTestObjects(inputValues);

    // when
    final List<String> foundValues = testRepository.findAll().stream()
      .map(TestObject::getValue)
      .collect(toList());

    // then
    assertThat(foundValues, hasSize(6));
    assertThat(foundValues, containsInAnyOrder(inputValues));
  }

  @Test
  public void shouldStreamAllEntries() {
    final String[] inputValues = {"testObject01", "testObject02", "testObject03", "testObject04", "testObject05", "testObject06"};
    createTestObjects(inputValues);

    // when
    final List<String> foundValues = testRepository.findAllAsStream()
      .map(TestObject::getValue)
      .collect(toList());

    // then
    assertThat(foundValues, hasSize(6));
    assertThat(foundValues, containsInAnyOrder(inputValues));
  }

  @Test
  public void shouldFindAllEntriesByPagingKey() {
    final String[] inputValues = {"testObject01", "testObject02", "testObject03", "testObject04", "testObject05", "testObject06"};
    createTestObjects(inputValues);

    // when
    final List<TestObject> firstThreeFoundObjects = testRepository.findAllAsStream().limit(3).collect(toList());
    final TestObject thirdFoundObject = firstThreeFoundObjects.get(firstThreeFoundObjects.size() - 1);
    final List<TestObject> lastThreeFoundObjects = testRepository.findAllAsStream(thirdFoundObject.getId()).collect(toList());

    final List<TestObject> allFoundObjects = new ArrayList<>();
    allFoundObjects.addAll(firstThreeFoundObjects);
    allFoundObjects.addAll(lastThreeFoundObjects);

    final List<String> allFoundValues = allFoundObjects.stream()
      .map(TestObject::getId)
      .collect(toList());

    // then
    assertThat(allFoundValues, hasSize(6));
    assertThat(allFoundValues, containsInAnyOrder(inputValues));
  }
}
