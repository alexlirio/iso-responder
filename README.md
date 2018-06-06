## Project ISO 8583 Responder

The Project ISO 8583 Responder receive and respond ISO 8583 messages. The content of messages is logging as ISO and JSON formats. 


## Installation

1. Download the Git repository project: [iso-responder](https://github.com/alexlirio/iso-responder.git)
2. To create the project's package, run the following maven command in the project's path: "mvn clean package".
3. The package will be create in the project's path as: "target/iso-responder.zip". This file can be extracted anywhere.


## Required Configuration Files

* ** cfg/packager.xml **, with the ISO packager. It's the same ISO packager of application used to send requests.
* ** cfg/config.properties **, with valid values to each property. The file contains comments to help about each property.
* ** deploy/04_qserver.xml **, with valid values to: "port", "packager" and "header". The file contains comments to help about each property.


## Starting the Service

	$ java -jar iso-responder.jar


## Configuring Sample Responses

HTTP GET - To see configured responses:

	http://127.0.0.1:9080/iso-responder/file-servlet?file=cfg/response.json


HTTP POST - To save configured responses:

	http://127.0.0.1:9080/iso-responder/file-servlet?file=cfg/response.json

```json
{"responses":[
	{
		"header":"0000000000",
		"0":"0210",
		"3":"009500",
		"11":"000001",
		"12":"235959",
		"13":"1231",
		"39":"00",
		"41":"POS80217",
		"42":"000000000083917",
		"61.1":"21",
		"61.2":"999999999",
		"61.3":"TEST001",
		"62":"1.00b01p01#9.51b27#PWWIN#ECF4BBFBC1C2"
	},
	{
		"header":"0000000000",
		"0":"0230",
		"3":"equals",
		"11":"equals",
		"12":"equals",
		"13":"equals",
		"39":"00",
		"41":"equals",
		"42":"equals",
		"62":"equals",
		"CONFIG_SLEEP":60000,
		"CONFIG_FILTER":{"0":"..2.","3":"009500"}
	}
]}
```

These "responses" may contain:
1. ** "header" ** (with same header size configured in "deploy/04_qserver.xml").  
2. ** "fields" ** with value to response or "equals" to return the same value of request.  
3. ** "CONFIG_SLEEP" ** to milliseconds delay before response.  
4. ** "CONFIG_FILTER" ** to configure an specific response to each request using regex.  


## API Reference

* [jpos.org](http://www.jpos.org/)
* [wikipedia.org/wiki/iso8583](https://en.wikipedia.org/wiki/ISO_8583)
* [docs.oracle.com/regex](https://docs.oracle.com/javase/tutorial/essential/regex/index.html)

