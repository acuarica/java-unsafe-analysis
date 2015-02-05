Unsafe study
============


Java Unsafe Analysis
====================

The project contains the following folders

* 2015-msr
* 2015-msr-slides
* boa-client

* unsafe-analysis


* References
  - the isthmus in the VM
  https://blogs.oracle.com/jrose/entry/the_isthmus_in_the_vm

Oracle (Paul Sandoz) did a survey (316 responses).
We should look at it, and complement:
http://www.infoq.com/news/2014/02/Unsafe-Survey
http://stackoverflow.com/questions/5574241/using-sun-misc-unsafe-in-real-world

Paul Sandoz. Atomic VarHandles. July 30, 2014.
http://www.oracle.com/technetwork/java/jvmls2014sandoz-2265216.pdf
(JEP 193 Enhanced Volatiles, are VarHandles the same of our Lvalues??)

 check this!  Paul Sandoz. Safety Not Guaranteed: sun.misc.Unsafe and the quest for safe alternatives. DEVOXX talk. 2014.
http://cr.openjdk.java.net/~psandoz/dv14-uk-paul-sandoz-unsafe-the-situation.pdf
(this is essentially the paper we were thinking about writing)
Table on slide 28:

* Github search API
- https://api.github.com/search/repositories?q=unsafe+language:java&sort=stars&order=desc
http://boa.cs.iastate.edu/boa/

* Systems Software Research is Irrelevant
http://herpolhode.com/rob/utah2000.pdf

* JSR166
http://g.oswego.edu/dl/concurrency-interest/

* Eli Gottlieb, Deca
  http://www.infosun.fim.uni-passau.de/publications/docs/SSG+2012.pdf

Java with sun.misc.Unsafe
=========================

* Enhanced atomic access
  - JEP 193 Enhanced Volatiles
    http://openjdk.java.net/jeps/193

* De/Serialization
  - JEP 187 Serialization 2.0
    http://openjdk.java.net/jeps/187 404 Not found

* Reduce GC
  - Value types
    http://cr.openjdk.java.net/~jrose/values/values-0.html
  - JEP 189 Shenandoah: Low Pause GC for >20GB heap size.
    http://openjdk.java.net/jeps/189

* Efficient memory layout
  - Value types
    http://cr.openjdk.java.net/~jrose/values/values-0.html
  - Arrays 2.0 & Layouts
    http://cr.openjdk.java.net/~jrose/pres/201207-Arrays-2.pdf
    http://hg.openjdk.java.net/sumatra/sumatra-dev/scratch/file/tip/src/org/openjdk/sumatra/data/prototype/README

* Very Large Collections
  - Value types
    http://cr.openjdk.java.net/~jrose/values/values-0.html
  - Arrays 2.0 & Layouts
    http://cr.openjdk.java.net/~jrose/pres/201207-Arrays-2.pdf
    http://hg.openjdk.java.net/sumatra/sumatra-dev/scratch/file/tip/src/org/openjdk/sumatra/data/prototype/README

* Communicate across JVM boundary
  - Project Panama
  - JEP 191 FFI
    http://openjdk.java.net/jeps/191

C# with unsafe block
====================

- http://msdn.microsoft.com/en-us/library/y31yhkeb.aspx

Structs/value types
"A pointer cannot point to a reference or to a struct that contains references,
because an object reference can be garbage collected even if a pointer is 
pointing to it. The garbage collector does not keep track of whether an 
object is being pointed to by any pointer types."

- stackalloc, fixed, unsafe, valuetypes/structs in C#

* Communicate across JVM boundary
- Platform invoke



* Sample 
  - org.jocl
  - bridj
  - C#


# unsafe in Racket: unsafe-fl+ performance unchecked operation.

# Typed Assembly Language TAL

# Request for a user account

# https://blogs.oracle.com/jrose/entry/larval_objects_in_the_vm
  - Typestate in java for larval objects
  - Comment of Niko Matsakis, Rust:
I just want to drop a note to my research on Intervals.
It allows support for "temporarily mutable" objects like the larval objects you refer
 to, but using a rather different approach than Type State.
The basic idea is that users can denote the span of time in which the object is
mutable as part of its type. Once that span of time has expired, 
the object can no longer be changed.
One nice feature of this approach is that aliasing is no problem,
as the type of the object never changes --- instead,
the compiler is aware of whether a particular method occurs during the mutable span 
or not, and so the permitted operations change depending on when a method will execute.
Some more details are available here:
http://harmonic-lang.org/news/larval-objects-using-interv.html or in our recent paper: 
http://www.lst.inf.ethz.ch/research/publications/SPLASH_2010.html


- http://smallcultfollowing.com/babysteps/blog/2014/04/01/value-types-in-javascript/
Niko Matsakis

http://kotka.de/blog/2010/12/What_are_Pods.html
Pods

http://ruby-doc.org/core-2.1.5/Object.html#M000356
Freeze method

http://openjdk.java.net/jeps/191

JEP 193- Enhanced Volatiles
* Find a reference implementation

http://qualitascorpus.com/ for analysis code dataset



http://stackoverflow.com/questions/13003871/how-do-i-get-the-instance-of-sun-misc-unsafe

http://stackoverflow.com/questions/18220435/using-sun-misc-unsafe-what-is-the-fastest-way-to-scan-bytes-from-a-direct-byteb

http://stackoverflow.com/questions/5761702/can-one-break-a-secury-manager-with-sun-misc-unsafe

http://stackoverflow.com/questions/22242836/strange-behaviour-of-sun-misc-unsafe-put-on-solaris-sparcv9

http://stackoverflow.com/questions/7934779/using-sun-misc-unsafe-to-get-address-of-java-array-items

http://stackoverflow.com/questions/9323416/using-memory-allocated-by-sun-misc-unsafe-allocatememory-in-native-code

http://stackoverflow.com/questions/26995856/strange-behavior-in-sun-misc-unsafe-compareandswap-measurement-via-jmh

http://stackoverflow.com/questions/21589159/java-sun-misc-unsafe-confusion

http://stackoverflow.com/questions/24241335/waiting-at-sun-misc-unsafe-parknative-method

http://stackoverflow.com/questions/20494387/how-unsafe-is-the-use-of-sun-misc-unsafe-actually

http://stackoverflow.com/questions/12972918/why-does-park-unpark-have-60-cpu-usage

http://stackoverflow.com/questions/12638761/getting-error-as-sun-misc-unsafe-cannot-be-resolved-while-modifying-library

http://stackoverflow.com/questions/18687243/using-sun-misc-usafe-as-off-heap-memory-and-writing-memory-managers

http://stackoverflow.com/questions/12226123/busted-how-to-speed-up-a-byte-lookup-to-be-faster-using-sun-misc-unsafe

http://stackoverflow.com/questions/16723244/dealing-with-16-bit-characters-using-sun-misc-unsafe

http://stackoverflow.com/questions/7823665/why-jni-call-to-native-method-is-slower-than-similar-in-sun-misc-unsafe

http://stackoverflow.com/questions/1490760/sun-misc-unsafe-how-to-get-the-bytes-from-an-address

http://stackoverflow.com/questions/25234679/why-is-sun-misc-unsafe-unpark-described-unsafe

http://stackoverflow.com/questions/17671066/java-direct-memory-using-sun-misc-cleaner-in-custom-classes

http://stackoverflow.com/questions/6042858/can-i-override-object-with-sun-misc-unsafe

http://stackoverflow.com/questions/8462200/examples-of-forcing-freeing-of-native-memory-direct-bytebuffer-has-allocated-us

http://stackoverflow.com/questions/22846538/correct-use-of-arraybaseoffset-and-arrayindexscale

http://stackoverflow.com/questions/20157508/is-there-a-way-to-force-unload-a-class-by-using-the-sun-misc-unsafe-class

http://stackoverflow.com/questions/23709378/modifying-memory-via-unsafe-causing-exception-access-violation
