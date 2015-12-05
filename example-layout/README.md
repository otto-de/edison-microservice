# edison-microservice:example-layout

Example Edison Microservices that shows how to modify the menu of the internal pages using Thymeleaf layout.

## About

Edison-core contains some page fragments that can be overridden in order to replace parts of the internal pages.

* The HomeController is serving requests to /example. The returned page is rendered using index.html. This page
 includes the navigation bar.
* The navigation bar contains two extra items in the main section. Have a look at the main.html in the navbar
template fragments (/src/main/resources/templates/fragments/navbar/main.html).
* The header of the navigation bar is also overridden in order to replace the name of the service. See header.html
 in the navbar template fragments (/src/main/resources/templates/fragments/navbar/header.html).

