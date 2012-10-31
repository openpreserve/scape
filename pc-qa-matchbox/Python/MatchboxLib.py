'''
# ======================================================== #
# Module  : MatchboxLib                                    #
#                                                          #
# @author : Alexander Schindler                            #
# @contact: Alexander.Schindler@ait.ac.at                  #
#                                                          #
# @version:                                                # 
#                                                          #
# -------------------------------------------------------- #
#                                                          #
# @summary:                                                # 
#                                                          #
# -------------------------------------------------------- #
#                                                          #
# @license:                                                #
#                                                          #
# ======================================================== #
'''
# === imports =====================================================================================

import re
import os
import sys
import glob
import gzip
import math
import Queue
import threading

import numpy as np
import subprocess as sub
import xml.etree.ElementTree as et

from xml.dom import minidom
from subprocess import call
from scipy.cluster.vq import kmeans2

import pylab

# === definitions =================================================================================

BOW_FILE_NAME          = "bow.xml"
SUPPORTED_IMAGE_TYPES  = ".(png|tif|jpe?g|bmp|gif|jp2)$"

# === Classes =====================================================================================

'''
# ======================================================== #
# Class  : ExtractFeaturesWorker                           #
#                                                          #
# @author: Alexander Schindler                             #
# -------------------------------------------------------- #
#                                                          #
# @summary:                                                #
#                                                          #
# ======================================================== #
'''
class ExtractFeaturesWorker(threading.Thread):

    idx           = 1
    numFiles      = 0
    incrementLock = threading.RLock()

    '''
    # ============================================
    # Constructor
    # ============================================
    #
    #   @summary: 
    #
    #   @param queue:   receive tasks from this queue
    '''
    def __init__(self, queue):
        
        threading.Thread.__init__(self)
        
        self.queue              = queue
        self.active             = True
        self.kill_received      = False
        self.__class__.numFiles = queue.qsize()

    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            while(not self.kill_received):
                
                task     = self.queue.get_nowait()
                
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













class KnnWorker(threading.Thread):

    # defining locks
    incrementLock = threading.RLock()
    
    '''
    # ============================================
    # Constructor
    # ============================================
    #
    #   @summary: 
    #
    #   @param config:  configuration containing path information to matchbox binaries
    #   @param queue:   receive tasks from this queue
    #   @param results: store results to this queue
    '''
    def __init__(self, config, queue, results):
        
        threading.Thread.__init__(self)
        
        self.queue         = queue    
        self.results       = results  
        self.config        = config   
        self.kill_received = False
    
    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            # receive tasks from queue as long as no kill signal was received
            while(not self.kill_received):
                
                # get the next task from the queue
                (image1,image2,distance) = self.queue.get_nowait()
                
                # execute compare.exe
                # ===========================================================================
                # self.config['BIN_COMPARE']    ... path to compare binary
                # --bowmetric CV_COMP_INTERSECT ... use histogram intersection for comparison
                # image[0]                      ... path to image1
                # image[1]                      ... path to image2
                process        = sub.Popen([self.config['BIN_COMPARE'], 
                                        "--metric", "CV_COMP_INTERSECT", 
                                        image1, 
                                        image2], 
                                        stdout=sub.PIPE,stderr=sub.PIPE)
                # store cmd-line output 
                output, errors = process.communicate()
                # convert output to utf-8
                outputStr      = output.decode("utf-8")
                errorStr       = errors.decode("utf-8")
    
                if len(errorStr) == 0:
                
                    xml_response = et.fromstring(outputStr)
                    
                    ssim         = float(xml_response[0][0].text)
                    sv           = distance * ssim
                    
                    ExtractFeaturesWorker.incrementLock.acquire() 
                    self.results.append([sv,image2])
                    ExtractFeaturesWorker.incrementLock.release()
                
                else:
                    
                    print("    compare.exe -l 4 {0} {1}".format(image1, image2))
                    print(errorStr)
                    ExtractFeaturesWorker.incrementLock.acquire() 
                    self.results.append([-1, image2])
                    ExtractFeaturesWorker.incrementLock.release()
                
        except (Queue.Empty):
            pass
        


'''
# ============================================
# Function: getKnn
# ============================================
#
# @summary: 
#
# @param config: 
# @param queryFile:
# @param collection:
# @param numThreads:
#   
# @return: results:
#          kill_received:
'''
def runSpatialVerification(config, queryFile, collection, numThreads = 1):

    # initialize variables
    results       = []
    pool          = []
    jobs          = Queue.Queue()

    kill_received = False
    
    # compare query file against each file of collection
    for (neighbor, distance) in collection:
        
        # do not compare query file with itself
        if (neighbor == queryFile):
            continue

        # put jobs into the working queue
        jobs.put([queryFile.replace("BOWHistogram", "SIFTComparison"),neighbor.replace("BOWHistogram", "SIFTComparison"), distance])

    # start worker threads
    for i in range(numThreads):
        
        # create thread and provide config and queues
        worker = KnnWorker(config, jobs, results)
        pool.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool:
                thread.join(10)
                if not thread.isAlive():
                    pool.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to threads..."
            
            kill_received = True
            for t in pool:
                t.kill_received = True    
    
    # sort results in descending order
    results.sort(reverse=True)
    
    return results, kill_received







# === functions ===================================================================================

'''
# ============================================
# Function: processExtractFeatures
# ============================================
#
# @summary: 
#
# @param queue: 
# @param numThreads:
#   
'''
def processExtractFeatures(queue, numThreads):
    
    pool = []

    if numThreads > 1:
        print "... parallel processing enabled: using {0} threads".format(numThreads)

    print "... {0} files to process\n".format(queue.qsize())

    # start worker threads
    for i in range(numThreads):
        
        # create thread and provide queues
        worker = ExtractFeaturesWorker(queue)
        pool.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool:
                thread.join(10)
                if not thread.isAlive():
                    pool.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to all threads..."
            
            for t in pool:
                t.kill_received = True

'''
# ============================================
# Function: extractFeatures
# ============================================
#
# @summary: 
#
# @param config: 
# @param collectiondir:
# @param sdk:
# @param numThreads:
# @param clahe:
# @param featdir:
# @param only:   
#   
'''
def extractFeatures(config, collectiondir, sdk, numThreads = 1, clahe = 1, featdir = "", only = ""):

    queue = Queue.Queue()
    
    print "... extracting features of dir {0}".format(collectiondir)

    # process all entries of the supllied directory
    for currentFile in glob.glob( os.path.join(collectiondir, "*")):
        
        # check if the current file type is supported by this script
        if re.search(SUPPORTED_IMAGE_TYPES, currentFile, re.IGNORECASE) == None:
            continue
        
        # create job description
        job_desc = [config['BIN_EXTRACTFEATURES'], '--sdk', str(sdk), '--clahe', str(clahe)]
        
        # an "only" filter has been supplied
        if len(only) > 0:
            job_desc.append("-o")
            job_desc.append(only)
        
        # a certain directory has been supplied to store feature files
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
        
        job_desc.append(currentFile)
        
        # add this job description to the queue
        queue.put(job_desc)

    # execute the queue
    processExtractFeatures(queue, numThreads)

'''
# ============================================
# Function: extractBoWHistograms
# ============================================
#
# @summary: 
#
# @param config: 
# @param collectiondir:
# @param numThreads:
# @param featdir:
# @param only:
# @param bowpath: 
#   
'''    
def extractBoWHistograms(config, collectiondir, numThreads = 1, featdir = "", bowpath = ""):

    queue = Queue.Queue()
    
    print "... extracting features of dir {0}".format(collectiondir)

    for currentFile in glob.glob( os.path.join(collectiondir, "*")):
        
        path = ""
        
        if len(featdir) > 0:
            path = "{0}/{1}.SIFTComparison.feat.xml.gz".format(featdir, extractFilename(currentFile))
        else:
            path = "{0}.SIFTComparison.feat.xml.gz".format(currentFile)

        if (re.search(SUPPORTED_IMAGE_TYPES, currentFile, re.IGNORECASE) == None) or (not os.path.exists(path)):
            continue
        
        # create job description
        job_desc = [config['BIN_EXTRACTFEATURES'], "-o", "BOWHistogram"]
        
        # if no bowpath is supplied, the bow-file is expected 
        # to be in the supplied feature directory
        if len(bowpath) == 0:
            bowpath = "{0}/{1}".format(featdir,BOW_FILE_NAME)
        
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
            
        job_desc.append("--bow")
        job_desc.append(bowpath)

        job_desc.append(currentFile)
        queue.put(job_desc)
        
    processExtractFeatures(queue, numThreads)


'''
# ============================================
# Function: extractFilename
# ============================================
#
# @summary: 
#
# @param path: 
#
# @return: filename   
'''   
def extractFilename(path):
    
    sep = "\\"
    
    if os.name == "posix":
        sep = "/"
    
    tmp = path.split(sep)
    return tmp[-1].replace(".feat.xml.gz", "")

'''
# ============================================
# Function: calculateBoW
# ============================================
#
# @summary:
#
# @param config:
# @param featdir:
# @param filenameFilter:
# @param clusterCenters:
# @param bowsize:   
#
'''      
def calculateBoW(config, featdir, filenameFilter, clusterCenters = 0, bowsize = 1000):
    
    call([config['BIN_TRAIN'], 
          "--bowsize",    str(bowsize), 
          "--precluster", '{0}'.format(clusterCenters), 
          "--filter",     filenameFilter, 
          "-o",           "{0}/{1}".format(featdir,BOW_FILE_NAME), 
          featdir])

'''
# ============================================
# Function: clearDirectory
# ============================================
#
# @summary:
#
# Deletes all automatically created files
# from the supplied directory
#
# @param path: the path that should be cleaned
#
'''   
def clearDirectory(path):
    
    for currentFile in glob.glob( os.path.join(path, '*.feat.xml.gz')):
        print "... deleting {0}".format(currentFile)
        os.remove(currentFile)
        
    for currentFile in glob.glob( os.path.join(path, BOW_FILE_NAME)):
        print "... deleting {0}".format(currentFile)
        os.remove(currentFile)
            
'''
# ============================================
# Function: loadBOWHistogram
# ============================================
#
# @summary:
#
# loads feature data from zipped xml files
#
# @param featureFile: the path of the feature file
#
# @return: loaded data
#
''' 
def loadBOWHistogram(featureFile):

    # read xml-data
    f = gzip.open(featureFile, 'rb')
    xmldoc = et.fromstring(f.read())
    f.close()
    
    # parse xml-data
    data = []    
    for t in xmldoc[0][0][3].text.replace("\n"," ").split(" "):
        if len(t) > 0:
            data.append(float(t))
    
    # free memory
    xmldoc.clear()

    return np.array(data)

'''
# ============================================
# Function: loadFeatureData2
# ============================================
#
# @summary:
#
# loads feature data from zipped xml files
#
# @param featureFile: the path of the feature file
# @param dataItemIDs: array of indexes that correspond to xml entries
#
# @return: loaded data
#
''' 
def loadFeatureData2(featureFile, dataItemIDs):

    data = []

    f = gzip.open(featureFile, 'rb')
    xmldoc = et.fromstring(f.read())
    f.close()
    
    for idx in dataItemIDs:
        data.append(float(xmldoc[0][idx].text))
        
    xmldoc.clear()
        
    return data

'''
# ============================================
# Function: getDistanceMatrix
# ============================================
#
# @summary:
#
# @param featureDirectory: 
#
# @return: 
#
''' 
def getDistanceMatrix(featureDirectory):
    
    histograms     = []
    distanceMatrix = []
    
    print "...loading features"
    
    # load bow histograms
    for featureFilename in glob.glob( os.path.join(featureDirectory, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms.append([featureFilename, data])
        
    print "...calculating distance matrix"
    for query in histograms:
        
        neighbors = []
        
        for collectionEntry in histograms:
            
            if (collectionEntry == query):
                continue
            
            # calculate histogram intersections
            dist = np.sum(np.minimum(query[1],collectionEntry[1]))
            neighbors.append([dist, collectionEntry[0], collectionEntry[1]])
        
        
        # sort list in descending order
        neighbors.sort(reverse=True)
        
        # add to distance matrix
        # 1. query filename
        # 2. nearest neighbor
        # 3. distance value
        distanceMatrix.append([query[0], neighbors[0][1], neighbors[0][0]])

    return distanceMatrix

'''
# ============================================
# Function: getShortLists
# ============================================
#
# @summary:
#
# @param featureDirectory: 
#
# @return: 
#
''' 
def getShortLists(featureDirectory, length):
    
    histograms     = []
    distanceMatrix = []
    
    print "...loading features"
    
    # load bow histograms
    for featureFilename in glob.glob( os.path.join(featureDirectory, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms.append([featureFilename, data])
        
    print "...calculating distance matrix"
    for query in histograms:
        
        neighbors = []
        
        for collectionEntry in histograms:
            
            if (collectionEntry == query):
                continue
            
            # calculate histogram intersections
            dist = np.sum(np.minimum(query[1],collectionEntry[1]))
            neighbors.append([dist, collectionEntry[0], collectionEntry[1]])
        
        
        # sort list in descending order
        neighbors.sort(reverse=True)
        
        shortlist = []
        
        for i in range(0,length):
            shortlist.append([neighbors[i][1], neighbors[i][0]])
        
        # add to distance matrix
        # 1. query filename
        # 2. Shortlist
        #   2.1 filename
        #   2.2 distance
        distanceMatrix.append([query[0], shortlist])

    return distanceMatrix

'''
# ============================================
# Function: compare
# ============================================
#
# @summary:
#
# @param config: 
# @param f1: 
# @param f2: 
# @param feature: 
# @param valueName: 
#
# @return: 
#
''' 
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
            
'''
# ============================================
# Function: pyFindDuplicates
# ============================================
#
# @summary:
#
# @param config: 
# @param featureDirectory: 
# @param csv: 
#
# @return: loaded data
#
''' 
def pyFindDuplicates(config, featureDirectory, csv):
    
    # initialize hardcoded values
    seclen       = 200
    
    # declare variables
    distVals     = []
    cluster_data = []
    
    # load distance matrix
    dmatrix      = getDistanceMatrix(featureDirectory)
    
    # calculate number of cluster centers
    num_cluster_center = math.ceil(len(dmatrix) / float(seclen))
    
    # exception: there should be at least three clusters
    if num_cluster_center < 3:
        num_cluster_center = 3
    
    print "...loading dispersion from feature files"
    
    i = 0    
    for entry in dmatrix:

        # building the feature vector:
        # ============================
        # i... control variable
        # 4... dispersion    => dat[0]
        # 5... uniformity    => dat[1]
        dat = loadFeatureData2(entry[0].replace("BOWHistogram", "SIFTComparison"), [4,5])
        cluster_data.append([ i, dat[0], dat[1] ])
        
        distVals.append(entry[2])
        
        i += 1
    
    [centers, labels] = kmeans2(np.array(cluster_data), num_cluster_center, 10, 1e-5, "points")
    
#    print labels
#    print centers
    
    
    # convert lists to numpy matrix
    k          = 3
    d          = np.matrix(distVals,dtype=float)
    
    ind = np.arange(len(distVals))[np.newaxis,:]
    ind += 1
    
#    pylab.plot(ind.transpose(), d.transpose())
#    pylab.show()
    
    
    r          = np.array(dmatrix)
    thresholds = np.zeros((1,len(labels)),dtype=float)
    
    print "...calculating Mean Absolute Deviations"
    
    # for each cluster center
    for center_number in range(0,int(num_cluster_center - 1)):
        
        # which images are from this cluster?
        indeces_of_images_of_this_cluster = (labels == center_number)
        
        # get distvals for this cluster
        distvals_of_this_cluster = np.extract(indeces_of_images_of_this_cluster, d)
    
        # calc median and MAD for these values
        medd = np.median(distvals_of_this_cluster)
        mad  = k * np.median(np.abs(distvals_of_this_cluster - medd))
    
        # set threshold values for images of this cluster
        thresholds = indeces_of_images_of_this_cluster.choose(thresholds,(medd+mad))
        
    
        
        
    print "...selecting duplicate candidates"
    indeces_of_images_exceeding_threshold = (d > thresholds) #duplicate candidates
    
    duplicates = r[np.where(indeces_of_images_exceeding_threshold)[1]]
    
    dup_found = set()
    result    = []

    print "...calculating structural similarity of candidates for spatial verification"
    
    print "\n=== List of detected duplicates ===\n"
    
    for dup in duplicates:
        
        if (dup[0,0] not in dup_found):
            
            f1 = str(dup[0,0]).replace("BOWHistogram", "SIFTComparison")
            f2 = str(dup[0,1]).replace("BOWHistogram", "SIFTComparison")
            
            ssim = compare(config, f1, f2, "SIFTComparison", "ssim")
            
            f1 = extractFilename(f1).replace(".SIFTComparison", "")
            f2 = extractFilename(f2).replace(".SIFTComparison", "")
            
            if ssim > 0.9:
                print "{0} => {1} [ {2}% ] ==> Duplicate".format(f1, f2, (ssim * 100))
                result.append([f1,f2,ssim])
                dup_found.add(dup[0,1])
            else:
                print "{0} => {1} [ {2}% ] ==> No Duplicate".format(f1, f2, (ssim * 100))

    return result


'''
# ============================================
# Function: pyFindDuplicates_SpatialVerification
# ============================================
#
# @summary:
#
# @param config: 
# @param featureDirectory: 
# @param csv: 
#
# @return: loaded data
#
''' 
def pyFindDuplicates_SpatialVerification(config, featureDirectory, csv):
    
    # initialize hardcoded values
    
    # declare variables
    
    # load distance matrix
    shortlist = getShortLists(featureDirectory, 4)
    
    idx = 1
    
    for (query,neighbors) in shortlist:
        
        result, kill = runSpatialVerification(config, query, neighbors, 4)
        
        duplicates = []
        
        for r in result:
            
            if r[0] >= 0.7:
                duplicates.append(int(r[1].split("\\")[-1].split(".")[0]))
        
        duplicates.sort()
        
        if len(duplicates) > 0:
            print "[{0} of {1}] {2} => {3}".format(idx, len(shortlist), int(query.split("\\")[-1].split(".")[0]), duplicates)
        else:
            print "[{0} of {1}] {2}".format(idx, len(shortlist), int(query.split("\\")[-1].split(".")[0]))
            
        idx += 1
