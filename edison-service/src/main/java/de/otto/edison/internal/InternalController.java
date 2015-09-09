package de.otto.edison.internal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static de.otto.edison.jobs.controller.UrlHelper.baseUriOf;

/**
 * Controller to get /internal/index.html as an overview of all internal tools.
 *
 * @author Guido Steinacker
 * @since 09.09.15
 */
@Controller
public class InternalController {

    @RequestMapping("/internal")
    public ModelAndView getInternal(final HttpServletRequest request) {
        return new ModelAndView("internal/index") {{
            addObject("baseUri", baseUriOf(request));
        }};
    }
}
