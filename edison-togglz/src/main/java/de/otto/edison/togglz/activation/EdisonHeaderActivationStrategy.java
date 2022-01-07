package de.otto.edison.togglz.activation;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

public class EdisonHeaderActivationStrategy implements ActivationStrategy {

    static final String ID = "edisonHeader";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "edisonHeader";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        HttpServletRequest request = getRequest();
        if (featureState == null || featureState.getFeature() == null) {
            return false;
        }
        String header = request.getHeader("X-Features");
        if (header == null) {
            return false;
        }
        String[] split = header.split(",");
        return Arrays.stream(split).anyMatch(feature -> feature.equals(featureState.getFeature().name()));
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    private static HttpServletRequest getRequest() {
        final ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.requireNonNull(sra).getRequest();
    }
}
