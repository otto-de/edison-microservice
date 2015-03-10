package de.otto.edison.example.web;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import de.otto.edison.example.service.HelloService;
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
    private HelloService service;

    @RequestMapping(
            value = "/",
            produces = "text/html",
            method = GET)
    @Metered(name = "sayHello", absolute = true)
    @ExceptionMetered(name = "sayHello.exceptions", absolute = true)
    public ModelAndView sayHelloAsHtml() {
        return new ModelAndView("example") {{
            addObject("hello", service.getMessage());
        }};
    }

}
