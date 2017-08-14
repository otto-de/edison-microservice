package de.otto.edison.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import de.otto.edison.status.domain.StatusDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.otto.edison.status.domain.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DynamoStatusDetailIndicatorTest {

    @Mock
    private AmazonDynamoDB dynamoClient;

    @InjectMocks
    private DynamoStatusDetailIndicator dynamoStatusDetailIndicator;

    @Test
    public void shouldReturnOKStatus() throws Exception {
        //when
        final StatusDetail statusDetail = dynamoStatusDetailIndicator.statusDetail();
        //then
        assertThat(statusDetail.getStatus(), is(OK));
    }

    @Test(expected = RuntimeException.class)
    public void shouldReturnErrorStatusWhenDatabaseThrowsException() throws Exception {
        //given
        when(dynamoClient.listTables()).thenThrow(new RuntimeException());
        //when
        dynamoStatusDetailIndicator.statusDetail();
    }
}