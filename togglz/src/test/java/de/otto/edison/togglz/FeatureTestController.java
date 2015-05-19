package de.otto.edison.togglz;

import de.otto.edison.togglz.configuration.TogglzConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FeatureTestController {
    public FeatureTestController() {

    }

    @RequestMapping(value = "/featurestate/test",method = RequestMethod.GET)
    @ResponseBody
    public String getFeatureState() {
        if (TogglzConfiguration.Features.TEST.isActive()) {
            return "feature is active";
        } else {
            return "feature is inactive";
        }
    }
}
