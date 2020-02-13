# SimpleNativeHooks
Java library (with simple native components) for global keyboard and mouse listeners.

# Features:
* Hook for key presses and releases.
* Distinguish between left and right keys (e.g. left control, right shift, left alt, ...).
* Hook for mouse button clicks and releases.
* Hook for mouse movements.
* Support for multiple monitors.


# Requirements
* Java 1.7+
* Apple OSX:
  * i586, amd64
  * Enable Access for Assistive Devices
* Windows:
  * i586, amd64
  * .NET framework v4.0+
* Linux:
  * i586
  * X11 window manager.

# Demo application:
See example [here](src/org/simplenativehooks/Example.java).

# Maven dependency:
Include maven dependency by adding the following in your pom file:

```
<dependencies>
  <dependency>
    ... (existing dependencies)
  </dependency>

  <dependency>
    <groupId>org.repeats.simplenativehooks</groupId>
    <artifactId>simplenativehooks</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>

<repositories>
    <repository>
        <id>SimpleNativeHooks-maven-export</id>
        <url>https://raw.github.com/repeats/SimpleNativeHooks/maven-export/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```
