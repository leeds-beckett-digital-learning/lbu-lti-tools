# LBU-LTI-Demo

A Java Web Application which implements an LTI 1.3 tool. Initially it can only be deployed in Blackboard Learn
because that system provides some extra functionality which means the LTI tool needs to do less work.

## Installation

I plan to add full installation instructions here but this is a fairly involved and complex process and 
it may take me a while to complete.

### Set Up a Web Server

This demo is created as a Java Web Application and so Apache Tomcat is recommended as a suitable server.
For development, a number of IDEs can automate the process of deploying web applications with Tomcat.
It is necessary for the server that runs Tomcat to be accessible via HTTPS from the Internet with
a domain name in DNS. You need to install a server certificate - self signed if you are happy to restrict
testing to development devices such as your own PC but ideally from a cerficate authority that is 
supported by popular browser software.

### Set Up Application in developer.blackboard.com

You need an account with the developer.blackboard.com and then you need to create an application.
This will handle a substantial chunk of the process of launching the tool from a Blackboard Learn
installation and so makes the tool simpler to create.

Instructions to follow...

### Clone/Fork and Clone this repository

This tool uses Gradle as its build tool and I use NetBeans to edit the source and run the Gradle tasks.
When you have a repository on your local computer you can build the tool which will produce a '.war'
web application archive file.

The build makes use of a number of dependent code libraries which are in the central Maven
repository. However, it also makes use of my own library, lbu-lti which is not in central Maven
but in GitHub. Bear this in mind if you have problems downloading dependencies. At some point
I'll get lbu-lti listed in a public repository.

### Deploy .war to Web Server

Can be done from within Netbeans if properly configured. Instruction to follow...

### Configure the Demo

You need to construct a JSON formatted text file containing a configuration. This involves taking a
template file and copying information from developer.blackboard.com into it. Instructions to follow...

Then you visit the home page of the web application, follow the link to the admin page, log in
with the system adminstration password and paste the prepared config file into the form and save.

### Configure your Blackboard Learn server

Now, you need to set up your Blackboard Learn server to make the LTI tool available to
users.

Instructions to follow...

### Test

You should be able to log into Blackboard Learn as a regular user, navigate to a course and
create an instance of the tool within the course content. Clicking on the content item
should take you to a new instance of the tool.

Instructions to follow...
