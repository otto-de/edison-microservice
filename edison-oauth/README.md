# Edison OAuth Capabilities

Basic set of functionality to use OAuth for securing APIs

## OAuth Public Key Synchronisation

### Description

This can be used to retrieve a list of public keys from an authorization server. These public keys
can be used to validate the issuer of OAuth Token when receiving requests.

### Usage

Enable by setting the following properties:

```properties
edison.oauth.public-key.enabled=true
edison.oauth.public-key.url=https://base.url/public/key/path
edison.oauth.public-key.interval=1800000
```

(`interval` defines the time in milliseconds between retrievals of public keys)

The retrieved Data has to be in the following format:

```json
[
    {
        publicKey: "-----BEGIN PUBLIC KEY----- publicKeyData -----END PUBLIC KEY----- ",
        publicKeyFingerprint: "fingerprintOfThePublicKey",
        validFrom: "ZonedDateTime formatted Date",
        validUntil: "ZonedDateTime formatted Date OR null"
    }
]
```

A key is valid if one of the following conditions is true:

`validFrom < now < validUntil` 

or 

`valid from < now && validUntil == null`

All valid keys are kept and can be validated against. 

## OAuth authorisation for Controller

### Description

An OAuth2 Security Filter is added and can be used to verify users based on their scope inside
their `Authorization`-Header.

### Usage

The configuration of the OAuthSecurity Beans is done automatically when adding the module `edison-oauth`.
Configure it by setting the following properties:

```properties
edison.oauth.authorization.resource.patterns=["/secured/path","/another/secured/path/**"]
edison.oauth.jwt.audience=https://api.otto.de/api-authorization
```

(`patterns` defaults to `/oauth/**`)

Afterwards, you can use an annotation at the controller method that checks the request
for a certain `scope` inside the JWT Data:

```java
    @RequestMapping(method = GET, value = "/secured/path", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("#oauth2.hasScope('some.oauth.scope')")
    public List<Object> getObjects() {
        ...
    }
```

If the user is not authorized, he'll retrieve a HttpStatus of `401`. See the Spring documentation on
OAuth for more details: https://spring.io/projects/spring-security-oauth 