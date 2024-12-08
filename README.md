# Coding Challenge
## What's Provided
A simple [Spring Boot](https://projects.spring.io/spring-boot/) web application has been created and bootstrapped with data. The application contains 
information about all employees at a company. On application start-up, an in-memory Mongo database is bootstrapped with 
a serialized snapshot of the database. While the application runs, the data may be accessed and mutated in the database 
without impacting the snapshot.

### How to Run
The application may be executed by running `gradlew bootRun`.

*Spring Boot 3 requires Java 17 or higher. This project targets Java 17. If you want to change the targeted Java 
version, you can modify the `sourceCompatibility` variable in the `build.gradle` file.*

### How to Use
At runtime, the OpenAPI Spec and interactive UI are available at:
[/swagger-ui](http://localhost:8080/swagger-ui)

The following endpoints are available to use:
```
* CREATE
    * HTTP Method: POST 
    * URL: localhost:8080/employee
    * PAYLOAD: Employee
    * RESPONSE: Employee
* READ
    * HTTP Method: GET 
    * URL: localhost:8080/employee/{id}
    * RESPONSE: Employee
* UPDATE
    * HTTP Method: PUT 
    * URL: localhost:8080/employee/{id}
    * PAYLOAD: Employee
    * RESPONSE: Employee
```

The Employee has a JSON schema of:
```json
{
  "title": "Employee",
  "type": "object",
  "properties": {
    "employeeId": {
      "type": "string"
    },
    "firstName": {
      "type": "string"
    },
    "lastName": {
      "type": "string"
    },
    "position": {
      "type": "string"
    },
    "department": {
      "type": "string"
    },
    "directReports": {
      "type": "array",
      "items": {
        "anyOf": [
          {
            "type": "string"
          },
          {
            "type": "object"
          }
        ]
      }
    }
  }
}
```
For all endpoints that require an `id` in the URL, this is the `employeeId` field.

## What to Implement
This coding challenge was designed to allow for flexibility in the approaches you take. While the requirements are 
minimal, we encourage you to explore various design and implementation strategies to create functional features. Keep in
mind that there are multiple valid ways to solve these tasks. What's important is your ability to justify and articulate
the reasoning behind your design choices. We value your thought process and decision-making skills. Also, If you 
identify any areas in the existing codebase that you believe can be enhanced, feel free to make those improvements.

### Task 1
Create a new type called `ReportingStructure` that has two fields: `employee` and `numberOfReports`.

The field `numberOfReports` should equal the total number of reports under a given employee. The number of reports is 
determined by the number of `directReports` for an employee, all of their distinct reports, and so on. For example,
given the following employee structure:
```
                   John Lennon
                 /             \
         Paul McCartney     Ringo Starr
                            /         \
                       Pete Best    George Harrison
```
The `numberOfReports` for employee John Lennon (`employeeId`: 16a596ae-edd3-4847-99fe-c4518e82c86f) would be equal to 4.

This new type should have a new REST endpoint created for it. This new endpoint should accept an `employeeId` and return
the fully filled out `ReportingStructure` for the specified `employeeId`. The values should be computed on the fly and 
will not be persisted.

### Task 2
Create a new type called `Compensation` to represent an employee's compensation details. A `Compensation` should have at 
minimum these two fields: `salary` and `effectiveDate`. Each `Compensation` should be associated with a specific 
`Employee`. How that association is implemented is up to you.

Create two new REST endpoints to create and read `Compensation` information from the database. These endpoints should 
persist and fetch `Compensation` data for a specific `Employee` using the persistence layer.

## Delivery
Please upload your results to a publicly accessible Git repo. Free ones are provided by GitHub and Bitbucket.

## Execution

### Preemptive changes
I prefer to use the Eclipse IDE (or IntelliJ) for Java, so I imported the project into Eclipse and added the recommended settings files to git (.classpath, .project, and updated .gitignore for Eclipse temp files). This won't prevent other developers from continuing to use their choice of IDE (like Visual Studio Code).

Next, the project is exclusively hosting web service endpoints with no interactive UI or hosting a specification document for consumers. A browser can be used to manually test GET requests, but building fetch requests for all other HTTP methods in a browser or requiring developers to download an external application is less ideal. I added a library that automatically generates a swagger-ui spec and interactive web UI endpoint. Developers are still free to use their own tools (like Postman), but this gives a simple and low maintenance built-in method for manual testing and consumption of the service.

### Task 1
Hierarchical structures can be tricky to calculate and expensive as they grow, so I prefer to have the database link the data rather than link the data in Java or execute multiple round trip DB calls. The data wasn't in an ideal format for $graphLookup (or maybe I misunderstood part of the syntax), so I decided to update the persistent data by converting the, "directReports" array of objects to an array of strings, as it only stores a single value (employeeId) anyway. With the data changed, I created a custom aggregate query to collect and summarize the data as needed.

It's also worth pointing out that this optimization could be seen as unnecessary for the given data set, as even a large company with thousands of employees is still a relatively small amount of data to join without $graphLookup.

### Unexpected interruption
As I started task 2 I ran into a bug that prevented me from moving forward. The update endpoint /employee/{id} [put] does execute without errors, but it doesn't actually update the record as expected. It instead creates a new record. This is not discovered by the unit test because no error is thrown (.save() is allowed to insert if needed) and no further queries are executed that would cause the issue (like /employee/{id} [get] request for that employee after the update). Furthermore, the database doesn't configure any data constraints (like assigning the _id or employeeId field as a unique index).

To fix the issue, the Employee, "_id" property needed to exist on the Java object in order for Mongo to find the record to update. I added an, "Id" property to Employee (to match the Java naming convention instead of the exact Mongo, "_id" name) and used an annotation, "@Id" to make sure it mapped properly (though the Mongo documentation states it looks for, "Id" or "_id", so the annotation likely isn't needed. I just prefer to explicitly decorate so the Javadocs make it clear why it exists).

The bug is now fixed, but I also updated the EmployeeService test with a situation that would trigger this bug in case there is a regression in the code at a later date.

### Task 2
Compensation is tied directly to an employee in the same way ReportingStructure is (neither can exist without employee, and they relate in a 1-to-1 or 1-to-many relationship), so I decided to include the compensation data inside the Employee collection. This means the existing employee endpoints can both read and update this data without any additional endpoints. However, I provided endpoints to update exclusively the compensation document as per the requirement (as it still may be useful, for instance if certain clients will have limited access, like a finance team, and they are only able to update compensation but not documents like the employee reporting structure).