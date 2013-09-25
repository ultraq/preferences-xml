
Preferences - XML
=================

An implementation of the Java Preferences API (java.util.prefs) to store
preferences in an XML file, in a subdirectory of a Java program's working
directory.  Written because of my frustration that the Windows implementation of
the Java Preferences API that comes with the JRE uses the registry.

 - Current version: 1.2.3
 - Release date: ?? ??? 2013


Requirements
------------

 - Java 6
 - JAXB 2.2 (2.2.6 and its dependencies included)
 - [JAXB2 Basics XJC Ant task](http://confluence.highsource.org/display/J2B/JAXB2+Basics+XJC+Ant+Task) (for generating the class files from the XML schema, 0.6.3 included)
 - [XmlElementWrapper](http://www.conspicio.dk/blog/bjarne/jaxb-xmlelementwrapper-plugin) (for generating the class files from the XML schema, included and recompiled for JAXB 2.2)


Installation
------------

### Standalone distribution
Copy the JAR from [the latest release bundle](https://github.com/ultraq/preferences-xml/releases),
or build the project from the source code here on GitHub.

### For Maven and Maven-compatible dependency managers
Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.preferences`
 - ArtifactId: `preferences-xml`
 - Version: `1.2.3`


Usage
-----

Once the JAR is in your project classpath it should be picked-up and used by
Java when you access the Java Preferences API.  Preferences will be written to
files inside a `.preferences` sub-directory of your project: 1 for the system
preferences, and 1 for every user account that uses the program.


Changelog
---------

### 1.2.3
 - Project structure reorganization after major fixes to the Gradle build
   scripts.

### 1.2.2
 - Switched from Ant to Gradle as a build tool.
 - Made project available from Maven Central.  Maven co-ordinates added to the
   [Installation](#installation) section.

### 1.2.1
 - Added JARs needed for generating Java class files from the schema.

### 1.2
 - Initial GitHub version.

