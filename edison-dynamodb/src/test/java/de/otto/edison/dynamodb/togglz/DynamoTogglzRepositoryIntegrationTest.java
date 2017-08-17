package de.otto.edison.dynamodb.togglz;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.togglz.core.repository.FeatureState;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = "de.otto.edison.dynamodb")
@EnableAutoConfiguration
@ActiveProfiles("test")
public class DynamoTogglzRepositoryIntegrationTest {

    @Autowired
    private DynamoTogglzRepository dynamoTogglzRepository;

    @Before
    public void setUp() {
        dynamoTogglzRepository.createTable();
        dynamoTogglzRepository.deleteAll();
    }

    @Test
    public void shouldLoadFeatureState() throws Exception {
        // Given
        final FeatureState featureState = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState.setEnabled(true);
        featureState.setStrategyId("someStrategy");
        featureState.setParameter("someKey1", "someValue1");
        featureState.setParameter("someKey2", "someValue2");
        dynamoTogglzRepository.create(featureState);

        // When
        final FeatureState loadedFeatureState = dynamoTogglzRepository.getFeatureState(TestFeatures.TEST_FEATURE_1);

        // Then
        assertThat(loadedFeatureState.getFeature(), is(TestFeatures.TEST_FEATURE_1));
        assertThat(loadedFeatureState.getStrategyId(), is("someStrategy"));
        assertThat(loadedFeatureState.isEnabled(), is(true));
        assertThat(loadedFeatureState.getParameter("someKey1"), is("someValue1"));
        assertThat(loadedFeatureState.getParameter("someKey2"), is("someValue2"));
    }

    @Test
    public void shouldSetFeatureState() throws Exception {
        // Given
        final FeatureState featureState = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState.setEnabled(true);
        featureState.setStrategyId("someStrategy");
        featureState.setParameter("someKey1", "someValue1");
        featureState.setParameter("someKey2", "someValue2");

        // When
        dynamoTogglzRepository.setFeatureState(featureState);
        final Optional<FeatureState> loadedFeatureState = dynamoTogglzRepository.findOne(TestFeatures.TEST_FEATURE_1.name());

        // Then
        assertThat(loadedFeatureState.isPresent(), is(true));
        assertThat(loadedFeatureState.get().getFeature(), is(TestFeatures.TEST_FEATURE_1));
        assertThat(loadedFeatureState.get().getStrategyId(), is("someStrategy"));
        assertThat(loadedFeatureState.get().isEnabled(), is(true));
        assertThat(loadedFeatureState.get().getParameter("someKey1"), is("someValue1"));
        assertThat(loadedFeatureState.get().getParameter("someKey2"), is("someValue2"));
    }

    @Test
    public void shouldLoadAllFeatureStates() throws Exception {
        // Given
        final FeatureState featureState1 = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState1.setEnabled(true);
        featureState1.setStrategyId("someStrategy");
        featureState1.setParameter("someKey1", "someValue1");
        featureState1.setParameter("someKey2", "someValue2");
        dynamoTogglzRepository.create(featureState1);

        final FeatureState featureState2 = new FeatureState(TestFeatures.TEST_FEATURE_2);
        featureState2.setEnabled(true);
        featureState2.setStrategyId("someStrategy2");
        featureState2.setParameter("someKey3", "someValue3");
        featureState2.setParameter("someKey4", "someValue4");
        dynamoTogglzRepository.create(featureState2);

        // When
        final List<FeatureState> loadedFeatureStates = dynamoTogglzRepository.findAll();

        // Then
        assertThat(loadedFeatureStates.size(), is(2));
    }
}
