import argparse
import re

def main():
    parser = argparse.ArgumentParser(description='Make Matrix')
    parser.add_argument('input_file', metavar='TXT', type=str,
                        help='path of input file')
    parser.add_argument('isoform_labels', metavar='TXT', type=str,
                        help='path of isoform labels file')
    parser.add_argument('output_file', metavar='TXT', type=str,
                        help='path of output file')
    args = parser.parse_args()

    input_file_path = args.input_file
    isoforms_file_path = args.isoform_labels
    output_file_path = args.output_file
       
    input_file = open(input_file_path, "r")
    isoforms_file = open(isoforms_file_path, "r")
    output_file = open(output_file_path, "w")

    isoforms  = set()
    transcript_id_pattern = re.compile("\s*transcript_id\s*\"(\S+)\"\s*")

    for line in isoforms_file:
        isoforms.add(line.rstrip())
        
    for line in input_file:
        tags = line.strip().split("\t")[8] 
        transcript_id = transcript_id_pattern.search(tags).group(1)
        if transcript_id in isoforms:
            output_file.write(line)
   
   
main()
