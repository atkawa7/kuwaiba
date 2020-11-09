# PaperDialog component for Vaadin Flow

This project is the Component wrapper implementation of [`<paper-dialog>`](https://www.webcomponents.org/element/@polymer/paper-dialog) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

## Running the component demo
Run from the command line:
- `mvn  -pl paper-dialog-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:9998/paper-dialog` to see the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.neotropic.flow.component</groupId>
    <artifactId>paper-dialog-flow</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Flow documentation
Documentation for flow can be found in [Flow documentation](https://github.com/vaadin/flow-and-components-documentation/blob/master/documentation/Overview.asciidoc).

## Contributing
- Use the coding conventions from [Flow coding conventions](https://github.com/vaadin/flow/tree/master/eclipse)
- [Submit a pull request](https://www.digitalocean.com/community/tutorials/how-to-create-a-pull-request-on-github) with detailed title and description
- Wait for response from one of Vaadin Flow team members

## License
Apache License 2.0