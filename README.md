<p align="center">
  <img src="https://github.com/bcgsc/RNA-Scoop/blob/master/src/wiki/images/RNA-Scoop_logo_small.png?raw=true"/>
</p>

**RNA Scoop** is a tool to visualize isoforms in single cell transcriptomes. Through use of an interactive cell cluster plot,
users are able to explore isoforms in a single-cell transcriptome dataset of thousands of cells. 

Specifically, users are able to:

* View isoform structure
* Identify isoform expression levels within cells
* Compare isoform expression across clusters

Written by [Maria Stephenson](mailto:mstephenson@bcgsc.ca) and [Ka Ming Nip](mailto:kmnip@bcgsc.ca) :email:

---    

## Dependency :pushpin:

* [Java SE Runtime Environment (JRE) 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

## Installation :wrench:

1. Download the binary tarball `rnascoop_vX.X.X.tar.gz` from the [releases](https://github.com/bcgsc/RNA-Scoop/releases) section
2. Extract the downloaded tarball with the command:
```
tar -zxf rnascoop_vX.X.X.tar.gz
```
RNA-Scoop can be run as simple as:
```
java -jar /path/to/RNA-Scoop.jar ...
```

## Setting max heap space in Java :floppy_disk:

The memory required for RNA-Scoop depends of the dimension (rows, columns) of the input expression matrix. If a memory error is encountered while running RNA-Scoop (i.e. `java.lang.OutOfMemoryError: Java heap space`), then the max heap space in Java needs to be increased, e.g.
```
java -Xmx16g -jar /path/to/RNA-Scoop.jar ...
```

This limits the maximum Java heap to 16 GB with the `-Xmx` option. See documentation for other [JVM options](https://docs.oracle.com/cd/E37116_01/install.111210/e23737/configuring_jvm.htm#OUDIG00071).

## Tutorial :mag:

Please consult our [wiki pages](https://github.com/bcgsc/RNA-Scoop/wiki) for an introduction to highlight features in RNA-Scoop.

## Implementation :pencil:

RNA-Scoop is written in Java with IntelliJ IDEA. It uses the following external libraries:
* [T-SNE-Java](https://github.com/lejon/T-SNE-Java)
* [Java UMAP](https://github.com/tag-bio/umap-java)
* [JFreeChart (Future State Edition)](https://github.com/jfree/jfreechart-fse)
* [JSON-Java](https://github.com/stleary/JSON-java)
* [ControlsFX](https://github.com/controlsfx/controlsfx)
