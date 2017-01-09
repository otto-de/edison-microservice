package de.otto.edison.example.web;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static de.otto.edison.example.togglz.Features.HELLO_TOGGLE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@RestController
public class ExampleController {

    @RequestMapping(
            value = "/",
            produces = "text/html",
            method = GET)
    public ModelAndView sayHelloAsHtml() {
        return new ModelAndView("example") {{
            if (HELLO_TOGGLE.isActive()) {
                addObject("hello", "Hello active toggle");
            } else {
                addObject("hello", "Hello inactive toggle");
            }
        }};
    }

}
