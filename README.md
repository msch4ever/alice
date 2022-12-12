This is a test task from ALICE

To run the server start the main method src/main/java/cz/los/alice/AliceApplication.java

Go to http://localhost:8080/ to start with welcome page or directly http://localhost:8080/process to get
the given JSON file processed

I have implemented CPM all by my self using the technique I learned in the University on
"Organization of Civil Engineering projects" course:)
It is not very far from Wiki page you provided in the task description

This code base can be seen as "over-engineered" from the first glance. I admit, it was possible to have it done in three
swiss knife classes with unreadable spaghetti methods. But on the other hand I wanted to show that I develop code
considering its testability and in OOP manner.
This code base has over 90% line coverage and over 90% forks coverage and this included autogenerated code.
(We can set jacoco up to ignore autogenerated getters, setters and constructors so coverage numbers would be even more
pleasant)

My tests are not just generic happy case scenarios, but a proper verity of realistic preconditions.

I do not use inline comments to help others to understand the code. I consider this as a first sign that the code smells.
Instead, I strive to write self-explanatory code and write JavaDocs. This makes code well documented and easy to read
and navigate.