package de.otto.edison.internal;

import de.otto.edison.cachestatistics.CacheStatisticsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static de.otto.edison.util.UrlHelper.baseUriOf;

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
        return new ModelAndView("internal/index");
    }
}
