package de.otto.edison.example.web;

import io.micrometer.core.annotation.Timed;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static de.otto.edison.util.UrlHelper.baseUriOf;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@RestController
public class ExampleController {

    @Timed
    @RequestMapping(
            value = "/",
            produces = "text/html",
            method = GET)
    public ModelAndView sayHelloAsHtml(HttpServletRequest request) {

        return new ModelAndView("example") {{
            addObject("hello", "Hello Microservice");
            addObject("baseUri", baseUriOf(request));
        }};
    }

}
