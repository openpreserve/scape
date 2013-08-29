#include "SIFTComparison.h"
#include "BoostSerializer.h"

const string SIFTComparison::TASK_NAME = "SIFTComparison";

TCLAP::ValueArg<int> argSDK          ("","sdk"  ,         "[SIFTComparison] Number of Spatial Distinctive Keypoints (0 = no SDK)",                   false,0 ,"int");
TCLAP::ValueArg<int> argCLAHE        ("","clahe",         "[SIFTComparison] Value of adaptive contrast enhancement (1 = no enhancement)",            false,1 ,"int");
TCLAP::ValueArg<int> argDOWNSAMPLE   ("","downsample",    "[SIFTComparison] Sample the image down to this resolution",                               false,1000000 ,"int");
TCLAP::SwitchArg     argBinaryOutput ("","binary",        "[SIFTComparison] Store extracted features also as binary archives",                       false);
TCLAP::SwitchArg     argBinaryInput  ("","binary",        "[SIFTComparison] Load extracted features from binary archives",                           false);
TCLAP::SwitchArg     argBinaryOnly   ("","binaryonly",    "[SIFTComparison] Store extracted features only as binary archives",                       false);


SIFTComparison::SIFTComparison(void):Level3Feature()
{
	name          = TASK_NAME;
	scale         = 1;

	addCharacterizationCommandlineArgument(&argSDK);
	addCharacterizationCommandlineArgument(&argCLAHE);
	addCharacterizationCommandlineArgument(&argBinaryOutput);
	addCharacterizationCommandlineArgument(&argBinaryOnly);
	addCharacterizationCommandlineArgument(&argDOWNSAMPLE);

	addComparisonCommandlineArgument(&argSDK);
	addComparisonCommandlineArgument(&argBinaryInput);
}

SIFTComparison::~SIFTComparison(void)
{
	descriptors.release();
	keypoints.clear();
}

vector<int> SIFTComparison::findSpatiallyDistinctiveLocalKeypoints(Mat& image, vector<KeyPoint>& keypoints)
{
	vector<int>      results_index;

	int vert_lines        = int(sqrt(double((sdk*image.cols)/image.rows)));
	int hor_lines         = sdk / vert_lines;

	int hor_raster_width  = image.rows / hor_lines;
	int vert_raster_width = image.cols / vert_lines;

	int x                 = 0;
	int y                 = 0;

	/// Detector parameters
	Mat    dst, img_gray, dst_norm, dst_norm_scaled;
	int    blockSize      = 2;
	int    apertureSize   = 3;
	double kc             = 0.04;

	cvtColor( image, img_gray, CV_BGR2GRAY );
	cornerHarris( img_gray, dst, blockSize, apertureSize, kc, BORDER_DEFAULT );
	normalize( dst, dst_norm, 0, 255, NORM_MINMAX, CV_32FC1, Mat() );
	convertScaleAbs( dst_norm, dst_norm_scaled );
	
	while((x < image.cols) || (y < image.rows))
	{
		int x_min      = x;
		int x_max      = x_min + vert_raster_width;

		int y_min      = y;
		int y_max      = y_min + hor_raster_width;

		int curr_x_min = 0;
		int curr_x_max = curr_x_min + vert_raster_width;

		x = x_max;
		y = y_max;

		while(curr_x_max < image.cols)
		{
			KeyPoint kp_max;
			int      kp_max_index = -1;
			float    val          = 0;
			bool     kpSet        = false;
			int      idx          = 0;

			for (vector<KeyPoint>::iterator it = keypoints.begin(); it!=keypoints.end(); ++it) {

				KeyPoint kp = *it;

				if (((kp.pt.x >= curr_x_min) && (kp.pt.x <= curr_x_max)) && ((kp.pt.y >= y_min) && (kp.pt.y <= y_max)))
				{	
					float* p = dst_norm_scaled.ptr<float>(kp.pt.y);

					if (val < p[int(kp.pt.x)])
					{
						val          = p[int(kp.pt.x)];
						kp_max       = kp;
						kp_max_index = idx;
						kpSet        = true;
					}
				}

				idx++;
			}

			if (kpSet)
			{
				results_index.push_back(kp_max_index);
			}

			curr_x_min = curr_x_max;
			curr_x_max = curr_x_min + vert_raster_width;
		}
	}

	return results_index;
}


void SIFTComparison::execute(Mat& img)
{
	Feature::verbosePrintln(string("execute"));

	Feature::verbosePrintln(string("...extracting features"));

	Mat& image = img;

	// too big images may fail to be processed due to resource limitations (e.g. internal memory)
	if ((img.rows * img.cols) > maxResolution)
	{
		// downsample image to approx. MAX_IMAGE_RESOLUTION
		VerboseOutput::println("SIFTComparison", "...orig. image resolution of %dx%d bigger than max. resolution of %d pixel", img.cols, img.rows, maxResolution);
		Feature::verbosePrintln(string("...downsampling image"));
		image = downsample(img);
		VerboseOutput::println("SIFTComparison", "...new image resolution: %dx%d", image.cols, image.rows);
		
	}
	
	if (clahe != 1)
	{
		Feature::verbosePrintln(string("...CLAHE"));

		Feature::verbosePrintln(string("...Convert image to grayscale"));
		cvtColor( image, image, CV_RGB2GRAY );

		IplImage ipl_src  = image;
		Mat      dest     = image.clone();
		IplImage ipl_dest = dest;

		cvCLAdaptEqualize(&ipl_src, &ipl_dest,16,16,256,clahe, CV_CLAHE_RANGE_FULL);

		Mat imgMat(&ipl_dest);
		image = imgMat;

		Feature::verbosePrintln(string("...Convert image back to RGB"));
		cvtColor( image, image, CV_GRAY2BGR );
	}

	
	try
	{
		// extract features
		Feature::verbosePrintln(string("...detecting keypoints"));
		SIFT* featureDetector = new SIFT();
		
		featureDetector->detect( image, keypoints );

		VerboseOutput::println("SIFTComparison", "...num keypoints: %d", keypoints.size());

		if (sdk != 0)
		{
			 vector<int> indeces = findSpatiallyDistinctiveLocalKeypoints(image, keypoints);
			 keypoints           = filterKeypoints(keypoints,indeces);
		}

		// calculate descriptors
		Feature::verbosePrintln(string("...computing descriptors"));
		DescriptorExtractor* descriptorExtractor = new SiftDescriptorExtractor();
		descriptorExtractor->compute( image, keypoints, descriptors );
	
		//normalizeDescriptors(descriptors);
		
		Feature::verbosePrintln(string("...Feature extraction done!"));

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
		RobustMatcher  matcher;
		vector<DMatch> matches;

		stringstream ss1;
		ss1 << "keypointsQuery: "     << keypointsQuery.size() << ", keypointsTrain: "   << keypointsTrain.size();
		ss1 << ", descriptorsQuery: " << descriptorsQuery.rows << ", descriptorsTrain: " << descriptorsTrain.rows;
		verbosePrintln(ss1.str());

		// load images

		// load image 1
		verbosePrintln(string("load images"));
		Mat matImageTrainOrig = imread(const_cast<char*>(filename.c_str()), CV_LOAD_IMAGE_COLOR);
		
		// check if image has been loaded correctly
		stringstream ss2;
		ss2 << "Image1 Resolution: " << matImageTrainOrig.cols << "x" << matImageTrainOrig.rows;
		verbosePrintln(ss2.str());

		if ((matImageTrainOrig.cols * matImageTrainOrig.rows) == 0)
		{
			stringstream msg;
			msg << "Image '" << filename << "' is corrupt or image path is wrong";
			throw runtime_error(msg.str());
		}
		
		// load image 2
		Mat matImageQueryOrig = imread(const_cast<char*>(task->getFilename().c_str()), CV_LOAD_IMAGE_COLOR );

		// check if image has been loaded correctly
		stringstream ss3;
		ss3 << "Image2 Resolution: " << matImageQueryOrig.cols << "x" << matImageQueryOrig.rows;
		verbosePrintln(ss3.str());
		
		if ((matImageQueryOrig.cols * matImageQueryOrig.rows) == 0)
		{
			stringstream msg;
			msg << "Image '" << task->getFilename() << "' is corrupt or image path is wrong";
			throw runtime_error(msg.str());
		}


		if (sdk != 0)
		{
			verbosePrintln(string("Searching for Spatially Distinctive Local Keypoints"));
			vector<int> indecesOrig  = findSpatiallyDistinctiveLocalKeypoints(matImageQueryOrig, keypointsQuery);
			keypointsQuery           = filterKeypoints   (keypointsQuery,   indecesOrig);
			descriptorsQuery         = filterDescriptors (descriptorsQuery, indecesOrig);

			vector<int> indecesTrain = findSpatiallyDistinctiveLocalKeypoints(matImageTrainOrig, keypointsTrain);
			keypointsTrain           = filterKeypoints   (keypointsTrain,   indecesTrain);
			descriptorsTrain         = filterDescriptors (descriptorsTrain, indecesTrain);

			stringstream ss;
			ss << "keypointsQuery: "     << keypointsQuery.size() << ", keypointsTrain: "   << keypointsTrain.size();
			ss << ", descriptorsQuery: " << descriptorsQuery.rows << ", descriptorsTrain: " << descriptorsTrain.rows;
			verbosePrintln(ss.str());
		}

		try
		{
			verbosePrintln("matching keypoints");
			
			matcher.match(keypointsQuery, keypointsTrain, descriptorsQuery, descriptorsTrain, matches );

			verbosePrintln("number of matches = " + StringConverter::toString((int)matches.size()));

			Mat affineTransform = calcAffineTransform(matches, keypointsTrain, keypointsQuery, ((SIFTComparison*)task)->getScale());

			verbosePrintln(string("warp image"));

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
		catch(runtime_error e)
		{
			DoubleOutputParameter *ssimParam = new DoubleOutputParameter("ssim");
			ssimParam->setData(-1);
			addOutputParameter(*ssimParam);

			DoubleOutputParameter *ssimMaskedParam = new DoubleOutputParameter("ssimMasked");
			ssimMaskedParam->setData(-1);
			addOutputParameter(*ssimMaskedParam);

			ErrorOutputParameter* error = new ErrorOutputParameter();
			error->setErrorMessage(e.what());
			addOutputParameter(*error);

			verboseError(e.what());
		}

	}
	catch (runtime_error e)
	{
		ErrorOutputParameter* error = new ErrorOutputParameter();
		error->setErrorMessage(e.what());
		addOutputParameter(*error);

		verboseError(e.what());
	}
	catch (exception e)
	{
		ErrorOutputParameter* error = new ErrorOutputParameter();
		error->setErrorMessage(e.what());
		addOutputParameter(*error);

		verboseError(e.what());
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
		scale = 1/sqrt((double)((matImg.rows * matImg.cols) / maxResolution));
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
		
		if (binaryOutput || binaryOnly)
		{
			verbosePrintln(string("writing data to binary archive"));
			ofstream ofs1(getFilepath(".descriptors.dat").c_str(), std::fstream::out | std::fstream::binary);
			boost::archive::binary_oarchive oa_desc(ofs1);
			oa_desc << descriptors;
			ofs1.close();
					
			ofstream ofs2(getFilepath(".keypoints.dat").c_str(), std::fstream::out | std::fstream::binary);
			boost::archive::binary_oarchive oa_keyp(ofs2);
			oa_keyp << keypoints;
			ofs2.close();
		}

		fs << TASK_NAME << "{";

		if (!binaryOnly)
		{	
			verbosePrintln(string("writing data to OpenCV FileStorage in xml.gz format"));
			write(fs, "keypoints", keypoints);
			write(fs, "descriptors", descriptors);
		}

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

	if (binaryInput)
	{
		verbosePrintln(string("reading data from binary archive"));

		try
		{	
			stringstream filePathDescriptors;
			filePathDescriptors << filename << ".SIFTComparison.descriptors.dat";

			VerboseOutput::println("SIFTComparison", "reading descriptors from file: %s", filePathDescriptors.str().c_str());
			std::ifstream ifsDesc(filePathDescriptors.str().c_str(), std::fstream::binary | std::fstream::in);
			boost::archive::binary_iarchive iaDesc(ifsDesc);
			iaDesc >> descriptors;
			ifsDesc.close();

			stringstream filePathKeypoints;
			filePathKeypoints << filename << ".SIFTComparison.keypoints.dat";

			VerboseOutput::println("SIFTComparison", "reading descriptors from file: %s", filePathKeypoints.str().c_str());
			std::ifstream ifsKeyp(filePathKeypoints.str().c_str(), std::fstream::binary | std::fstream::in);
			boost::archive::binary_iarchive iaKeyp(ifsKeyp);
			iaKeyp >> keypoints;
			ifsKeyp.close();
		}
		catch (exception& e)
		{
			stringstream ss;
			ss << "*** WARNING: failed to load descriptors from binary archive: " << e.what();
			VerboseOutput::println(string("train"), ss.str() );
		}
	}
	else
	{
		verbosePrintln(string("reading data from OpenCV FileStorage in xml.gz format"));

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
	}
}

list<string>* SIFTComparison::getCmdlineArguments()
{
	list<string>* result = new list<string>;
	result->push_back(StringConverter::toString(sdk));
	result->push_back(StringConverter::toString(binaryInput));

	return result;
}

void SIFTComparison::setCmdlineArguments(list<string> *args)
{
	list<string>::iterator item = args->begin();

	sdk         = StringConverter::toInt(&*item);

	item++;

	binaryInput = StringConverter::toInt(&*item);
}

void SIFTComparison::parseCommandlineArguments()
{
	sdk               = argSDK.getValue();
	clahe             = argCLAHE.getValue();
	maxResolution     = argDOWNSAMPLE.getValue();
	binaryOutput      = argBinaryOutput.getValue();
	binaryOnly        = argBinaryOnly.getValue();
	binaryInput       = argBinaryInput.getValue();
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



void SIFTComparison::normalizeDescriptors( Mat& descriptors )
{
	for (int i = 0; i < descriptors.rows; i++)
	{
		Mat row = descriptors.row(i);

		double sqrtOfSum = std::sqrt(sum(row)[0]);

		for (int j = 0; j < 127; j++)
		{
			row.at<float>(0,j) = row.at<float>(0,j) / sqrtOfSum;
		}
	}
}

vector<KeyPoint> SIFTComparison::filterKeypoints( vector<KeyPoint>& origKeypoints, vector<int>& indeces )
{
	vector<KeyPoint> result;

	for (vector<int>::iterator it = indeces.begin(); it!=indeces.end(); ++it)
	{
		result.push_back(keypoints.at((*it)));
	}

	return result;
}

cv::Mat SIFTComparison::filterDescriptors( Mat& origDescriptors, vector<int>& indeces )
{
	Mat result(indeces.size(), origDescriptors.cols, origDescriptors.type());

	int idx = 0;

	for (vector<int>::iterator it = indeces.begin(); it!=indeces.end(); ++it)
	{
		result.row(idx) = origDescriptors.row((*it));
		idx++;
	}

	return result;

}
