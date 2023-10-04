package de.otto.edison.acceptance;

import org.springframework.context.annotation.ComponentScan;

import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
@ComponentScan(basePackages = {"de.otto.edison"})
public class SpringTestBaseWithComponentScan extends SpringTestBase {

}
