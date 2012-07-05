#include "BOWHistogram.h"

const string BOWHistogram::TASK_NAME = "BOWHistogram";

TCLAP::ValueArg<string> argBOW("","bow","[BOWHistogram] Bag of Words file",false,"","filename");
TCLAP::ValueArg<string> argBOWMetric("","bowmetric","[BOWHistogram] Metric for histogram comparison (CV_COMP_CHISQR,CV_COMP_CORREL,CV_COMP_INTERSECT,CV_COMP_BHATTACHARYYA)",false,"CV_COMP_INTERSECT","str");

BOWHistogram::BOWHistogram(SIFTComparison* siftcomp):Level4Feature()
{
	name = TASK_NAME;
	addCharacterizationCommandlineArgument(&argBOW);
	addComparisonCommandlineArgument(&argBOWMetric);

	sift = siftcomp;
}

BOWHistogram::BOWHistogram(void):Level4Feature()
{
	name = TASK_NAME;
	addCharacterizationCommandlineArgument(&argBOW);
	addComparisonCommandlineArgument(&argBOWMetric);
}

BOWHistogram::~BOWHistogram(void)
{
}

void BOWHistogram::execute(Mat &img)
{
	try {

		if (vocabularyFilename.length() == 0)
		{
			throw runtime_error("No vocabulary file provided. Please supply a BoW file by using the --bow option!");
		}

		// loading data
		verbosePrintln(string("load SIFT descriptors"));
		sift->loadData();

		Mat descriptors = sift->getDescriptors();

		// check if there were any SIFT features extracted
		if (descriptors.rows == 0)
		{
			// no keypoints/descriptors have been found for this image
			// thus, no BOWHistogram can be calculated for it.
			verbosePrintln(string("No descriptors found for this image!"));
			return;
		}

		verbosePrintln(string("load vocabulary"));
		vocabulary = loadVocabulary(vocabularyFilename);

		if (vocabulary.rows == 0)
		{
			stringstream msg;
			msg << "Invalid BOW vocabulary!";
			throw runtime_error(msg.str());
		}

		// ==================================================================
		// this is a re-implementation of the original OpenCV implementation
		// for BoW-Histogram matching. The original code computed  keypoints
		// and descriptors even if they were supplied through parameters
		// ==================================================================

		verbosePrintln(string("computing response histogram"));	

		// calculate descriptors
		Ptr<DescriptorMatcher> dmatcher(new FlannBasedMatcher());

		int clusterCount = vocabulary.rows;
		

		// Match keypoint descriptors to cluster center (to vocabulary)
		vector<DMatch> matches;
		dmatcher->clear();
		dmatcher->add( vector<Mat>(1, vocabulary) );
		dmatcher->match( descriptors, matches );

		// Compute image descriptor
		response_hist = Mat( 1, clusterCount, CV_32FC1, Scalar::all(0.0) );
		float *dptr = (float*)response_hist.data;
		for( size_t i = 0; i < matches.size(); i++ )
		{
			int queryIdx = matches[i].queryIdx;
			int trainIdx = matches[i].trainIdx; // cluster index
			CV_Assert( queryIdx == (int)i );
			dptr[trainIdx] = dptr[trainIdx] + 1.f;
		}

		// Normalize image descriptor.
		response_hist /= descriptors.rows;

	}
	catch (Exception &ex)
	{
		stringstream msg;
		msg << "Error while extracting BOWHistogram features: " << ex.msg;
		throw runtime_error(msg.str());
	}
}

Mat BOWHistogram::loadVocabulary(string filename)
{
	verbosePrintln(string("loading vocabulary from file"));

	Mat voc;

	FileStorage fs(filename.c_str() , FileStorage::READ);
	read(fs["vocabulary"], voc);
	fs.release();

	return voc;
}

void BOWHistogram::compare(Feature *task)
{
	verbosePrintln(string("comparing"));

	Mat myResponseHist = response_hist;
	Mat ohterResponseHist = ((BOWHistogram*)task)->getResponseHistogram();

	// checks
	if (myResponseHist.cols != ohterResponseHist.cols)
	{
		ErrorOutputParameter *errmsg = new ErrorOutputParameter();
		errmsg->setErrorMessage("compared response histograms have different bin sizes!");
		addOutputParameter(*errmsg);

		verbosePrintln(string("ERROR: compared histograms have different bin sizes!"));

		return;
	}

	int method = CV_COMP_INTERSECT;
				
	if(metric.compare("CV_COMP_CORREL") == 0)
	{
		method = CV_COMP_CORREL;
	}
	else if(metric.compare("CV_COMP_CHISQR") == 0)
	{
		method = CV_COMP_CHISQR;
	}
	else if(metric.compare("CV_COMP_BHATTACHARYYA") == 0)
	{
		method = CV_COMP_BHATTACHARYYA;
	}

	double dist = compareHist(myResponseHist, ohterResponseHist, method);


	DoubleOutputParameter *param1 = new DoubleOutputParameter("result");
	param1->setData(dist);
	addOutputParameter(*param1);
}

void BOWHistogram::parseCommandlineArguments()
{
	vocabularyFilename = argBOW.getValue();
	metric             = argBOWMetric.getValue();
}

list<string>* BOWHistogram::getCmdlineArguments()
{
	list<string>* result = new list<string>;
	result->push_back(metric);

	return result;
}

void BOWHistogram::setCmdlineArguments(list<string> *args)
{
	metric  = args->front();
}

void BOWHistogram::writeOutput(cv::FileStorage &fs)
{
	if (response_hist.rows > 0)
	{
		verbosePrintln(string("writing data to filestorage"));
		fs << TASK_NAME << "{";
		write(fs, "response_hist", response_hist);
		fs << "}";
	}
	else
	{
		verbosePrintln(string("no histogram data - skipping output writing"));
	}
}

void BOWHistogram::readData(cv::FileNode &fs)
{
	verbosePrintln(string("reading data"));
	read(fs["response_hist"], response_hist);
}

Mat& BOWHistogram::getResponseHistogram()
{
	return response_hist;
}


