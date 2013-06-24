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
import glob
import gzip
import Queue
import threading
import operator
import time

import numpy as np
import subprocess as sub
import xml.etree.ElementTree as et

from xml.dom import minidom
from subprocess import call

# === definitions =================================================================================

BOW_FILE_NAME = "bow.xml"
SUPPORTED_IMAGE_TYPES = ".(png|tif|jpe?g|bmp|gif|jp2)$"

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

    idx = 1
    numFiles = 0
    incrementLock = threading.RLock()

    '''
    # ============================================
    # Constructor
    # ============================================
    #
    #   @summary: 
    #
    #   @param queue_images_to_process:   receive tasks from this queue_images_to_process
    '''
    def __init__(self, queue_images_to_process):
        
        threading.Thread.__init__(self)
        
        self.queue = queue_images_to_process
        self.active = True
        self.kill_received = False
        self.__class__.numFiles = queue_images_to_process.qsize()
        self.__class__.idx = 1

    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            while(not self.kill_received):
                
                task = self.queue.get_nowait()
                
                featureName = "ImageHistogram"
                
                for i in range(len(task)):
                    if task[i] == "-o":
                        featureName = task[i + 1]
                        break
                    
                filename = "{0}.{1}.feat.yml.gz".format(task[-1], featureName)
                
                for i in range(len(task)):
                    if task[i] == "-d":
                        filename = "{0}/{1}.{2}.feat.yml.gz".format(task[i + 1], extractFilename(task[-1]), featureName)
                        break
                
                exists = os.path.exists(filename)
                
                msg = "done"
                
                if exists:
                    msg = "feature file found"
                
                ExtractFeaturesWorker.incrementLock.acquire() 
                print "    [{1} of {2}] {0} ...{3}".format(extractFilename(task[-1]), self.__class__.idx, self.__class__.numFiles, msg)
                self.__class__.idx += 1
                ExtractFeaturesWorker.incrementLock.release()
                
                if not exists:
                    call(task)
                    
                
        except (Queue.Empty):
            pass
        
        # print "ExtractFeaturesWorker Thread stopped"
    
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
    #   @param queue_images_to_process:   receive tasks from this queue_images_to_process
    #   @param results: store results to this queue_images_to_process
    '''
    def __init__(self, config, queue_images_to_process, results):
        
        threading.Thread.__init__(self)
        
        self.queue = queue_images_to_process    
        self.results = results  
        self.config = config   
        self.kill_received = False
        
    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            # receive tasks from queue_images_to_process as long as no kill signal was received
            while(not self.kill_received):
                
                
                # get the next task from the queue_images_to_process
                (image1, image2, binary) = self.queue.get_nowait()
                
                
                # execute compare.exe
                # ===========================================================================
                # self.config['BIN_COMPARE']    ... path to compare binary
                # --bowmetric CV_COMP_INTERSECT ... use histogram intersection for comparison
                # image[0]                      ... path to image1
                # image[1]                      ... path to image2        
                cmd = [self.config['BIN_COMPARE'], "--metric", "CV_COMP_INTERSECT", image1, image2]
                
                if binary:
                    cmd.append("--binary")
                            
                process = sub.Popen(cmd, stdout=sub.PIPE, stderr=sub.PIPE)
                # store cmd-line output 
                output, errors = process.communicate()
                # convert output to utf-8
                outputStr = output.decode("utf-8")
                errorStr = errors.decode("utf-8")
    
                if len(errorStr) == 0:
                
                    xml_response = et.fromstring(outputStr)
                    
                    ssim = float(xml_response[0][0].text)
                    
                    KnnWorker.incrementLock.acquire() 
                    self.results.append([ssim, image2])
                    KnnWorker.incrementLock.release()
                
                else:
                    
                    print("    compare.exe -l 4 {0} {1}".format(image1, image2))
                    print(errorStr)
                    KnnWorker.incrementLock.acquire() 
                    self.results.append([-1, image2])
                    KnnWorker.incrementLock.release()
                
        except (Queue.Empty):
            pass
        

class ThreadedVerification(threading.Thread):

    # defining locks
    incrementLock = threading.RLock()
    current_idx   = 0
    t0 = None
    
    '''
    # ============================================
    # Constructor
    # ============================================
    #
    #   @summary: 
    #
    #   @param config:  configuration containing path information to matchbox binaries
    #   @param queue_images_to_process:   receive tasks from this queue_images_to_process
    #   @param results: store results to this queue_images_to_process
    '''
    def __init__(self, config, queue_images_to_process, results, initialListSize, binary):
        
        threading.Thread.__init__(self)
        
        self.queue           = queue_images_to_process    
        self.results         = results  
        self.config          = config   
        self.kill_received   = False
        self.binary          = binary
        self.initialListSize = initialListSize
        
        ThreadedVerification.incrementLock.acquire()
        if ThreadedVerification.t0 == None:
            ThreadedVerification.t0 = time.clock()
        ThreadedVerification.incrementLock.release()
        
    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            # receive tasks from queue_images_to_process as long as no kill signal was received
            while(not self.kill_received):
                
                
                # get the next task from the queue_images_to_process
                (query_key,neighbor_key), (query_filepath,neighbor_filepath), job_id = self.queue.get_nowait()
                
                # execute compare.exe
                # ===========================================================================
                # self.config['BIN_COMPARE']    ... path to compare binary
                # --bowmetric CV_COMP_INTERSECT ... use histogram intersection for comparison
                # image[0]                      ... path to image1
                # image[1]                      ... path to image2
                
                cmd = [self.config['BIN_COMPARE'], "--metric", "CV_COMP_INTERSECT", query_filepath.replace("BOWHistogram", "SIFTComparison"), neighbor_filepath.replace("BOWHistogram", "SIFTComparison")]
                
                #print cmd
                
                if self.binary:
                    cmd.append("--binary")
                
                process = sub.Popen(cmd, stdout=sub.PIPE, stderr=sub.PIPE)
                # store cmd-line output 
                output, errors = process.communicate()
                # convert output to utf-8
                outputStr = output.decode("utf-8")
                errorStr  = errors.decode("utf-8")
    
                if len(errorStr) == 0:
                
                    xml_response = et.fromstring(outputStr)
                    
                    ssim = float(xml_response[0][0].text)
                    
                    ThreadedVerification.incrementLock.acquire() 
                    self.results.append([job_id, ssim, query_key,neighbor_key])
                    t_diff = time.clock() - ThreadedVerification.t0
#                    print t_diff
                    estimated_time = (float(t_diff) / float(ThreadedVerification.current_idx+1)) * float(self.initialListSize - ThreadedVerification.current_idx - 1)
#                    print estimated_time
                    #print "[{0} of {1}] {2}, {3}, {4}, {5}, {6}".format(ThreadedVerification.current_idx, self.initialListSize, (query_key,neighbor_key), ssim, job_id, int(t_diff), int(estimated_time))
                    print "[{0} of {1}] {2}, {3}".format(ThreadedVerification.current_idx, self.initialListSize, (query_key,neighbor_key), ssim)
                    ThreadedVerification.current_idx += 1
                    ThreadedVerification.incrementLock.release()
                
                else:
                    
                    print("    compare.exe -l 4 {0} {1}".format(query_filepath, neighbor_filepath))
                    print(errorStr)
                    ThreadedVerification.incrementLock.acquire() 
                    self.results.append([job_id, -1, query_key,neighbor_key])
                    print "[{0} of {1}] {2}, {3}".format(ThreadedVerification.current_idx, self.initialListSize, (query_key,neighbor_key), 0)
                    ThreadedVerification.current_idx += 1
                    ThreadedVerification.incrementLock.release()
                
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
# @param NUM_THREADS_EXTRACTOR:
#   
# @return: results:
#          kill_received:
'''
def runSpatialVerification(config, queryFile, collection, dissimilarity_list, document_image_collection_entries, NUM_THREADS_EXTRACTOR=1, offset=0, limit=1, binary=False):

    # initialize variables
    results = []
    pool_ExtractFeaturesWorker = []
    jobs = Queue.Queue()

    kill_received = False
    
    idx = 0
    
    # compare query file against each file of collection
    for (neighbor, distance) in collection:
        
        query_key    = getFilename(queryFile)
        neighbor_key = getFilename(neighbor)
        
        if (idx - offset) >= limit:
            break
        
        # do not compare query file with itself
        if neighbor == queryFile:
            continue
        
        if neighbor_key in dissimilarity_list[query_key] or query_key in dissimilarity_list[neighbor_key]:
            print "!!! SKIP !!!"
            continue
        
        if neighbor_key in document_image_collection_entries[query_key]:
            print "!!! DUPLICATE DETECTED BEFORE !!!", query_key, "-", neighbor_key
            results.append([1,neighbor])
            idx += 1
            continue
        
        idx += 1
        
        if  idx <= offset:
            continue
        
        # put jobs into the working queue_images_to_process
        jobs.put([queryFile.replace("BOWHistogram", "SIFTComparison"), neighbor.replace("BOWHistogram", "SIFTComparison"), binary])

    
    # start worker threads
    for i in range(NUM_THREADS_EXTRACTOR):
        
        # create thread and provide config and queues
        worker = KnnWorker(config, jobs, results)
        pool_ExtractFeaturesWorker.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool_ExtractFeaturesWorker) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool_ExtractFeaturesWorker:
                thread.join()
                if not thread.isAlive():
                    pool_ExtractFeaturesWorker.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to threads..."
            
            kill_received = True
            for t in pool_ExtractFeaturesWorker:
                t.kill_received = True    
    
    # sort results in descending order
    results.sort(reverse=True)
    
    if kill_received:
        exit()
    
    return results







# === functions ===================================================================================

'''
# ============================================
# Function: processExtractFeatures
# ============================================
#
# @summary: 
#
# @param queue_images_to_process: 
# @param NUM_THREADS_EXTRACTOR:
#   
'''
def processExtractFeatures(queue_images_to_process, NUM_THREADS_EXTRACTOR):
    
    pool_ExtractFeaturesWorker = []

    if NUM_THREADS_EXTRACTOR > 1:
        print "... parallel processing enabled: using {0} threads".format(NUM_THREADS_EXTRACTOR)

    print "... {0} files to process\n".format(queue_images_to_process.qsize())

    # start worker threads
    for i in range(NUM_THREADS_EXTRACTOR):
        
        # create thread and provide queues
        worker = ExtractFeaturesWorker(queue_images_to_process)
        pool_ExtractFeaturesWorker.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool_ExtractFeaturesWorker) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool_ExtractFeaturesWorker:
                thread.join(10)
                if not thread.isAlive():
                    pool_ExtractFeaturesWorker.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to all threads..."
            
            for t in pool_ExtractFeaturesWorker:
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
# @param NUM_THREADS_EXTRACTOR:
# @param clahe:
# @param featdir:
# @param only:   
#   
'''
def extractFeatures(config, collectiondir, sdk, NUM_THREADS_EXTRACTOR=1, clahe=1, featdir="", only="", downsample=1000000, verbose=False, binary=False, binaryonly=False):

    queue_images_to_process = Queue.Queue()
    
    print "... extracting features of dir {0}".format(collectiondir)

    # process all entries of the supllied directory
    for currentFile in glob.glob(os.path.join(collectiondir, "*")):
        
        # check if the current file type is supported by this script
        if re.search(SUPPORTED_IMAGE_TYPES, currentFile, re.IGNORECASE) == None:
            continue
        
        # create job description
        job_desc = [config['BIN_EXTRACTFEATURES'], '--sdk', str(sdk), '--downsample', str(downsample), '--clahe', str(clahe)]
        
        # an "only" filter has been supplied
        if len(only) > 0:
            job_desc.append("-o")
            job_desc.append(only)
            
        if verbose:
            job_desc.append("-v")

        if binary:
            if binaryonly:
                job_desc.append("--binaryonly")
            else:
                job_desc.append("--binary")
        
        # a certain directory has been supplied to store feature files
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
        
        job_desc.append(currentFile)
        
        # add this job description to the queue_images_to_process
        queue_images_to_process.put(job_desc)

    # execute the queue_images_to_process
    processExtractFeatures(queue_images_to_process, NUM_THREADS_EXTRACTOR)

'''
# ============================================
# Function: extractBoWHistograms
# ============================================
#
# @summary: 
#
# @param config: 
# @param collectiondir:
# @param NUM_THREADS_EXTRACTOR:
# @param featdir:
# @param only:
# @param bowpath: 
#   
'''    
def extractBoWHistograms(config, collectiondir, NUM_THREADS_EXTRACTOR=1, featdir="", binary=False):

    queue_images_to_process = Queue.Queue()
    
    print "... extracting features of dir {0}".format(collectiondir)

    for currentFile in glob.glob(os.path.join(collectiondir, "*")):
        
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
        bowpath = "{0}/{1}".format(featdir, BOW_FILE_NAME)
        
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
            
        job_desc.append("--bow")
        job_desc.append(bowpath)
        
        if binary:
            job_desc.append("--binary")

        job_desc.append(currentFile)
        queue_images_to_process.put(job_desc)
        
    processExtractFeatures(queue_images_to_process, NUM_THREADS_EXTRACTOR)


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
    
    path = path.replace("\\", "/")
    
    tmp = path.split("/")
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
def calculateBoW(config, featdir, filenameFilter, clusterCenters=0, bowsize=1000, verbose=False, binary=False):
    
    cmd = [config['BIN_TRAIN'],
          "--bowsize", str(bowsize),
          "--precluster", '{0}'.format(clusterCenters),
          "--filter", filenameFilter,
          "-o", "{0}/{1}".format(featdir, BOW_FILE_NAME),
          featdir]
    
    if verbose:
        cmd.append("-v")
    
    if binary:
        cmd.append("--binary")
    
    call(cmd)

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
def clearDirectory(path, verbose=False):
    
    for currentFile in glob.glob(os.path.join(path, '*.feat.xml.gz')):
        if verbose:
            print "... deleting {0}".format(currentFile)
        os.remove(currentFile)

    for currentFile in glob.glob(os.path.join(path, BOW_FILE_NAME)):
        if verbose:
            print "... deleting {0}".format(currentFile)
        os.remove(currentFile)

    for currentFile in glob.glob(os.path.join(path, '*.SIFTComparison.descriptors.dat')):
        if verbose:
            print "... deleting {0}".format(currentFile)
        os.remove(currentFile)

    for currentFile in glob.glob(os.path.join(path, '*.SIFTComparison.keypoints.dat')):
        if verbose:
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
    for t in xmldoc[0][0][3].text.replace("\n", " ").split(" "):
        if len(t) > 0:
            data.append(float(t))
    
    # free memory
    xmldoc.clear()

    return np.array(data)


def loadSIFTFeatures(featureFile):

    # read xml-data
    f = gzip.open(featureFile, 'rb')
    xmldoc = et.fromstring(f.read())
    f.close()
    
    i = 0
    
    keypoints = []
    descriptors = []
    
    curr_keypoint = []
    curr_descriptor = []
    
    # parse xml-data
    for t in xmldoc[0][0].text.replace("\n", " ").split(" "):
        
        if len(t) > 0:
            if i <= 5:
                curr_keypoint.append(float(t))
                i += 1
        
            elif i > 5:
                curr_keypoint.append(float(t))
                keypoints.append(curr_keypoint)
                curr_keypoint = []
                i = 0
                
    for t in xmldoc[0][1][3].text.replace("\n", " ").split(" "):
            
        if len(t) > 0:
            if i <= 126:
                curr_descriptor.append(float(t))
                i += 1
        
            elif i > 126:
                curr_descriptor.append(float(t))
                descriptors.append(curr_descriptor)
                curr_descriptor = []
                i = 0

    # free memory
    xmldoc.clear()

    return keypoints, descriptors

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
    
    histograms = []
    distanceMatrix = []
    
    print "...loading features"
    
    # load bow histograms
    for featureFilename in glob.glob(os.path.join(featureDirectory, "*.BOWHistogram.feat.xml.gz")):
        
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
            dist = np.sum(np.minimum(query[1], collectionEntry[1]))
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
    
    histograms = []
    distanceMatrix = []
    
    print "...loading features"
    
    # load bow histograms
    for featureFilename in glob.glob(os.path.join(featureDirectory, "*.BOWHistogram.feat.xml.gz")):
        
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
            dist = np.sum(np.minimum(query[1], collectionEntry[1]))
            neighbors.append([dist, collectionEntry[0], collectionEntry[1]])
        
        
        # sort list in descending order
        neighbors.sort(reverse=True)
        
        shortlist = []
        
        for i in range(0, length):
            shortlist.append([neighbors[i][1], neighbors[i][0]])
        
        # add to distance matrix
        # 1. query filename
        # 2. Shortlist
        #   2.1 filename
        #   2.2 distance
        distanceMatrix.append([query[0], shortlist])

    return distanceMatrix

def getShortLists2(featureDirectory1, featureDirectory2, length):
    
    histograms1 = []
    histograms2 = []
    distanceMatrix = []
    
    print "...loading features"
    
    # load bow histograms1
    for featureFilename in glob.glob(os.path.join(featureDirectory1, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms1.append([featureFilename, data])
        
    # load bow histograms2
    for featureFilename in glob.glob(os.path.join(featureDirectory2, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms2.append([featureFilename, data])
        
    print "...calculating distance matrix"
    for query_filename, query_hist in histograms1:
        
        neighbors = []
        
        for col2_filename, col2_hist in histograms2:
            
            # calculate histogram intersections
            dist = np.sum(np.minimum(query_hist, col2_hist))
            neighbors.append([dist, col2_filename, col2_hist])
        
        
        # sort list in descending order
        neighbors.sort(reverse=True)
        
        shortlist = []
        
#        print "=================="
#        print ">", query_filename
        
        # dispKeypoints(query_filename, neighbors[0][1])
        
        if length == -1:
            length = len(neighbors) - 1
        
        for i in range(0, length):
#            print neighbors[i][0], neighbors[i][1]
            shortlist.append([neighbors[i][1], neighbors[i][0]])
        
        # add to distance matrix
        # 1. query filename
        # 2. Shortlist
        #   2.1 filename
        #   2.2 distance
        distanceMatrix.append([query_filename, shortlist])

    return distanceMatrix

def getShortLists3(query_filename, query_hist, featureDirectory2, length):
    
    histograms2 = []
    distanceMatrix = []
    
    
    print "...loading features"
        
    # load bow histograms2
    for featureFilename in glob.glob(os.path.join(featureDirectory2, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms2.append([featureFilename, data])

    print "...calculating distance matrix"
        
    print query_filename
    
    neighbors = []
    
    for col2_filename, col2_hist in histograms2:
        
        # calculate histogram intersections
        dist = np.sum(np.minimum(query_hist, col2_hist))
        neighbors.append([dist, col2_filename, col2_hist])
    
    
    # sort list in descending order
    neighbors.sort(reverse=True)
    
    shortlist = []
    
    if length == -1:
        length = len(neighbors)
    
    for i in range(0, length):
        shortlist.append([neighbors[i][1], neighbors[i][0]])
        
    print ">>>>", len(shortlist)
    
    # add to distance matrix
    # 1. query filename
    # 2. Shortlist
    #   2.1 filename
    #   2.2 distance
    distanceMatrix.append([query_filename, shortlist])

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
    
    process = sub.Popen([config['BIN_COMPARE'], "--metric", "CV_COMP_INTERSECT", f1, f2], stdout=sub.PIPE, stderr=sub.PIPE)
    output, errors = process.communicate()
    
    outputStr = output.decode("utf-8")
    errorStr = errors.decode("utf-8")
    
    if (len(errorStr) == 0):
        xmldoc = minidom.parseString(outputStr)
        xmltasks = xmldoc.getElementsByTagName('task')
        
        if len(xmltasks) > 0:
            for xmltask in xmltasks:
                if xmltask.hasAttribute('name'):
                    if xmltask.getAttribute('name') == feature:
                        if len(xmltask.getElementsByTagName(valueName)) > 0:
                            resultValue = float(xmltask.getElementsByTagName(valueName)[0].childNodes[0].nodeValue)
    else:
        print errorStr
                            
    return resultValue
            
# '''
## ============================================
# # Function: pyFindDuplicates
## ============================================
# #
# # @summary:
# #
# # @param config: 
# # @param featureDirectory: 
# # @param csv: 
# #
# # @return: loaded data
# #
# ''' 
# def pyFindDuplicates(config, featureDirectory, csv):
#    
#    # initialize hardcoded values
#    seclen       = 200
#    
#    # declare variables
#    distVals     = []
#    cluster_data = []
#    
#    # load distance matrix
#    dmatrix      = getDistanceMatrix(featureDirectory)
#    
#    # calculate number of cluster centers
#    num_cluster_center = math.ceil(len(dmatrix) / float(seclen))
#    
#    # exception: there should be at least three clusters
#    if num_cluster_center < 3:
#        num_cluster_center = 3
#    
#    print "...loading dispersion from feature files"
#    
#    i = 0    
#    for entry in dmatrix:
#
#        # building the feature vector:
#        # ============================
#        # i... control variable
#        # 4... dispersion    => dat[0]
#        # 5... uniformity    => dat[1]
#        dat = loadFeatureData2(entry[0].replace("BOWHistogram", "SIFTComparison"), [4,5])
#        cluster_data.append([ i, dat[0], dat[1] ])
#        
#        distVals.append(entry[2])
#        
#        i += 1
#    
#    [centers, labels] = kmeans2(np.array(cluster_data), num_cluster_center, 10, 1e-5, "points")
#    
# #    print labels
# #    print centers
#    
#    
#    # convert lists to numpy matrix
#    k          = 3
#    d          = np.matrix(distVals,dtype=float)
#    
#    ind = np.arange(len(distVals))[np.newaxis,:]
#    ind += 1
#    
# #    pylab.plot(ind.transpose(), d.transpose())
# #    pylab.show()
#    
#    
#    r          = np.array(dmatrix)
#    thresholds = np.zeros((1,len(labels)),dtype=float)
#    
#    print "...calculating Mean Absolute Deviations"
#    
#    # for each cluster center
#    for center_number in range(0,int(num_cluster_center - 1)):
#        
#        # which images are from this cluster?
#        indeces_of_images_of_this_cluster = (labels == center_number)
#        
#        # get distvals for this cluster
#        distvals_of_this_cluster = np.extract(indeces_of_images_of_this_cluster, d)
#    
#        # calc median and MAD for these values
#        medd = np.median(distvals_of_this_cluster)
#        mad  = k * np.median(np.abs(distvals_of_this_cluster - medd))
#    
#        # set threshold values for images of this cluster
#        thresholds = indeces_of_images_of_this_cluster.choose(thresholds,(medd+mad))
#        
#    
#        
#        
#    print "...selecting duplicate candidates"
#    indeces_of_images_exceeding_threshold = (d > thresholds) #duplicate candidates
#    
#    duplicates = r[np.where(indeces_of_images_exceeding_threshold)[1]]
#    
#    dup_found = set()
#    result    = []
#
#    print "...calculating structural similarity of candidates for spatial verification"
#    
#    print "\n=== List of detected duplicates ===\n"
#    
#    for dup in duplicates:
#        
#        if (dup[0,0] not in dup_found):
#            
#            f1 = str(dup[0,0]).replace("BOWHistogram", "SIFTComparison")
#            f2 = str(dup[0,1]).replace("BOWHistogram", "SIFTComparison")
#            
#            ssim = compare(config, f1, f2, "SIFTComparison", "ssim")
#            
#            f1 = extractFilename(f1).replace(".SIFTComparison", "")
#            f2 = extractFilename(f2).replace(".SIFTComparison", "")
#            
#            if ssim > 0.9:
#                print "{0} => {1} [ {2}% ] ==> Duplicate".format(f1, f2, (ssim * 100))
#                result.append([f1,f2,ssim])
#                dup_found.add(dup[0,1])
#            else:
#                print "{0} => {1} [ {2}% ] ==> No Duplicate".format(f1, f2, (ssim * 100))
#
#    return result


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

def getFilename(path):
    return path.replace("\\", "/").split("/")[-1].split(".")[0]


def pyFindDuplicates_SpatialVerification_googlebooks_optimized(config, featureDirectory, csv, threads=1, binary=False):
    
    # load distance matrix
    list_of_shortlists = getShortLists(featureDirectory, 5)
    
    idx = 1
    
    dissimilarity_list = {}
    document_image_collection_entries = {}
    duplicates = set()
    duplicate_keys = set()


    results = {}
    pool_ExtractFeaturesWorker = []
    jobs = Queue.Queue()
    
    test = []
    
    combinations = []
    
    job_id = 0
    
    for (query_filepath, shortlist) in list_of_shortlists:

        query_key = getFilename(query_filepath)
        
        for neighbor_filepath, distance in shortlist:
            
            neighbor_key = getFilename(neighbor_filepath)
            
            if (query_key,neighbor_key) not in combinations and (neighbor_key,query_key) not in combinations:
                combinations.append((query_key,neighbor_key))
                test.append([int(query_key),int(neighbor_key), distance])
                job = [(query_key,neighbor_key), (query_filepath,neighbor_filepath), job_id, distance]
                jobs.put_nowait(job)
                
        
        job_id += 1
    
    test = sorted(test)    
    
    
    last_in_streak = 0
    
    streaks_raw = []
    
    for i in range(len(test)):
        
        streaks_raw.append([test[i][1]-test[i][0], test[i][0], test[i][1], test[i][2]])
        
#        if test[i][0] == test[last_in_streak][0] + 1:
#            print test[i], (test[i][1]-test[i][0])
#            last_in_streak = i
        
    streaks_raw = sorted(streaks_raw)
    
    streaks = []
    
    
    for i in range(len(streaks_raw)):
        
        if i < len(streaks_raw) - 1 and (streaks_raw[i+1][1] - streaks_raw[i][1]) == 1 and (streaks_raw[i+1][2] - streaks_raw[i][2] == 1):  
            #print "STREAK:", streaks_raw[i], (streaks_raw[i+1][1] - streaks_raw[i][1]) == 1 and (streaks_raw[i+1][2] - streaks_raw[i][2] == 1),(streaks_raw[i+1][1] - streaks_raw[i][1]), (streaks_raw[i+1][2] - streaks_raw[i][2])
            streaks.append([streaks_raw[i][1], streaks_raw[i][2], streaks_raw[i][0], streaks_raw[i][3]])
        elif i > 1 and (streaks_raw[i][1] - streaks_raw[i-1][1]) == 1 and (streaks_raw[i][2] - streaks_raw[i-1][2] == 1):
            #print "STREAK:", streaks_raw[i], (streaks_raw[i+1][1] - streaks_raw[i][1]) == 1 and (streaks_raw[i+1][2] - streaks_raw[i][2] == 1),(streaks_raw[i+1][1] - streaks_raw[i][1]), (streaks_raw[i+1][2] - streaks_raw[i][2])
            streaks.append([streaks_raw[i][1], streaks_raw[i][2], streaks_raw[i][0], streaks_raw[i][3]])
        #else:
            #print "    NO:", streaks_raw[i]#, (streaks_raw[i+1][1] - streaks_raw[i][1]) == 1 and (streaks_raw[i+1][2] - streaks_raw[i][2] == 1),(streaks_raw[i+1][1] - streaks_raw[i][1]), (streaks_raw[i+1][2] - streaks_raw[i][2])
            
        
    
    streaks = sorted(streaks)
    
    start = 0
    curr_pnr = 2
    
    while start < len(streaks) - 1:
        
        streaks_final = []
    
        for i in range(start,len(streaks)):
            
            start = i 
            if streaks[i][0] - curr_pnr <= 1:
                streaks_final.append([streaks[i][2], streaks[i]])
                curr_pnr = streaks[i][0]
            else:
                curr_pnr = streaks[i][0]
                break
        
        streaks_final = sorted(streaks_final)
        
        test3 = {}
        
        for s in streaks_final:
            
            if test3.has_key(s[0]):
                test3[s[0]].append(s)
            else:
                test3[s[0]] = []
                test3[s[0]].append(s)
    
    
        test4 = []
    
        for t in test3.itervalues():
            test4.append([len(t),t])
    
        test4 = sorted(test4)
        
        print  test4[-1][1][0][1][1], test4[-1][1][-1][1][0], "\n"
        if test4[-1][1][0][1][1] > test4[-1][1][-1][1][0]:
            for t in test4[-1][1]:
                print "CORRECT:", t
        else:
            for t in test4[-1][1]:
                print "WRONG:", t
            
        print ""
    
    exit()
    
    
    
    
    initialListSize = jobs.qsize()
    
    # start worker threads
    for i in range(threads):
        
        # create thread and provide config and queues
        worker = ThreadedVerification(config, jobs, results, initialListSize, binary)
        pool_ExtractFeaturesWorker.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool_ExtractFeaturesWorker) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool_ExtractFeaturesWorker:
                thread.join()
                if not thread.isAlive():
                    pool_ExtractFeaturesWorker.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to threads..."
            
            kill_received = True
            for t in pool_ExtractFeaturesWorker:
                t.kill_received = True   
    
        
    exit()
    
    for (query_filepath, shortlist) in list_of_shortlists:
        
        # run spatial verification for shortlist
        spatialVerificationResult = runSpatialVerification(config, query_filepath, shortlist, dissimilarity_list, document_image_collection_entries, NUM_THREADS_EXTRACTOR=threads)
        
        # check spatial verification results
        for (SSIM, neighbor_filepath) in spatialVerificationResult:
            
            # extract filenames from path. The filenames are used as indexes
            query_key    = getFilename(query_filepath)
            neighbor_key = getFilename(neighbor_filepath)
            
            # if SSIM is higher than a given threshold, it can be 
            # concluded that the corresponding images are duplicates
            if SSIM >= 0.8:
                
                # we have found a duplicated image pair

                # update duplicate information for corresponding entries
                document_image_collection_entries[query_key].add(neighbor_key)
                document_image_collection_entries[neighbor_key].add(query_key)
                duplicate_keys.add(query_key)
                duplicate_keys.add(neighbor_key)
                
                # distribute changes
                all_keys = set()
                map(lambda x: all_keys.add(x), document_image_collection_entries[query_key])
                map(lambda x: all_keys.add(x), document_image_collection_entries[neighbor_key])
                
                for neighbor_key in all_keys.copy():
                    map(lambda x: all_keys.add(x), document_image_collection_entries[neighbor_key])

                for neighbor_key in all_keys:
                    map(lambda x: document_image_collection_entries[neighbor_key].add(x), all_keys)
                
                for neighbor_key, val in document_image_collection_entries.iteritems():
                    if len(val) > 0:
                        print neighbor_key, val
                        
            else:
                
                if neighbor_key not in duplicate_keys:
                    dissimilarity_list[query_key].add(neighbor_key)
                else:
                    print "is duplicate", neighbor_key
                if query_key not in duplicate_keys:
                    dissimilarity_list[neighbor_key].add(query_key)
                else:
                    print "is duplicate", query_key
                
#                for neighbor_key, val in dissimilarity_list.iteritems():
#                    if len(val) > 0:
#                        print neighbor_key, val
                
        print "[{0} of {1}]".format(idx, len(list_of_shortlists))


        idx += 1

    map(lambda x: duplicates.add(frozenset(x)), document_image_collection_entries.values())
    
    print "\n=== Summary ==============================================\n"
    
    print "{0} sets of duplicates detected".format(len(duplicates))
    
    
    list_duplicates = []
    
    for x in duplicates:
        
        list_item = []
        
        for y in sorted(x, neighbor_key=lambda item: (int(item.partition(' ')[0])
                               if item[0].isdigit() else float('inf'), item)):
            
            list_item.append(y)
            
        list_duplicates.append(list_item)
    
    
    for x in sorted(list_duplicates):
        
        print x

    print "\n==========================================================\n"




def pyFindDuplicates_SpatialVerification_fast(config, featureDirectory, csv, threads=1, binary=False, benchmark=False):
    
    # load distance matrix
    list_of_shortlists = getShortLists(featureDirectory, 3)
    
    idx = 1
    
    dissimilarity_list = {}
    document_image_collection_entries = []
    duplicates = set()
    duplicate_keys = set()


    results = []
    pool_ExtractFeaturesWorker = []
    jobs = Queue.Queue()
    
    combinations = set()
    
    job_id = 0
    
    tmp_idx = 0
    
    for (query_filepath, shortlist) in list_of_shortlists:
        
        
#        if tmp_idx > 20:
#            break

        query_key = getFilename(query_filepath)
        
        for neighbor_filepath, distance in shortlist:
            
            tmp_idx += 1
            
            neighbor_key = getFilename(neighbor_filepath)
            
#            if (query_key,neighbor_key) not in combinations and (neighbor_key,query_key) not in combinations:
#                document_image_collection_entries.append([query_key,neighbor_key, query_filepath,neighbor_filepath, job_id])
#                combinations.add((query_key,neighbor_key))
            document_image_collection_entries.append([query_key,neighbor_key, query_filepath,neighbor_filepath, job_id])
            combinations.add((query_key,neighbor_key))
                
        job_id += 1
                

    document_image_collection_entries = sorted(document_image_collection_entries)
    
    for query_key,neighbor_key, query_filepath,neighbor_filepath, job_id in document_image_collection_entries:
                
        job = [(query_key,neighbor_key), (query_filepath,neighbor_filepath), job_id]
        jobs.put_nowait(job)
                
    
    initialListSize = jobs.qsize()
    
    # start worker threads
    for i in range(threads):
        
        # create thread and provide config and queues
        worker = ThreadedVerification(config, jobs, results, initialListSize, binary)
        pool_ExtractFeaturesWorker.append(worker)
        
        # start thread
        worker.start()
    
    # wait for all threads to finish
    while len(pool_ExtractFeaturesWorker) > 0:
        try:
            # Join all threads using a timeout so it doesn't block
            # Filter out threads which have been joined or are None
            for thread in pool_ExtractFeaturesWorker:
                thread.join()
                if not thread.isAlive():
                    pool_ExtractFeaturesWorker.remove(thread)
            
        except KeyboardInterrupt:
            
            # control-c detected - shut down threads and terminate
            print "Ctrl-c received! Sending kill to threads..."
            
            kill_received = True
            for t in pool_ExtractFeaturesWorker:
                t.kill_received = True   
    
    
    #results_npz = np.load(os.path.join(featureDirectory, "matchbox_data_results.npz"))
    #results = results_npz["results"].tolist()
    
    #np.savez(os.path.join(featureDirectory, "matchbox_data_results"), results=results)
    
    results = sorted(results)

    # find threshold
    #from scipy.signal import find_peaks_cwt
    
    dists = []

    for entry in results:
        dists.append(float(entry[1]))
    
    #peaks = find_peaks_cwt(dists, np.arange(1,40))

    #peak_dists = []
    
    #for i in peaks:
    #    peak_dists.append( dists[i])
    
#    dists2 = []
#    
#    threshold = np.percentile(dists,85)
#    for d in dists:
#        if d > threshold:
#            dists2.append(d)
#    
#    threshold = np.percentile(dists2,90)
#    
    threshold = np.percentile(dists,97.5) 
    
#    for r in results:
#        print r
#        
#        
#    print "------------------------"
        
    
    results_lookup_table = {}
    
    # create lookup table
    for job_results in results:
        results_lookup_table[(job_results[2],job_results[3])] = job_results[1]
        
#        if job_results[0] == current_job_id:
#            current_job.append([job_results[1],job_results[2],job_results[3]])
#        else:
#            list_of_job_results.append(current_job)
#            current_job = []
#            current_job.append([job_results[1],job_results[2],job_results[3]])
#            current_job_id = job_results[0]
            
            
    list_of_job_results = []
    current_job = []
    
    for (query_filepath, shortlist) in list_of_shortlists:
        
        query_key = getFilename(query_filepath)
        
        for neighbor_filepath, distance in shortlist:
            
            neighbor_key = getFilename(neighbor_filepath)
            
            if (query_key,neighbor_key) in results_lookup_table.keys():
                current_job.append([results_lookup_table[(query_key,neighbor_key)],query_key,neighbor_key])
            elif (neighbor_key,query_key) in results_lookup_table.keys():
                current_job.append([results_lookup_table[(neighbor_key,query_key)],neighbor_key,query_key])
            else:
                print "***", (query_key,neighbor_key), "not found in lookup table!"
        
        current_job = sorted(current_job)
        list_of_job_results.append(current_job)
        current_job = []
    
    items = {}
    undecided_items = {}
    
    benchmark_file_result_list = None
    benchmark_file_result_sets = None
    benchmark_file_undecided   = None
    benchmark_results          = None
    
    
    if benchmark:
        benchmark_file_result_list = open(os.path.join(featureDirectory, "benchmark_result_list.csv"), 'w')
        benchmark_file_result_sets = open(os.path.join(featureDirectory, "benchmark_result_sets.csv"), 'w')
        benchmark_file_undecided   = open(os.path.join(featureDirectory, "benchmark_undecided.csv"),   'w')
        benchmark_results = []
        
    for job_results in list_of_job_results:
        
        undecided = False
        ids = {}
        
        #print job_results
        for j in job_results:
            
            if not ids.has_key(j[1]):
                ids[j[1]] = 0
            if not ids.has_key(j[2]):
                ids[j[2]] = 0
            
            ids[j[1]] += 1
            ids[j[2]] += 1
            
            if float(j[0]) == float(-1):
                undecided = True
            
        if undecided:
            sorted_ids = sorted(ids.iteritems(), key=operator.itemgetter(1))
            query_id = sorted_ids[-1][0]
            
            undecided_items[query_id] = []
            
            for j in job_results:
                if j[1] == query_id:
                    undecided_items[query_id].append(j[2])
                else:
                    undecided_items[query_id].append(j[1])
            
            continue
        
        
        if len(job_results) > 0 and float(job_results[-1][0]) > threshold:
            
            job_result = job_results[-1]
            
            print job_result
            
            if benchmark:
                benchmark_results.append([job_result[1],job_result[2],job_result[0]])

    
            if not items.has_key(job_result[1]):
                items[job_result[1]] = set()
            
            if not items.has_key(job_result[2]):
                items[job_result[2]] = set()
                    
            items[job_result[1]].add(job_result[2])
            items[job_result[1]].add(job_result[1])
            items[job_result[2]].add(job_result[1])
            items[job_result[2]].add(job_result[2])
            
            # distribute changes
            all_keys = set()
            all_keys.add(job_result[1])
            all_keys.add(job_result[2])
            map(lambda x: all_keys.add(x), items[job_result[1]])
            map(lambda x: all_keys.add(x), items[job_result[2]])
            
            for key in all_keys.copy():
                map(lambda x: all_keys.add(x), items[key])
    
            for key in all_keys:
                map(lambda x: items[key].add(x), all_keys)
            
#            for key, val in items.iteritems():
#                if len(val) > 0:
#                    print key, val

    if benchmark:
        for b in sorted(benchmark_results):
            benchmark_file_result_list.write("{0};{1};{2}\n".format(b[0],b[1],b[2]))
    
    duplicates = set()
    
    map(lambda x: duplicates.add(frozenset(x)), items.values())
    
    print "\n=== Summary ==============================================\n"
    
    print "{0} sets of duplicates detected:\n".format(len(duplicates))
    
    
    list_duplicates = []
    
    for x in duplicates:
        
        list_item = []
        
        for y in sorted(x, key=lambda item: (int(item.partition(' ')[0])
                               if item[0].isdigit() else float('inf'), item)):
            
            list_item.append(y)
            
        list_duplicates.append(list_item)
    
    
    for x in sorted(list_duplicates):
        
        print x
        
        if benchmark:
            
            line = x[0]
            
            for i in range(1,len(x)):
                line = "{0};{1}".format(line,x[i])
            
            benchmark_file_result_sets.write("{0}\n".format(line))
        
    print "\n{0} undecided images with possible duplicates:\n".format(len(undecided_items))

    for k,v in undecided_items.iteritems():
        print k,v
        
        benchmark_file_undecided.write(k,v,"\n")
    
    
    np.savez(os.path.join(featureDirectory, "matchbox_data_duplicates"), list_duplicates=list_duplicates, undecided_items=undecided_items)
    
    if benchmark:
        benchmark_file_result_list.close()
        benchmark_file_result_sets.close()
        benchmark_file_undecided.close()
    
    exit()
    
    
    
    
    
    for (query_filepath, shortlist) in list_of_shortlists:
        
        # run spatial verification for shortlist
        spatialVerificationResult = runSpatialVerification(config, query_filepath, shortlist, dissimilarity_list, document_image_collection_entries, NUM_THREADS_EXTRACTOR=threads)
        
        # check spatial verification results
        for (SSIM, neighbor_filepath) in spatialVerificationResult:
            
            # extract filenames from path. The filenames are used as indexes
            query_key    = getFilename(query_filepath)
            neighbor_key = getFilename(neighbor_filepath)
            
            # if SSIM is higher than a given threshold, it can be 
            # concluded that the corresponding images are duplicates
            if SSIM >= 0.8:
                
                # we have found a duplicated image pair

                # update duplicate information for corresponding entries
                document_image_collection_entries[query_key].add(neighbor_key)
                document_image_collection_entries[neighbor_key].add(query_key)
                duplicate_keys.add(query_key)
                duplicate_keys.add(neighbor_key)
                
                # distribute changes
                all_keys = set()
                map(lambda x: all_keys.add(x), document_image_collection_entries[query_key])
                map(lambda x: all_keys.add(x), document_image_collection_entries[neighbor_key])
                
                for neighbor_key in all_keys.copy():
                    map(lambda x: all_keys.add(x), document_image_collection_entries[neighbor_key])

                for neighbor_key in all_keys:
                    map(lambda x: document_image_collection_entries[neighbor_key].add(x), all_keys)
                
                for neighbor_key, val in document_image_collection_entries.iteritems():
                    if len(val) > 0:
                        print neighbor_key, val
                        
            else:
                
                if neighbor_key not in duplicate_keys:
                    dissimilarity_list[query_key].add(neighbor_key)
                else:
                    print "is duplicate", neighbor_key
                if query_key not in duplicate_keys:
                    dissimilarity_list[neighbor_key].add(query_key)
                else:
                    print "is duplicate", query_key
                
#                for neighbor_key, val in dissimilarity_list.iteritems():
#                    if len(val) > 0:
#                        print neighbor_key, val
                
        print "[{0} of {1}]".format(idx, len(list_of_shortlists))


        idx += 1

    map(lambda x: duplicates.add(frozenset(x)), document_image_collection_entries.values())
    
    print "\n=== Summary ==============================================\n"
    
    print "{0} sets of duplicates detected".format(len(duplicates))
    
    
    list_duplicates = []
    
    for x in duplicates:
        
        list_item = []
        
        for y in sorted(x, neighbor_key=lambda item: (int(item.partition(' ')[0])
                               if item[0].isdigit() else float('inf'), item)):
            
            list_item.append(y)
            
        list_duplicates.append(list_item)
    
    
    for x in sorted(list_duplicates):
        
        print x

    print "\n==========================================================\n"




def pyFindDuplicates_SpatialVerification(config, featureDirectory, csv, threads=1, binary=False):
    
    # load distance matrix
    list_of_shortlists = getShortLists(featureDirectory, 3)
    
    idx = 1
    
    dissimilarity_list = {}
    document_image_collection_entries = {}
    duplicates = set()
    duplicate_keys = set()
    
    for (query_filepath, shortlist) in list_of_shortlists:
        query_key    = getFilename(query_filepath)
        dissimilarity_list[query_key] = set()
        document_image_collection_entries[query_key] = set()
    
    for (query_filepath, shortlist) in list_of_shortlists:
        
        # run spatial verification for shortlist
        spatialVerificationResult = runSpatialVerification(config, query_filepath, shortlist, dissimilarity_list, document_image_collection_entries, NUM_THREADS_EXTRACTOR=threads, limit=len(shortlist), binary=binary)
        
        rank = 1
        
        # check spatial verification results
        for (SSIM, neighbor_filepath) in spatialVerificationResult:
            
            # extract filenames from path. The filenames are used as indexes
            query_key    = getFilename(query_filepath)
            neighbor_key = getFilename(neighbor_filepath)
            
            # if SSIM is higher than a given threshold, it can be 
            # concluded that the corresponding images are duplicates
            if SSIM >= 0.90 and rank == 1:
                
                # we have found a duplicated image pair

                # update duplicate information for corresponding entries
                document_image_collection_entries[query_key].add(neighbor_key)
                document_image_collection_entries[neighbor_key].add(query_key)
                duplicate_keys.add(query_key)
                duplicate_keys.add(neighbor_key)
                
                # distribute changes
                all_keys = set()
                map(lambda x: all_keys.add(x), document_image_collection_entries[query_key])
                map(lambda x: all_keys.add(x), document_image_collection_entries[neighbor_key])
                
                for key in all_keys.copy():
                    map(lambda x: all_keys.add(x), document_image_collection_entries[key])

                for key in all_keys:
                    map(lambda x: document_image_collection_entries[key].add(x), all_keys)
                
                for key, val in document_image_collection_entries.iteritems():
                    if len(val) > 0:
                        print key, val
                        
            elif SSIM < 0.80:
                
                if neighbor_key not in duplicate_keys:
                    dissimilarity_list[query_key].add(neighbor_key)
                if query_key not in duplicate_keys:
                    dissimilarity_list[neighbor_key].add(query_key)

            rank += 1
                
        print "[{0} of {1}]".format(idx, len(list_of_shortlists))


        idx += 1

    map(lambda x: duplicates.add(frozenset(x)), document_image_collection_entries.values())
    
    print "\n=== Summary ==============================================\n"
    
    print "{0} sets of duplicates detected".format(len(duplicates))
    
    
    list_duplicates = []
    
    for x in duplicates:
        
        list_item = []
        
        for y in sorted(x, key=lambda item: (int(item.partition(' ')[0])
                               if item[0].isdigit() else float('inf'), item)):
            
            list_item.append(y)
            
        list_duplicates.append(list_item)
    
    
    for x in sorted(list_duplicates):
        
        print x

    print "\n==========================================================\n"



def pyFindReferences(config, col1, col2):
    
    shortlist = getShortLists2(col1, col2, -1)
    
    idx = 1
    concurrent_searches = 6
    
    print "...running spatial verification"
    
    for (query, neighbors) in shortlist:
        
#        if idx < 4:
#            idx += 1
#            continue
        
        result = runSpatialVerification(config, query, neighbors, 2, limit=2)
        
        duplicates = []
        
        for r in result:
            
            if r[0] >= 0.7:
                
                duplicates.append(r[1].replace("\\", "/").split("/")[-1].split(".")[0])
        
        offset = 2
        
        while len(duplicates) == 0 and offset < len(neighbors):
            
            result = runSpatialVerification(config, query, neighbors, NUM_THREADS_EXTRACTOR=concurrent_searches, offset=offset, limit=concurrent_searches)
        
            for r in result:
                
                if r[0] >= 0.7:
                    
                    duplicates.append(r[1].replace("\\", "/").split("/")[-1].split(".")[0])
            
            offset += concurrent_searches
        
        duplicates.sort()
        
        if len(duplicates) > 0:
            print "[{0} of {1}] {2} => {3}".format(idx, len(shortlist), query.replace("\\", "/").split("/")[-1].split(".")[0], duplicates)
        else:
            print "[{0} of {1}] {2}".format(idx, len(shortlist), query.replace("\\", "/").split("/")[-1].split(".")[0])
            
        idx += 1
        
    
