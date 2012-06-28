#pragma once

#include "opencv/highgui.h"
#include "opencv2/core/core.hpp"
#include "opencv2/opencv.hpp"

using namespace cv;

#define ALLCHANNEL -1

class UIQI
{
public:

	UIQI(void);
	~UIQI(void);


	static double calcUIQI(Mat& src1,
		                   Mat& src2, 
						   int channel = 0, 
						   int method=CV_BGR2YUV,
						   int blocksize = 8,
						   Mat mask=Mat(),
						   Mat uiqi_map=Mat());


};
