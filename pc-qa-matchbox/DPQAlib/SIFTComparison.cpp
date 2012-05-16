#include "SIFTComparison.h"

const string SIFTComparison::TASK_NAME = "SIFTComparison";

TCLAP::ValueArg<int> argSDK  ("","sdk"  , "[SIFTComparison] Number of Spatial Distinctive Keypoints"                   ,false,1000,"int");
TCLAP::ValueArg<int> argCLAHE("","clahe", "[SIFTComparison] Value of adaptive contrast enhancement (1 = no enhancement)",false,1   ,"int");

SIFTComparison::SIFTComparison(void):Level3Feature()
{
	name  = TASK_NAME;
	scale = 1;

	addCharacterizationCommandlineArgument(&argSDK);
	addCharacterizationCommandlineArgument(&argCLAHE);
}

SIFTComparison::~SIFTComparison(void)
{
	descriptors.release();
	keypoints.clear();
}

vector<KeyPoint> SIFTComparison::findSpatiallyDistinctiveLocalKeypoints(Mat& image, vector<KeyPoint>& keypoints)
{

	if (sdk == 0)
	{
		return keypoints;
	}
	
	vector<KeyPoint> results;

	int vert_lines = int(sqrt(double((sdk*image.cols)/image.rows)));
	int hor_lines  = sdk / vert_lines;

	int hor_raster_width  = image.rows / hor_lines;
	int vert_raster_width = image.cols / vert_lines;

	int x = 0;
	int y = 0;

	/// Detector parameters
	Mat    dst, img_gray, dst_norm, dst_norm_scaled;
	int    blockSize    = 2;
	int    apertureSize = 3;
	double kc = 0.04;

	cvtColor( image, img_gray, CV_BGR2GRAY );
	cornerHarris( img_gray, dst, blockSize, apertureSize, kc, BORDER_DEFAULT );
	normalize( dst, dst_norm, 0, 255, NORM_MINMAX, CV_32FC1, Mat() );
	convertScaleAbs( dst_norm, dst_norm_scaled );
	
	while((x < image.cols) || (y < image.rows))
	{
		int x_min = x;
		int x_max = x_min + vert_raster_width;

		int y_min = y;
		int y_max = y_min + hor_raster_width;

		x = x_max;
		y = y_max;

		int curr_x_min = 0;
		int curr_x_max = curr_x_min + vert_raster_width;

		while(curr_x_max < image.cols)
		{
			float    val = 0;
			KeyPoint kp_max;
			bool kpSet = false;

			for (vector<KeyPoint>::iterator it = keypoints.begin(); it!=keypoints.end(); ++it) {

				KeyPoint kp = *it;

				if (((kp.pt.x >= curr_x_min) && (kp.pt.x <= curr_x_max)) && ((kp.pt.y >= y_min) && (kp.pt.y <= y_max)))
				{	
					float* p = dst_norm_scaled.ptr<float>(kp.pt.y);

					if (val < p[int(kp.pt.x)])
					{
						val = p[int(kp.pt.x)];
						kp_max = kp;
						kpSet = true;
					}
				}
			}

			if (kpSet)
			{
				results.push_back(kp_max);
			}

			curr_x_min = curr_x_max;
			curr_x_max = curr_x_min + vert_raster_width;
		}
	}
	
	return results;
}


void SIFTComparison::execute(Mat& img)
{
	Feature::verbosePrintln(string("extracting features"));

	image = img.clone();

	// too big images may fail to be processed due to resource limitations (e.g. internal memory)
	if ((img.rows * img.cols) > MAX_IMAGE_RESOLUTION)
	{
		// downsample image to approx. MAX_IMAGE_RESOLUTION
		Feature::verbosePrintln(string("downsampling image"));
		image = downsample(image);

	} 
	
	if (clahe != 1)
	{
		Feature::verbosePrintln(string("CLAHE"));

		Feature::verbosePrintln(string("Convert image to grayscale"));
		cvtColor( image, image, CV_RGB2GRAY );

		IplImage ipl_src  = image;
		Mat      dest     = image.clone();
		IplImage ipl_dest = dest;

		cvCLAdaptEqualize(&ipl_src, &ipl_dest,16,16,256,clahe, CV_CLAHE_RANGE_FULL);

		//Feature::verbosePrintln(string("CLAHE done 1"));

		Mat imgMat(&ipl_dest);
		image = imgMat;

		Feature::verbosePrintln(string("Convert image back to RGB"));
		cvtColor( image, image, CV_GRAY2BGR );
	}

	
	try
	{
		vector<KeyPoint> kps;

		// extract features
		Ptr<FeatureDetector> featureDetector = new SiftFeatureDetector();
		Feature::verbosePrintln(string("detecting keypoints"));
		featureDetector->detect( image, kps );

		keypoints = findSpatiallyDistinctiveLocalKeypoints(image, kps);

		// calculate descriptors
		Ptr<DescriptorExtractor> descriptorExtractor = DescriptorExtractor::create("SIFT");
		Feature::verbosePrintln(string("computing descriptors"));
		descriptorExtractor->compute( image, keypoints, descriptors );
	}
	catch (Exception& ex)
	{
		stringstream msg;
		msg << "Error while extracting SIFT features: " << ex.msg;
		throw runtime_error(msg.str());
	}
}

void SIFTComparison::compare(Feature *task)
{
	try
	{
		Feature::verbosePrintln(string("comparing"));

		vector<KeyPoint> keypointsTrain = keypoints;
		vector<KeyPoint> keypointsQuery = ((SIFTComparison*)task)->getKeypoints();

		Mat descriptorsTrain = descriptors;
		Mat descriptorsQuery = ((SIFTComparison*)task)->getDescriptors();

		verbosePrintln(string("match descriptors"));

		// find matching keypoints
		RobustMatcher matcher;
		vector<DMatch> matches;

		stringstream ss;
		ss << "keypointsQuery: " << keypointsQuery.size() << ", keypointsTrain: " << keypointsTrain.size();
		ss << ", descriptorsQuery: " << descriptorsQuery.rows << ", descriptorsTrain: " << descriptorsTrain.rows;
		verbosePrintln(ss.str());

		matcher.match(keypointsQuery, keypointsTrain, descriptorsQuery, descriptorsTrain, matches );

		verbosePrintln("number of matches = " + StringConverter::toString((int)matches.size()));

		Mat affineTransform = calcAffineTransform(matches, keypointsTrain, keypointsQuery, ((SIFTComparison*)task)->getScale());

		verbosePrintln(string("load images"));

		// load images
		Mat matImageTrainOrig = imread(const_cast<char*>(filename.c_str()), CV_LOAD_IMAGE_COLOR);
		Mat matImageQueryOrig = imread(const_cast<char*>(task->getFilename().c_str()), CV_LOAD_IMAGE_COLOR );

		verbosePrintln(string("warp"));

		Mat dst(matImageTrainOrig.size(),matImageTrainOrig.type());
		warpAffine(matImageQueryOrig,dst,affineTransform,matImageTrainOrig.size(),INTER_LINEAR,BORDER_CONSTANT,0);

		double ssim = SSIM::calcSSIM(matImageTrainOrig, dst,-1);

		DoubleOutputParameter *ssimParam = new DoubleOutputParameter("ssim");
		ssimParam->setData(ssim);
		addOutputParameter(*ssimParam);

		// create mask
		Mat dstMask(matImageTrainOrig.size(),CV_8U);
		Mat queryMask = Mat::ones(matImageQueryOrig.size(),CV_8U);

		warpAffine(queryMask,dstMask,affineTransform,matImageTrainOrig.size(),INTER_NEAREST,BORDER_CONSTANT,0);

		double ssimMasked = SSIM::calcSSIM(matImageTrainOrig, dst,-1,CV_BGR2YUV, dstMask);

		DoubleOutputParameter *ssimMaskedParam = new DoubleOutputParameter("ssimMasked");
		ssimMaskedParam->setData(ssimMasked);
		addOutputParameter(*ssimMaskedParam);

	}
	catch (exception e)
	{
		ErrorOutputParameter* error = new ErrorOutputParameter();
		error->setErrorMessage(e.what());
		addOutputParameter(*error);
	}
}

Mat SIFTComparison::calcAffineTransform(vector<DMatch>& matches, vector<KeyPoint>& keypointsTrain, vector<KeyPoint>& keypointsQuery, double scale2)
{

	// Build a
	Mat a((matches.size() * 2), 6, CV_64F);
	Mat b((matches.size() * 2), 1, CV_64F);

	// foreach
	int j = 0;

	for(int i = 0; i < matches.size(); ++i)
	{		
		DMatch match = matches[i];

		Mat M1 = (Mat_<double>(1,6) << keypointsTrain[match.trainIdx].pt.x, keypointsTrain[match.trainIdx].pt.y, 0, 0, 1, 0);
		M1.copyTo(a.row(j));

		M1.release();

		b.at<double>(j,0) = keypointsQuery[match.queryIdx].pt.x;

		j++;

		Mat M2 = (Mat_<double>(1,6) << 0, 0, keypointsTrain[match.trainIdx].pt.x, keypointsTrain[match.trainIdx].pt.y, 0, 1);
		M2.copyTo(a.row(j));

		M2.release();

		b.at<double>(j,0) = keypointsQuery[match.queryIdx].pt.y;

		j++;
	}

	// calculate transform
	Mat at = a.t() * a;
	at = at.inv() * a.t() * b;

	// re-arrange matrix
	Mat M(2,3,CV_64F);

	// translation matrix
	M.at<double>(0,0) = at.at<double>(0,0);
	M.at<double>(0,1) = at.at<double>(1,0);
	M.at<double>(1,0) = at.at<double>(2,0);
	M.at<double>(1,1) = at.at<double>(3,0);

	// affine matrix
	M.at<double>(0,2) = at.at<double>(4,0);
	M.at<double>(1,2) = at.at<double>(5,0);

	invertAffineTransform(M, M);

	M.at<double>(0,0) = M.at<double>(0,0) / (scale / scale2);
	M.at<double>(0,1) = M.at<double>(0,1) / (scale / scale2);
	M.at<double>(1,0) = M.at<double>(1,0) / (scale / scale2);
	M.at<double>(1,1) = M.at<double>(1,1) / (scale / scale2);

	M.at<double>(0,2) = M.at<double>(0,2) / scale;
	M.at<double>(1,2) = M.at<double>(1,2) / scale;

	return M;
}

Mat SIFTComparison::downsample(Mat& matImg)
{
	Mat orImg;

	try
	{
		// calculate scaling factor
		scale = 1/sqrt((double)((matImg.rows * matImg.cols) / MAX_IMAGE_RESOLUTION));
		resize(matImg,orImg, Size(), scale, scale,INTER_LINEAR);
	}
	catch(Exception& e)
	{
		stringstream msg;
		msg << "Error while downsampling image: " << e.msg;
		throw runtime_error(msg.str());
	}
		
	return orImg;
}

void SIFTComparison::writeOutput(FileStorage& fs)
{
	try
	{
		verbosePrintln(string("writing data"));

		fs << TASK_NAME << "{";
		
		write(fs, "keypoints", keypoints);
		write(fs, "descriptors", descriptors);
		fs << "scale" << scale;
		fs << "filename" << filename;

		fs << "}";
	}
	catch (Exception& e)
	{
		stringstream msg;
		msg << "Error while writing SIFT data: " << e.msg;
		throw runtime_error(msg.str());
	}
}

void SIFTComparison::readData(FileNode& fs)
{
	verbosePrintln(string("reading data"));

	read(fs["keypoints"], keypoints);
	if (keypoints.size() == 0)
	{
		throw runtime_error("Error while reading SIFTCompairison data: No Keypoints read!");
	}

	read(fs["descriptors"], descriptors);
	if ((descriptors.rows == 0) || (descriptors.cols == 0))
	{
		throw runtime_error("Error while reading SIFTCompairison data: No Descriptors read!");
	}

	fs["scale"] >> scale;
	if ((scale <= 0) || (scale > 1))
	{
		throw runtime_error("Error while reading SIFTCompairison data: Scale out of range!");
	}

	fs["filename"] >> filename;
	if (filename.size() == 0)
	{
		throw runtime_error("Error while reading SIFTCompairison data: Empty filename!");
	}
}

list<string>* SIFTComparison::getCmdlineArguments()
{
	return NULL;
}

void SIFTComparison::setCmdlineArguments(list<string> *args)
{
}

void SIFTComparison::parseCommandlineArguments()
{
	sdk   = argSDK.getValue();
	clahe = argCLAHE.getValue();
}

Mat& SIFTComparison::getImage(void)
{
	return image;
}

Mat SIFTComparison::getDescriptors(void)
{
	return descriptors;
}

vector<KeyPoint> SIFTComparison::getKeypoints(void)
{
	return keypoints;
}

double SIFTComparison::getScale(void)
{
	return scale;
}