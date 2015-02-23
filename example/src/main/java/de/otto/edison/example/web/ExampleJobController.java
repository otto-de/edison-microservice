package de.otto.edison.example.web;

import de.otto.edison.example.jobs.FooJob;
import de.otto.edison.jobs.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static de.otto.edison.example.jobs.FooJob.fooJob;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@RestController
public class ExampleJobController {

    @Autowired
    private JobService jobService;

    @RequestMapping(
            value = "/startJob",
            method = POST)
    public void startJob(final HttpServletResponse response) throws IOException {
        final URI jobUri = jobService.startAsyncJob(
                fooJob()
        );
        response.setHeader("Location", jobUri.toString());
        response.setStatus(SC_NO_CONTENT);
    }

}
