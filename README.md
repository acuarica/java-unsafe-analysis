
# Use at Your Own Risk: The Java Unsafe API in the Wild

# Introduction

This artifact implements the study in the paper [Use at Your Own Risk: The Java Unsafe API in the Wild](http://dx.doi.org/10.1145/2814270.2814313)

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



## Step-by-Step Instructions

The following instructions describe how to reproduce the data in Section 4 and Section 5 of our paper.

### Gathering Artifacts

The first step in our workflow is to get a representative subset of artifacts to get analyzed.
This section implements Section 4.1 of our paper.

#### Get Maven Index

To begin with, it is needed an index of all Maven artifacts.
We get the maven index from a mirror and then uncompress it.
This step requires an active Internet connection.
This can be done using:

    ant fetchindex

**Output:**

* cache/nexus-maven-repository-index.gz: The compressed Maven Index.
* cache/nexus-maven-repository-index: Uncompressed Maven Index for further processing. This file is in a binary format. It consists of a list of records. Each record represent a file in the repository. We have written a parser for this index.


#### Build Download List

Afterwards, it is necessary to build a list of artifacts to download in order to analyze them.
Due to space constraints, the debug target only downloads a subset of artifacts to analysis, while the release target downloads all artifacts.
But because the dependency analysis does not make sense in a subset of the artifacts,
we download all [POM files](http://maven.apache.org/pom.html).
The following command does so:

    ant buildurilist-debug
    
Or alternatively, if you want to download every artifact needed for the analysis:

    ant buildurilist-release

**Output:**

* out/uri.list: List of URIs to download in aria2 format. Refer to <http://aria2.sourceforge.net/manual/en/html/aria2c.html#input-file> for more information about this file format.


#### Get Artifacts

The next step is to actually download the artifacts specified by the previous step.
This command downloads all files specified in the out/uri.list file.
Again, this step requires an active Internet connection.
This can be done using:

    ant fetchartifacts

**Output:**

* cache/repo/ * * .jar and cache/repo/ * * .pom: Files are downloaded in the cache/repo folder.
Some artifacts that appear in the Maven Index do not exist in the repository, therefore it is possible to get errors for those artifacts.


### Determining Usage

At this stage, everything is set-up to actually start the analysis.
This section implements Section 4.2 of our paper.

#### Run Analysis on Maven Repository

To analyse the Maven Repository use:

    ant analyze-debug

Or alternatively,

    ant analyze-release

If you want to run the complete analysis.

**Output:**

* out/unsafe-maven.csv: CSV file with the call sites and field usage to Unsafe in Maven and JDK8, represented one per line.
This file contains the following columns:

  * className: Name of the class where the call site or field usage appears;
  * methodName: Name of the method where the call site or field usage appears;
  * methodDesc: Descriptor of the method where the call site or field usage appears;
  * owner: Type of the call or usage target. This value must be ***sun/misc/Unsafe***.
  * name: Name of the target method or field in sun.misc.Unsafe;
  * desc: Descriptor of the target method or field in sun.misc.Unsafe;
  * groupId, artifactId, and version: [Maven Coordinates](http://maven.apache.org/pom.html#Maven_Coordinates) of the artifacts where the call site of field appears;
  * size: Size of the Java archive;
  * ext: Extension of the Java archive (usually jar, but it can be ejb, war, or ear).


### Determining Impact

This section implements Section 4.3 of our paper.
To compute dependencies, two commands are required.

#### Extract Depedencies

To extract all dependency information from the POM files, run:

    ant extractdeps

**Output:**

* out/maven-depgraph.csv: Dependency information gathered from the POM files.
This file contains the following columns:

  * groupId and artifactId: Maven Coordinates of the POM file being analyzed.
  * depGroupId, depArtifactId, depVersion, and depScope: Maven Coordinates and scope of the dependency.

#### Compute Transitive Dependencies

We compute the depedencies considering [Maven Scopes](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html).
To build the transitive clousure of reverse dependencies of each artifact, run:

    ant computedeps

**Output:**

* out/maven-invdeps-all-list.csv: List of dependencies in all scopes.
This file contains two columns.
It states that the artifact in the first column has a dependency to the artifact specified in the second column.

* out/maven-invdeps-production-list.csv:
List of dependencies without the test scope.
Same file format as above.

* out/maven-invdeps-all.csv:
Total count of dependencies in all scopes.
This file contains two columns.
The first one indicates the artifact, and the second how many transitive inverse dependencies it has.

* out/maven-invdeps-production.csv:
Total count of dependencies without the test scope.
Same file format as above.


### Which Features of Unsafe Are Actually Used?

This section implements Section 4.4 of our paper.

#### R Analysis

The R scripts are executed by a Makefile script.
To run the R analysis, issue:

    make

**Output:**

All output files are pdf documents containing plots and tables.
The following plots are included in the paper:

* out/overview.pdf: Shows the overall call sites to Unsafe. This plot corresponds to Figure 1 in the paper.

* out/overview-field.pdf: Shows the overall field usages of Unsafe. This plot corresponds to Figure 2 in the paper.


The following plots are not shown in the paper, but they helped us to identify patterns.

* out/artifacts.pdf: Contains the call sites to unsafe for each artifact by artifact and class.

* out/classunit.pdf: Contains the groups by cluster and how similar are in the dendrogram.


***Notice.***
When executing \*-debug targets, the numbers that appear in these plots are different from the ones appearing in the paper.
This is because, due to space limitations, not all jar files were downloaded, and therefore were not analyzed.
Giving fewer call sites and field usages to Unsafe.
However, when running \*-release targets, the numbers obtained should be similar to the ones in our paper.
Still there might be differences because Java archives in the Maven repository might have changed since we performed the analysis. See Section 4.1 for more details.


### Question/Answer Database Analysis

This section implements Section 5 of our paper.
We provide the sources, in Scala, of the scripts needed to perform the analysis of the usage of sun.misc.Unsafe in \stackoverflow.
The whole process requires four steps to analyze the Stack Overflow discussions, extract the information.


#### Identifying Relevant Discussions

The first step consists in identifying discussions talking about sun.misc.Unsafe.
Due to space constraints, we cannot replicate the full parsing phase. Instead, we provide a balanced dataset of 1152 discussions containing 576 discussions related to sun.misc.Unsafe and 576 discussions randomly taken among the Stack Overflow discussions tagged as java, and not concerning sun.misc.Unsafe.
The discussions are already parsed and provided in JSON format.
Run the following:

    cd stackoverflow

    sh step1-default.sh


**Output:**

* results/candidates.csv: A CSV file containing all the ids of the discussions referring to sun.misc.Unsafe.


#### Refining Parsing Results

In the second step we refined the result by analyzing each question and answer to understand how sun.misc.Unsafe is used.
We take into consideration 

Run the following:

    sh step2-default.sh

**Output:**

* results/intermediate.csv: An intermediate CSV file containing information regarding the usage of sun.misc.Unsafe (i.e., presence of type, method, or term ``unsafe'') in each post (e.g., question or answer) of a discussion.


#### Grouping Usages per Post

In the third step we extract all the usages of sun.misc.Unsafe, and we build data data to be prepare the plotting in last step.

Run the following:

    sh step3-default.sh

**Output:**

* results/method-usages.csv: A CSV file containing usages of sun.misc.Unsafe members in question, answers, or both, grouped per member.
* results/method-usages-details.csv: A CSV file containing each single usage of \smu{}.

#### Plotting sun.misc.Unsafe Usages

We generate the bar chart (Figure 3 in the paper) representing the usages of sun.misc.Unsafe in Stack Overflow.

Run the following:

    cd ..

    make so

**Output:**

* out/so.pdf: Shows how many questions and answers related to Unsafe appear in StackOverflow.
This plot corresponds to Figure 3 in the paper.
