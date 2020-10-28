import argparse
import logging
import numpy
import umap


parser = argparse.ArgumentParser(description='Create UMAP embedding from input matrix.')
parser.add_argument('inpath',
                    help='path of input matrix file')
parser.add_argument('outpath',
                    help='path of output embedding file')
parser.add_argument('--neighbors', dest='neighbors', default='100', metavar='INT', type=int,
                    help='number of neighbors (default: %(default)s)')
parser.add_argument('--min_dist', dest='min_dist', default='0.4', metavar='FLOAT', type=float,
                    help='minimum distance (default: %(default)s)')
args = parser.parse_args()

logging.basicConfig(
    format='%(asctime)s %(levelname)-8s %(message)s',
    level=logging.INFO,
    datefmt='%Y-%m-%d %H:%M:%S')

logging.info('parsing matrix file...')
my_data = numpy.loadtxt(args.inpath)

logging.info('creating embedding...')
embedding = umap.UMAP(n_components=2, min_dist=args.min_dist, n_neighbors=args.neighbors).fit_transform(my_data)
logging.info('shape:' + str(embedding.shape))

logging.info('writing embedding file...')
numpy.savetxt(args.outpath, embedding, delimiter='\t')

logging.info('done')
