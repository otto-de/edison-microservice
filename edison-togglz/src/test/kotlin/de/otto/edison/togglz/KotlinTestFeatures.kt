package de.otto.edison.togglz;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.togglz.core.annotation.EnabledByDefault;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

enum class KotlinTestFeatures {
    @EnabledByDefault
    FOO,

    @Label("bar feature")
    BAR;

    fun isActive(): Boolean {
        return FeatureContext.getFeatureManager().isActive { name }
    }
}

@Configuration
open class FeatureProviderConfiguration {

    @Bean
    open fun featureProvider() = EnumClassFeatureProvider(KotlinTestFeatures::class.java)
}
