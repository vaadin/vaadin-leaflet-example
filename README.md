# Project example for integrating a JS library with Vaadin 14+

This project demonstrates how to integrate a 3rd party JS library (LeafletJS in this case) into a Vaadin application.

## Running the Application

Import the project to the IDE of your choosing as a Maven project.

Run the application using `mvn spring-boot:run` or by running the `Application` class directly from your IDE.

Open http://localhost:8080/ in your browser.

If you want to run the application locally in the production mode, run `mvn spring-boot:run -Pproduction`.

To run Integration Tests, execute `mvn verify -Pintegration-tests`.


