# Raven (module)
Main module of the Raven project in java. It provides a client to send messages
to a Sentry server as well as an implementation of an [`Handler`](http://docs.oracle.com/javase/7/docs/api/java/util/logging/Handler.html)
for `java.util.logging`.

## Installation

### Maven
```xml
<dependency>
    <groupId>com.getsentry.raven</groupId>
    <artifactId>raven</artifactId>
    <version>7.8.1</version>
</dependency>
```

### Gradle
```
compile 'com.getsentry.raven:raven:7.8.1'
```

### Other dependency managers
Details in the [central Maven repository](https://search.maven.org/#artifactdetails%7Ccom.getsentry.raven%7Craven%7C7.8.1%7Cjar).

## Usage (`java.util.logging`)
### Configuration
Add the `SentryHandler` to the `logging.properties` file:

```properties
# Enable the Console and Sentry handlers
handlers=java.util.logging.ConsoleHandler,com.getsentry.raven.jul.SentryHandler

# Set the default log level to INFO
.level=INFO

# Override the Sentry handler log level to WARNING
com.getsentry.raven.jul.SentryHandler.level=WARNING
```

When starting your application, add the `java.util.logging.config.file` to the
system properties, with the full path to the `logging.properties` as its value.

    $ java -Djava.util.logging.config.file=/path/to/app.properties MyClass

Next, you'll need to configure your DSN (client key) and optionally other
values such as `environment` and `release`. See below for the two
ways you can do this.

#### Configuration via runtime environment

This is the most flexible method to configure the `SentryAppender`,
because it can be easily changed based on the environment you run your
application in.

The following can be set as System Environment variables:

```bash
SENTRY_EXAMPLE=xxx java -jar app.jar
```

or as Java System Properties:

```bash
java -Dsentry.example=xxx -jar app.jar
```

Configuration parameters follow:

| Environment variable | Java System Property | Example value | Description |
|---|---|---|---|
| `SENTRY_DSN` | `sentry.dsn` | `https://host:port/1?options` | Your Sentry DSN (client key), if left blank Raven will no-op |
| `SENTRY_RELEASE` | `sentry.release` | `1.0.0` | Optional, provide release version of your application |
| `SENTRY_ENVIRONMENT` | `sentry.environment` | `production` | Optional, provide environment your application is running in |
| `SENTRY_SERVERNAME` | `sentry.servername` | `server1` | Optional, override the server name (rather than looking it up dynamically) |
| `SENTRY_RAVENFACTORY` | `sentry.ravenfactory` | `com.foo.RavenFactory` | Optional, select the ravenFactory class |
| `SENTRY_TAGS` | `sentry.tags` | `tag1:value1,tag2:value2` | Optional, provide tags |
| `SENTRY_EXTRA_TAGS` | `sentry.extratags` | `foo,bar,baz` | Optional, provide tag names to be extracted from MDC when using SLF4J |

#### Configuration via `logging.properties`

You can also configure everything statically within the `logging.properties` file
itself. This is less flexible because it's harder to change when you run
your application in different environments.

```properties
# Enable the Console and Sentry handlers
handlers=java.util.logging.ConsoleHandler,com.getsentry.raven.jul.SentryHandler

# Set the default log level to INFO
.level=INFO

# Override the Sentry handler log level to WARNING
com.getsentry.raven.jul.SentryHandler.level=WARNING

# Set Sentry DSN
com.getsentry.raven.jul.SentryHandler.dsn=https://host:port/1?options
# Optional, provide tags
com.getsentry.raven.jul.SentryHandler.tags=tag1:value1,tag2:value2
# Optional, provide release version of your application 
com.getsentry.raven.jul.SentryHandler.release=1.0.0
# Optional, provide environment your application is running in
com.getsentry.raven.jul.SentryHandler.environment=production
# Optional, override the server name (rather than looking it up dynamically)
com.getsentry.raven.jul.SentryHandler.serverName=server1
# Optional, select the ravenFactory class 
com.getsentry.raven.jul.SentryHandler.ravenFactory=com.foo.RavenFactory
# Optional, provide tag names to be extracted from MDC when using SLF4J
com.getsentry.raven.jul.SentryHandler.extraTags=foo,bar,baz
```

### In practice
```java
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyClass {
    private static final Logger logger = Logger.getLogger(MyClass.class.getName());

    void logSimpleMessage() {
        // This adds a simple message to the logs
        logger.log(Level.INFO, "This is a test");
    }

    void logException() {
        try {
            unsafeMethod();
        } catch (Exception e) {
            // This adds an exception to the logs
            logger.log(Level.SEVERE, "Exception caught", e);
        }
    }

    void unsafeMethod() {
        throw new UnsupportedOperationException("You shouldn't call that");
    }
}
```

### Unsupported features

As `java.util.logging` has no notion of MDC, the `extraTags` parameter is only
available when logging via SLF4J.

## Manual usage (NOT RECOMMENDED)
It is possible to use the client manually rather than using a logging framework
in order to send messages to Sentry. It is not recommended to use this solution
as the API is more verbose and requires the developer to specify the value of
each field sent to Sentry.

### In practice
```java
import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;


public class MyClass {
    private static Raven raven;

    public static void main(String... args) {
        // Creation of the client with a specific DSN
        String dsn = args[0];
        raven = RavenFactory.ravenInstance(dsn);

        // It is also possible to use the DSN detection system like this
        raven = RavenFactory.ravenInstance();
    }

    void logSimpleMessage() {
        // This adds a simple message to the logs
        raven.sendMessage("This is a test");
    }

    void logException() {
        try {
            unsafeMethod();
        } catch (Exception e) {
            // This adds an exception to the logs
            raven.sendException(e);
        }
    }

    void unsafeMethod() {
        throw new UnsupportedOperationException("You shouldn't call that");
    }
}
```

### In practice (advanced)

For more complex messages, it will be necessary to build an `Event` with the
`EventBuilder` class.

```java
import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import com.getsentry.raven.event.interfaces.MessageInterface;

public class MyClass {
    private static Raven raven;

    public static void main(String... args) {
        // Creation of the client with a specific DSN
        String dsn = args[0];
        raven = RavenFactory.ravenInstance(dsn);

        // It is also possible to use the DSN detection system like this
        raven = RavenFactory.ravenInstance();

        // Advanced: To specify the ravenFactory used
        raven = RavenFactory.ravenInstance(new Dsn(dsn), "com.getsentry.raven.DefaultRavenFactory");
    }

    void logSimpleMessage() {
        // This adds a simple message to the logs
        EventBuilder eventBuilder = new EventBuilder()
                        .withMessage("This is a test")
                        .withLevel(Event.Level.INFO)
                        .withLogger(MyClass.class.getName());
        raven.runBuilderHelpers(eventBuilder); // Optional
        raven.sendEvent(eventBuilder.build());
    }

    void logException() {
        try {
            unsafeMethod();
        } catch (Exception e) {
            // This adds an exception to the logs
            EventBuilder eventBuilder = new EventBuilder()
                            .withMessage("Exception caught")
                            .withLevel(Event.Level.ERROR)
                            .withLogger(MyClass.class.getName())
                            .withSentryInterface(new ExceptionInterface(e));
            raven.runBuilderHelpers(eventBuilder); // Optional
            raven.sendEvent(eventBuilder.build());
        }
    }

    void unsafeMethod() {
        throw new UnsupportedOperationException("You shouldn't call that");
    }
}
```

This gives more control over the content of the `Event` and gives access to the
complete API supported by Sentry.

### Static access

The most recently constructed `Raven` instance is stored statically so it may
be used easily from anywhere in your application.

```java
import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;

public class MyClass {
    public static void main(String... args) {
        // Create a Raven instance
        RavenFactory.ravenInstance();
    }
    
    public somewhereElse() {
        // Use the Raven instance statically. Note that we are
        // using the Class (and a static method) here 
        Raven.capture("Error message");
        
        // Or pass it a throwable
        Raven.capture(new Exception("Error message"));
        
        // Or build an event yourself
        EventBuilder eventBuilder = new EventBuilder()
                        .withMessage("Exception caught")
                        .withLevel(Event.Level.ERROR);
        Raven.capture(eventBuilder.build());
    }

}
```

Note that a Raven instance *must* be created before you can use the `Raven.capture`
method, otherwise a `NullPointerException` (with an explaination) will be thrown.
