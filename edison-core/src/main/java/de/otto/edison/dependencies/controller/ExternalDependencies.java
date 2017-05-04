package de.otto.edison.dependencies.controller;

import de.otto.edison.dependencies.domain.ExternalDependency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A component used to access all available ExternalDependency objects.
 * <p>
 *     This class is mostly used for testing purposes, as it helps to mock the
 *     list of ExternalDependency objects.
 * </p>
 */
@Component
class ExternalDependencies {
    @Autowired(required = false)
    private List<ExternalDependency> dependencies;

    public List<ExternalDependency> getDependencies() {
        return dependencies != null ? dependencies : emptyList();
    }
}
