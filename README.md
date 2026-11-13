# Assignment 5 – CO5: Data Persistence Engineering – Files and Databases

**Student:** B. Santhosh Kumar  
**Register Number:** 192511053  
**Course:** Programming in Java (CSA09)  
**Java version:** OpenJDK 21


## Task folders

- Task01 – text and binary bus-pass record design and algorithmic pseudocode
- Task02 – unbuffered and buffered attendance-log reading
- Task03 – generic DAO interface and StudentDao
- Task04 – Hostel/Room/Resident object serialization
- Task05 – JDBC transaction and PreparedStatement login
- Task06 – completed PassStore, seven-field file store and growth measurement
- Task07 – adversarial login input and damaged-file tests
- Task08 – completed PassLoader and JDBC comparison
- Task09 – CSV migration and DatabaseMetaData inspection

## Compile and run standalone tasks

Open a terminal in the task folder and run the Java file. Example:

```sh
cd Task02
javac BufferingDemo.java
java BufferingDemo
```

Task06 examples:

```sh
cd Task06
javac PassStore.java BusPassStore.java PassSearchGrowth.java
java PassStore
java BusPassStore
java PassSearchGrowth
```

Task07 file validation uses PassStore from Task06:

```sh
cd Task07
javac -cp ../Task06 AdversarialInputDemo.java LoginAttackTest.java DamagedPassTest.java
java AdversarialInputDemo
java -cp .:../Task06 DamagedPassTest
```

## MySQL tasks

Task05, the JDBC portion of Task07, Task08 and Task09 require MySQL and MySQL Connector/J. Create the local test database using the supplied schema.sql file and set:

- DB_URL
- DB_USER
- DB_PASSWORD

Linux/macOS classpath example:

```sh
java -cp .:mysql-connector-j.jar JdbcComparison
```

On Windows, use `;` instead of `:` in the classpath.


