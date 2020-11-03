# PaperToggleButton component for Vaadin Flow

This project is the Component wrapper implementation of [`<paper-toggle button>`](https://www.webcomponents.org/element/@polymer/paper-toggle-button) element
for use from the server side with [Vaadin Flow](https://github.com/vaadin/flow).

## Running the component demo
Run from the command line:
- `mvn  -pl paper-dialog-flow-demo -Pwar install jetty:run`

Then navigate to `http://localhost:8080/` to see the demo.

## Installing the component
Run from the command line:
- `mvn clean install -DskipTests`

## Using the component in a Flow application
To use the component in an application using maven,
add the following dependency to your `pom.xml`:
```
<dependency>
    <groupId>com.neotropic.flow.component</groupId>
    <artifactId>paper-togggle-button</artifactId>
    <version>2.0</version>
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