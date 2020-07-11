<p align="center">
  <img src="https://i.ibb.co/93BM9VW/RNA-Scoop-logo-small.png"/>
</p>

**RNA Scoop** is a tool to visualize isoforms in single cell transcriptomes. Through use of an interactive t-SNE plot,
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
RNA-Scoop can be run as `java -jar /path/to/RNA-Scoop.jar ...`


## Implementation :pencil:

RNA-Scoop is written in Java with IntelliJ IDEA. It uses [T-SNE-Java](https://github.com/lejon/T-SNE-Java) and [JFreeChart - Future State Edition (FSE)](https://github.com/jfree/jfreechart-fse).
