
           -------------------------------------------------
           
                              MATCHBOX
                              
             Digital Preservation Quality Assurance Tools
         
               Austrian Institute of Technology - 2013
           
          Alexander Schindler  <alexander.schindler@ait.ac.at>
          Reinhold Huber-Mörk <Reinhold.Huber-Moerk@ait.ac.at>
           
           -------------------------------------------------


------------------
1. About Matchbox
------------------

	Matchbox is an open source tool providing decision-making support for
	quality assurance tasks concerning document image  collections  (e.g.
	duplicate detection in or across document image collections). It pro-
	vides functions to identify duplicated content, even where files  are
	different, eg in format,  size, rotated,  cropped, colour-enhanced. A
	main advantage is its general approach that works where OCR will not,
	for example images of handwriting or music scores.  Further  Matchbox
	is useful in assembling collections from multiple sources, and ident-
	ifying missing files. 


----------------
2. Installation
----------------

	Please refere to the INSTALL file in the source directory.

	
---------------
3. Basic Usage
---------------

3.1. Matchbox Tools
--------------------

3.1.1. extractfeatures

	This tool extracts features from images. The set of features impleme-
	nted  contains  the basic image metadata extraction, the basic  image
	processing features Color Histograms and Image Profiles, as well as
	more complex features based on interest point detection.
	
	Features can be either extracted all at once or by distinctivly spec-
	ifiyng the required feature. Extracted features are either stored  to
	the same directory of the corresponding image, or to a specified dir-
	ectory.  Features  can  be  stored in gzipped xml format or in binary 
	format. Binary storage  enables  faster processing while xml provides
	more flexibility for data processing with third party tools.
	
	The stored feature filenames have the format:
	
		<original_image_filename>.<featurename>.<dat|xml.gz>
		e.g. img00001.tif.ImageHistogram.xml.gz
	
3.1.2 compare

	The  compare  tool  compares two extracted features and  calculates a
	similarity estimation.
	
	Input files  have  to  be of the same feature set. Comparison between
	different feature  sets  is  not possible. Also only two files can be 
	compared with each other, not a set of files.
	
	The resulting similarity estimation is written in xml format to stan-
	dard output (e.g. the command line interface).
	
3.1.3 train

	The train  tool  is  a specialized tool to create visual vocabularies
	based on visual bag-of-words. A  visual  bag-of-words is a pendant to
	the bag-of-words in classical  information retrieval, where each text
	document is represented as a histogram of its distinctive word occur-
	nces. This approach  has  been  adopted  in image processing based on 
	features from interest point detectors - especially SIFT features.
	
	The  train tool takes a list of SIFT descriptors and applies a clust-
	ering  algorithm  onto  it.  The  calculated  centroids represent the 
	visual vocabulary that will be used in further processing  of certain
	workflows.
	

3.2. Basic Workflows
---------------------

3.2.1. Duplicate Detection

	Duplicate detection is the task of detecting duplicates within an im-
	age collection.
	
	1. extract SIFTComaparison features of all images
	2. train a visual vocabulary on the extracted features
	3. extract BoWHistograms using the vocabulary and all extracted 
	   SIFTCompairison features
	4. create a similarity matrix for the collection using compare on
	   all BoWHistogram features
	5. take the top-most similar images for each image and compare
	   their SIFTComparison features.
	6. Set a threshold based on the retrieved data.
	7. images with an SSIM exceeding the threshold are considered to be
	   duplicates.
