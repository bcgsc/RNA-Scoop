import argparse
import re

def main():
    parser = argparse.ArgumentParser(description='Make Matrix')
    parser.add_argument('input_file', metavar='TXT', type=str,
                        help='path of input file')
    parser.add_argument('output_isoform_labels', metavar='TXT', type=str,
                        help='path of output isoform labels file')
    parser.add_argument('output_matrix', metavar='TXT', type=str,
                        help='path of output matrix file')
    parser.add_argument('--tpm', metavar='INT', type=int, default=0,
                        help='minimum tpm')
    parser.add_argument('--quorum', metavar='INT', type=int, default=10,
                        help='minimum quorum')
    args = parser.parse_args()

    input_file_path = args.input_file
    output_isoform_labels_path = args.output_isoform_labels
    output_matrix_path = args.output_matrix
    min_tpm = args.tpm
    min_quorum = args.quorum   
    
    input_file = open(input_file_path, "r")
    output_isoform_labels_file = open(output_isoform_labels_path, "w")
    output_matrix_file = open(output_matrix_path, "w")

    read_first_line = False
    matrix_array = []
        
    for line in input_file:
        line_elems = line.strip().split("\t")
        if read_first_line:
            isoform_expr_per_cell = list(map(get_float_count, line_elems[1:]))
            transcript_id = line_elems[0]
            if  meets_quorum_req(isoform_expr_per_cell, min_quorum, min_tpm):
                output_isoform_labels_file.write(transcript_id + "\n")
                matrix_array.append(isoform_expr_per_cell)
        else:
            read_first_line = True

    make_matrix_file(matrix_array, output_matrix_file)

def get_float_count(count):
    if count == "NA":
       return 0
    else:
       return float(count)

   
def make_cell_labels_file(output_cell_labels_file, names):
    last_name_index = len(names) - 1
    for i in range(1, last_name_index):
        output_cell_labels_file.write(names[i] + "\n")
    output_cell_labels_file.write(names[last_name_index])


def meets_quorum_req(isoform_expr_per_cell, min_quorum, min_tpm):
    quorum = 0
    for cell_expression in isoform_expr_per_cell:
        if cell_expression > min_tpm:
            quorum += 1          
    return quorum >= min_quorum


def make_matrix_file(matrix_array, output_matrix_file):
    num_rows = len(matrix_array)
    if (num_rows > 0):
        num_cols = len(matrix_array[0])
        for col_index in range(0, num_cols):
            col_items = []
            for row_index in range(0, num_rows):
                col_items.append(str(matrix_array[row_index][col_index]))
            output_matrix_file.write('\t'.join(col_items) + '\n')


main()
