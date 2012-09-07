
# === imports ===================================

import argparse
import os
import MatchboxLib

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
configs["Linux"]["BIN_EXTRACTFEATURES"]       = "extractfeatures"
configs["Linux"]["BIN_COMPARE"]               = "compare"
configs["Linux"]["BIN_TRAIN"]                 = "train"

configs["PC-Reinhold"] = {}
configs["PC-Reinhold"]["BIN_EXTRACTFEATURES"] = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/extractfeatures.exe"
configs["PC-Reinhold"]["BIN_COMPARE"]         = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/compare.exe"
configs["PC-Reinhold"]["BIN_TRAIN"]           = "C:/Dokumente und Einstellungen/huber-moerkr/Eigene Dateien/TFS/SCAPE/SCAPE QA/Release/train.exe"

BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]

SUPPORTED_IMAGE_TYPES  = ".(png|tif|jpe?g|bmp|gif|jp2)$"

# === Classes ===================================



if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    
    parser.add_argument('dir1',         help='directory 1 containing image files')
    parser.add_argument('dir2',         help='directory 2 containing image files')
    parser.add_argument('action',       help='define which step of the workflow shouold be executed',       choices=['all', 'extract', 'duplicates', 'references', 'clean'])
    parser.add_argument('--threads',    help='number of concurrent threads',                                type=int, default=1)
    parser.add_argument('--filter',     help='Filter for BOW creation',                                     type=str, default=".SIFTComparison.feat.xml.gz")
    parser.add_argument('--sdk',        help='Number of Spatial Distincitve Keypoints',                     type=int, default=0)
    parser.add_argument('--nn',         help='Number of Nearest Neighbors to display',                      type=int, default=1)
    parser.add_argument('--precluster', help='Number of Preclustering centers',                             type=int, default=100)
    parser.add_argument('--clahe',      help='Value of adaptive contrast enhancement (1 = no enhancement)', type=int, default=1)
    parser.add_argument('--config',     help='Configuration Parameter',                                     type=str, default="Linux")
    parser.add_argument('--featdir',    help='Alternative directory for storing feature files',             type=str, default="")
    parser.add_argument('--csv',        help='Update Feature',                                              action='store_true')
    parser.add_argument('--bowsize',    help='Size of Bag of Words',                                        type=int, default=1000)
    parser.add_argument('-v',           help="Print verbose messages",                                      dest='verbose', action='store_true')
    
    args = vars(parser.parse_args())
    
    config = configs[args['config']]
    
    if (args['action'] == 'clean'):
#        MatchboxLib.clearDirectory(args['dir'])
#        if len(args['featdir']) > 0:
#            MatchboxLib.clearDirectory(args['featdir'])
        exit()
    
    
    if (args['action'] == 'extract') or (args['action'] == 'all'):
        
        print "\n=== creating directories ===\n"
        
        print "... feature directory: {0}".format(args['featdir'])
        
        print "... feature directory: {0}/col1/sift".format(args['featdir'])
        if not os.path.exists("{0}/col1/sift".format(args['featdir'])):
            os.makedirs("{0}/col1/sift".format(args['featdir']))
        
        print "... feature directory: {0}/col2/sift".format(args['featdir'])
        if not os.path.exists("{0}/col2/sift".format(args['featdir'])):
            os.makedirs("{0}/col2/sift".format(args['featdir']))
                
        print "\n=== extracting features from directory {0} ===\n".format(args['dir1'])
        MatchboxLib.extractFeatures(config, args['dir1'], args['sdk'],args['threads'], args['clahe'], "{0}/col1/sift".format(args['featdir']))
        
        print "\n=== extracting features from directory {0} ===\n".format(args['dir2'])
        MatchboxLib.extractFeatures(config, args['dir2'], args['sdk'],args['threads'], args['clahe'], "{0}/col2/sift".format(args['featdir']))
    
    
    
    if (args['action'] == 'duplicates') or (args['action'] == 'all'):
        
        print "\n=== Searching for duplicates ===\n"
        
        # ===== COLLECTION 1 =====
        
        print "\n=== Processing Collection 1: {0} ===\n".format(args['dir1'])
        
        print "... calculating Visual Bag of Words"
        if not os.path.exists("{0}/col1/sift/bow.xml".format(args['featdir'])):
            MatchboxLib.calculateBoW(config, "{0}/col1/sift".format(args['featdir']), args['filter'], args['precluster'])
        
        print "... creating feature directory: {0}/col1/bowhist".format(args['featdir'])
        if not os.path.exists("{0}/col1/bowhist".format(args['featdir'])):
            os.makedirs("{0}/col1/bowhist".format(args['featdir']))
    
        print "... extract BoW Histograms"
        MatchboxLib.extractBoWHistograms(config,args['dir1'], args['threads'], "{0}/col1/sift".format(args['featdir']))
        
        print "... moving BoW Histograms feature files"
        MatchboxLib.moveFiles("{0}/col1/sift".format(args['featdir']), "{0}/col1/bowhist".format(args['featdir']), "*.BOWHistogram.feat.xml.gz", True)
        
        print "... searching for duplicates"
        duplicates = MatchboxLib.pyFindDuplicates(config, "{0}/col1/sift".format(args['featdir']), args['nn'], args['csv'])
        
        print "... writing duplicates to file"
        print "TODO!!!"
        
        # ===== COLLECTION 2 =====
    
        print "\n=== Processing Collection 2: {0} ===\n".format(args['dir2'])
        
        print "... calculating Visual Bag of Words"
        if not os.path.exists("{0}/col2/sift/bow.xml".format(args['featdir'])):
            MatchboxLib.calculateBoW(config, "{0}/col2/sift".format(args['featdir']), args['filter'], args['precluster'])
        
        print "... creating feature directory: {0}/col2/bowhist".format(args['featdir'])
        if not os.path.exists("{0}/col2/bowhist".format(args['featdir'])):
            os.makedirs("{0}/col2/bowhist".format(args['featdir']))

        print "... extract BoW Histograms"
        MatchboxLib.extractBoWHistograms(config,args['dir2'], args['threads'], "{0}/col2/sift".format(args['featdir']))

        print "... moving BoW Histograms feature files"
        MatchboxLib.moveFiles("{0}/col1/sift".format(args['featdir']), "{0}/col1/bowhist/duplicates".format(args['featdir']), "*.BOWHistogram.feat.xml.gz", True)
    
        print "... searching for duplicates"
        duplicates = MatchboxLib.pyFindDuplicates(config, "{0}/col2/sift".format(args['featdir']), args['nn'], args['csv'])
        
        print "... writing duplicates to file"
        print "TODO!!!"



    if (args['action'] == 'references') or (args['action'] == 'all'):

#        print "\n=== Searching for referencing images in both collections ===\n"
#        
#        print "... extract BoW Histograms of collection2"
#        MatchboxLib.extractBoWHistograms(config,args['dir2'], args['threads'], "{0}/col2/sift".format(args['featdir']), "{0}/col1/sift/bow.xml".format(args['featdir']))
#        
#        print "... moving BoW Histograms feature files"
#        MatchboxLib.moveFiles("{0}/col2/sift".format(args['featdir']), "{0}/col2/bowhist/references".format(args['featdir']), "*.BOWHistogram.feat.xml.gz", True)
        
        print "... searching references"
        dmatrix = MatchboxLib.pyFindReferences(config, "{0}/col1/bowhist".format(args['featdir']), "{0}/col2/bowhist/references".format(args['featdir']))

        

            


    if (args['action'] == 'compare') or (args['action'] == 'all'):
        
        dir = args['dir']
        
        if len(args['featdir']) > 0:
            dir = args['featdir']
            
        print "\n=== compare images from directory {0} ===\n".format(dir)
        
        MatchboxLib.pyFindDuplicates(config, dir, args['nn'], args['csv'])
