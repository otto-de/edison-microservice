package de.otto.edison.mongo;

import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.status.domain.StatusDetail;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static de.otto.edison.status.domain.Status.ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import static de.otto.edison.status.domain.Status.OK;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MongoStatusDetailIndicatorTest {

    @Mock
    private MongoDatabase mongoDatabase;

    private MongoStatusDetailIndicator testee;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        testee = new MongoStatusDetailIndicator(mongoDatabase);
    }

    @Test
    public void shouldReturnOKStatus() throws Exception {
        //given
        when(mongoDatabase.runCommand(new Document().append("ping", 1))).thenReturn(new Document().append("ok", 1.0d));
        //when
        StatusDetail statusDetail = testee.statusDetails().get(0);
        //then
        assertThat(statusDetail.getStatus(), is(OK));
    }

    @Test
    public void shouldReturnErrorStatusWhenDatabaseDoesntReturnOKForPing() throws Exception {
        //given
        when(mongoDatabase.runCommand(new Document().append("ping", 1))).thenReturn(new Document().append("error", 1.0d));
        //when
        StatusDetail statusDetail = testee.statusDetails().get(0);
        //then
        assertThat(statusDetail.getStatus(), is(ERROR));
        assertThat(statusDetail.getMessage(), containsString("Mongo database unreachable or ping command failed."));
    }

    @Test
    public void shouldReturnErrorStatusWhenDatabaseTimesOut() {
        //given
        when(mongoDatabase.runCommand(new Document().append("ping", 1))).thenThrow(new MongoTimeoutException("Timeout"));
        //when
        StatusDetail statusDetail = testee.statusDetails().get(0);
        //then
        assertThat(statusDetail.getStatus(), is(ERROR));
        assertThat(statusDetail.getMessage(), containsString("Mongo database check ran into timeout"));
    }

    @Test
    public void shouldReturnErrorStatusOnAnyException() {
        //given
        when(mongoDatabase.runCommand(new Document().append("ping", 1))).thenThrow(new MongoException("SomeException"));
        //when
        StatusDetail statusDetail = testee.statusDetails().get(0);
        //then
        assertThat(statusDetail.getStatus(), is(ERROR));
        assertThat(statusDetail.getMessage(), containsString("Exception during database check"));
    }
}