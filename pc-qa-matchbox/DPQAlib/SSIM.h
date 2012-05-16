#pragma once

#include <highgui.h>
#include "opencv2/core/core.hpp"
#include "opencv2/opencv.hpp"

using namespace cv;

#define ALLCHANNEL -1

class SSIM
{
public:

	SSIM(void);
	~SSIM(void);


	static double calcSSIM(Mat& src1,
		                   Mat& src2, 
						   int channel = 0, 
						   int method=CV_BGR2YUV, 
						   const Mat& mask = Mat(),
						   const double K1 = 0.01, 
						   const double K2 = 0.03,	
						   const int L = 255, 
						   const int downsamplewidth=256, 
						   const int gaussian_window=11, 
						   const double gaussian_sigma=1.5, 
						   Mat ssim_map = Mat());

	static double calcSSIMBB(Mat& src1, 
		                     Mat& src2, 
							 int channel = 0, 
							 int method=CV_BGR2YUV, 
							 int boundx=0,int boundy=0,
							 const double K1 = 0.01, 
							 const double K2 = 0.03,	
							 const int L = 255, 
							 const int downsamplewidth=256, 
							 const int gaussian_window=11, 
							 const double gaussian_sigma=1.5, 
							 Mat ssim_map=Mat());

	static double calcDSSIM(Mat& src1, 
		                    Mat& src2, 
							int channel = 0, 
							int method=CV_BGR2YUV, 
							const Mat& mask = Mat(),
							const double K1 = 0.01, 
							const double K2 = 0.03,	
							const int L = 255, 
							const int downsamplewidth=256, 
							const int gaussian_window=11, 
							const double gaussian_sigma=1.5, 
							Mat ssim_map=Mat());

	static double calcDSSIMBB(Mat& src1, 
		                      Mat& src2, 
							  int channel = 0, 
							  int method=CV_BGR2YUV, 
							  int boundx=0,
							  int boundy=0,
							  const double K1 = 0.01, 
							  const double K2 = 0.03,	
							  const int L = 255, 
							  const int downsamplewidth=256, 
							  const int gaussian_window=11, 
							  const double gaussian_sigma=1.5, 
							  Mat ssim_map=Mat());

};
