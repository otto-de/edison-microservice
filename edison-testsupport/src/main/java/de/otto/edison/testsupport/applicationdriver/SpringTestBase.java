package de.otto.edison.testsupport.applicationdriver;

import de.otto.edison.testsupport.TestServer;
import org.springframework.context.ApplicationContext;

public class SpringTestBase {

    static {
        TestServer.main(new String[0]);
    }

    public static ApplicationContext applicationContext() {
        return TestServer.applicationContext();
    }
}
