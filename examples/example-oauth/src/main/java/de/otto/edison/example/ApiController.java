package de.otto.edison.example;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class ApiController {

    @RequestMapping(
            value = "/api/hello",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = GET)
    @ResponseBody
    @PreAuthorize("#oauth2.hasScope('hello.read')")
    public String sayHelloAsHtml() {
        return "{\"hello\": \"world\"}";
    }

}
