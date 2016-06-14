package de.otto.edison.togglz.repository.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.togglz.FeatureClassProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class MongoFeatureRepositoryTest {

    private MongoFeatureRepository testee;

    @BeforeMethod
    public void setUp() throws Exception {
        Fongo fongo = new Fongo("inmemory-mongodb");
        MongoDatabase database = fongo.getDatabase("features");
        FeatureClassProvider featureClassProvider = new TestFeatureClassProvider();
        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.getCurrentUser()).thenReturn(new SimpleFeatureUser("someUser"));
        testee = new MongoFeatureRepository(database, featureClassProvider, userProvider);
    }

    @Test
    public void shouldLoadFeatureState() throws Exception {
        // Given
        FeatureState featureState = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState.setEnabled(true);
        featureState.setStrategyId("someStrategy");
        featureState.setParameter("someKey1", "someValue1");
        featureState.setParameter("someKey2", "someValue2");
        testee.create(featureState);

        // When
        FeatureState loadedFeatureState = testee.getFeatureState(TestFeatures.TEST_FEATURE_1);

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
        FeatureState featureState = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState.setEnabled(true);
        featureState.setStrategyId("someStrategy");
        featureState.setParameter("someKey1", "someValue1");
        featureState.setParameter("someKey2", "someValue2");

        // When
        testee.setFeatureState(featureState);
        Optional<FeatureState> loadedFeatureState = testee.findOne(TestFeatures.TEST_FEATURE_1.name());

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
        FeatureState featureState1 = new FeatureState(TestFeatures.TEST_FEATURE_1);
        featureState1.setEnabled(true);
        featureState1.setStrategyId("someStrategy");
        featureState1.setParameter("someKey1", "someValue1");
        featureState1.setParameter("someKey2", "someValue2");
        testee.create(featureState1);

        FeatureState featureState2 = new FeatureState(TestFeatures.TEST_FEATURE_2);
        featureState2.setEnabled(true);
        featureState2.setStrategyId("someStrategy2");
        featureState2.setParameter("someKey3", "someValue3");
        featureState2.setParameter("someKey4", "someValue4");
        testee.create(featureState2);

        // When
        List<FeatureState> loadedFeatureStates = testee.findAll();

        // Then
        assertThat(loadedFeatureStates.size(), is(2));
    }
}
