package de.otto.edison.togglz.api;

import com.google.common.base.Strings;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.domain.FeatureDTO;
import de.otto.hmac.authorization.AllowedForRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/api/toggles")
@RestController
public class FeaturesController {

    @Autowired
    private de.otto.edison.togglz.repository.MongoFeatureRepository mongoFeatureRepository;

    @Autowired
    private FeatureClassProvider featureClassProvider;

    @RequestMapping(method = RequestMethod.GET, path = "", produces = "application/vnd.otto.toggle+json")
    public FeatureDTO[] toggles(){
        final List<FeatureDTO> featureDTOs = mongoFeatureRepository.loadAll();
        return featureDTOs.toArray(new FeatureDTO[featureDTOs.size()]);
    }

    @RequestMapping(value = "/{toggleName}", method = RequestMethod.PUT, consumes = "application/vnd.otto.toggle+json",
            produces = "application/vnd.otto.toggle+json")
    @AllowedForRoles(value = "orderAlertToggle")
    public FeatureDTO updateToggleState(@PathVariable final String toggleName, @RequestBody final FeatureDTO dto,
                                       final HttpServletResponse response) {
        try {
            if (Strings.isNullOrEmpty(dto.getName()) || !dto.getName().equals(toggleName)) {
                throw new IllegalArgumentException("toggle name in request body differs form name in URI");
            }
            final FeatureState state = new FeatureState(resolveEnumValue(dto.getName()), dto.isActive());
            mongoFeatureRepository.setFeatureState(state);
            response.setStatus(HttpStatus.OK.value());
            return dto;
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException();
        } catch (final Throwable t) {
            throw new UnknownError(t.getMessage());
        }
    }

    private Feature resolveEnumValue(String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
    }
}
