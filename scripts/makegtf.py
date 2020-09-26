import argparse
import gzip
import operator
import re
from intervaltree import IntervalTree
from enum import Enum

class Containment(Enum):
    CONTAINED = 1
    NOT_CONTAINED = 2
    LAST_EXON_LONGER = 3

class Exon:
    def __init__(self, start_coord, end_coord):
        self.start_coord = start_coord
        self.end_coord = end_coord

    def __eq__(self, other):
        if isinstance(other, Exon):
            return self.start_coord == other.start_coord and self.end_coord == other.end_coord
        return False

    def __hash__(self):
        return hash(tuple(sorted(self.__dict__.items())))


class Transcript:
    def __init__(self, id, strand, chromosome, num_matching, prefix=None, gene_id="1"):
        self.prefix = prefix
        self.transcript_id = self.prefix + id
        self.strand = strand
        self.start_coord = float("inf")
        self.end_coord = 0
        self.num_matching = num_matching
        self.chromosome = chromosome
        self.gene_id = self.prefix + gene_id
        self.exons = []

    def add_exon(self, start_coord, end_coord):
        self.update_start_coord(start_coord)
        self.update_end_coord(end_coord)
        self.exons.append(Exon(start_coord, end_coord))
        self.exons.sort(key=lambda exon: exon.start_coord)
    
    def set_transcript_id(self, transcript_id):
        self.transcript_id = transcript_id

    def set_gene_id(self, gene_id):
        self.gene_id = gene_id

    def has_an_exon(self):
        return len(self.exons) != 0

    def update_start_coord(self, start_coord):
        if self.start_coord > start_coord:
            self.start_coord = start_coord

    def update_end_coord(self, end_coord):
        if self.end_coord < end_coord:
            self.end_coord = end_coord

    # this transcript is considered to be contained in the given transcript if
    # all of its exons match consecutive exons in the other transcript, the exceptions being that
    # this transcript's first exon does not have to contain the beginning of the exon it matches with, and
    # its last exon does not have to contain the end of the exon it matches with
    def is_contained_in(self, other_transcript, dangling_edge_threshold):
        if len(other_transcript.exons) == 1:
            return self.is_contained_in_one_exon(other_transcript, dangling_edge_threshold)
        other_offset = self.get_other_first_match_index(other_transcript, dangling_edge_threshold)
        if other_offset is not None:
            last_exon_index = len(self.exons) - 1

            # check if middle exons (all exons except first and last) match
            for i in range(1, last_exon_index):
                transcript_longer_than_other = (other_offset + i >= len(other_transcript.exons))
                if transcript_longer_than_other:
                    return Containment.NOT_CONTAINED
                else:
                    exon_doesnt_match = (not self.exons[i] == other_transcript.exons[i + other_offset])
                    if exon_doesnt_match:
                        return Containment.NOT_CONTAINED

            # check if last exon matches
            other_transcript_longer = (last_exon_index + other_offset < len(other_transcript.exons))

            if other_transcript_longer:
                if self.exons[last_exon_index].start_coord == other_transcript.exons[last_exon_index + other_offset].start_coord:
                    if self.exons[last_exon_index].end_coord <= (other_transcript.exons[last_exon_index + other_offset].end_coord +
                                                                 dangling_edge_threshold):
                        return Containment.CONTAINED
                    return Containment.LAST_EXON_LONGER
                else:
                    return Containment.NOT_CONTAINED
            else:
                return Containment.NOT_CONTAINED
        return Containment.NOT_CONTAINED

    def is_contained_in_one_exon(self, other_transcript, dangling_edge_threshold):
        exons = self.exons
        other_exons = other_transcript.exons

        if len(exons) == 1:
            if exons[0].start_coord >= (other_exons[0].start_coord - dangling_edge_threshold):
                if exons[0].end_coord <= (other_exons[0].end_coord + dangling_edge_threshold):
                    return Containment.CONTAINED
                return Containment.LAST_EXON_LONGER
            else:
                return Containment.NOT_CONTAINED
        else:
            return Containment.NOT_CONTAINED

    # returns index of first exon in other transcript that matches this transcripts first exon
    # "matches" means that the first exon of this transcript is contained in the other exon, and that
    # they have the same end coordinate
    def get_other_first_match_index(self, other_transcript, dangling_edge_threshold):
        other_offset = None
        for i in range(len(other_transcript.exons)):
            if self.exons[0].start_coord >= (other_transcript.exons[i].start_coord - dangling_edge_threshold) and \
                    self.exons[0].end_coord ==  other_transcript.exons[i].end_coord:
                other_offset = i
                break
        return other_offset

    def __eq__(self, other):
        if isinstance(other, Transcript):
            return self.transcript_id == other.transcript_id
        return False

    def __hash__(self):
        return hash(self.transcript_id)


def main():
    parser = argparse.ArgumentParser(description='Generate GTF from PAF file')
    parser.add_argument('paf', metavar='PAF', type=str, nargs='+',
                        help='path of PAF file')
    parser.add_argument('gtf', metavar='GTF', type=str,
                        help='path of GTF file')
    parser.add_argument('--strand_specific', action='store_true',
                        help='aligned sequences are stand-specific')
    parser.add_argument('--indel', metavar='INT', type=int,
                        default=10,
                        help='max indel size allowed [%(default)s]')
    parser.add_argument('--de',  metavar='INT', type=int,
                        default=5,
                        help='max allowed dangling edge size when collasping transcripts  [%(default)s]')
    parser.add_argument('--prefixes', metavar='STR', type=str, nargs='*',
                        help='prefix for transcript IDs')
    parser.add_argument('--identity',  metavar='FLOAT', type=float,
                        default=0.99,
                        help='min sequence identity of alignment  [%(default)s]')
    parser.add_argument('--include_chimeras', action='store_true',
                        help='include segments from chimeric alignments')

    args = parser.parse_args()

    paf_paths = args.paf
    gtf_path = args.gtf
    strand_specific = args.strand_specific
    indel_threshold = args.indel
    dangling_edge_threshold = args.de
    prefixes = args.prefixes
    min_identity = args.identity
    include_chimeras = args.include_chimeras

    if prefixes is not None:
        assert (len(prefixes) == len(paf_paths))
    paf_to_gtf(paf_paths, gtf_path, strand_specific, indel_threshold, dangling_edge_threshold, prefixes, min_identity, include_chimeras)


def paf_to_gtf(paf_paths, gtf_path, strand_specific=False, indel_threshold=10, dangling_edge_threshold=5, prefixes=None, min_identity=0.99, include_chimeras=False):
    gtf_file = open(gtf_path, "w")
    num_pafs = len(paf_paths)
    transcripts = get_transcripts_from_paf(indel_threshold, num_pafs, paf_paths, prefixes, min_identity, include_chimeras)
    if len(transcripts) > 0:
       if not strand_specific:
          transcripts.sort(key=lambda transcript: [transcript.chromosome, transcript.start_coord, -transcript.end_coord])
       else:
          transcripts.sort(key=lambda transcript: [transcript.chromosome, transcript.strand, transcript.start_coord,
                                                 -transcript.end_coord])
       write_transcripts_to_gtf(transcripts, gtf_file, dangling_edge_threshold, strand_specific)

def has_gzip_ext(p):
    return p.lower().endswith('.gz')

def get_transcripts_from_paf(indel_threshold, num_pafs, paf_paths, prefixes, min_identity, include_chimeras):
    transcripts = set()
    for i in range(num_pafs):
        prefix = create_prefix(i, num_pafs, prefixes)
        last_parsed_transcript = None
        last_parsed_transcript_id = None
        last_parsed_transcript_id_not_chimera = False
        chimera_part_num = 1
        paf_path = paf_paths[i]
        paf_fh = gzip.open(paf_path, "rt") if has_gzip_ext(paf_path) else open(paf_path, "rt")
        
        with paf_fh as paf_file:
            for line in paf_file:
                line_elems = line.rstrip().split("\t")
                q_start = int(line_elems[2])
                q_end = int(line_elems[3])
                num_matches = int(line_elems[9])
                if float(num_matches)/float(q_end - q_start) >= min_identity:
                    tags = line_elems[12:]
                    if tp_is_p(tags):
                        cigar = get_cigar(tags)
                        if cigar is not None and meets_req(cigar, indel_threshold):
                            transcript = store_exons_in_transcript(line_elems[0], get_strand(tags, line_elems), line_elems[5],
                                                                   int(line_elems[7]), int(line_elems[9]), cigar, prefix)
                            if transcript is not None:
                               if last_parsed_transcript_id is None or transcript.transcript_id != last_parsed_transcript_id:
                                    if not last_parsed_transcript_id_not_chimera:
                                       last_parsed_transcript_id_not_chimera = True
                                       chimera_part_num = 1
                                    last_parsed_transcript = transcript
                                    last_parsed_transcript_id = transcript.transcript_id
                                    add_transcript_if_pass_threshold(transcript, transcripts)
                               elif include_chimeras:
                                     if last_parsed_transcript_id_not_chimera:
                                        last_parsed_transcript.set_transcript_id(last_parsed_transcript.transcript_id + "_p" + str(chimera_part_num))
                                        last_parsed_transcript_id_not_chimera = False
                                     chimera_part_num += 1
                                     transcript.set_transcript_id(transcript.transcript_id + "_p" + str(chimera_part_num))
                                     add_transcript_if_pass_threshold(transcript, transcripts)
                               elif last_parsed_transcript_id_not_chimera:
                                    transcripts.discard(last_parsed_transcript)
                                    last_parsed_transcript_id_not_chimera = False
    return list(transcripts)

def get_strand(tags, line_elems):
    for tag in tags:
        tag_elems = tag.split(":")
        if len(tag_elems) == 3 and tag_elems[0] == "ts" and tag_elems[1] == "A":
            strand = tag_elems[2]
            if strand == "+" or strand == "-":
                return tag_elems[2]
            else:
                return line_elems[4]
    return line_elems[4]


def add_transcript_if_pass_threshold(transcript, transcripts):
    if transcript.num_matching >= 200:
        transcripts.add(transcript)
    return transcript


def create_prefix(i, num_pafs, prefixes):
    if prefixes is None:
        if num_pafs == 1:
            prefix = ""
        else:
            prefix = str(i) + "_"
    else:
        prefix = prefixes[i] + "_"
    return prefix


def tp_is_p(tags):
    for tag in tags:
        tag_elems = tag.split(":")
        if len(tag_elems) == 3 and tag_elems[0] == "tp" and tag_elems[1] == "A":
            return tag_elems[2] == "P"
    return False


def get_cigar(tags):
    for tag in tags:
        tag_elems = tag.split(":")
        if len(tag_elems) == 3 and tag_elems[0] == "cg" and tag_elems[1] == "Z":
            return tag_elems[2]
    return None


def meets_req(cigar, min_threshold=10):
    items = re.findall("(\d+)[ID]", cigar)
    for item in items:
        if int(item) >= min_threshold: 
            return False
    return True


def store_exons_in_transcript(id, strand, chromosome, start, num_matching, cigar, prefix=None, threshold=20):
    transcript = Transcript(id, strand, chromosome, num_matching, prefix)
    pos_exons = re.findall("(\d+[MNDI])", cigar)
    exon_start = start + 1
    exon_end = exon_start - 1
    for pos_exon in pos_exons:
        if pos_exon[-1] == "N":
            exon_len = exon_end - exon_start
            if (exon_len >= threshold and not transcript.has_an_exon()) or (
                    exon_end > exon_start and transcript.has_an_exon()):
                transcript.add_exon(exon_start, exon_end)
            num_unmatched = int(pos_exon[:-1])
            exon_start = exon_end + num_unmatched + 1
            exon_end = exon_start - 1
        elif pos_exon[-1] == "M":
            num_matched = int(pos_exon[:-1])
            exon_end += num_matched
        elif pos_exon[-1] == "D":
            num_deleted = int(pos_exon[:-1])
            exon_end += num_deleted
    exon_len = exon_end - exon_start
    if exon_len >= threshold:
        transcript.add_exon(exon_start, exon_end)
    if transcript.has_an_exon():
        return transcript
    else:
        return None


# assumes given transcripts are sorted
def write_transcripts_to_gtf(transcripts, gtf_file, dangling_edge_threshold=5, strand_specific=False):
    gene_id_number = 1
    transcript_intervals_of_same_gene = IntervalTree()
    first_transcript = transcripts[0]
    gene_id = first_transcript.prefix + "GENE" + str(gene_id_number)
    first_transcript.set_gene_id(gene_id)
    transcript_intervals_of_same_gene[first_transcript.start_coord:first_transcript.end_coord + 1] = first_transcript

    for i in range(1, len(transcripts)):
        curr_transcript = transcripts[i]
        prev_transcript = transcripts[i - 1]
        overlapping_intervals = transcript_intervals_of_same_gene[curr_transcript.start_coord]
        part_of_same_gene = len(overlapping_intervals) != 0 and \
                            curr_transcript.chromosome == prev_transcript.chromosome and \
                            (curr_transcript.strand == prev_transcript.strand if strand_specific else True)
        if part_of_same_gene:

            #print("--------------------------", curr_transcript.transcript_id)
            is_contained = False
            for overlapping_transcript_interval in overlapping_intervals:
                overlapping_transcript = overlapping_transcript_interval.data
                containment = curr_transcript.is_contained_in(overlapping_transcript, dangling_edge_threshold)
                #print(overlapping_transcript.transcript_id)
                if containment == Containment.CONTAINED:
                    is_contained = True
                    break
                elif containment == Containment.LAST_EXON_LONGER:
                    replace_overlapping_transcript = (len(overlapping_transcript.exons) == len(curr_transcript.exons) and \
                                                      curr_transcript.start_coord - overlapping_transcript.start_coord <= dangling_edge_threshold)
                    #print("",  curr_transcript.start_coord - overlapping_transcript.start_coord)
                    if replace_overlapping_transcript:
                        #print("replaced ", overlapping_transcript_interval.data.transcript_id)
                        transcript_intervals_of_same_gene.remove(overlapping_transcript_interval)
                        break
            if not is_contained:
                curr_transcript.set_gene_id(gene_id)
                transcript_intervals_of_same_gene[curr_transcript.start_coord:curr_transcript.end_coord + 1] = curr_transcript

        else:
            write_transcripts_of_gene_to_gtf(gtf_file, transcript_intervals_of_same_gene)
            gene_id_number += 1
            gene_id = curr_transcript.prefix + "GENE" + str(gene_id_number)
            curr_transcript.set_gene_id(gene_id)
            transcript_intervals_of_same_gene.clear()
            transcript_intervals_of_same_gene[
            curr_transcript.start_coord:curr_transcript.end_coord + 1] = curr_transcript
    write_transcripts_of_gene_to_gtf(gtf_file, transcript_intervals_of_same_gene)


def write_transcripts_of_gene_to_gtf(gtf_file, transcript_intervals_of_same_gene):
    for transcript_interval in transcript_intervals_of_same_gene:
        transcript = transcript_interval.data
        write_transcript_to_gtf(transcript, gtf_file)


def write_transcript_to_gtf(transcript, gtf_file, prefix=None):
    seq_name = transcript.chromosome
    source = "ask_kmn"
    feature = "exon"
    strand = transcript.strand
    if prefix is not None:
        transcript_id = prefix + transcript.transcript_id
        gene_id = prefix + transcript.gene_id
    else:
        transcript_id = transcript.transcript_id
        gene_id = transcript.gene_id

    for exon in transcript.exons:
        gtf_exon_line = seq_name + "\t" + source + "\t" + feature + "\t" + str(exon.start_coord) + "\t" + str(
            exon.end_coord) + \
                        "\t" + "." + "\t" + strand + "\t" + "0" + "\t" + "gene_id \"" + gene_id + "\"; " + "transcript_id \"" + transcript_id + "\";" + "\n"
        gtf_file.write(gtf_exon_line)


main()
