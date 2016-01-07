package de.otto.edison.status.controller;

import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.About;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Controller that is serving HTML and JSON representations of the /internal/about resource.
 *
 * About provides information about the application, team, system, and so on.
 *
 */
@Beta
@RestController
public class AboutController {

    private final About about;

    @Autowired
    public AboutController(final About about) {
        this.about = about;
    }

    @RequestMapping(
            value = "/internal/about",
            produces = {"application/vnd.otto.monitoring.status+json", "application/json"},
            method = GET
    )
    public About getAboutAsJson() {
        return about;
    }

    @RequestMapping(
            value = "/internal/about",
            produces = "text/html",
            method = GET
    )
    public ModelAndView getStatusAsHtml() {
        return new ModelAndView("about", "about", about);

    }

}

