# LBU-LTI-Demo Data Security Statement

[Documentation Index]()

**Work in Progress**

Statement of mechanisms to protect data that are stored on the application 
server and transmitted to and from it.

## Abbreviations

* __AS__ Application Server. The server that runs our application.
* __PS__ Platform Server. The server running the learning environment that launches the application.
* __AuthS__ 
* __DTP__ Data Transmission Partner.
* __LaunchP__ The  'Launch' protocol defined in the LTI 1.3 specification.

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