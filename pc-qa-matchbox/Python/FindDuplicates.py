
# === imports ===================================

import argparse
import glob
import os
import sys
import re
import subprocess as sub
import multiprocessing
import time
import threading, Queue
import gzip
import numpy as np
import copy
import MatchboxLib

from multiprocessing import Pool
from subprocess import call
from xml.dom import minidom

# === definitions ===============================

BOW_FILE_NAME          = "bow.xml"
KNN_THRESHOLD          = 0

configuration          = "Linux"

configs = {}

configs["PC-Alex"] = {}
configs["PC-Alex"]["BIN_EXTRACTFEATURES"]     = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/extractfeatures.exe"
configs["PC-Alex"]["BIN_COMPARE"]             = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/compare.exe"
configs["PC-Alex"]["BIN_TRAIN"]               = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/train.exe"

configs["Linux"]   = {}
configs["Linux"]["BIN_EXTRACTFEATURES"]       = "/data/SCAPE/matchbox/src/DPQA_ExtractFeatures/extractfeatures"
configs["Linux"]["BIN_COMPARE"]               = "/data/SCAPE/matchbox/src/DPQA_Compare/compare"
configs["Linux"]["BIN_TRAIN"]                 = "/data/SCAPE/matchbox/src/DPQA_Train/train"

configs["PC-Reinhold"] = {}
configs["PC-Reinhold"]["BIN_EXTRACTFEATURES"] = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/extractfeatures.exe"
configs["PC-Reinhold"]["BIN_COMPARE"]         = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/compare.exe"
configs["PC-Reinhold"]["BIN_TRAIN"]           = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/train.exe"

BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]

SUPPORTED_IMAGE_TYPES  = ".(png|tif|jpe?g|bmp|gif|jp2)$"

# === Classes ===================================


def pyCompare(config, dirname, k, csv):
    
    k       = 0.02

    distVals = []
    results  = []
    
    dmatrix = MatchboxLib.getDistanceMatrix(dirname)
    
    print "...calculating Mean Absolute Deviation"
    
    for entry in dmatrix:
        distVals.append(entry[1][0][0])
        results.append([entry[0], entry[1][0][1][0], entry[1][0][0]])
    
    d = np.matrix(distVals,dtype=float)
    r = np.array(results)
    
    medd = np.median(d,axis=1)
    
    mad = k * np.median(np.abs(d - medd),axis=1)

    print "...selecting duplicate candidates"
    idx = np.where(d > (medd+mad)) #duplicate candidates
    
    duplicates = r[idx[1].tolist()]
    
    dup_found = set()

    print "...calculating structural similarity of candidates for spatial verification"
    
    print "\n=== List of detected duplicates ===\n"
    
    for dup in duplicates:
        
        if (dup[0] not in dup_found):
            
            f1 = dup[0].replace("BOWHistogram", "SIFTComparison")
            f2 = dup[1].replace("BOWHistogram", "SIFTComparison")
            
            ssim = MatchboxLib.compare(config, f1, f2, "SIFTComparison", "ssim")
            
            f1 = MatchboxLib.extractFilename(f1).replace(".SIFTComparison", "")
            f2 = MatchboxLib.extractFilename(f2).replace(".SIFTComparison", "")
            
            print "{0} => {1} [ {2}% ]".format(f1, f2, (ssim * 100))

            dup_found.add(dup[1])
        



if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    
    parser.add_argument('dir',          help='directory containing image files')
    parser.add_argument('action',       help='define which step of the workflow shouold be executed',       choices=['all', 'extract', 'compare', 'train', 'bowhist', 'clean'])
    parser.add_argument('--threads',    help='number of concurrent threads',                                type=int, default=1)
    parser.add_argument('--filter',     help='Filter for BOW creation',                                     type=str, default=".SIFTComparison.feat.xml.gz")
    parser.add_argument('--sdk',        help='Number of Spatial Distincitve Keypoints',                     type=int, default=0)
    parser.add_argument('--nn',         help='Number of Nearest Neighbors to display',                      type=int, default=1)
    parser.add_argument('--precluster', help='Number of Preclustering centers',                             type=int, default=100)
    parser.add_argument('--clahe',      help='Value of adaptive contrast enhancement (1 = no enhancement)', type=int, default=1)
    parser.add_argument('--config',     help='Configuration Parameter',                                     type=str, default="Linux")
    parser.add_argument('--featdir',    help='Alternative directory for storing feature files',             type=str, default="")
    parser.add_argument('--csv',        help='Update Feature',                                              action='store_true')
    parser.add_argument('-v',           help="Print verbose messages",                                      dest='verbose', action='store_true')
    
    args = vars(parser.parse_args())
    
    config = configs[args['config']]
    
    if (args['action'] == 'clean'):
        MatchboxLib.clearDirectory(args['dir'])
        if len(args['featdir']) > 0:
            MatchboxLib.clearDirectory(args['featdir'])
        exit()
    
    if (args['action'] == 'extract') or (args['action'] == 'all'):
        print "\n=== extracting features from directory {0} ===\n".format(args['dir'])
        MatchboxLib.extractFeatures(config, args['dir'], args['sdk'],args['threads'], args['clahe'], args['featdir'])
    
    if (args['action'] == 'train') or (args['action'] == 'all'):
        print "\n=== calculating Visual Bag of Words ===\n"
        
        dir = args['dir']
        
        if len(args['featdir']) > 0:
            dir = args['featdir']
        
        MatchboxLib.calculateBoW(config, dir, args['filter'], args['precluster'])
    
    if (args['action'] == 'bowhist') or (args['action'] == 'all'):
        print "\n=== extract BoW Histograms from directory {0} ===\n".format(args['dir'])
        MatchboxLib.extractBoWHistograms(config,args['dir'], args['threads'], args['featdir'])
    
#    if (args['action'] == 'compare') or (args['action'] == 'all'):
#        print "=== compare images from directory {0} ===".format(args['dir'])
#        MatchboxLib.findDuplicatesInSameCollection(config, args['dir'], "jp2",args['threads'])

    if (args['action'] == 'compare') or (args['action'] == 'all'):
        
        dir = args['dir']
        
        if len(args['featdir']) > 0:
            dir = args['featdir']
            
        print "\n=== compare images from directory {0} ===\n".format(dir)
        
        pyCompare(config, dir, args['nn'], args['csv'])

#    if (args['action'] == 'pycomparemad'):
#        print "=== compare images from directory {0} ===".format(args['dir'])
#        MatchboxLib.pyCompareMAD(args['dir'], args['nn'], args['csv'])