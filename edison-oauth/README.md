# Edison OAuth Capabilities

Basic set of functionality to use OAuth for securing APIs

## OAuth Public Key Synchronisation

### Description

This can be used to retrieve a list of public keys from an authorization server. These public keys
can be used to validate the issuer of OAuth Token when receiving requests.

### Usage

Enable by setting the following properties:

```properties
api.oauth.public-key.enabled=true
api.oauth.public-key.url=https://base.url/public/key/path
api.oauth.public-key.interval=1800
```

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

