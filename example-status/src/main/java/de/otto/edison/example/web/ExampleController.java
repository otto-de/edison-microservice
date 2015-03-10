package de.otto.edison.example.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

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
            addObject("hello", "Hello Microservice");
        }};
    }

}
