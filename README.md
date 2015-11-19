
# Use at Your Own Risk: The Java Unsafe API in the Wild

# Getting Started

Our artifact uses external tools to build and run, which are:

* [Git](https://git-scm.com/): To clone our repository.
* [Java 7](http://openjdk.java.net/):  To compile and run our applications.
* [Ant](http://ant.apache.org/): Software tool for automating software build processes.
* [Aria2](http://aria2.sourceforge.net/): Tool to automate downloads of a large amount of files.
* [R](http://www.r-project.org/): The R interpreter used in our analysis.
Version of R required is 3.1.0 or higher, because we depend on R package [plyr](http://cran.r-project.org/web/packages/plyr/index.html)
We also installed the following R packages used in our analysis: *reshape2*, *ggplot2*, *ggdendro*, and *gridExtra*.
* [LaTeX](http://www.latex-project.org/) and [Ghostscript](http://www.ghostscript.com/): To post-process some PDF plots.

# Repository Setup

Our artifact consists of three main components:
a) a Java project to download and extract information for Java archives and POM files from Maven Repository,
b) a Scala project to extract information about Stack Overflow posts, and
c) R scripts to analyze the data and plot the results.
An Eclipse Java project is located at the root of the repository.
The Scala project is located inside the *stackoverflow* folder.
The R scripts are inside the *analysis* folder.

To start, within a terminal, change the current directory to the repository root directory.
Our repository contains an *ant* build file to manage the build process.
To see all available ant targets, issue the following command in the root folder of the repository mentioned above.

    ant -projecthelp

You should see the following output:

    Buildfile: /home/ae/java-unsafe-analysis/build.xml
    Java Unsafe Analysis to study usage and impact of Unsafe API in Java
    Main targets:

     analyze-debug         Analyze the Maven repository and JDK8 runtime in Debug mode.
     analyze-release       Analyze the Maven repository and JDK8 runtime in Release mode.
     analyze-tests         Run tests for Unsafe analysis.
     buildurilist-debug    Builds the list of artifacts to download in Debug mode.
     buildurilist-release  Builds the list of artifacts to download in Release mode.
     check                 Run tests for Unsafe analysis, dependency extraction and aria2.
     clean                 Removes the build and out directories.
     compile               Compiles all Java files.
     computedeps           Computes the inverse transitive dependencies.
     extractdeps           Extract the dependency information from POM files.
     extractdeps-tests     Run tests for the dependency extraction.
     fetchartifacts        Fetches all artifacts specified by the URI list using aria2.
     fetchgzindex          Fetches the Maven Index (compressed) from a mirror using aria2.
     fetchindex            Fetches and uncompress the Maven Index from a mirror using aria2.
     mkbuilddir            Creates the build directory, used for output of compilation.
     mkcachedir            Creates the cache directory, used to cache downloaded files.
     mkoutdir              Creates the output directory, used for output files.


To check whether all components are setup correctly, first run:

    ant check

This target runs the tests of the Unsafe analysis, and dependency extraction.
Moreover it checks whether *aria2* is correctly installed.

Then, to check whether *R*, the *R* packages, LaTeX, and Ghostscript are succefully installed, issue:

    make check

Your output on the console should be similar to the following:

    r --slave --vanilla --file=analysis/check.r
    [1] "R packages succefully loaded"
    latexmk -quiet -view=pdf -output-directory=out analysis/check.tex
    Latexmk: Run number 1 of rule 'pdflatex'
    This is pdfTeX, Version 3.1415926-2.4-1.40.13 (TeX Live 2012)
     restricted \write18 enabled.
    entering extended mode
    gs -q -sDEVICE=pdfwrite -sOutputFile="out/check-land.pdf" -dNOPAUSE -dEPSCrop 
      -c "<</Orientation 2>> setpagedevice" -f "out/check.pdf" -c quit

and the check.pdf and check-land.pdf files should be created in the out folder.

At any point, if you want to start the experiments from scratch, you can run the following command to remove the build and out folders:

    ant clean

Notice that this command does not remove the *cache* folder.
This is to avoid downloading the Maven Index and artifacts over again.

