
Preferences - XML
=================

An implementation of the Java Preferences API (java.util.prefs) to store
preferences in an XML file, in a subdirectory of a Java program's working
directory.  Written because of my frustration that the Windows implementation of
the Java Preferences API that comes with the JRE uses the registry.

Precompiled Jar downloads available in the [Downloads](preferences-xml/downloads)
section.


Requirements
------------

 - Java 6
 - JAXB 2.2 (included)
 - [JAXB2 Basics XJC Ant task](http://confluence.highsource.org/display/J2B/JAXB2+Basics+XJC+Ant+Task) (for generating the class files from the XML schema, included)
 - [XmlElementWrapper](http://www.conspicio.dk/blog/bjarne/jaxb-xmlelementwrapper-plugin) (for generating the class files from the XML schema, included and recompiled for JAXB 2.2)


Usage
-----

Just put the JAR somewhere in your project classpath and it should be picked-up
and used by Java when you access the Java Preferences API.  Preferences will be
written to files inside a `.preferences` sub-directory of your project: 1 for
the system preferences, and 1 for every user account that uses the program.
