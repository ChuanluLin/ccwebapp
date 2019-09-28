# CSYE 6225 - Fall 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
|Chuanlu Lin|001839299|lin.chua@husky.neu.edu|
|Shangrui Xie|001870007|xie.sha@husky.neu.edu|
|Tianli Feng|001825503|feng.tian@husky.neu.edu|

## Technology Stack

- [Spring Boot](https://projects.spring.io/spring-boot/) - The web back-end framework
- [Maven](https://maven.apache.org/) - Dependency Management
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) - IDE used to develop the web app
- [MySQL](https://mysql.com/) - Database

## Build Instructions

The following instructions will help you run this project on local environment.

### Environment

 - JDK 11
 - Maven 3.2+
 - IntelliJ IDEA
 
### Setup

1. Import the web project in `webapp` folder straight into IDEA.

2. Database setup.

Change the following code in `application.properties` file, as your database setting:
```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/csye6225db?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```
Then execute the following command in your MySQL:
```
create database csye6225db;
```

## Deploy Instructions

Run `mvn` command to package the project to a `jar` file:
```
mvn clean package  -Dmaven.test.skip=true
```
Run the `jar` file under `target` folder to start the server:
```
java -jar target/demo-0.0.1-SNAPSHOT.jar
```
The server will run at port `8080`.

If you don't want to shutdown the server after closing the console, use the following command to run in background:
```
nohup java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Running Tests


## CI/CD


