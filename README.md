# Java Unsafe Analysis --- Usage and Impact of the Unsafe Java API

## Introduction

We have installed OpenJDK version 7.
git
openjdk-7-jdk
ant
aria2c

Framework to analyse maven artifacts.


## Getting Started Guide

This artifact is packaged as a VirtualBox image.
The version we have used is **VirtualBox 4.3.28**.
The guest operating system in the VirtualBox image is Linux Ubuntu Desktop edition 14.04.2 LTS 32-bits.
The virtual machine is configured with 1GB of memory.
The host machine should have enough physical memory to support this configuration.
The VM is setup with GMT+2 time.
You may want to change this according to your location.
The keyboard is English US keyboard.
Both the VirtualBox and guest VM names is vm-oopsla-ae-221. 

the virtual machine is configured to automatically login after boot up.
So you don't need to enter any user name, not password.
If needed, the username and password are the following:

    username: ae
    password: ae

Our tools and analysis are available online in the BitBucket service as Git repository:
https://bitbucket.org/acuarica/java-unsafe-analysis

We used the command 

    git clone https://acuarica@bitbucket.org/acuarica/java-unsafe-analysis.git

to clone the repo inside the virtual machine in the user directory (/home/ae denoted as ~).

Thus, the repository is located at:

    ~/java-unsafe-analysis

We have installed OpenJDK version 7.
git
openjdk-7-jdk
ant

For convenience we also have installed the following packages:
vim
emacs


## Step-by-Step Instructions



### Virtual machine virtual box

For each pattern that we have found, and maybe for some others use cases,
we can make an evaluation whether using *Unsafe* makes a real difference, 
in terms of performance.
We can compare different implementations, e.g., 
Reflection-based (when applicable) vs. JNI vs. *Unsafe*.

Walter seemed quite interested in *Unsafe*.
They (with Yudi) are using *Unsafe* to manipulate an extended object header.
Walter explicitly stated not to remove *Unsafe* 
because there are cases when the programmer needs to manipulate memory directly 
independently of the patterns we found.
Therefore Walter might be interested to work in Demystifying *Unsafe* performance.

### Maven mining infrastructure

Now that we have the new disks, we can download the whole maven repo.
I would like to do something with that, if it is possible.
Maybe something like exposing a service like boa.
In the short term (one week or so), I would like to study the usage trend of Unsafe in Maven.

**Architecture**
How to best convert the Maven repository to a database suitable to query.

**Interface**
Possibly expose a RESTful API to query the Maven repository, in a database like manner.
Maybe we could give access to the API to registered users like BOA does.

Current downloaded data:

    Database size: 1.8T db/
    # of .jar files: 1371377
    # of .pom files: 899498
    # of -sources.jar files: 724963

**Potential Implementation**
Using bddbddb.

**Potential Uses**
Approximational computing. Search in Maven.
Loop perforation, profiling (Martin Rinard)
Automatically and Dynamically Trading Accuracy for Performance and Power

**Related Work**
The Sourcerer Project:
"Sourcerer is an ongoing research project at the University of California, Irvine 
aimed at exploring the open source phenomenon through the use of code analysis.
The open source movement has generated an extremely large body of code, 
which presents a tremendous opportunity to software engineering researchers.
Not only do we leverage this code for our own research, 
but we provide the open source Sourcerer Infrastructure for other researchers to use."

### Study the evolution of APIs

Related to Unsafe performance study.
Having the whole Maven repository will allows us to study the trend of APIs usage.
In particular study APIs adoption, i.e., which features are used the most and which ones are not used.
This topic was mentioned by Michele in one of the meetings.

### Automatic DSL generation from pattern-based usages on APIs

The biggest problem I saw while working on the paper was how to discover usage patterns.
If we improve the bytecode analysis,
maybe it would be possible to came up with a method to systematically discover usage patterns,
which in turn they can be used to create DSLs.

Jungloid Mining: Helping to Navigate the API Jungle.

### Cassandra maven search consistency policy

Work in collaboration with Nosheen to search for Cassandra consistency policies.

### Do something similar to defects4j

Mohammed defects4j maven.

### Request for Dissertation Proposal

Make the *Unsafe*, Safe.

### Automatic class partioning

    class MyArrayList<T> extends Iterator {
        T[] elems = new T[10];
        int size = 0;
        int whereAreWe = 0;

        public void add(T elem) {
            if (size == elems.length) {
                T[] ts = new T[elems.length * 2];
                for (int i = 0; i < elems.length) {
                    ts[i] = elems[i];
                }
                elems = ts;
            }

            elems[size] = elem;
            size++;
        }

        public T get(int index) {
            return elems[index];
        }

        public int size() {
            return size;
        }

        public boolean hasNext() {
            whereAreWe < size;
        }

        public T next() {
            T result = elems[whereAreWe];
            whereAreWe++;
            return result;
        }
    }

Partition class MySet into two classes, MySet<T> with methods add, get and size, 
and MySetIterator<T> with methods hasNext and next 
despite the fact that these methods implemement Iterator interface (without remove).

**Single Responsibility Principle**
It is about a class doing more than on thing.
This is similar, a class should do only one thing.
The final goal is a refactor tool that enables this kind of change.

**Relation with typestates**
The idea is to determine pre and post conditions for each method.
If we can find that on of the methods if the pre of another one, we can related them and create a partition.

**Future work**
This idea can be extended to analyse different kind of patterns, design patterns, best practices,
SOLID practices (but may be not LSP), identify them, and refactor target code for best design.

## Minutes

### 16/04/2014 FAN meeting @ ETH

* Walter seemed quite interested in *Unsafe*.
  They (with Yudi) are using *Unsafe* to manipulate an extended object header.
  Walter explicitly stated not to remove *Unsafe* 
  because there are cases when the programmer needs to manipulate memory directly 
  independently of the patterns we found.
  Therefore Walter might be interested to work in Demystifying *Unsafe* performance.

### 17/04/2015 Meet with Matthias and Nate

* Gathering students for PF2.
  Taking the best PF2 students to help to implement some stuff.
  Software Performance is not worthy, because good bachelor students leave after graduating.

* Using mastery check for PF1.
  Teaching two languages in one semester.
  Nate mention brown, teaching parrot and racket.
  half semester one language, and for the good ones another extra course.

* Standford framwerok datalog bytecode bddbddb.
  query analysis for control flow graph.
  scalability problem.
  Optimized to add new versions. bytecode as facts.

### 29/04/2015 Meet with Matthias and Nate

* Ask nate about the language with values as types.
Related ask about language with optimizations at language level,
x * 2 = x << 1;

* Guod/Unsafe related, what's the mail goal?
  Rust, Cyclone, Racket, C# already have this feature,
  Implement this in java would be a dead end?
  Only syntax sugar?
  - Valuetypes
  - take the address

* Maven mining, what's the point?
  bddbddb investigated a bit, but what for?
  LogicBlocks, company selling datalog.

* Unsafe performance study, maybe.

* code smells, refactoring papers, floss refactor
  introduce generics, hummers paper.
  johanes most general types.
  ecoop essence paper.

