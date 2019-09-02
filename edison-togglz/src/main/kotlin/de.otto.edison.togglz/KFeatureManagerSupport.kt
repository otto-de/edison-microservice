package de.otto.edison.togglz

import org.togglz.core.Feature
import org.togglz.core.context.FeatureContext.clearCache
import org.togglz.core.context.FeatureContext.getFeatureManager
import org.togglz.core.manager.FeatureManager
import org.togglz.core.manager.FeatureManagerBuilder
import org.togglz.core.repository.FeatureState
import kotlin.reflect.KClass

object KFeatureManagerSupport {

    fun allEnabledFeatureConfig(featureClass: KClass<out Enum<*>>) {
        val featureManager = initFeatureManager(featureClass)
        enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(featureManager)
        clearCache()
    }

    fun initFeatureManager(featureClass: KClass<out Enum<*>>): FeatureManager {
        val featureManager = createFeatureManager(featureClass)
        KFeatureManagerProvider.instance = featureManager
        return featureManager
    }

    fun allDisabledFeatureConfig(featureClass: KClass<out Enum<*>>) {
        val featureManager = initFeatureManager(featureClass)
        for (feature in featureManager.features) {
            featureManager.setFeatureState(FeatureState(feature, false))
        }
        clearCache()
    }

    private fun createFeatureManager(featureClass: KClass<out Enum<*>>): FeatureManager {
        return FeatureManagerBuilder.begin()
                .featureProvider(EnumClassFeatureProvider(featureClass.java))
                .build()
    }

    fun enable(feature: Feature) {
        getFeatureManager().setFeatureState(FeatureState(feature, true))
    }

    fun disable(feature: Feature) {
        getFeatureManager().setFeatureState(FeatureState(feature, false))
    }

    private fun enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(featureManager: FeatureManager) {
        for (feature in featureManager.features) {
            if (shouldRunInTests(feature, featureManager)) {
                featureManager.setFeatureState(FeatureState(feature, true))
            }
        }
    }

    fun shouldRunInTests(feature: Feature, featureManager: FeatureManager): Boolean {
        val label = featureManager.getMetaData(feature).label
        return !label.contains("[inactiveInTests]")
    }

}
