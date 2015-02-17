package de.otto.µservice.testsupport.applicationdriver;

import de.otto.µservice.testsupport.TestServer;
import org.springframework.context.ApplicationContext;

public class SpringTestBase {

    static {
        TestServer.main(new String[0]);
    }

    public static ApplicationContext applicationContext() {
        return TestServer.applicationContext();
    }
}
