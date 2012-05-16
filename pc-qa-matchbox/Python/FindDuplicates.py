
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

from multiprocessing import Pool
from subprocess import call
from xml.dom import minidom

# === definitions ===============================

BOW_FILE_NAME          = "bow.xml"
KNN_THRESHOLD          = 0

configuration          = "Linux"

configs = {}

configs["PC-Alex"] = {}
configs["PC-Alex"]["BIN_EXTRACTFEATURES"] = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/extractfeatures.exe"
configs["PC-Alex"]["BIN_COMPARE"]         = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/compare.exe"
configs["PC-Alex"]["BIN_TRAIN"]           = "D:/WORK/AIT_TFS/s3ms16.d03.arc.local/SCAPE/SCAPE QA/Release/train.exe"

configs["Linux"]   = {}
configs["Linux"]["BIN_EXTRACTFEATURES"]   = "/data/SCAPE/matchbox/src/DPQA_ExtractFeatures/extractfeatures"
configs["Linux"]["BIN_COMPARE"]           = "/data/SCAPE/matchbox/src/DPQA_Compare/compare"
configs["Linux"]["BIN_TRAIN"]             = "/data/SCAPE/matchbox/src/DPQA_Train/train"

BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]

SUPPORTED_IMAGE_TYPES  = ".(png|tif|jpe?g|bmp|gif|jp2)$"

# === Classes ===================================

class KnnWorker(threading.Thread):

    incrementLock = threading.RLock()
    
    def __init__(self, queue, results):
        threading.Thread.__init__(self)
        self.queue = queue
        self.results = results
        self.kill_received = False

    def run(self):
        
        try:
            while(not self.kill_received):
                
                task = self.queue.get_nowait()
                
                process = sub.Popen([BIN_COMPARE, "-l", "4", "--bowmetric", "CV_COMP_INTERSECT", task[0], task[1]], stdout=sub.PIPE,stderr=sub.PIPE)
                output, errors = process.communicate()
        
                outputStr = output.decode("utf-8")
                errorStr  = errors.decode("utf-8")
        
                if (len(errorStr) == 0):
                    xmldoc = minidom.parseString(outputStr)
                    xmltasks = xmldoc.getElementsByTagName('task')
                    
                    if len(xmltasks) > 0:
                        for xmltask in xmltasks:
                            if xmltask.hasAttribute('name'):
                                if xmltask.getAttribute('name') == 'BOWHistogram':
                                    result  = float(xmltask.getElementsByTagName('result')[0].childNodes[0].nodeValue)
                                    
                                    #print xmltask.getAttribute('name'), result, task[1]
        
                                    ExtractFeaturesWorker.incrementLock.acquire() 
                                    self.results.append([result, task[1]])
                                    ExtractFeaturesWorker.incrementLock.release()
                    
                else:
                    print("    compare.exe -l 4 {0} {1}".format(task[0], task[1]))
                    print(errorStr)
                    ExtractFeaturesWorker.incrementLock.acquire() 
                    self.results.append([-1, task[1]])
                    ExtractFeaturesWorker.incrementLock.release()
                
        except (Queue.Empty):
            pass
        
            
            
            

class ExtractFeaturesWorker(threading.Thread):

    idx           = 1
    incrementLock = threading.RLock()

    def __init__(self, queue):
        threading.Thread.__init__(self)
        self.queue = queue
        self.active = True
        self.kill_received = False
        
    def run(self):
        
        try:
            while(not self.kill_received):
                
                task = self.queue.get_nowait()
                
                print "[{0}] {1}".format(self.__class__.idx, task[1:])
                ExtractFeaturesWorker.incrementLock.acquire() 
                self.__class__.idx += 1
                ExtractFeaturesWorker.incrementLock.release()
                call(task)
                    
                
        except (Queue.Empty):
            pass
        
        print "ExtractFeaturesWorker Thread stopped"
    
    def stop(self):
        self.active = False

# === functions =================================

def getKnn(fa, files, numThreads = 1):


    kill_received = False

    results = []
    jobs    = Queue.Queue()

    for fb in files:

        if (fa == fb):
            continue

        jobs.put([fa,fb])

    pool = []
    
    for i in range(numThreads):
        worker = KnnWorker(jobs, results)
        pool.append(worker)
        worker.start()
    
    while len(pool) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool:
                thread.join(10)
                if not thread.isAlive():
                    pool.remove(thread)
            
        except KeyboardInterrupt:
            print "Ctrl-c received! Sending kill to threads..."
            kill_received = True
            for t in pool:
                t.kill_received = kill_received    
    
    results.sort(reverse=True)
    
    return results, kill_received

def extractFeatures(dir, sdk, numThreads = 1, clahe = 1):

    queue = Queue.Queue()
    
    print "\n... extracting features of dir {0}\n".format(dir)

    dirContents = glob.glob( os.path.join(dir, "*"))

    for infile in dirContents:
        
        if re.search(SUPPORTED_IMAGE_TYPES, infile, re.IGNORECASE) == None:
            continue
        queue.put([BIN_EXTRACTFEATURES, '--sdk', '{0}'.format(sdk), '--clahe', '{0}'.format(clahe), infile])

    processExtractFeatures(queue, numThreads)

    #queue.join()
    
def extractBoWHistograms(dir, numThreads = 1, update=False):

    queue = Queue.Queue()
    
    print "\n... extracting features of dir {0}\n".format(dir)

    dirContents = glob.glob( os.path.join(dir, "*"))

    for infile in dirContents:
        
        if re.search(SUPPORTED_IMAGE_TYPES, infile, re.IGNORECASE) == None or not os.path.exists("{0}.feat.xml".format(infile)):
            continue
        
        if update:
            queue.put([BIN_EXTRACTFEATURES,"--update", "-o BOWHistogram", "--bow", "{0}/{1}".format(dir,BOW_FILE_NAME), infile])
        else:
            queue.put([BIN_EXTRACTFEATURES,"-a", "-o BOWHistogram", "--bow", "{0}/{1}".format(dir,BOW_FILE_NAME), infile])

    processExtractFeatures(queue, numThreads)

def processExtractFeatures(queue, numThreads):
    
    print "... {0} files to be processed\n".format(queue.qsize())

    if numThreads > 1:
        print "... parallel processing enabled: using {0} threads\n".format(numThreads)

    pool = []
    
    for i in range(numThreads):
        worker = ExtractFeaturesWorker(queue)
        pool.append(worker)
        worker.start()
    
    while len(pool) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool:
                thread.join(10)
                if not thread.isAlive():
                    pool.remove(thread)
            
        except KeyboardInterrupt:
            print "Ctrl-c received! Sending kill to threads..."
            for t in pool:
                t.kill_received = True

def getDuplicates(results, limit):

    duplicates = []
    i = 0

    if (limit == 0):
        limit = len(results)
    
    for res in results:
        
        if (res[0] > KNN_THRESHOLD) and (i < limit):
            duplicates.append(res)
            i += 1

    return duplicates



def extractFilename(path):
    
    tmp = path.split("\\")
    
    
    
    return tmp[-1].replace(".feat.xml", "")

def findCorrespondingImageInDifferentCollections(dirA, typeA, dirB, typeB, numThreads = 1):
    findDuplicatesInDifferentCollections(dirA, typeA, dirB, typeB, 1, numThreads)

def findDuplicatesInDifferentCollections(dirA, typeA, dirB, typeB, limit = 0, numThreads = 1):
    
    filesA = glob.glob( os.path.join(dirA, '{0}.feat.xml'.format(typeA)))
    filesB = glob.glob( os.path.join(dirA, '{0}.feat.xml'.format(typeB)))
    
    print("... {0} files found in Collection A\n".format(len(filesA)))
    print("... {0} files found in Collection B\n".format(len(filesB)))
    
    numIterations = (len(filesA) * (len(filesB) - 1)) / 2
    print("... {0} compare operations\n".format(numIterations))

    findDuplicates(filesA, filesB, limit, numThreads)
    
def findDuplicatesInSameCollection(dirA, typeA, numthreads):
    
    filesA = glob.glob( os.path.join(dirA, '*.{0}.feat.xml'.format(typeA)))
    
    print("... {0} files found in Collection A\n".format(len(filesA)))
    
    numIterations = (len(filesA) * (len(filesA) - 1)) / 2
    print("... {0} compare operations\n".format(numIterations))
    
    findDuplicates(filesA, filesA, 1, numthreads)
    
def findDuplicates(filesA, filesB, limit = 0, numThreads = 1):
    
    for fa in filesA:

        print("\nQ> {0}".format(extractFilename(fa)))
        
        results, kill_received = getKnn(fa, filesB, numThreads)
        
        if kill_received:
            break
        
        duplicates = getDuplicates(results, limit)


        for dup in duplicates:
            
            print("R> {0} [{1}]".format(extractFilename(dup[1]), dup[0]))
            #filesB.remove(dup[1])
        
        print("")
        
        #filesA.remove(fa)
        
def calculateBoW(featdir, filter):
    
    call([BIN_TRAIN, "-v", "--filter", filter, "-o", "{0}/{1}".format(featdir,BOW_FILE_NAME), featdir])

def clearDirectory(path):
    
    for f in glob.glob( os.path.join(path, '*.feat.xml')):
        print "... deleting {0}".format(f)
        os.remove(f)
        
    for f in glob.glob( os.path.join(path, BOW_FILE_NAME)):
        print "... deleting {0}".format(f)
        os.remove(f)
            
# === MAIN ======================================


def evaluation(dirA, typeA, dirB, typeB, numThreads, classfilepath, outputFilename = None):
    
    classfile = open(classfilepath, 'r')
    
    classes           = {}
    classmember_count = {}
    evalresult        = {}
    
    for line in classfile:
        tmp = line.split(';')
        classes[tmp[0]] = tmp[1].replace("\n","")
        
    
    filesA = glob.glob( os.path.join(dirA, '{0}.feat.xml'.format(typeA)))
    filesB = glob.glob( os.path.join(dirA, '{0}.feat.xml'.format(typeB)))
    
    print("... {0} files found in Collection A\n".format(len(filesA)))
    print("... {0} files found in Collection B\n".format(len(filesB)))
    
    numIterations = (len(filesA) * (len(filesB) - 1)) / 2
    print("... {0} compare operations\n".format(numIterations))

    for fa in filesA:

        queryFile = extractFilename(fa)
        print("\nQ> {0} ({1})".format(queryFile, classes[queryFile]))
        
        results, kill_received = getKnn(fa, filesB, numThreads)
        
        if kill_received:
            break

        if len(results) == 0:
            print "*** No Results!!!"
            continue
        
        classnameQuery = classes[queryFile]
        
        if classmember_count.has_key(classnameQuery):
            classmember_count[classnameQuery] += 1
        else:
            classmember_count[classnameQuery] = 1
            evalresult[classnameQuery]        = 0
        
        
        dup = results[0]

        resultFile = extractFilename(dup[1])
        classnameResult = classes[resultFile]
        
        print("R> {0} ({2}) [{1}]".format(resultFile, dup[0], classnameResult))
        
        if resultFile[:-5] == queryFile[:-5]:
            print "> SUCCESS"
            evalresult[classnameQuery] += 1
        else:
            print "> FAILED"
        #filesB.remove(dup[1])
        percentage = float(evalresult[classnameQuery])/float(classmember_count[classnameQuery])
        print "! Precission for %s : %02f" % (classnameQuery, percentage)
        
        print("")
    
    print "=== Summary: ===\n"
    
    file = None
    
    if outputFilename != None:
        file = open(outputFilename, 'w')
        
    
    for key in classmember_count.keys():
        
        percentage = float(evalresult[key])/float(classmember_count[key])
        print " * {0} \t: {1}".format(key, percentage)
        
        if outputFilename != None:
            file.write("{0};{1}\n".format(key,percentage))
            
            
    
    if outputFilename != None:
        file.close()
    print"\n"


def eval():
    
    global BIN_EXTRACTFEATURES, BIN_COMPARE, BIN_TRAIN, configuration
    
    configuration = "PC-Alex"
    
    BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
    BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
    BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]

    sdk = 250
    
    while (sdk <= 2500):
        
        clahe = 0
        
        while (clahe < 10):
            
            clahe += 1

            print "================================================================================"
            print " SDK   : ", sdk
            print " CLAHE : ", clahe
            print "================================================================================"
            
            outputFileName = "E:/_DATA/scape/EvalResult_SDK_{0}_CLAHE_{1}.txt".format(sdk,clahe)
            
            if os.path.exists(outputFileName):
                continue
            
            clearDirectory("E:/_DATA/scape/chinese_testsample/")
        
            print "=== extracting features from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
            extractFeatures("E:/_DATA/scape/chinese_testsample/", sdk,4, clahe)
        
            print "=== calculate BoW ==="
            calculateBoW("E:/_DATA/scape/chinese_testsample/", ".feat.xml")
        
            print "=== extract BoW Histograms from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
            extractBoWHistograms("E:/_DATA/scape/chinese_testsample/", 4)
        
            print "=== compare images from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
            evaluation("E:/_DATA/scape/chinese_testsample/", "*R.tif", "E:/_DATA/scape/chinese_testsample/", "*L.tif",4, "E:/_DATA/scape/IDP_Image_classes.csv", outputFileName)
            
        sdk += 250


if __name__ == '__main__':
    
    eval()

    global BIN_EXTRACTFEATURES, BIN_COMPARE, BIN_TRAIN, configuration

    parser = argparse.ArgumentParser()
    parser.add_argument('dir',       help='directory containing image files')
    parser.add_argument('action',    help='define which step of the workflow shouold be executed', choices=['all', 'extract', 'compare', 'train', 'bowhist'])
    parser.add_argument('--threads', help='number of concurrent threads',            type=int, default=1)
    parser.add_argument('--clear',   help='remove all generated files',              action='store_true')
    parser.add_argument('--filter',  help='Filter for BOW creation',                 type=str, default=".feat.xml")
    parser.add_argument('--update',  help='Update Feature',                          action='store_true')
    parser.add_argument('--sdk',     help='Number of Spatial Distincitve Keypoints', type=int, default=1000)
    parser.add_argument('--clahe',   help='Value of adaptive contrast enhancement (1 = no enhancement)', type=int, default=1)
    parser.add_argument('--config',  help='Configuration Parameter',                 type=str, default="PC-Alex")
    parser.add_argument('-v',        help="Print verbose messages",                  dest='verbose', action='store_true')
    args = vars(parser.parse_args())
    
    print args
    
    configuration = args['config']
    
    BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
    BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
    BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]
    
    if (args['clear']):
        clearDirectory(args['dir'])
        exit()
    
    if (args['action'] == 'extract') or (args['action'] == 'all'):
        print "=== extracting features from directory {0} ===".format(args['dir'])
        extractFeatures(args['dir'], args['sdk'],args['threads'], args['clahe'])
    
    if (args['action'] == 'train') or (args['action'] == 'all'):
        print "=== calculate BoW ==="
        calculateBoW(args['dir'], args['filter'])
    
    if (args['action'] == 'bowhist') or (args['action'] == 'all'):
        print "=== extract BoW Histograms from directory {0} ===".format(args['dir'])
        extractBoWHistograms(args['dir'], args['threads'], args['update'])
    
    if (args['action'] == 'compare') or (args['action'] == 'all'):
        print "=== compare images from directory {0} ===".format(args['dir'])
        evaluation(args['dir'], "*R.tif", args['dir'], "*L.tif",args['threads'], "E:/_DATA/scape/IDP_Image_classes.csv")
    
        #findDuplicatesInDifferentCollections(COLLECTION_A_DIR, COLLECTION_A_IMAGETYPE, COLLECTION_B_DIR, COLLECTION_B_IMAGETYPE)
        #findDuplicatesInSameCollection(args['dir'], "jp2",args['threads'])
        #findCorrespondingImageInDifferentCollections(args['dir'], "*R.tif", args['dir'], "*L.tif",args['threads'])
    