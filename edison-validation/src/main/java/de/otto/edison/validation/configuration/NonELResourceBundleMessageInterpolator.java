package de.otto.edison.validation.configuration;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class NonELResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {

    private static final Logger LOG = LoggerFactory.getLogger(NonELResourceBundleMessageInterpolator.class);

    public NonELResourceBundleMessageInterpolator(ResourceBundleLocator resourceBundleLocator) {
        super(resourceBundleLocator);
    }

    @Override
    public String interpolate(Context context, Locale locale, String term) {
        if (InterpolationTerm.isElExpression(term)) {
            LOG.warn("Message contains EL expression: {}, which is not supported by the selected message interpolator", term);
            return term;
        } else {
            ParameterTermResolver parameterTermResolver = new ParameterTermResolver();
            return parameterTermResolver.interpolate(context, term);
        }
    }
}
