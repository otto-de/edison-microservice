package de.otto.edison.togglz

import org.togglz.core.manager.FeatureManager
import org.togglz.core.spi.FeatureManagerProvider

class KFeatureManagerProvider: FeatureManagerProvider {

    override fun getFeatureManager(): FeatureManager? {
        return instance
    }

    override fun priority(): Int {
       return 8
    }

    companion object {
        var instance: FeatureManager? = null

    }

}