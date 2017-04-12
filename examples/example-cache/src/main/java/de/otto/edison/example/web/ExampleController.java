package de.otto.edison.example.web;

import de.otto.edison.example.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class ExampleController {

    private final HelloService helloService;

    @Autowired
    public ExampleController(final HelloService helloService) {
        this.helloService = helloService;
    }

    @RequestMapping(
            value = "/",
            produces = "text/html",
            method = GET)
    public ModelAndView sayHelloAsHtml() {

        return new ModelAndView("example") {{
            addObject("hello", helloService.getMessage());
            addObject("time", helloService.getTime());
        }};
    }

}
