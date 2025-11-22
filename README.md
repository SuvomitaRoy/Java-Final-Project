# Java-Final-Project

To execute the following steps, you need to be in the src folder in the terminal.

To get the database, run the following code:
- To compile: javac -cp . dao/DatabaseManager.java TestDB.java
- To execute: java -cp .:../lib/sqlite-jdbc-3.51.0.0.jar TestDB

To run the main function:
- To compile: javac TestLibrary.java
- To execute: java -cp .:../lib/sqlite-jdbc-3.51.0.0.jar TestLibrary

However, before doing these steps, run the files in the model folder (located in src):
- Document: javac model/Document.java
- Book: javac model/Document.java model/Book.java
- Magazine: javac model/Document.java model/Magazine.java
- Member: javac model/Member.java
- Borrow: javac model/Borrow.java
- PenaltyStatus: javac model/PenaltyStatus.java
