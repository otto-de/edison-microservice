package de.otto.edison.example.web;

import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import de.otto.edison.example.status.ExampleStatusIndicator;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ExampleStatusIndicator statusIndicator;

    @RequestMapping(
            value = "/",
            produces = "text/html",
            method = GET)
    public ModelAndView sayHelloAsHtml() {

        statusIndicator.incGreetings();

        final ModelAndView modelAndView = new ModelAndView("example");
        modelAndView.addObject("hello", "Hello Microservice");
        return modelAndView;
    }

}
