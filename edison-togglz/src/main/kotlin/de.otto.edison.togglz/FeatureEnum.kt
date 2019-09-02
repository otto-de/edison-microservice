package de.otto.edison.togglz

import org.togglz.core.Feature

data class FeatureEnum(private val featureEnum: Enum<*>) : Feature {

    override fun name() = featureEnum.name
}