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
* __DTP__ Data Transmission Partner.
* __LaunchP__ The  'Launch' protocol defined in the LTI 1.3 specification.

### Terminology for Types of HTTPS request

The HTTP protocol is implemented as requests from nodes on the Internet to
other nodes on the internet which generate responses. The HTTPS protocol adds
to that authentication of the nodes that respond and encryption of the data in
both requests and responses while in transit.  In this page the types of request
are categorised as follows:

* __Browser request__ - a request originating in the user's web browser targetted at a server.
* __Automated browser request__ - as above but triggered by Javascript in a previous
response or by a forwarding instruction in a previous response.
* __Backchannel__ - a series of requests originating from a server targetted at another server.

## Technologies

### LTI 1.3 Launch

[Official Specification](https://www.imsglobal.org/spec/lti/v1p3/)

For a full description of the way data flow is secured consult the specification.
This application complies with the specification. Three servers are involved
in the launch process - the PS, AuthS and AS.  If the PS is any installation of
Blackboard Learn then the AuthS __must__ be developer.blackboard.com. This is
enforced by Blackboard software.

The application has only been tested with Blackboard Learn as the PS which 
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

## Data Transmission Partners

The sources and destinations of data transmitted to and from the AS.

### Platform Server

A user session with the AS is initiated by the PS using the LaunchP. 
Information flows through the user's browser in a
sequence of page requests each resulting in a forwarding instruction to 
the browser.

To do.

### Authorisation Server

Is involved in two ways...

## Transmission Mechanisms

All data transmission for the application uses the HTTPS network protocol.

### Server certificates

All URLs presented to users will use the HTTPS protocol. All hosts must 
present server certificates that will be successfully verified by 
popular browser software using the standard supplied set of CA certificates.

When the AS connects directly to DTPs it will use its configured DNS service
to resolve the IP number. It will only trust that IP number if the server 
certificate presented contains the correct server name and verifies using the
standard CAs bundled with the Java runtime that is used to run the AS software.

Further development could add further restrictions if deemed necessary. For 
example, a shorter list of CA certificates could be selected.

### Via browser

...

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

 