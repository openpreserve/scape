'''
Created on 28.06.2012

@author: SchindlerA
'''


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

from multiprocessing import Pool
from subprocess import call
from xml.dom import minidom

# === definitions ===============================

BOW_FILE_NAME          = "bow.xml"
KNN_THRESHOLD          = 0

SUPPORTED_IMAGE_TYPES  = ".(png|tif|jpe?g|bmp|gif|jp2)$"

# === Classes ===================================

class KnnWorker(threading.Thread):

    incrementLock = threading.RLock()
    
    def __init__(self, config, queue, results):
        threading.Thread.__init__(self)
        self.queue = queue
        self.results = results
        self.kill_received = False
        self.config = config

    def run(self):
        
        try:
            while(not self.kill_received):
                
                task = self.queue.get_nowait()
                
                process = sub.Popen([self.config['BIN_COMPARE'], "-l", "4", "--bowmetric", "CV_COMP_INTERSECT", task[0], task[1]], stdout=sub.PIPE,stderr=sub.PIPE)
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
                                    
                                    if len(xmltask.getElementsByTagName('result')) > 0:
                                        result  = float(xmltask.getElementsByTagName('result')[0].childNodes[0].nodeValue)
                                    else:
                                        result = -1
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
    numFiles      = 0
    incrementLock = threading.RLock()

    def __init__(self, queue):
        threading.Thread.__init__(self)
        self.queue = queue
        self.active = True
        self.kill_received = False
        self.__class__.numFiles = queue.qsize()
        
    def run(self):
        
        try:
            while(not self.kill_received):
                
                task = self.queue.get_nowait()
                
                filename = "{0}.{1}.feat.xml.gz".format(task[-1], task[2])
                
                for i in range(len(task)):
                    if task[i] == "-d":
                        filename = "{0}/{1}.{2}.feat.xml.gz".format(task[i+1],extractFilename(task[-1]), task[2])
                        break
                
                exists = os.path.exists(filename)
                
                msg = "done"
                
                if exists:
                    msg = "feature file found"
                
                ExtractFeaturesWorker.incrementLock.acquire() 
                print "    [{1} of {2}] {0} ...{3}".format(extractFilename(task[-1]), self.__class__.idx,self.__class__.numFiles, msg)
                self.__class__.idx += 1
                ExtractFeaturesWorker.incrementLock.release()
                
                if not exists:
                    call(task)
                    
                
        except (Queue.Empty):
            pass
        
        #print "ExtractFeaturesWorker Thread stopped"
    
    def stop(self):
        self.active = False

# === functions =================================

def getKnn(config, fa, files, numThreads = 1):


    kill_received = False

    results = []
    jobs    = Queue.Queue()

    for fb in files:

        if (fa == fb):
            continue

        jobs.put([fa,fb])

    pool = []
    
    for i in range(numThreads):
        worker = KnnWorker(config, jobs, results)
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

def extractFeatures(config, collectiondir, sdk, numThreads = 1, clahe = 1, featdir = "", precluster = 0):

    queue = Queue.Queue()
    
    print "... extracting features of dir {0}".format(collectiondir)

    dirContents = glob.glob( os.path.join(collectiondir, "*"))

    for infile in dirContents:
        
        if re.search(SUPPORTED_IMAGE_TYPES, infile, re.IGNORECASE) == None:
            continue
        
        job_desc = [config['BIN_EXTRACTFEATURES'], "-o", "SIFTComparison", '--sdk', '{0}'.format(sdk), '--clahe', '{0}'.format(clahe), '--precluster', '{0}'.format(precluster)]
        
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
        
        job_desc.append(infile)
        
        queue.put(job_desc)

    processExtractFeatures(queue, numThreads)

    
def extractBoWHistograms(config, dir, numThreads = 1, featdir = ""):

    queue = Queue.Queue()
    
    print "... extracting features of dir {0}".format(dir)

    dirContents = glob.glob( os.path.join(dir, "*"))
    

    for infile in dirContents:
        
        path = "{0}.SIFTComparison.feat.xml.gz".format(infile)
        
        if len(featdir) > 0:
            path = "{0}/{1}.SIFTComparison.feat.xml.gz".format(featdir, extractFilename(infile))

        if re.search(SUPPORTED_IMAGE_TYPES, infile, re.IGNORECASE) == None or not os.path.exists(path):
            continue
        
        job_desc = [config['BIN_EXTRACTFEATURES'], "-o", "BOWHistogram"]
        
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
            job_desc.append("--bow")
            job_desc.append("{0}/{1}".format(featdir,BOW_FILE_NAME))
        else:
            job_desc.append("--bow")
            job_desc.append("{0}/{1}".format(dir,BOW_FILE_NAME))

        job_desc.append(infile)
        queue.put(job_desc)
        
    processExtractFeatures(queue, numThreads)

def processExtractFeatures(queue, numThreads):
    
    print "... {0} files to be processed".format(queue.qsize())

    if numThreads > 1:
        print "... parallel processing enabled: using {0} threads".format(numThreads)

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
            print "Ctrl-c received! Sending kill to all threads..."
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
    
    sep = "\\"
    
    if os.name == "posix":
        sep = "/"
    
    tmp = path.split(sep)
    return tmp[-1].replace(".feat.xml.gz", "")

def findCorrespondingImageInDifferentCollections(dirA, typeA, dirB, typeB, numThreads = 1):
    findDuplicatesInDifferentCollections(dirA, typeA, dirB, typeB, 1, numThreads)

def findDuplicatesInDifferentCollections(dirA, typeA, dirB, typeB, limit = 0, numThreads = 1):
    
    filesA = glob.glob( os.path.join(dirA, '{0}.BOWHistogram.feat.xml.gz'.format(typeA)))
    filesB = glob.glob( os.path.join(dirA, '{0}.BOWHistogram.feat.xml.gz'.format(typeB)))
    
    print("... {0} files found in Collection A\n".format(len(filesA)))
    print("... {0} files found in Collection B\n".format(len(filesB)))
    
    numIterations = (len(filesA) * (len(filesB) - 1)) / 2
    print("... {0} compare operations\n".format(numIterations))

    findDuplicates(filesA, filesB, limit, numThreads)
    
def findDuplicatesInSameCollection(config, dirA, typeA, numthreads):
    
    filesA = glob.glob( os.path.join(dirA, '*.{0}.BOWHistogram.feat.xml.gz'.format(typeA)))
    
    print("... {0} files found in Collection A\n".format(len(filesA)))
    
    numIterations = (len(filesA) * (len(filesA) - 1)) / 2
    print("... {0} compare operations\n".format(numIterations))
    
    findDuplicates(config, filesA, filesA, 1, numthreads)
    
def findDuplicates(config, filesA, filesB, limit = 0, numThreads = 1):
    
    for fa in filesA:
        
        print("\nQ> {0}".format(extractFilename(fa)))
        
        results, kill_received = getKnn(config, fa, filesB, numThreads)
        
        if kill_received:
            break
        
        duplicates = getDuplicates(results, limit)

        for dup in duplicates:
            
            print("R> {0} [{1}]".format(extractFilename(dup[1]), dup[0]))
        
        print("")
        
def calculateBoW(config, featdir, filter, clusterCenters = 0):
    
    call([config['BIN_TRAIN'], "--precluster", '{0}'.format(clusterCenters), "--filter", filter, "-o", "{0}/{1}".format(featdir,BOW_FILE_NAME), featdir])

def clearDirectory(path):
    
    for f in glob.glob( os.path.join(path, '*.feat.xml.gz')):
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
    
    print("... {0} files found in Collection A".format(len(filesA)))
    print("... {0} files found in Collection B".format(len(filesB)))
    
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
    
def loadBOWHistogram(filename):

    data = []

    f = gzip.open(filename, 'rb')

    xmldoc = minidom.parseString(f.read())
    
    f.close()
    
    xmlBowhistNode = xmldoc.getElementsByTagName('BOWHistogram')
    
    if len(xmlBowhistNode) > 0: # ==> data found
        dataNode = xmlBowhistNode[0].getElementsByTagName('data')
        
        if len(dataNode) > 0: # ==> data found
            rawdata = dataNode[0].childNodes[0].nodeValue
            rawdata = rawdata.replace("      ", "").replace("\n", " ").split(" ")
            
            for rd in rawdata:
                
                if rd == '':
                    continue
                
                data.append(float(rd))

    return data

def histIntersect(A,B):
    
    sumMin = 0
    
    for i in range(len(A[1])):
        sumMin += min(A[1][i], B[1][i])

    return sumMin

def getDistanceMatrix(dirname):
    
    histograms = []
    dmatrix = []
    
    print "...loading features"
    
    dirContents = glob.glob( os.path.join(dirname, "*.BOWHistogram.feat.xml.gz"))
    
    for featFile in dirContents:
        data = loadBOWHistogram(featFile)
        histograms.append([featFile, data])
        
    print "...calculating distances"
    
    for q1 in histograms:
        
        neighbors = []
        
        for q2 in histograms:
            
            if (q1 == q2):
                continue
            
            dist = histIntersect(q1, q2)
            neighbors.append([dist, q2])
                
        neighbors.sort(reverse=True)
        
        dmatrix.append([q1[0], neighbors])

    return dmatrix
            
def pyCompareMAD(dirname):
    
    histograms = []
    
    print "...loading features"
    
    dirContents = glob.glob( os.path.join(dirname, "*.BOWHistogram.feat.xml.gz"))
    
    i = 0
    for featFile in dirContents:
    
        if i % 10 == 0:
            print i, "of", len(dirContents)
        
        i += 1
    
        data = loadBOWHistogram(featFile)
        
        histograms.append([featFile, data])
        
#        if i > 100:
#            break
        
    
    query = histograms[0]
    
    print "...calculating distances"
    
    
    results = []
    distVals = []
    
    idx = 0
    
    histograms2 = copy.copy(histograms)
    
    for q1 in histograms:
        
        neighbors = []
        
        for q2 in histograms2:
            
            if (q1 == q2):
                continue
            
            dist = histIntersect(q1, q2)
            neighbors.append([dist, q2])
                
        neighbors.sort(reverse=True)
        
        
        results.append([q1[0], neighbors[0][1][0], neighbors[0][0]])
        distVals.append(neighbors[0][0])
        
        #histograms.remove(neighbors[0][1])
        #histograms2.remove(q1)
        #histograms2.remove(neighbors[0][1])
        
        print len(histograms),len(histograms2)
        
        
        idx += 1


    print "...", idx, "distances calculated"
    
    k       = 3
    srange  = 10
    sthresh = 3
   
    d = np.matrix(distVals,dtype=float)
    r = np.array(results)
    
    medd = np.median(d,axis=1)
    
    mad = k * np.median(np.abs(d - medd),axis=1)

    idx = np.where(d > (medd+mad)) #duplicate candidates
    
    print r[idx[1].tolist()]
    
    print "...local aggregation of duplicate candidates"
    
    dlist = []
    run = []
    
    
    for j in range(len(distVals)):
        didx = np.abs(j-idx[1])
        dlist.append(np.count_nonzero(np.where(didx<srange)))
        
    print dlist

    print "...duplicate run detection"     
    nruns = 0;
    inrun = 0;
    j=0;
    
    while (j < len(distVals)):       
        if (inrun == 0) and (dlist[j] > sthresh):
            inrun = 1;
            run.append([{'start': j}])
           
        if inrun > 0 and (dlist[j] < sthresh):
            inrun = 0;
            run[nruns].append({'end':j})                      
            nruns += 1;
                
        j += 1;


    print run

#def eval():
#    
#    global BIN_EXTRACTFEATURES, BIN_COMPARE, BIN_TRAIN, configuration
#    
#    configuration = "PC-Alex"
#    
#    BIN_EXTRACTFEATURES = configs[configuration]["BIN_EXTRACTFEATURES"]
#    BIN_COMPARE         = configs[configuration]["BIN_COMPARE"]
#    BIN_TRAIN           = configs[configuration]["BIN_TRAIN"]
#
#    sdk = 250
#    
#    while (sdk <= 2500):
#        
#        clahe = 0
#        
#        while (clahe < 10):
#            
#            clahe += 1
#
#            print "================================================================================"
#            print " SDK   : ", sdk
#            print " CLAHE : ", clahe
#            print "================================================================================"
#            
#            outputFileName = "E:/_DATA/scape/EvalResult_SDK_{0}_CLAHE_{1}.txt".format(sdk,clahe)
#            
#            if os.path.exists(outputFileName):
#                continue
#            
#            clearDirectory("E:/_DATA/scape/chinese_testsample/")
#        
#            print "=== extracting features from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
#            extractFeatures("E:/_DATA/scape/chinese_testsample/", sdk,4, clahe)
#        
#            print "=== calculate BoW ==="
#            calculateBoW("E:/_DATA/scape/chinese_testsample/", ".feat.xml")
#        
#            print "=== extract BoW Histograms from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
#            extractBoWHistograms("E:/_DATA/scape/chinese_testsample/", 4)
#        
#            print "=== compare images from directory {0} ===".format("E:/_DATA/scape/chinese_testsample/")
#            evaluation("E:/_DATA/scape/chinese_testsample/", "*R.tif", "E:/_DATA/scape/chinese_testsample/", "*L.tif",4, "E:/_DATA/scape/IDP_Image_classes.csv", outputFileName)
#            
#        sdk += 250
        

def compare(config, f1, f2, feature, valueName):
    
    resultValue = -1
    
    process = sub.Popen([config['BIN_COMPARE'],"--metric", "CV_COMP_INTERSECT", f1, f2], stdout=sub.PIPE,stderr=sub.PIPE)
    output, errors = process.communicate()
    
    outputStr = output.decode("utf-8")
    errorStr  = errors.decode("utf-8")
    
    if (len(errorStr) == 0):
        xmldoc = minidom.parseString(outputStr)
        xmltasks = xmldoc.getElementsByTagName('task')
        
        if len(xmltasks) > 0:
            for xmltask in xmltasks:
                if xmltask.hasAttribute('name'):
                    if xmltask.getAttribute('name') == feature:
                        if len(xmltask.getElementsByTagName(valueName)) > 0:
                            resultValue  = float(xmltask.getElementsByTagName(valueName)[0].childNodes[0].nodeValue)
    else:
        print errorStr
                            
    return resultValue
    
