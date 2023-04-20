# LBU-LTI-Demo Data Security Statement

[Documentation Index](index.md)

**Work in Progress**

Statement of mechanisms to protect data that are stored on the application 
server and transmitted to and from it.

## Abbreviations

* __AS__ Application Server. The server that runs our application.
* __PS__ Platform Server. The server running the learning environment that launches the application.
* __AuthS__ The authorisation server. The server that holds asymmetric key pairs which it
uses to secure connections between AS and PS by signing data blocks.
* __LaunchP__ The  'Launch' protocol defined in the LTI 1.3 specification.

### Terminology for HTTPS request Scenarios

The HTTP protocol is implemented as requests from nodes on the Internet to
other nodes on the internet which generate responses. The HTTPS protocol adds
to that authentication of the nodes that respond and encryption of the data in
both requests and responses while in transit.  In this page the types of request
are categorised as follows:

* __Browser request__ - a request originating in the user's web browser targetted at a server.
* __Automated browser request__ - as above but triggered by Javascript in a previous
response or by a forwarding instruction in a previous response.
* __Backchannel__ - a series of requests originating from a server targetted at another server.

## Network Protocols

A full list of network protocols used by the applications. (Does not reference
network protocols that might be used by system adminisrators to manage the AS.)

### HTTP (unencrypted, server not authenticated)

Not used by this application.

### HTTPS

All request - response communication uses HTTPS. To protect the security of
communications the private keys paired with the public keys in the server
certificates need to be kept securely on those servers. It also means that
the copies of CA certificates stored on all servers and on user browsers need 
to be safe from tampering. I.e. standard security measures for web services 
and clients.

As a Java Web Applicaton this application accepts requests from HTTPS from
browsers (both user initiated and automated browser requests) and from
servers via backchannels.

The Application also acts as an HTTPS client in order to make backchannel
connections to PS and AuthS servers.

### SMTP

This application can be configured to send email messages to users with CC
to an administrator account via SMTP. This is for the purposes of record keeping and
will not be used to transfer sensitive information. __Currently the application
does not implement authentication or encryption for SMTP__ and it is intended that the
target SMTP server be on the same network and to be configured to accept
incoming connections based on network topology.  __If the connection cannot be
properly secured by configuring the SMTP server this feature should be disabled.__

## Technologies

### LTI 1.3 Launch

[Official Specification](https://www.imsglobal.org/spec/lti/v1p3/)

For a full description of the way data flow is secured consult the specification.
Here is an abbreviated summary. The first step is the user navigating to a URI
on the PS which is an LTI launch URI - a manual browser request. This is 
followed by a sequence of automated browser requests. When a server responds
it can specify or otherwise arrange for data to be included in the next
automated response and this enables server to server communication without
the need for a backchannel.

- User requests LTI resource launch by selecting link in browser to URL on PS.
  - PS generates URL to AS containing information about the launch 'login'.
    This information contains absolutely NO information about the user or
    user account. It references only information about the three servers
    involved in the the launch - PS, AS and AuthS.
- User's browser automatically requests login page on AS.
  - AS generates a 'cookie check' page 
    for the user. URL contains same data as incoming URL. This page contains
    Javascript that checks that the user's browser support session only cookies.
    If the test fails an error message is displayed. Otherwise the browser is
    automatically instructed to load the same URL again but with a flag indicating that
    the cookie check passed.
- User's browser automatically requests the AS login page again.
  - AS creates a launch session locally and assigns it a unique ID (a random UUID).
    It then generates a URL pointing at the AuthS with the same information
    provided by the PS with the addition of the AS's unique session ID.
- User's browser auto-forwards to the AuthS URL.
  - AuthS validates the information provided to establish that the PS is entitled
    to launch a resource on the AS and that both those other servers are genuine.
    Then it constructs a URL that will forward the user's browser back to the PS.
- User's browser auto-forwards to PS URL
  - PS verifies that the AuthS go ahead is verifiable and constructs a web page
    for the user. This page contains Javascript that constructs a URL to the AS's
    launch functionality and then auto-requests that URL. The request method is
    POST and data is sent in the body of the request concerning the resource that
    is requested, the user who needs access to the resource and how to open
    LTI backchannels to the PS (if any have been enabled by PS sysadmin.) Putting 
    this data in the body of a POST request means that data will not appear in
    web logs and will not be visible to web proxies.
- User's browser auto-requests launch URL on AS
  - AS receives request and identifies the login session from the session ID 
    provided. It also examines the data in the body of the request. This data
    is only accepted if it is digitally signed and the signature is verified
    using the configured public key for the AuthS. The session is marked as
    verified and a URL to the resource on the AS is constructed.
- User's browser auto-forwards to the resource on AS
  - The session ID is checked against a cache of active sessions and the resource
    is delivered if the session is marked as successfully launched.  Session data
    will affect the presentation and functionality of the resource.
- User will make further requests on the AS to access its functionality.

This application complies with the specification. Three servers are involved
in the launch process - the PS, AuthS and AS.  If the PS is any installation of
Blackboard Learn then the AuthS __must__ be developer.blackboard.com. This is
enforced by Blackboard software.

Currently the application has only been tested with Blackboard Learn as the PS which 
means that the AuthS will always
be developer.blackboard.com. That server holds current asymmetric key pairs
specifically for use with this application. Our application stores a copy of the public
key set and the AuthS users the private key to digitally sign certain important
blocks of data used in the launch process.

This application verifies the signatures on signed data using the public key
obtained from developer.blackboard.com.

### Backchannel to AuthS key set

It is possible to manually add public keys originating from AuthS to the AS
configuration for use in the LTI launch process. It is also possible to fetch 
them automatically with an HTTPS request. The IP number will be resolved 
using the AS's configured DNS service
and the digital certificate of the AuthS will be verified - meaning that a 
reputable server certificate supplier must have supplied a valid certificate
with the correct domain name.

### LTI 1.3 Advantage Services

In LTI 1.3 the "Advantage" specification defines how a PS can declare to an
AS at launch time that it offers REST-like functionality and provides some
specific URLs as entry points.

There are a limited number of 'standard' services with specifications defined
on the IMS web site.  This application makes use of one of these services to
obtain a list of students enrolled on a course from which a peer group assessment
tool was launched.

This application complies with the specification for authentication. This involves
opening a backchannel to the AuthS to obtain a one time use authorization token
followed by accessing a backchannel on the PS to actually exchange data as
defined by the service.

The PS will also open a backchannel to the AS to access a set of its public keys
for data signing.
It will obtain the correct URI of for this backchannel from the AuthS and will
check the server certificate before completing the download.


### Blackboard REST API

[BB Rest API Specification](https://developer.blackboard.com/portal/displayApi/Learn)

This is __not__ defined by LTI but is rather a proprietory API provided by
Anthology for their Blackboard Learn product. It is implemented as a set of URIs
on the PS. The AS initiates a backchannel connection to first obtain an
authentication token. Then, subsequent requests to the API include the token
in request headers.

The PS system administrators have to enable this backchannel and can configure
a user account which will be used to determine in fine detail which facets of
the API can be accessed. A list of required permissions are detailed in a
later section of this page.


## Server certificates

All URLs presented to users will use the HTTPS protocol. All hosts must 
present server certificates that will be successfully verified by 
popular browser software using the standard supplied set of CA certificates.

When the AS connects directly to DTPs it will use its configured DNS service
to resolve the IP number. It will only trust that IP number if the server 
certificate presented contains the correct server name and verifies using the
standard CAs bundled with the Java runtime that is used to run the AS software.

Further development could add further restrictions if deemed necessary. For 
example, a shorter list of CA certificates could be selected.


## Blackboard Principal for REST backchannel

To implement some functionality the AS connects to the PS using the
Blackboard Learn REST API.  The PS system administrator sets up a user account
and connects that account with the application ID. When the AS connects it
operates on the REST API with the permissions settings of the assigned user
account. There are 479 available permissions. Of these the permissions
required for this application are:

### Administrator Panel (Courses) > Courses > Edit > Course Properties 

To see certain privileged fields in course search results. 

### Administrator Panel (Organizations) > Organizations > Edit > Organization Properties 

To see certain privileged fields in organization search results. 

### Administrator Panel (Users) > Users > Edit > View Course Enrollments 

To find enrolments for a particular user ID. 

### Course/Organization Control Panel (Users and Groups) > Users > Change User's Role in Course/Organization

To make it possible to enrol user in role other than ‘student’. 

### Course/Organization Control Panel (Users and Groups) > Users > Enroll User 

To create new enrolments. 

## Audit of specific user related data

A detailed report on how they are transmitted between PS, AS and AuthS and 
whether they are stored by the AS.

- Name
  - __From__
  - __Mechanism__
  - __Encryption__
  - __Authenticity__
  - __Storage__

- Login Name (Username)
  - __From__
  - __Mechanism__
  - __Encryption__
  - __Authenticity__
  - __Storage__

- UUID
  - __From__
  - __Mechanism__
  - __Encryption__
  - __Authenticity__
  - __Storage__

- Password
  - __From__ Never requested, offered or stored.
  - __Mechanism__
  - __Encryption__
  - __Authenticity__
  - __Storage__

