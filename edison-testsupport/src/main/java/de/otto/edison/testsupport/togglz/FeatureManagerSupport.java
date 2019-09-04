package de.otto.edison.testsupport.togglz;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import static org.togglz.core.context.FeatureContext.clearCache;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

public class FeatureManagerSupport {

    public static void allEnabledFeatureConfig(final Class<? extends Feature> featureClass) {
        TestFeatureManager featureManager = new TestFeatureManager(featureClass);
        TestFeatureManagerProvider.setFeatureManager(featureManager);
        allEnabledFeatureConfig(featureManager);
    }

    public static void allEnabledFeatureConfig(FeatureManager featureManager) {
        enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(featureManager);
        clearCache();
    }

    public static void allDisabledFeatureConfig(final Class<? extends Feature> featureClass) {
        TestFeatureManager featureManager = new TestFeatureManager(featureClass);
        TestFeatureManagerProvider.setFeatureManager(featureManager);
        allDisabledFeatureConfig(featureManager);
    }

    public static void allDisabledFeatureConfig(FeatureManager featureManager) {
        featureManager.getFeatures().forEach(feature -> {
            featureManager.setFeatureState(new FeatureState(feature, false));
        });
        clearCache();
    }

    public static void disable(final Feature feature) {
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(feature, false));
    }

    private static void enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(final FeatureManager featureManager) {
        featureManager.getFeatures().forEach(feature -> {
            if (shouldRunInTests(featureManager, feature)) {
                featureManager.setFeatureState(new FeatureState(feature, true));
            }
        });
    }

    public static boolean shouldRunInTests(Feature feature) {
        return shouldRunInTests(getFeatureManager(), feature);
    }

    private static boolean shouldRunInTests(FeatureManager featureManager, Feature feature) {
        return !featureManager.getMetaData(feature).getLabel().contains("[inactiveInTests]");
    }

    public static void enable(final Feature feature) {
        getFeatureManager().setFeatureState(new FeatureState(feature, true));
    }

}
