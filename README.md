# JGen
Java Bytecode generation API

JGen is basically an interface from a user to a class file. It allows writing to and (limited) reading from a class file.
Furthermore, it allows user to not care at all about the Constant Pool, which is probably the most complex structure of a class.

A familiarity with the .class format is recommended. The class generation interface is pretty simple, but method code
has to be provided in Java assembly with named jumps. An example can be found in test/Test.java.

WARNINGS:

- The generated classes are of version 50 (Java 6), so features beyond that are not supported.

- All currently missing features are documented in the file jgen/missing_features.

- Feedback on the interface and recommended improvements is encouraged.


little note: There will probably be a C++ version (coming soon).
