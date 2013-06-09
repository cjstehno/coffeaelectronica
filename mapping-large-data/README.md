# Mapping Large Data Sets (Demos)

This project contains the demos for my blog posting "[Mapping Large Data Sets]()".

This is not the most user-friendly project; sorry for that, but it's really just for generating the dat used in the post.

## Generate Data

You will need to generate some data to be used by the application (resources/data.ser). It's just serialzied Java objects. To do this you will need to run the DataGenerator - see source for options - and be sure that your data.ser file is in the resources directory.

## Run Server

The main map application is web-based. I have provided a bundled tomcat setup to make it simple:

```
mvn clean package tomcat7:run
```

Then load the application in your browser: [http://localhost:8080/oldemo](http://localhost:8080/oldemo)
