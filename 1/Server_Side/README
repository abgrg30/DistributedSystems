MAKEFILE TARGETS

To compile all Java files, execute
        make
To run all test cases, run
        make tests
To package source files into an archive, run
        make archive

To generate documentation, execute
        make docs
The documentation for the package rmi can then be viewed at javadoc/index.html.
Alternatively, complete documentation of all classes and members, including test
cases, can be generated using
        make docs-all
and then viewed at javadoc-all/index.html.

To clean the build directories, execute
        make clean


TESTS

Various tests can be run by executing:
        java conformance.ConformanceTests
        java -cp ./:./unit unit.UnitTests
        java test.SelfTest
Conformance tests check the public interfaces of the classes in the rmi package
for conformance to the written specifications. The tests are thorough but not
exhaustive. Conformance tests are grouped by the packages they test. For
example, conformance tests for the RMI library, which is in the package rmi, are
grouped in the package conformance.rmi. Conformance tests are used for grading.
You have been provided with a number of conformance tests to help you find
problems with your code. However, there may be additional tests used by the
staff during grading. Testing thoroughly is your responsibility.

Unit tests can be written to check package-private classes. Unit tests are in
the same package as the class they are testing: a unit test for a class in the
package rmi would also be in the package rmi (whereas a conformance test would
be in the different package conformance.rmi). Unit tests, are, however, kept in
a different directory in the source tree. The Java classpath is altered when
running unit tests to put the unit tests logically in the same package as the
code they are testing.

The class test.SelfTest runs some basic self-tests on the testing library.
