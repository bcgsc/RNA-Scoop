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
    occurrences_dict = dict()
    for i, key in enumerate(labels):
        if key in occurrences_dict:
            occurrences_dict[key].append(i)
        else:
            occurrences_dict[key] = [i]
    new_labels = sorted(occurrences_dict.keys())
    occurrences = list()
    for key in new_labels:
        occurrences.append(occurrences_dict[key])
    return new_labels, occurrences

def reduce_arr(arr, occurrences, op=sum):
    new_arr = list()
    for x in occurrences:
        new_arr.append(op(arr[i] for i in x))
    return new_arr

def reduce_arr_str(arr, occurrences):
    new_arr = list()
    for x in occurrences:
        new_arr.append(str(sum(float(arr[i]) for i in x)))
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

num_new_labels = len(new_labels)
logging.info(str(num_new_labels) + ' unique labels')

logging.info('writing output labels file...')
with file_handler(args.out_labels, 'wt') as fw:
    for x in new_labels:
        fw.write(x + '\n')

logging.info('reducing matrix...')
with file_handler(args.out_matrix, 'wt') as fw:
    with file_handler(args.in_matrix, 'rt') as fr:
        for line in fr:
            row = line.strip().split('\t')
            row_new = reduce_arr_str(row, occurrences)
            fw.write('\t'.join(row_new) + '\n')

logging.info('done')
