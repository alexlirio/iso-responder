## Project ISO 8583 Responder

The Project ISO 8583 Responder receives and responses messages in ISO 8583 format. To check the content of messages, it is logged in JSON format. 


## Installation

1. Download the Git repository project: [iso-responder](https://github.com/alexlirio/iso-responder.git)
2. To create the project's zip package, run the following maven command in the project's root folder: "mvn clean package".
3. The package is created in the project folder as: "target/iso-responder.zip". This file can be extracted anywhere.


## Required Configuration

For operation it is necessary to configure the following files:

1. ** cfg/packager.xml**, with the ISO packager used in the application that will send the ISO message.
2. ** cfg/config.properties **, with the values needed to send the ISO message. Each property is commented on in the file, with its respective function.
3. ** deploy/04_qserver.xml **, with the values of "port", "packager" and "header". Each property is commented on in the file, with its respective function.


## Use

Use of responder:

1. To start the service, leaving it ready to respond to requests, simply execute the following command:

		java -jar iso-responder-0.0.1.jar
		

## API Reference

www.jpos.org

