#pragma once

#include "opencv/cv.h"
#include "opencv2/imgproc/imgproc_c.h"
#include "opencv/highgui.h"

#include <cstdlib>
#include <iostream>

using namespace std;
using namespace cv;

class LoweSIFTMatcher
{

public:
	
	LoweSIFTMatcher(void);
	~LoweSIFTMatcher(void);

	void match(Mat &queryDescriptors, Mat &trainDescriptors, vector<DMatch> &matches);
	

};
