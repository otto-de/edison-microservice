package de.otto.edison.togglz

import io.kotlintest.shouldBe
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.togglz.core.Feature

@ExtendWith(MockKExtension::class)
internal class KFeatureManagerSupportTest {

    @BeforeEach
    internal fun setUp() {
        KFeatureManagerSupport.initFeatureManager(KotlinTestFeatures::class)
    }

    @Test
    internal fun `should change toggle state after enable`() {
        KFeatureManagerSupport.allEnabledFeatureConfig(KotlinTestFeatures::class)

        KotlinTestFeatures.BAR.isActive() shouldBe true
        KotlinTestFeatures.FOO.isActive() shouldBe true

        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should change toggle state after disable`() {
        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.BAR.name })

        KotlinTestFeatures.BAR.isActive() shouldBe false

        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.BAR.name })
        KotlinTestFeatures.BAR.isActive() shouldBe true
    }

    @Test
    internal fun `should enable all toggles`() {
        //given
        KFeatureManagerSupport.disable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe false

        //when
        KFeatureManagerSupport.allEnabledFeatureConfig(KotlinTestFeatures::class)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe true }
    }

    @Test
    internal fun `should disable all toggles`() {
        //given
        KFeatureManagerSupport.enable(Feature { KotlinTestFeatures.FOO.name })
        KotlinTestFeatures.FOO.isActive() shouldBe true

        //when
        KFeatureManagerSupport.allDisabledFeatureConfig(KotlinTestFeatures::class)

        //then
        KotlinTestFeatures.values().forEach { it.isActive() shouldBe false }
    }

    @AfterEach
    internal fun tearDown() {
        KFeatureManagerProvider.instance = null
    }
}