## Utility scripts for input files

*GTF operations:*
| make_gtf.py       | generate GTF file from a PAF file |
| annotate_gtf.py   | annotate GTF file with known transcript/gene names and IDs based on given annotation |
| filter_gtf.py     | filter GTF file based on a list of transcript IDs |

*Matrix operations:*
| make_matrix.py    | generate expression matrix file (rows: cells, columns: isoforms) from merged Salmon quant results |
| reduce_matrix.py  | reduce matrix dimension based on column labels (e.g. gene names) |
| make_umap.py      | generate UMAP 2D embedding from a matrix file |
