# edison-microservice:edison-hateoas

Library to create application/hal+json representations of REST resources using Jackson.
See https://tools.ietf.org/html/draft-kelly-json-hal-06 for details.

## Status

BETA - work in progress.

Currently, creating HAL compatible links is supported. No curies, no _embedded resources.
Next steps:
- Embedded resources
- Curies
- Client-side support for HAL 

## About

Edison-Microservices should only communicate via REST APIs with other 
 microservices. HAL is a nice format to implement the HATEOAS part 
 of REST.

Currently, there are only few libraries supporting HAL and even
 less that support the full media type including all link properties,
 curies (compact URIs) and embedded resources. 
 
Spring HATEOAS, for
 example, is lacking many link properties, such as title, name, type and
 others. Beside of this, including Spring HATEOAS into Spring Boot
 applications has some unwanted (at least to me) side-effects: 
 for example, an "Actuator" endpoint is automatically registered unter 
 /internal, so we would loose the possibility to provide an html 
 representation at this URI (it is a big WTF? to me, that Spring Boot 
 Actuator endpoints do not support content negotiation, btw).

## Usage

*Include edison-hateoas*:
 
```gradle
    dependencies {
        compile "de.otto.edison:edison-hateoas:0.64.0",
        ...
    }
```
 

## Examples

Pending

