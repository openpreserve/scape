#include "ImageHistogram.h"


const string ImageHistogram::TASK_NAME = "ImageHistogram";
const string ImageHistogram::TAG_BINS   = "bins";

TCLAP::ValueArg<int> argBinSize("","numbins","[ImageHistogram] Number of histogram bins",false,256,"int");
TCLAP::ValueArg<string> argMetric("","metric","[ImageHistogram] Metric for histogram comparison (CV_COMP_CHISQR,CV_COMP_CORREL,CV_COMP_INTERSECT,CV_COMP_BHATTACHARYYA)",false,"CV_COMP_CHISQR","str");


ImageHistogram::ImageHistogram(void):Level2Feature()
{
	name = TASK_NAME;

	// add commandline arguments
	addCharacterizationCommandlineArgument(&argBinSize);
	addComparisonCommandlineArgument(&argMetric);
}

ImageHistogram::~ImageHistogram(void)
{
}

void ImageHistogram::execute(Mat& image)
{
	// checks
	if (binSize < 1)
	{
		throw runtime_error("number of histogram bins must be greater than 0!");
	}

	// declarations
    int   histSize[]      = {binSize, binSize, binSize};
    float range[]         = {0, 255};
	const float* ranges[] = { range, range, range };		
	int   channels[]      = {0, 1, 2};

	string* sbin   = new string("bins");
	string* sdim   = new string("dimension");
	string* snum   = new string("numberofbins");
	string* srange = new string("range");

    Mat hist;

	CvSize size;
	size.height = image.rows;
	size.width  = image.cols;

	IplImage* rPlane = cvCreateImage( size, 8, 1);
	IplImage* gPlane = cvCreateImage( size, 8, 1);
	IplImage* bPlane = cvCreateImage( size, 8, 1);

	Mat planes[] = {rPlane, gPlane, bPlane};

	IplImage iplimage = image;
	cvSplit( &iplimage, rPlane, gPlane, bPlane, NULL );

	entropy  = 0;
	variance = 0;

	for (int i = 0; i < 3; i++)
	{
		string ichar = StringConverter::toString((i+1));

		// create histogram for channel i
		calcHist( &planes[i], 1, channels, Mat(), hist, 1, histSize, ranges, true, false );

		normalizeHist(hist, (planes[i].rows * planes[i].cols));

		bins.push_back(hist);

		//// calculate entropy
		//double sumIntermediate = 0;

		//for( int e = 0; e < binSize; e++ )
		//{
		//	double p_ai = hist.at<double>(e);
		//	
		//	if (p_ai > 0)
		//	{
		//		sumIntermediate += (p_ai * (log10(p_ai)/log10((double)2)));
		//	}
		//}

		//sumIntermediate = -(sumIntermediate);

		//if (sumIntermediate > entropy)
		//{
		//	entropy = sumIntermediate;
		//}

		//Mat meanVal;
		//Mat stdevVal;

		//meanStdDev(hist,meanVal,stdevVal);
		//variance += stdevVal.at<double>(0) * stdevVal.at<double>(0);
	}

	//variance = variance / 3;
}

void ImageHistogram::compare(Feature *task)
{
	verbosePrintln(string("comparing"));

	vector<Mat> otherBins = ((ImageHistogram*)task)->getBins();

	double dist = 0;

	int channels = bins.size();
	for (int i = 0; i < channels; i++)
	{
		Mat m1 = bins[i];
		Mat m2 = otherBins[i];

		// checks
		if (m1.cols != m2.cols)
		{
			ErrorOutputParameter *errmsg = new ErrorOutputParameter();
			errmsg->setErrorMessage("compared histograms have different bin sizes!");
			addOutputParameter(*errmsg);

			verbosePrintln(string("ERROR: compared histograms have different bin sizes!"));

			return;
		}

		int method = CV_COMP_INTERSECT;
					
		if(metric.compare("CV_COMP_CORREL") == 0)
		{
			method = CV_COMP_CORREL;
		}
		else if(metric.compare("CV_COMP_INTERSECT") == 0)
		{
			method = CV_COMP_INTERSECT;
		}
		else if(metric.compare("CV_COMP_BHATTACHARYYA") == 0)
		{
			method = CV_COMP_BHATTACHARYYA;
		}

		dist += compareHist(m1, m2, method);

		/*bins.pop_back();
		otherBins.pop_back();*/
	}

	DoubleOutputParameter *param1 = new DoubleOutputParameter("result");
	param1->setData(dist / channels);
	addOutputParameter(*param1);
}

void ImageHistogram::writeOutput(FileStorage& fs)
{
	fs << "ImageHistogram" << "{";

	for (int i = 0; i < bins.size(); i++)
	{
		Mat m1 = bins[i];

		stringstream ssStream;
		ssStream << "bin" << i;

		fs << ssStream.str().c_str() << m1;
	}

	//fs << "entropy"  << entropy;
	//fs << "variance" << variance;

	fs << "}";
}

void ImageHistogram::readData(FileNode& node)
{
	verbosePrintln(string("reading data"));

	for( FileNodeIterator it = node.begin() ; it != node.end(); ++it )
	{	
		Mat bin;
		(*it) >> bin;
		bins.push_back(bin);
	}
}

void ImageHistogram::parseCommandlineArguments()
{
	binSize = argBinSize.getValue();
	metric  = argMetric.getValue();
}

void ImageHistogram::normalizeHist(Mat hist, int size)
{
	for( int i = 0; i < hist.rows; i++ )
    {
		hist.at<float>(i) = hist.at<float>(i) / size;
    }
}

vector<Mat> ImageHistogram::getBins(void)
{
	return bins;
}

list<string>* ImageHistogram::getCmdlineArguments(void)
{
	list<string>* result = new list<string>;
	result->push_back(StringConverter::toString(binSize));
	result->push_back(metric);

	return result;
}

void ImageHistogram::setCmdlineArguments(list<string>* args)
{
	binSize = StringConverter::toInt(&args->front());
	metric  = args->back();
}