package de.otto.edison.testsupport.togglz;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.util.FeatureAnnotations;

import static org.togglz.core.context.FeatureContext.clearCache;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

public class FeatureManagerSupport {

    public static void allEnabledFeatureConfig(final Class<? extends Feature> featureClass) {
        TestFeatureManager featureManager = new TestFeatureManager(featureClass);
        enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(featureClass,featureManager);
        TestFeatureManagerProvider.setFeatureManager(featureManager);
        clearCache();
    }

    public static void allDisabledFeatureConfig(final Class<? extends Feature> featureClass) {
        TestFeatureManager featureManager = new TestFeatureManager(featureClass);
        for (Feature feature : featureClass.getEnumConstants()) {
                featureManager.disable(feature);
        }
        TestFeatureManagerProvider.setFeatureManager(featureManager);
        clearCache();
    }


    public static void disable(final Feature feature) {
        FeatureContext.getFeatureManager().setFeatureState(new FeatureState(feature, false));
    }

     private static void enableAllFeaturesThatAreOkToEnableByDefaultInAllTests(final Class<? extends Feature> featureClass, final TestFeatureManager featureManager) {
        for (Feature feature : featureClass.getEnumConstants()) {
            if (shouldRunInTests(feature)) {
                featureManager.enable(feature);
            }
        }
    }

    public static boolean shouldRunInTests(Feature feature) {
        String label = FeatureAnnotations.getLabel(feature);
        return !label.contains("[inactiveInTests]");
    }

    public static void enable(final Feature feature) {
        getFeatureManager().setFeatureState(new FeatureState(feature, true));
    }

}
