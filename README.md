
Preferences - XML
=================

[![Build Status](https://travis-ci.org/ultraq/preferences-xml.svg)](https://travis-ci.org/ultraq/preferences-xml)
[![Coverage Status](https://coveralls.io/repos/github/ultraq/preferences-xml/badge.svg?branch=master)](https://coveralls.io/github/ultraq/preferences-xml?branch=master)
[![GitHub Release](https://img.shields.io/github/release/ultraq/preferences-xml.svg?maxAge=3600)](https://github.com/ultraq/preferences-xml/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/nz.net.ultraq.preferences/preferences-xml.svg?maxAge=3600)](http://search.maven.org/#search|ga|1|g%3A%22nz.net.ultraq.preferences%22%20AND%20a%3A%22preferences-xml%22)
[![License](https://img.shields.io/github/license/ultraq/preferences-xml.svg?maxAge=2592000)](https://github.com/ultraq/preferences-xml/blob/master/LICENSE.txt)

An implementation of the Java Preferences API (`java.util.prefs`) to store
preferences in an XML file, in a subdirectory of a Java program's working
directory.  Written because of my frustration that the Windows implementation of
the Java Preferences API that comes with the JRE uses the registry.


Installation
------------

Minimum of Java 8 required.

### Standalone distribution

Copy the JAR from [the latest release bundle](https://github.com/ultraq/preferences-xml/releases),
or build the project from the source code here on GitHub.

### For Maven and Maven-compatible dependency managers

Add a dependency to your project with the following co-ordinates:

 - GroupId: `nz.net.ultraq.preferences`
 - ArtifactId: `preferences-xml`
 - Version: (as per the badges above)


Usage
-----

Once the JAR is in your project classpath it should be picked-up and used by
Java when you access the Java Preferences API.  Preferences will be written to
files inside a `.preferences` sub-directory of your project: 1 for the system
preferences, and 1 for every user account that uses the program.


Shout outs
----------

 - [JAXB2 Basics XJC Ant task](http://confluence.highsource.org/display/J2B/JAXB2+Basics+XJC+Ant+Task) (for generating the class files from the XML schema, 0.6.3 included)
 - [XmlElementWrapper](http://www.conspicio.dk/blog/bjarne/jaxb-xmlelementwrapper-plugin) (for generating the class files from the XML schema, included and recompiled for JAXB 2.2)
