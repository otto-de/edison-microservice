package de.otto.edison.dynamodb.togglz;

import org.springframework.stereotype.Component;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Component
public class TestUserProvider implements UserProvider {

    @Override
    public FeatureUser getCurrentUser() {
        return new SimpleFeatureUser("admin-user", true);
    }
}
