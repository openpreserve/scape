#include "ImageProfile.h"

const string ImageProfile::TASK_NAME = "ImageProfile";


ImageProfile::ImageProfile(void):Level2Feature()
{
	name = TASK_NAME;
}

ImageProfile::~ImageProfile(void)
{
}

void ImageProfile::parseCommandlineArguments()
{
}

void ImageProfile::execute(Mat& image)
{
	// for each channel
	CvSize size;
	size.height = image.rows;
	size.width  = image.cols;

	IplImage* rPlane = cvCreateImage( size, 8, 1);
	IplImage* gPlane = cvCreateImage( size, 8, 1);
	IplImage* bPlane = cvCreateImage( size, 8, 1);

	Mat planes[] = {rPlane, gPlane, bPlane};

	IplImage iplimage = image;
	cvSplit( &iplimage, rPlane, gPlane, bPlane, NULL );
	
	for (int i = 0; i < 3; i++)
	{
		Scalar sum = 0;
		vector<double> vertProfileList;

		// create profile for each col
		for(int c = 0; c < planes[i].cols; c++)
		{
			Mat col = planes[i].col(c);

			Scalar s = mean(col);

			sum.val[0] += s.val[0];

			vertProfileList.push_back(s.val[0]);			
		}

		// normalize profile
		vector<double>::iterator pelem;
		for( pelem = vertProfileList.begin(); pelem != vertProfileList.end(); ++pelem) *pelem /= sum.val[0]; 

		Mat vertProfile(vertProfileList);
		vertProfile.convertTo(vertProfile, CV_32F);

		
		profiles.push_back(vertProfile);

		sum.val[0] = 0;

		vector<double> horProfileList;

		// create profile for each row
		for(int r = 0; r < planes[i].rows; r++)
		{
			Mat row = planes[i].row(r);

			Scalar s = mean(row);
			
			sum.val[0] += s.val[0];

			horProfileList.push_back(s.val[0]);
		}

		// normalize profile		
		for( pelem = horProfileList.begin(); pelem != horProfileList.end(); ++pelem) *pelem /= sum.val[0]; 

		Mat horProfile(horProfileList);
		horProfile.convertTo(horProfile, CV_32F);
		profiles.push_back(horProfile);
	}
	
}

void ImageProfile::writeOutput(FileStorage& fs)
{
	fs << TASK_NAME << "{";
	
	for (int i = 0; i < profiles.size(); i++)
	{
		Mat m1 = profiles[i];

		stringstream ssStream;
		ssStream << "profile" << i;

		fs << ssStream.str().c_str() << m1;
	}

	fs << "}";
}

void ImageProfile::readData(FileNode& node)
{
	verbosePrintln("reading data");

	for( FileNodeIterator it = node.begin() ; it != node.end(); ++it )
	{	
		Mat profile;
		(*it) >> profile;

		if ((profile.rows == 0) || (profile.cols == 0))
		{
			throw runtime_error("Error while reading profile data. Empty profile matrix!");
		}

		profiles.push_back(profile);
	}
}

void ImageProfile::compare(Feature *task)
{
	verbosePrintln("comparing");

	vector<Mat> otherProfiles = ((ImageProfile*)task)->getProfiles();

	double dist = 0;

	int size = otherProfiles.size();
	for (int i = 0; i < size; i++)
	{
		Mat m1 = profiles[i];
		Mat m2 = otherProfiles[i];

		// checks
		if ((m1.rows * m1.cols) > (m2.rows * m2.cols))
		{
			Size s(m2.cols,m2.rows);
			resize(m1,m1,s,0,0,INTER_LINEAR);
		}
		else if ((m1.rows * m1.cols) < (m2.rows * m2.cols))
		{
			Size s(m1.cols,m1.rows);
			resize(m2,m2,s,0,0,INTER_LINEAR);
		}

		int method = CV_COMP_CHISQR;
					
		/*if(metric.compare("CV_COMP_CORREL") == 0)
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
		}*/

		dist += compareHist(m1, m2, method);

		/*profiles.pop_front();
		otherProfiles.pop_front();*/
	}

	DoubleOutputParameter *param1 = new DoubleOutputParameter("result");
	param1->setData(dist / size);
	addOutputParameter(*param1);
}

list<string>* ImageProfile::getCmdlineArguments(void)
{
	return NULL;
}

void ImageProfile::setCmdlineArguments(list<string>* args)
{
}

vector<Mat> ImageProfile::getProfiles(void)
{
	return profiles;
}