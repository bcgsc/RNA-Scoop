import argparse
import gzip
import logging

def is_gzip(p):
    return p[-3:].lower() == '.gz'

def file_handler(p, tags):
    if is_gzip(p):
        return gzip.open(p, tags)
    else:
        return open(p, tags)

def get_encoding(labels):
    visited = set()
    new_labels = list()
    occurrences = list()
    for index in range(len(labels)):
        if index not in visited:
            key = labels[index]
            indices = [i for i, x in enumerate(labels) if x == key]
            new_labels.append(key)
            occurrences.append(indices)
            visited.update(indices)
    return new_labels, occurrences

def reduce_arr(arr, occurrences, op=sum):
    new_arr = list()
    for x in occurrences:
        new_arr.append(op([arr[i] for i in x]))
    return new_arr
    
parser = argparse.ArgumentParser(description='Reduce matrix based on column labels.')
parser.add_argument('in_matrix',
                    help='input matrix file')
parser.add_argument('in_labels',
                    help='input labels file')
parser.add_argument('out_matrix',
                    help='output matrix file')
parser.add_argument('out_labels',
                    help='output labels file')
args = parser.parse_args()

logging.basicConfig(
    format='%(asctime)s %(levelname)-8s %(message)s',
    level=logging.INFO,
    datefmt='%Y-%m-%d %H:%M:%S')

logging.info('parsing input labels file...')
labels = list()
with file_handler(args.in_labels, 'rt') as fr:
    for line in fr:
        labels.append(line.strip())

logging.info(str(len(labels)) + ' labels')

new_labels, occurrences = get_encoding(labels)

logging.info(str(len(occurrences)) + ' unique labels')

logging.info('writing output labels file...')
with file_handler(args.out_labels, 'wt') as fw:
    for x in new_labels:
        fw.write(x + '\n')

logging.info('reducing matrix...')
with file_handler(args.out_matrix, 'wt') as fw:
    with file_handler(args.in_matrix, 'rt') as fr:
        for line in fr:
            row = [float(x) for x in line.strip().split('\t')]
            row_new = reduce_arr(row, occurrences)
            fw.write('\t'.join([str(x) for x in row_new]) + '\n')

logging.info('done')
