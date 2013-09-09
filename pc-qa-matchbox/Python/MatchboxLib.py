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

import numpy as np
import subprocess as sub
import xml.etree.ElementTree as et

# === definitions =================================================================================

BOW_FILE_NAME         = "bow.xml"
SUPPORTED_IMAGE_TYPES = ".(png|tiff?|jpe?g|bmp|gif|jp2)$"

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

    incrementLock = threading.RLock()
    idx           = 1
    numFiles      = 0

    '''
    # ============================================
    # Constructor
    # ============================================
    #
    #   @summary: 
    #
    #   @param queue_images_to_process:   receive tasks from this queue_images_to_process
    '''
    def __init__(self, queue_images_to_process, update):
        
        threading.Thread.__init__(self)
        
        self.queue              = queue_images_to_process
        self.active             = True
        self.kill_received      = False
        self.update             = update
        self.__class__.numFiles = queue_images_to_process.qsize()
        self.__class__.idx      = 1

    '''
    # ============================================
    # Method: run
    # ============================================
    '''
    def run(self):
        
        try:
            
            while(self.active and not self.kill_received):
                
                # get next task from queue
                task   = self.queue.get_nowait()
                msg    = "done"
                exists = False

                # if feature files should not be updated,
                # check if they already exist                
                if not self.update:
                    
                    # if not a specific feature is to be extracted,
                    # it can be assumed that all features were extracted
                    # previously. Thus, ImageHistogram features have to
                    # be available in the features directory 
                    featureName = "ImageHistogram"
                    
                    # search in the command description if a specific 
                    # feature name is supplied and use this one instead 
                    for i in range(len(task)):
                        
                        if task[i] == "-o":
                            featureName = task[i + 1]
                            break
                    
                    # create filename
                    filename = "{0}.{1}.feat.xml.gz".format(task[-1], featureName)
                        
                    # check if a separate feature directory was supplied 
                    for i in range(len(task)):
                        
                        if task[i] == "-d":
                            filename = "{0}/{1}.{2}.feat.xml.gz".format(task[i + 1], 
                                                                        extractFilename(task[-1]), 
                                                                        featureName)
                            break
                    
                    # check if file exists
                    exists = os.path.exists(filename)
                
                # call shell command
                if self.update or not exists:
                    
                    # open pipe to shell and execute command
                    process = sub.Popen(task, stdout=sub.PIPE, stderr=sub.PIPE)
                    
                    # store cmd-line output 
                    _, errors = process.communicate()
                    
                    # convert output to utf-8
                    errorStr  = errors.decode("utf-8")
                    error_msg = ""
                    
                    # process error string
                    if len(errorStr) > 0:
                        
                        # extract error string
                        for line in errorStr.split("\n"):
                            tmp = line.strip().split(":")
                            if len(tmp) == 2 and tmp[0] == "Reason":
                                error_msg = tmp[1].strip()
        
                    if error_msg != "":
                        msg = "error: {0}".format(error_msg)
        
                else:
                    msg = "existing feature file found"

                # write some output to shell
                ExtractFeaturesWorker.incrementLock.acquire() 
                
                print "[{1} of {2}] {0} {3}".format(extractFilename(task[-1]), self.__class__.idx, self.__class__.numFiles, msg)
                self.__class__.idx += 1
                
                ExtractFeaturesWorker.incrementLock.release()
                
        except (Queue.Empty):
            pass
        
        except:
            
            ExtractFeaturesWorker.incrementLock.acquire() 
            print " Thread interrupted => shutting down!"
            ExtractFeaturesWorker.incrementLock.release()
        
    def stop(self):
        self.active = False



class ThreadedVerification(threading.Thread):

    # defining locks
    incrementLock = threading.RLock()
    current_idx   = 0
    
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
    def __init__(self, config, queue_images_to_process, results, initialListSize, binary, verbose):
        
        threading.Thread.__init__(self)
        
        self.queue           = queue_images_to_process    
        self.results         = results  
        self.config          = config   
        self.kill_received   = False
        self.verbose         = verbose
        self.binary          = binary
        self.initialListSize = initialListSize
        
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
                (query_key,neighbor_key), (query_filepath,neighbor_filepath), col_entry_id = self.queue.get_nowait()
                
                # execute compare.exe
                # ===========================================================================
                # self.config['BIN_COMPARE']    ... path to compare binary
                # --bowmetric CV_COMP_INTERSECT ... use histogram intersection for comparison
                # image[0]                      ... path to image1
                # image[1]                      ... path to image2
                
                cmd = [self.config['BIN_COMPARE'], 
                       "--metric", "CV_COMP_INTERSECT", 
                       query_filepath.replace("BOWHistogram", "SIFTComparison"), 
                       neighbor_filepath.replace("BOWHistogram", "SIFTComparison")]
                
                if self.binary:
                    cmd.append("--binary")

                # open pipe to shell and execute command
                process = sub.Popen(cmd, stdout=sub.PIPE, stderr=sub.PIPE)
                
                # store cmd-line output 
                output, errors = process.communicate()
                
                # convert output to utf-8
                outputStr = output.decode("utf-8")
                errorStr  = errors.decode("utf-8")
    
                # translate response    
                if len(errorStr) == 0:
                
                    xml_response = et.fromstring(outputStr)
                    ssim         = float(xml_response[0][0].text)
                    
                    error_str = ""
                    
                    for elem in xml_response[0]:
                        if elem.tag == 'ERROR':
                            error_str = elem.text
                            
                    if error_str != "":
                        error_str = ", Error: '{0}'".format(error_str)
                        
                    ThreadedVerification.incrementLock.acquire()
                    
                    self.results.append([col_entry_id, ssim, query_key,neighbor_key])
                    
                    print "[{0} of {1}] Query-Image: '{2}', Duplicate-Candidate: '{3}' => Structured-Similarity: {4}{5}".format(ThreadedVerification.current_idx, 
                                                                                                                                self.initialListSize, 
                                                                                                                                query_key,
                                                                                                                                neighbor_key, 
                                                                                                                                ssim, 
                                                                                                                                error_str)
                    ThreadedVerification.current_idx += 1
                    
                    ThreadedVerification.incrementLock.release()
                
                else:
                    
                    try:
                        xml_response = et.fromstring(outputStr)
                        error_msg    = xml_response[0][0].text
                    except Exception as e:
                        error_msg = "Unknown Error"
                    
                    ThreadedVerification.incrementLock.acquire()

                    if self.verbose:
                        
                        print "\n*** ERROR IN: compare binary comparing the files:"
                        print "     ", query_filepath
                        print "     ", neighbor_filepath
                        print "--- BEGIN - OUTPUT MESSAGE ---"
                        print outputStr.strip()
                        print "--- END   - OUTPUT MESSAGE ---"
                        print "--- BEGIN - ERROR MESSAGE  ---"
                        print errorStr.strip()
                        print "--- END   - ERROR MESSAGE  ---\n"
                        
                    print "[{0} of {1}] Query-Image: '{2}', Duplicate-Candidate: '{3}' => Structured-Similarity: {4}, *** ERROR '{5}'".format(ThreadedVerification.current_idx, self.initialListSize, query_key,neighbor_key, -1, error_msg)
                    
                    self.results.append([col_entry_id, -1, query_key,neighbor_key])
                    
                    ThreadedVerification.current_idx += 1
                    ThreadedVerification.incrementLock.release()
                
        except (Queue.Empty):
            pass
        
        except Exception as e:
            
            if str(e).find("no element found: line 1, column 0") != -1:
                
                ThreadedVerification.incrementLock.acquire() 
                print " Thread interrupted => shutting down!"
                ThreadedVerification.incrementLock.release()
        



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
def processExtractFeatures(queue_images_to_process, NUM_THREADS_EXTRACTOR, update):
    
    # tell user what is happening
    if NUM_THREADS_EXTRACTOR > 1:
        print "... parallel processing enabled: using {0} threads".format(NUM_THREADS_EXTRACTOR)

    print "... {0} files to process\n".format(queue_images_to_process.qsize())

    # create worker pool
    pool_ExtractFeaturesWorker = []

    # start worker threads
    for _ in range(NUM_THREADS_EXTRACTOR):
        
        # create thread and provide queues
        worker = ExtractFeaturesWorker(queue_images_to_process, update)
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
def extractFeatures(config, collectiondir, sdk, num_threads=1, clahe=1, featdir="", only="", downsample=1000000, verbose=False, binary=False, binaryonly=False, update=False):

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
    processExtractFeatures(queue_images_to_process, num_threads, update)

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
def extractBoWHistograms(config, collectiondir, num_threads=1, featdir="", binary=False, update=False):

    print "... extracting features of dir {0}".format(collectiondir)

    queue_images_to_process = Queue.Queue()

    for currentFile in glob.glob(os.path.join(collectiondir, "*")):
        
        # check if SIFT features are available for the current
        # image file
        path = ""
        
        if len(featdir) > 0:
            path = "{0}/{1}.SIFTComparison.feat.xml.gz".format(featdir, extractFilename(currentFile))
        else:
            path = "{0}.SIFTComparison.feat.xml.gz".format(currentFile)

        if (re.search(SUPPORTED_IMAGE_TYPES, currentFile, re.IGNORECASE) == None) or (not os.path.exists(path)):
            continue
        
        # create job description
        job_desc = [config['BIN_EXTRACTFEATURES'], "-o", "BOWHistogram"]
        
        # a different directory has been supplied to store feature files
        if len(featdir) > 0:
            job_desc.append("-d")
            job_desc.append(featdir)
            
        # if no bowpath is supplied, the bow-file is expected 
        # to be in the supplied feature directory
        bowpath = "{0}/{1}".format(featdir, BOW_FILE_NAME)
        job_desc.append("--bow")
        job_desc.append(bowpath)
        
        if binary:
            job_desc.append("--binary")

        job_desc.append(currentFile)
        
        # add this job description to the queue_images_to_process
        queue_images_to_process.put(job_desc)
    
    # execute the queue_images_to_process
    processExtractFeatures(queue_images_to_process, num_threads, update)


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
    return path.replace("\\", "/").split("/")[-1].replace(".feat.xml.gz", "")

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
    
    # create shell command
    cmd = [config['BIN_TRAIN'],
          "--bowsize",    str(bowsize),
          "--precluster", '{0}'.format(clusterCenters),
          "--filter",     filenameFilter,
          "-o",           "{0}/{1}".format(featdir, BOW_FILE_NAME),
          featdir]
    
    if verbose:
        cmd.append("-v")
    
    if binary:
        cmd.append("--binary")
    
    try:
        # open pipe to shell and execute command
        process = sub.Popen(cmd, stdout=sub.PIPE, stderr=sub.PIPE)
        
        # store cmd-line output 
        _, errors = process.communicate()
        
        # convert output to utf-8
        errorStr  = errors.decode("utf-8")
    
        # translate response    
        if len(errorStr) == 0:
        
            print "*** Error during train step:"
            print errorStr
            
    except KeyboardInterrupt:
        print "training aborted!"
        
        
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
    f      = gzip.open(featureFile, 'rb')
    xmldoc = et.fromstring(f.read())
    f.close()
    
    # parse xml-data
    data = []    
    for t in xmldoc[0][0][3].text.replace("\n", " ").split(" "):
        if len(t) > 0:
            data.append(float(t))
    
    # free memory
    xmldoc.clear()

    return np.asarray(data)


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
    
    print "...loading features"

    histograms     = []
    distanceMatrix = []
    
    # load bow histograms
    for featureFilename in glob.glob(os.path.join(featureDirectory, "*.BOWHistogram.feat.xml.gz")):
        
        # load histogram
        data = loadBOWHistogram(featureFilename)
        
        # store data to list with filename
        histograms.append([featureFilename, data])
        
    print "...calculating distance matrix"
    
    for query_featureFilename, query_data in histograms:
        
        neighbors = []
        
        for neigh_featureFilename, neigh_data in histograms:
            
            if (neigh_featureFilename == query_featureFilename):
                continue
            
            # calculate histogram intersections
            dist = np.sum(np.minimum(query_data, neigh_data))
            neighbors.append([dist, neigh_featureFilename, neigh_data])
        
        
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
        distanceMatrix.append([query_featureFilename, shortlist])

    return distanceMatrix

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


def pyFindDuplicates_SpatialVerification_fast(config, featureDirectory, threads=1, binary=False, verbose=False):
    
    results                    = []
    pool_ExtractFeaturesWorker = []
    jobs                       = Queue.Queue()

    # load distance matrix
    list_of_shortlists = getShortLists(featureDirectory, 2)
    
    # ===================================
    # ---            MAP              ---
    # ===================================
    
    # create list of jobs   
    col_entry_id = 0
    
    # map shortlist structure to job sequence that
    # can be reproduced in the reduce step
    for query_filepath, shortlist in list_of_shortlists:
        
        query_filename = getFilename(query_filepath)
        
        for nearest_neighbor_filepath, _ in shortlist:
            
            jobs.put_nowait([(query_filename, getFilename(nearest_neighbor_filepath)), 
                             (query_filepath, nearest_neighbor_filepath), 
                             col_entry_id])
            
        col_entry_id += 1
                
    initialListSize = jobs.qsize()
    
    # start worker threads
    for _ in range(threads):
        
        # create thread and provide config and queues
        worker = ThreadedVerification(config, jobs, results, initialListSize, binary, verbose)
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
            
            for t in pool_ExtractFeaturesWorker:
                t.kill_received = True
                
            exit()
    
    
    dists              = []
    shortlist_results  = {}

    # merge result list back into shortlist representation
    for col_entry_id, ssim, query_f_name, neigh_f_name in results:
        
        if query_f_name not in shortlist_results.keys():
            shortlist_results[query_f_name] = []
        
        ssim = float(ssim)
        dists.append(ssim)
        
        shortlist_results[query_f_name].append([ssim, neigh_f_name])
        
    
    # calculate adaptive threshold for decission making
    threshold = np.percentile(dists,97.5) 

    
    duplicates  = []
    
    for query_entry in sorted(shortlist_results.keys()):
        
        
        for ssim, neigh_filename in shortlist_results[query_entry]:
            
            if ssim > threshold:
                
                if (query_entry,neigh_filename) not in duplicates and (neigh_filename,query_entry) not in duplicates:
                    duplicates.append((query_entry,neigh_filename))


    # assemble result list
    result = {}

    for query_f_name, duplicate in sorted(duplicates):
        
        query_duplicate_key    = None
        neighbor_duplicate_key = None

        for k in result.keys():
            
            if query_f_name in result[k]:
                query_duplicate_key = k
        
            if duplicate in result[k] or duplicate == k:
                neighbor_duplicate_key = k
        
            
        if query_duplicate_key != None:
            result[query_duplicate_key].add(duplicate)
         
        elif neighbor_duplicate_key != None:
            result[neighbor_duplicate_key].add(query_f_name)
                
        else:
            
            if query_f_name not in result.keys():
                result[query_f_name] = set()
                
            result[query_f_name].add(duplicate)
            
        
    
    print "\n=== Summary ==============================================\n"
    
    print "{0} sets of duplicates detected:\n".format(len(result))

    for k in sorted(result.keys()):
        print k, "=>", list(result[k])
 
    print "\n==========================================================\n"

    