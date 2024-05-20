
# LBU-LTI-Tools Attack Surface

All URLs are relative to a base URL of:

```https://servername/lbu-lti-tools/```

## Servlets

### Login Servlet `/login`

For access by user via browser.

Implements the IMS LTI Advantage standard login functionality. Works a little like a single 
sign-on entry point. Platform forwards user to this URL, passing in form fields to identify the
platform. Servlet looks up security provider and forwards user to URL or prints error message.

### Launch Servlet `/launch`

For access by user via browser.

Receives an encrypted and digitally set of signed claims from platform in JWT format.
Either displays error message or forwards user to a Java Server Page selected based on the
claims.

### JWKS Servlet `/jwks`

For access by other servers in the LTI eco system. Simply returns a JSON formated text file
containing the public keys used by this service. The basis of the trust for these keys is the
HTTPS server certificate.

### Auto registration Servlet `/autoreg`

Used by platform when platform sysadmin registers the platform with our provider. Uses IMS
LTI auto registration mechanism.

## Java Server Pages

### Deep Linking `/deeplinking/index.jsp`

### Peer Group Assessment `/peergroupassessment/index.jsp`

### Self Enrol `/selfenrol/index.jsp`


## WebSockets

### Deep Linking `/socket/deeplinking`

### Peer Group Assessment `/socket/peergroupassessment`

### Self Enrol `/socket/selfenrol`

