<p align="center">
  <img src="https://github.com/bcgsc/RNA-Scoop/blob/master/src/wiki/images/RNA-Scoop_logo_small.png?raw=true"/>
</p>

**RNA Scoop** is a tool to visualize isoforms in single cell transcriptomes. Through use of an interactive cell cluster plot,
users are able to explore isoforms in a single-cell transcriptome dataset of thousands of cells. 

Specifically, users are able to:

* View isoform structure
* Identify isoform expression levels within cells
* Compare isoform expression across clusters

Written by [Maria Stephenson](mailto:maria.stephenson235@gmail.com) and [Ka Ming Nip](mailto:kmnip@bcgsc.ca) :email:

---    

## Disclaimer :warning:

RNA-Scoop is designed to work with data produced from single-cell protocols that supports transcript isoform level analysis. In other words, 3' end capture protocols (such as 10x Genomics or Drop-Seq) designed for measuring gene expression in single cells are NOT supported.

## Dependency :pushpin:

* [Java SE Runtime Environment (JRE) 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
  * This is not the same as OpenJDK!

Alternatively, you can create a dedicated `conda` environment to install JRE8:
```
conda create -n rnascoop -c cyclus java-jre -y
```
**Please note that the above command does not install RNA-Scoop in `conda`!**

To activate this `conda` environment, use
```
conda activate rnascoop
```
You can run RNA-Scoop within this `conda` environment if you already have the RNA-Scoop JAR file. See the next section for more details.

To deactivate the active `conda` environment, use
```
conda deactivate
```

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

## Tutorial :mag:

Consult our [wiki pages](https://github.com/bcgsc/RNA-Scoop/wiki) for an introduction to highlight features in RNA-Scoop.

Watch Maria's 8-minute conference talk at the BioVis COSI at ISMB 2020.

[![RNA-Scoop @ BioVis ISMB 2020](https://img.youtube.com/vi/QPR_NVUQz5M/0.jpg)](https://www.youtube.com/watch?v=QPR_NVUQz5M)


## Setting max heap space in Java :floppy_disk:

The memory required for RNA-Scoop depends of the dimension (rows, columns) of the input expression matrix. If a memory error is encountered while running RNA-Scoop (i.e. `java.lang.OutOfMemoryError: Java heap space`), then the max heap space in Java needs to be increased, e.g.
```
java -Xmx16g -jar /path/to/RNA-Scoop.jar ...
```

This limits the maximum Java heap to 16 GB with the `-Xmx` option. See documentation for other [JVM options](https://docs.oracle.com/cd/E37116_01/install.111210/e23737/configuring_jvm.htm#OUDIG00071).

## Implementation :pencil:

RNA-Scoop is written in Java with IntelliJ IDEA. It uses the following external libraries:
* [T-SNE-Java](https://github.com/lejon/T-SNE-Java)
* [Java UMAP](https://github.com/tag-bio/umap-java)
* [JFreeChart (Future State Edition)](https://github.com/jfree/jfreechart-fse)
* [JSON-Java](https://github.com/stleary/JSON-java)
* [ControlsFX](https://github.com/controlsfx/controlsfx)

## Citing RNA-Scoop :scroll:

If you use RNA-Scoop in your work, please cite [our publication](https://academic.oup.com/nargab/article-abstract/doi/10.1093/nargab/lqab105/6445923):

> Maria Stephenson, Ka Ming Nip, Saber HafezQorani, Kristina K Gagalova, Chen Yang, René L Warren, Inanc Birol. RNA-Scoop: interactive visualization of transcripts in single-cell transcriptomes. NAR Genomics and Bioinformatics, Volume 3, Issue 4, December 2021, lqab105, https://doi.org/10.1093/nargab/lqab105
