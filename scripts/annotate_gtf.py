import re
import argparse
import time

from intervaltree import IntervalTree


class Gene:
    def __init__(self, gene_id, chromosome, start_coord, end_coord, strand, gene_name=None):
        self.gene_name = gene_name
        self.gene_id = gene_id
        self.chromosome = chromosome
        self.strand = strand
        self.start_coord = start_coord
        self.end_coord = end_coord
        self.transcripts = []
        self.transcript_tree = IntervalTree()

    def add_transcript(self, transcript):
        self.transcripts.append(transcript)
        self.transcript_tree[transcript.start_coord:transcript.end_coord] = transcript

    def set_start_coord(self, start_coord):
        self.start_coord = start_coord

    def set_end_coord(self, end_coord):
        self.end_coord = end_coord


class Transcript:
    def __init__(self, transcript_id, chromosome, strand, start_coord, end_coord, transcript_name=None,
                 orig_gene_id=None):
        self.transcript_name = transcript_name
        self.transcript_id = transcript_id
        self.chromosome = chromosome
        self.strand = strand
        self.gene_id = None
        self.gene_name = None
        self.orig_gene_id = orig_gene_id
        self.start_coord = start_coord
        self.end_coord = end_coord
        self.exons = []

    def add_exon(self, exon):
        self.exons.append(exon)
        self.exons.sort(key=lambda exon: exon.start_coord)

    def set_start_coord(self, start_coord):
        self.start_coord = start_coord

    def set_strand(self, strand):
        self.strand = strand

    def set_end_coord(self, end_coord):
        self.end_coord = end_coord

    def set_gene_id(self, gene_id):
        self.gene_id = gene_id

    def set_gene_name(self, gene_name):
        self.gene_name = gene_name


class Exon:
    def __init__(self, start_coord, end_coord):
        self.start_coord = start_coord
        self.end_coord = end_coord


gene_tree = {}
transcripts = {}

gene_name_pattern = re.compile("\s*gene_name\s*\"(\S+)\"\s*")
gene_id_pattern = re.compile("\s*gene_id\s*\"(\S+)\"\s*")
transcript_name_pattern = re.compile("\s*transcript_name\s*\"(\S+)\"\s*")
transcript_id_pattern = re.compile("\s*transcript_id\s*\"(\S+)\"\s*")


def main():
    parser = argparse.ArgumentParser(description='Generate GTF from PAF file')
    parser.add_argument('annotation_gtf', metavar='GTF', type=str,
                        help='path of annotation GTF file')
    parser.add_argument('input_gtf', metavar='GTF', type=str,
                        help='path of input GTF file')
    parser.add_argument('output_gtf', metavar='GTF', type=str,
                        help='path of output GTF file')
    parser.add_argument('--threshold', metavar='INT', type=int,
                        default=50,
                        help='minimum overlap between transcript and gene (MUST BE <= TRANSCRIPT LENGTH) [%(default)s]')
    args = parser.parse_args()

    annotation_gtf_path = args.annotation_gtf
    input_gtf_path = args.input_gtf
    output_gtf_path = args.output_gtf
    threshold = args.threshold
    start_time = time.time()
    parse_annotation_gtf(annotation_gtf_path)
    end_time = time.time()
    print("parsing annotation took: ", end_time - start_time)
    parse_input_gtf(input_gtf_path)
    start_time = end_time
    end_time = time.time()
    print("parsing input took: ", end_time - start_time)
    add_gene_information(threshold)
    start_time = end_time
    end_time = time.time()
    print("adding gene info took: ", end_time - start_time)
    write_to_output_file(output_gtf_path)
    start_time = end_time
    end_time = time.time()
    print("writing to file took: ", end_time - start_time)


def parse_annotation_gtf(annotation_gtf_path):
    last_parsed_gene = None
    last_parsed_transcript = None
    annotation_gtf = open(annotation_gtf_path, "r")
    for line in annotation_gtf:
        line_elems = get_line_elems(line)
        if len(line_elems) == 9:
            last_parsed_gene, last_parsed_transcript = parse_annotation_gtf_line(last_parsed_gene,
                                                                                 last_parsed_transcript, line_elems)


def parse_input_gtf(input_gtf_path):
    input_gtf = open(input_gtf_path, "r")
    for line in input_gtf:
        line_elems = get_line_elems(line)
        if len(line_elems) == 9:
            parse_input_gtf_line(line_elems)


def add_gene_information(threshold):
    for transcript in transcripts.values():
        chromosome_gene_tree = gene_tree.get(transcript.chromosome)
        if chromosome_gene_tree is not None:
            overlapping_genes_intervals = chromosome_gene_tree[transcript.start_coord + threshold]
            overlapping_genes_intervals.update(chromosome_gene_tree[transcript.end_coord - threshold])
            num_overlapping_gene_intervals = len(overlapping_genes_intervals)

            if num_overlapping_gene_intervals == 1:
                overlapping_gene = overlapping_genes_intervals.pop().data
                assign_transcript_to_gene(overlapping_gene, transcript)

            elif num_overlapping_gene_intervals > 1:
                overlapping_genes = list(map(lambda gene_interval: gene_interval.data, overlapping_genes_intervals))
                potential_genes = find_potential_genes_by_junctions(transcript, overlapping_genes)

                if len(potential_genes) == 1:
                    assign_transcript_to_gene(potential_genes[0], transcript)
                else:
                    if len(potential_genes) > 1:
                        potential_genes = find_potential_genes_by_num_base_match(transcript, potential_genes)
                    else:
                        potential_genes = find_potential_genes_by_num_base_match(transcript, overlapping_genes)
                    assign_transcript_to_gene(potential_genes[0], transcript)


def write_to_output_file(output_file_path):
    output_file = open(output_file_path, "w")
    source = "rnascoop"
    feature = "exon"
    novel_count = 0
    for transcript in transcripts.values():
        seq_name = transcript.chromosome
        strand = transcript.strand
        transcript_id = transcript.transcript_id
        if transcript.gene_id is None:
            gene_id = "NOVEL" + "_" + transcript.orig_gene_id
            novel_count += 1
        else:
            gene_id = transcript.gene_id
        gene_name = transcript.gene_name
        if gene_name is None:
            for exon in transcript.exons:
                gtf_exon_line = seq_name + "\t" + source + "\t" + feature + "\t" + str(exon.start_coord) + "\t" + \
                                str(exon.end_coord) + "\t" + "." + "\t" + strand + "\t" + "0" + "\t" + "gene_id \"" + \
                                gene_id + "\"; " + "transcript_id \"" + transcript_id + "\";" + "\n"
                output_file.write(gtf_exon_line)
        else:
            for exon in transcript.exons:
                gtf_exon_line = seq_name + "\t" + source + "\t" + feature + "\t" + str(exon.start_coord) + "\t" + \
                                str(exon.end_coord) + "\t" + "." + "\t" + strand + "\t" + "0" + "\t" + "gene_id \"" + \
                                gene_id + "\"; " + "transcript_id \"" + transcript_id + "\";" + \
                                " gene_name \"" + gene_name + "\";\n"
                output_file.write(gtf_exon_line)


def remove_comments(line):
    return line.split("#")[0]


def get_line_elems(line):
    no_comments_line = remove_comments(line)
    line_elems = no_comments_line.split("\t")
    return line_elems


def parse_annotation_gtf_line(last_parsed_gene, last_parsed_transcript, line_elems):
    chromosome = line_elems[0]
    feature = line_elems[2]
    start_coord = int(line_elems[3])
    end_coord = int(line_elems[4])
    strand = line_elems[6]
    tags = line_elems[8].split(";")
    if feature == "gene":
        last_parsed_gene = parse_annotation_gene(chromosome, end_coord, start_coord, strand, tags)
    elif feature == "transcript":
        last_parsed_transcript = parse_annotation_transcript(last_parsed_gene, chromosome, end_coord, start_coord,
                                                             strand, tags)
    elif feature == "exon":
        parse_annotation_exon(last_parsed_transcript, end_coord, start_coord, tags)
    return last_parsed_gene, last_parsed_transcript


def parse_annotation_gene(chromosome, end_coord, start_coord, strand, tags):
    gene_names = get_attribute_from_tags(gene_name_pattern, tags)
    gene_ids = get_attribute_from_tags(gene_id_pattern, tags)
    if len(gene_names) == 1:
        gene = Gene(gene_ids[0], chromosome, start_coord, end_coord, strand, gene_names[0])
    else:
        gene = Gene(gene_ids[0], chromosome, start_coord, end_coord, strand)
    chromosome_gene_tree = gene_tree.get(chromosome)
    if chromosome_gene_tree is None:
        chromosome_gene_tree = IntervalTree()
        gene_tree[chromosome] = chromosome_gene_tree
    chromosome_gene_tree[start_coord:end_coord] = gene
    return gene


def parse_annotation_transcript(gene, chromosome, end_coord, start_coord, strand,
                                tags):
    transcript_names = get_attribute_from_tags(transcript_name_pattern, tags)
    transcript_ids = get_attribute_from_tags(transcript_id_pattern, tags)
    if len(transcript_names) == 1:
        transcript = Transcript(transcript_ids[0], chromosome, strand, start_coord, end_coord, transcript_names[0])
    else:
        transcript = Transcript(transcript_ids[0], chromosome, strand, start_coord, end_coord)
    gene.add_transcript(transcript)
    return transcript


def parse_annotation_exon(transcript, end_coord, start_coord, tags):
    exon = Exon(start_coord, end_coord)
    transcript.add_exon(exon)
    return transcript


def parse_input_gtf_line(line_elems):
    chromosome = line_elems[0]
    feature = line_elems[2]
    start_coord = int(line_elems[3])
    end_coord = int(line_elems[4])
    strand = line_elems[6]
    tags = line_elems[8].split(";") 
    if feature == "transcript":
       parse_input_transcript(chromosome, strand, start_coord, end_coord, tags)
    elif feature == "exon":
       parse_input_exon(chromosome, end_coord, start_coord, strand, tags)

def parse_input_transcript(chromosome, strand, start_coord, end_coord, tags):
    transcript_ids = get_attribute_from_tags(transcript_id_pattern, tags)
    transcript_id = transcript_ids[0]
    if transcripts.get(transcript_id) is None:
       gene_ids = get_attribute_from_tags(gene_id_pattern, tags)
       transcript = Transcript(transcript_id, chromosome, strand, start_coord, end_coord, None, gene_ids[0])
       transcripts[transcript_id] = transcript 


def parse_input_exon(chromosome, end_coord, start_coord, strand, tags):
    transcript_ids = get_attribute_from_tags(transcript_id_pattern, tags)
    gene_ids = get_attribute_from_tags(gene_id_pattern, tags)
    exon = Exon(start_coord, end_coord)
    transcript = transcripts.get(transcript_ids[0])
    if transcript is not None:
        transcript.add_exon(exon)
        update_transcript_start_coord(transcript, exon.start_coord)
        update_transcript_end_coord(transcript, exon.end_coord)
    else:
        transcript = Transcript(transcript_ids[0], chromosome, strand, exon.start_coord, exon.end_coord, None,
                                gene_ids[0])
        transcript.add_exon(exon)
        transcripts[transcript.transcript_id] = transcript


# if given start coord is less than transcript's, sets transcript's start
# coord to it
def update_transcript_start_coord(transcript, start_coord):
    if transcript.start_coord > start_coord:
        transcript.start_coord = start_coord


# if given end coord is greater than transcript's, sets transcript's end
# coord to it
def update_transcript_end_coord(transcript, end_coord):
    if transcript.end_coord < end_coord:
        transcript.end_coord = end_coord


def find_potential_genes_by_junctions(transcript, genes):
    most_junctions_matching = 0
    most_junctions_matching_genes = []
    transcript_exons = transcript.exons
    num_transcript_exons = len(transcript_exons)
    for gene in genes:
        gene_most_junctions_matching = 0
        for gene_transcript in gene.transcripts:
            transcript_junctions_matching = 0
            num_first_exons_exclude = 0
            for i in range(num_transcript_exons - 1):
                start_exon = transcript_exons[i]
                end_exon = transcript_exons[i + 1]
                match, new_num_first_exons_exclude = find_matching_junction(gene_transcript, start_exon, end_exon,
                                                                            num_first_exons_exclude)
                if new_num_first_exons_exclude is not None:
                    num_first_exons_exclude = new_num_first_exons_exclude
                if match:
                    transcript_junctions_matching += 1
                if num_first_exons_exclude == len(gene_transcript.exons) - 1:
                    break
            if transcript_junctions_matching > gene_most_junctions_matching:
                gene_most_junctions_matching = transcript_junctions_matching
        if gene_most_junctions_matching > most_junctions_matching:
            most_junctions_matching = gene_most_junctions_matching
            most_junctions_matching_genes = [gene]
        elif gene_most_junctions_matching == most_junctions_matching:
            most_junctions_matching_genes.append(gene)
    return most_junctions_matching_genes


# sees if the exon junction between the given start and end exons matches any junction
# in the given transcript
#
# if num_first_exons_exclude = n, then the transcripts first n exons are disregarded when
# looking for matching junctions
#
# second return value is the number of exons on the given transcript that should be disregarded
# when looking for matching junctions between exons further down
def find_matching_junction(transcript, start_exon, end_exon, num_first_exons_exclude):
    transcript_exons = transcript.exons

    for i in range(num_first_exons_exclude, len(transcript_exons) - 1):
        junction_start_match = (transcript_exons[i].end_coord == start_exon.end_coord)
        junction_end_match = (transcript_exons[i + 1].start_coord == end_exon.start_coord)
        if junction_start_match and junction_end_match:
            return True, i + 1
    return False, None


def find_potential_genes_by_num_base_match(transcript, genes):
    most_bases_matched = 0
    most_bases_matching_genes = []
    transcript_exons = transcript.exons
    num_transcript_exons = len(transcript_exons)
    for gene in genes:
        gene_most_bases_matching = 0
        for gene_transcript in gene.transcripts:
            transcript_bases_matched = 0
            num_first_exons_exclude = 0
            for i in range(num_transcript_exons):
                exon = transcript_exons[i]
                bases_matched, new_num_first_exons_exclude = find_num_matching_bases(gene_transcript, exon,
                                                                                     num_first_exons_exclude)
                if new_num_first_exons_exclude is not None:
                    num_first_exons_exclude = new_num_first_exons_exclude
                transcript_bases_matched += bases_matched
                if num_first_exons_exclude == len(gene_transcript.exons):
                    break
            if transcript_bases_matched > gene_most_bases_matching:
                gene_most_bases_matching = transcript_bases_matched
        if gene_most_bases_matching > most_bases_matched:
            most_bases_matched = gene_most_bases_matching
            most_bases_matching_genes = [gene]
        elif gene_most_bases_matching == most_bases_matched:
            most_bases_matching_genes.append(gene)
    return most_bases_matching_genes


def find_num_matching_bases(transcript, exon, num_first_exons_exclude):
    bases_matched = 0
    transcript_exons = transcript.exons
    num_transcript_exons = len(transcript_exons)
    for i in range(num_first_exons_exclude, num_transcript_exons):
        transcript_exon = transcript_exons[i]
        if exon.start_coord > transcript_exon.end_coord:
            return bases_matched, i + 1
        elif exon.end_coord < transcript_exon.start_coord:
            return bases_matched, i
        elif exon.start_coord < transcript_exon.start_coord:
            bases_matched += exon.end_coord - transcript_exon.start_coord + 1
        elif exon.end_coord < transcript_exon.end_coord:
            bases_matched += exon.end_coord - exon.start_coord + 1
        else:
            bases_matched += transcript_exon.end_coord - exon.start_coord + 1
    return bases_matched, num_transcript_exons


def assign_transcript_to_gene(gene, transcript):
    transcript.set_gene_id(gene.gene_id)
    transcript.set_gene_name(gene.gene_name)
    transcript.set_strand(gene.strand)


def get_attribute_from_tags(attribute_pattern, tags):
    return list(filter(lambda id: id is not None,
                       list(map(lambda tag: get_attribute_from_tag(attribute_pattern, tag), tags))))


def get_attribute_from_tag(attribute_pattern, tag):
    matchObject = attribute_pattern.match(tag)
    if matchObject is not None:
        return matchObject.group(1)
    else:
        return None


main()
