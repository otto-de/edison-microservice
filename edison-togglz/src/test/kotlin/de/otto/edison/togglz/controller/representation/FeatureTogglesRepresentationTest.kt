package de.otto.edison.togglz.controller.representation

import de.otto.edison.togglz.*
import de.otto.edison.togglz.controller.FeatureToggleRepresentation
import de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class FeatureTogglesRepresentationTest {
    @Test
    fun testGetFeatureRepresentationForKotlinEnumClass() {

        //given
        KFeatureManagerSupport.allEnabledFeatureConfig(KotlinTestFeatures::class)

        //when
        val togglzRepresentation = togglzRepresentation { FeatureEnum::class.java }

        //then
        val features = togglzRepresentation.features
        val fooRepresentation: FeatureToggleRepresentation? = features["FOO"]
        fooRepresentation!!.enabled shouldBe true

        val barRepresentation: FeatureToggleRepresentation? = features["BAR"]
        barRepresentation!!.enabled shouldBe true
    }

    @AfterEach
    internal fun tearDown() {
        KFeatureManagerProvider.instance = null
    }
}