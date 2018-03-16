[Testrecorder](http://testrecorder.amygdalum.net/)
============
[![Build Status](https://travis-ci.org/almondtools/testrecorder.svg?branch=master)](https://travis-ci.org/almondtools/testrecorder)
[![codecov](https://codecov.io/gh/almondtools/testrecorder/branch/master/graph/badge.svg)](https://codecov.io/gh/almondtools/testrecorder)

__Testrecorder__ is a tool for recording runtime behavior of java programs. The results of such a recording are executable JUnit-tests replaying the recorded behavior.

* You can use these tests as part of your characterization tests
* Or you can refactor them (they are pure java) to make up proper unit tests
* Even without reusing the generated code it could give valuable insights for code understanding

__Testrecorder__ uses an api to serialize objects to executable java code or hamcrest matchers.


Basic Usage
===========

## 1. Annotate the method to record
Annotate the method to record with `@Recorded`. For example you want to record this simple example

    package fizzbuzz;
    
    public class FizzBuzz {
        @Recorded
        public String fizzBuzz(int i) {
            if (i % 15 == 0) {
                return "FizzBuzz";
            } else if (i % 3 == 0) {
                return "Fizz";
            } else if (i % 5 == 0) {
                return "Buzz";
            } else {
                return String.valueOf(i);
            }
        }
    }

## 2. Configure the test serialization
Create a directory `agentconfig` (either on your classpath or in the directory you run from). This directory should contain at least two files:

* `net.amygdalum.testrecorder.SerializationProfile` containing a single line:

    fizzbuzz.SerializationProfile
 
* `net.amygdalum.testrecorder.SnapshotConsumer` containing a single line:

    fizzbuzz.TestGenerator

These files refer two the configuration classes. A `SerializationProfile` configures which classes/methods/fields should be analyzed, included or excluded from recording. A `SnapshotConsumer` is notified if some recorded `ContextSnapshot` is available. Typically we want to generate Tests from the snapshot, so we plug in a configured instance of `ScheduledTestGenerator`.

Now implement the two configuration classes:

    package fizzbuzz;
     
    public class SerializationProfile extends DefaultSerializationProfile {
    
        @Override
        public List<Classes> getClasses() {
            return asList(Classes.byPackage("fizzbuzz"));
        }
    
    }

`getClasses` defines all classes that are analyzed. Any method of an analyzed class may be recorded.

    package fizzbuzz;
    
    public class TestGenerator extends ScheduledTestGenerator {
    
        public TestGenerator(AgentConfiguration config) {
            super(config);
            this.generateTo = Paths.get("target/generated");
            this.dumpOnShutdown(true);
        }
    
    }

We use a typical `ScheduledTestGenerator` modifying it two have following properties:

* all tests will be generated to the directory `target/generated`
* shutdown of the host program should trigger test generation
   

## 3. Run your program with TestRecorderAgent
To run your program with test recording activated you have to call ist with an agent

`-javaagent:testrecorder-[version]-jar-with-dependencies.jar`

`testrecorder-[version]-jar-with-dependencies.jar` is an artifact provided by the maven build (available in maven repository).

## 4. Interact with the program and check results
You may now interact with your program and every call to a `@Recorded` method will be captured. After shutdown of your program all captured recordings will be transformed to executable JUnit tests, e.g.

    @Test
    public void testFizzBuzz0() throws Exception {
    
        //Arrange
        FizzBuzz fizzBuzz1 = new FizzBuzz();
        
        //Act
        String string1 = fizzBuzz1.fizzBuzz(1);
        
        //Assert
        assertThat(string1, equalTo("1"));
        assertThat(fizzBuzz1, new GenericMatcher() {
        }.matching(FizzBuzz.class));
    }

    ...
    
Advanced Topics
===============
Following subjects could be of further interest:

### [An Introduction to the Testrecorder Architecture](doc/Architecture.md)

### [Tuning the Output of Testrecorder](doc/TuningOutput.md)

### [Recordering Input/Output with Testrecorder](doc/RecordingIO.md)

### [Using the Testrecorder-API to serialize data and generate code](doc/API.md)

### [Extending Testrecorder with Custom Components](doc/Extending.md)

Limitations
===========
TestRecorder serialization (for values and tests) does not cover all of an objects properties. Problems might occur with:

* static fields
* synthetic fields (e.g. added by some bytecode rewriting framework)
* native state
* proxies
* state that influences object access (e.g. modification counter in collections) 

Examples
========
Examples can be found at [testrecorder-examples](https://github.com/almondtools/testrecorder-examples)

Some additional notes ...
=========================
The objective of Testrecorder is to provide an interface that is powerful, clean and extensible. To achieve this we will provide more and more configuration settings to extend the core framework. The fact that tests are generated automatically might rise wrong expectations: Testrecorder will probably always be an experts tool, meaning strong programming and debug skills are recommended to find the correct configuration and the necessary custom extensions.

Testrecorder was not yet tested on a large set of code examples. Some classes are not as easy to serialize as others, so if you encounter problems, try to write an issue. Hopefully - most fixes to such problems should be solvable with custom serializers or custom deserializers. 
 