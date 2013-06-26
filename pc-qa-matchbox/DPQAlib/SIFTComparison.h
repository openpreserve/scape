#pragma once

#include "stdafx.h"

#include <string>
#include <math.h>

#include "Level3Feature.h"
#include "DoubleOutputParameter.h"

#include "ErrorOutputParameter.h"
#include "opencv2/nonfree/features2d.hpp"
#include "opencv2/nonfree/nonfree.hpp"

#include "opencv/cv.h"

#include "RobustMatcher.h"
#include "SSIM.h"

#include "CLAHE.h"

#include <algorithm>

#define _USE_MATH_DEFINES


using namespace cv;
using namespace std;


class SIFTComparison :
	public Level3Feature
{

private:
	Mat              image;
	Mat              descriptors;
	vector<KeyPoint> keypoints;
	double           scale;

	// commandline arguments
	int              sdk;
	int              clahe;
	int              maxResolution;
	bool             binaryInput;
	bool             binaryOutput;
	bool             binaryOnly;


	vector<DMatch>   calcGoodMatches(vector<DMatch>& matches);
	Mat              calcAffineTransform(vector<DMatch>& matches, vector<KeyPoint>& keypointsTrain, vector<KeyPoint>& keypointsQuery, double scale2);
	Mat              downsample(Mat& matImg);	
	double           calcDispersion(vector<KeyPoint>& keypoints, Mat& image);
	double			 calcUniformity(vector<KeyPoint>& keypoints);
	double           calcSizeVariation(vector<KeyPoint>& keypoints);

	vector<int>      findSpatiallyDistinctiveLocalKeypoints(Mat& image, vector<KeyPoint>& keypoints);
	vector<KeyPoint> filterKeypoints(vector<KeyPoint>& origKeypoints, vector<int>& indeces);
	Mat              filterDescriptors(Mat& origDescriptors, vector<int>& indeces);

protected:
	using Feature::name;

public:
	static const string TASK_NAME;
	
	SIFTComparison(void);
	~SIFTComparison(void);

	// implement virtual methods
	void             execute(Mat& image);
	void             precluster(int centers);

	void             compare(Feature *task);
	void             parseCommandlineArguments();
	list<string>*    getCmdlineArguments(void);
	void             setCmdlineArguments(list<string>* args);
	void             writeOutput(FileStorage& fs);
	void             readData(FileNode& fs);

	Mat&             getImage(void);
	Mat              getDescriptors(void);
	vector<KeyPoint> getKeypoints(void);
	double           getScale(void);
	void             normalizeDescriptors( Mat& descriptors );
};
