Meteor.js Java DDP Client
=========================

Origins/Acknowledgements
------------------------
This is a fork and significant rewrite out of [Key Yee's DDP Client](https://github.com/kenyee/java-ddp-client),
which is a fork and fairly big fleshing out of [Peter Kutrumbos' DDP Client](https://github.com/kutrumbo/java-ddp-client).

Differences from kenyee:
* Switched to POJO models
* Upgrade to match Meteor 1.3.5.1 and up
* Fix thread safety
* Significant refactor to Java 1.7 standards
* Added Subscribe functionality and notify-listener pattern for DDP messages.
* Separate message type listeners to allow consumer to focus on desired functionality
   * Heartbeat listener (client ping/pong messages)
   * Connection listener (Connect/disconnect messages)
   * Method call response listener (result of method call messages)
   * Meteor Subscribe listener (Subscription response messages)
   * Collection listener (Changes to collections messages)

Differences from kutrumbo:

* switched to using Gradle for builds to remove duplicated Websocket
  and Gson libraries from source code
* added JUnit testing for all the DDP messages/results and auth/collections
* returns DDP command results and removes handlers when done
* handles all the DDP message fields (switched to static strings instead of
  using extraneous class) from server
* handles all the DDP message types from the server
* websocket closed/error state changes converted to regular observer events instead
  of dumping errors to System.out
* added full Javadocs
* use slf4j for logging instead of java.util.Logging
* added a disconnect method to close the websocket connection

Usage
-----
The best thing to do is to look at the JUnit tests.  The tests are separated
into authentication tests and collection tests.  

To run the tests, you will need to run a Meteor application that has some packages
for testing, such as `accounts-password` and `insecure`.

Download this [Meteor project](https://github.com/jazeee/meteor-test-ddp-endpoint)
and run Meteor. Then run `gradle test` to verify that the tests pass.

The TestDDPConnections is a good example of how you can listen and handle message responses
and is a simple example of holding enough state to implement a simple
Meteor client.  Note that in a real application, you'll probably want to use an
eventbus to implement the DDP message handling.

Note that you may want to use a local SQLite DB to store the data instead of using
Maps if you are memory constrained and/or if you need to do any sorting.  Otherwise,
you'll have to have separate SortedMap collection for each of your sorts.

If you're planning to use this with Android, look at the
[Android DDP Library](https://github.com/kenyee/android-ddp-client)
which builds on top of kenyee's library
to make it easier to work with an Android application.

If you see this error:
    Error generating final archive: Found duplicate file for APK: LICENSE.txt
    Origin 1: C:\Users\you\.gradle\caches\artifacts-23\filestore\junit\junit\4.11\jar\4e031bb61df09069aeb2bffb4019e7a5034a4ee0\junit-4.11.jar
    Origin 2: C:\Users\you\.gradle\caches\artifacts-23\filestore\org.hamcrest\hamcrest-core\1.3\jar\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar
delete the LICENSE.txt from one of those jar files using "zip -d".  This is a bug in
Eclipse's Gradle plugin. Upgrade to [Gradle's Buildship](https://projects.eclipse.org/projects/tools.buildship)

Design
------
This package uses immutable messages to help ensure thread safety. All messages can be considered
as immutable, although some may contain mutable Collections.
This package uses Google's GSON library to convert
JSON to maps and ArrayLists (used for arrays of strings or objects).  

One important thing to note is that integer values are always represented as
Doubles in JSON so that's how they're translated by the GSON library.  If you're
sending numbers to Meteor, note that they will be sent as Doubles and what
you get back from Meteor as numbers show up as Doubles.  This isn't an issue in
Javascript because it will autoconvert objects to the needed datatype, but Java
is strongly typed, so you have to do the conversions yourself.

Javascript's callback handling is done using Java's Observer/Listener pattern,
which is what most users are familiar with if they've used any of the JDK UI
frameworks.  When issuing a DDP command, you can attach a listener by creating one
and then overriding any methods you want to handle:
```Java
ddp.addConnectionListener(ddpConnectionListener);
ddp.addCollectionListener(ddpCollectionListener);
```

DDP Protocol Version
--------------------
This library currently supports DDP Protocol 1 (previous version supported pre1).

Maven Artifact
--------------
This library is in the Maven Central Library hosted by Sonatype.
In Gradle, you can reference it with this in your dependencies:
```gradle
compile group: 'com.jazeee', name: 'ddp-client', version: '2.0.0.+'
```
And in Maven, you can reference it with this:
```maven
<dependency>
	<groupId>com.jazeee</groupId>
	<artifactId>ddp-client</artifactId>
	<version>2.0.0.0</version>
	<type>pom</type>
</dependency>
```
Changes
-------
* 2.0.0.0 - Significant rewrite and redesign

To-Do
-----
* Add "create new user" test.
* Test all possible EJSON data types.
* Handle addBefore and addAfter collection update messages
