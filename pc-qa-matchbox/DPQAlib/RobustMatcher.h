#pragma once

#include "opencv/cv.h"
#include "opencv2/imgproc/imgproc_c.h"
#include "opencv2/features2d/features2d.hpp"
#include "opencv/highgui.h"

#include <cstdlib>
#include <iostream>

using namespace std;
using namespace cv;

class RobustMatcher
{
private:

	float ratio; // max ratio between 1st and 2nd NN
	bool refineF; // if true will refine the F matrix
	double distance;
	double confidence;
	int ratioTest(vector<vector<DMatch> > &matches);
	void symmetryTest(const vector<vector<DMatch> >& matches1,
		const vector<vector<DMatch> >& matches2,
		vector<DMatch>& symMatches);
	Mat ransacTest(
		const vector<DMatch>& matches,
		const vector<KeyPoint>& keypoints1,
		const vector<KeyPoint>& keypoints2,
		vector<DMatch>& outMatches);

public:
	RobustMatcher() : ratio(0.65f), refineF(true),
		confidence(0.99), distance(3.0) {}

	Mat match(
		vector<KeyPoint>& keypoints1,
		vector<KeyPoint>& keypoints2,
		Mat& descriptors1,
		Mat& descriptors2,
		vector<DMatch>& matches);
	
};
