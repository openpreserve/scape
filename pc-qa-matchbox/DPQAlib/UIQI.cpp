#include "UIQI.h"


UIQI::UIQI(void)
{
}

UIQI::~UIQI(void)
{
}

double getUIQI(IplImage* src1, 
					  IplImage* src2, 
					  IplImage* mask,
					  const int blocksize,
					  IplImage* dest)
{	
	int N = blocksize * blocksize;

	int x=src1->width, y=src1->height;
	CvSize size_L = cvSize(x, y);

	int nChan=1, d=IPL_DEPTH_32F;
		
	cv::Ptr<IplImage> img1 = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2 = cvCreateImage( size_L, d, nChan);

	cvConvert(src1, img1);
	cvConvert(src2, img2);
	
	cv::Ptr<IplImage> img1_sq = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img12 = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2_sq = cvCreateImage( size_L, d, nChan);

	cvMul( img1, img1, img1_sq );
	cvMul( img2, img2, img2_sq );
	cvMul( img1, img2, img12 );
		
	cv::Ptr<IplImage> img1_sum = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2_sum = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img1_sq_sum = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2_sq_sum = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img12_sum = cvCreateImage( size_L, d, nChan);

	
	CvMat *kernel = cvCreateMat( blocksize, blocksize, CV_32FC1); //d ); 
	for( int i=0; i<blocksize; i++ )
		for ( int j=0; j<blocksize; j++)
			cvSet2D( kernel, i, j, cvRealScalar(1) );
	int anchor = blocksize/2;

	cvFilter2D( img1, img1_sum, kernel, cvPoint(anchor,anchor) );
	cvFilter2D( img2, img2_sum, kernel, cvPoint(anchor,anchor) );
	cvFilter2D( img1_sq, img1_sq_sum, kernel, cvPoint(anchor,anchor) );
	cvFilter2D( img2_sq, img2_sq_sum, kernel, cvPoint(anchor,anchor) );
	cvFilter2D( img12, img12_sum, kernel, cvPoint(anchor,anchor) );

	cv::Ptr<IplImage> img12_sum_mul = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img12_sq_sum_mul = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img1_sum_sq = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2_sum_sq = cvCreateImage( size_L, d, nChan);
	
	cvMul( img1_sum, img2_sum, img12_sum_mul );
	cvMul( img1_sum, img1_sum, img1_sum_sq );
	cvMul( img2_sum, img2_sum, img2_sum_sq );
	cvAdd( img1_sum_sq, img2_sum_sq, img12_sq_sum_mul );
	
	cv::Ptr<IplImage> numerator = cvCreateImage( size_L, d, nChan);
	cvScale( img12_sum, numerator, N);
	cvSub( numerator, img12_sum_mul, numerator);
	cvScale( numerator, numerator, 4);
	cvMul( numerator, img12_sum_mul, numerator );

	cv::Ptr<IplImage> denominator = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> denominator1 = cvCreateImage( size_L, d, nChan);

	cvAdd( img1_sq_sum, img2_sq_sum, denominator1 );
	cvScale( denominator1, denominator1, N );
	cvSub( denominator1, img12_sq_sum_mul, denominator1 );
    cvMul( denominator1, img12_sq_sum_mul, denominator );

	cv::Ptr<IplImage> quality_map = cvCreateImage( size_L, d, nChan);

	CvScalar eps;
	eps.val[0]=0.000001;
	cvAddS( denominator, eps, denominator );
	cvDiv( numerator, denominator, quality_map ); // fix this equiv. to matlab-code

	cv::Ptr<IplImage> stemp = cvCreateImage( size_L, IPL_DEPTH_8U, 1);
	cv::Ptr<IplImage> mask2 = cvCreateImage( size_L, IPL_DEPTH_8U, 1);

	cvConvertScale(quality_map, stemp, 255.0, 0.0);
	cvResize(stemp,dest);
	cvResize(mask,mask2);

	CvScalar quality = cvAvg( quality_map, mask2 );

	return quality.val[0];
}



double xcvCalcUIQI(IplImage* src, IplImage* dest, int channel, int method, int blocksize, IplImage* _mask, IplImage* uiqi_map )
{
	
	IplImage* mask;
	IplImage* __mask=cvCreateImage(cvGetSize(src),8,1);
	IplImage* smap=cvCreateImage(cvGetSize(src),8,1);

	cvSet(__mask,cvScalarAll(255));


	if(_mask==NULL)mask=__mask;
	else mask=_mask;
	IplImage* ssrc;
	IplImage* sdest;
	if(src->nChannels==1)
	{
		ssrc=cvCreateImage(cvGetSize(src),8,3);
		sdest=cvCreateImage(cvGetSize(src),8,3);
		cvCvtColor(src,ssrc,CV_GRAY2BGR);
		cvCvtColor(dest,sdest,CV_GRAY2BGR);
	}
	else
	{
		ssrc = cvCloneImage(src);
		sdest = cvCloneImage(dest);
		cvCvtColor(dest,sdest,method);
		cvCvtColor(src,ssrc,method);
	}

	IplImage* gray[4];
	IplImage* sgray[4];
	for(int i=0;i<4;i++)
	{
		gray[i] = cvCreateImage(cvGetSize(src),8,1);
		sgray[i] = cvCreateImage(cvGetSize(src),8,1);
	}

	cvSplit(sdest,gray[0],gray[1],gray[2],NULL);
	cvSplit(ssrc,sgray[0],sgray[1],sgray[2],NULL);

	double sn=0.0;

	if(channel==ALLCHANNEL)
	{
		for(int i=0;i<src->nChannels;i++)
		{
			sn+=getUIQI(sgray[i],gray[i],mask,blocksize,smap);
		}
		sn/=(double)src->nChannels; 
	}
	else
	{
		sn	= getUIQI(sgray[channel],gray[channel],mask,blocksize,smap);
	}

	for(int i=0;i<4;i++)
	{
		cvReleaseImage(&gray[i]);
		cvReleaseImage(&sgray[i]);
	}

	if (uiqi_map!=NULL) cvCopy( smap, uiqi_map);
	cvReleaseImage(&smap);
	cvReleaseImage(&__mask);
	cvReleaseImage(&ssrc);
	cvReleaseImage(&sdest);
	return sn;
}



double UIQI::calcUIQI(cv::Mat& src1, cv::Mat& src2, int channel, int method, int blocksize, cv::Mat mask, cv::Mat uiqi_map)
{
	if(uiqi_map.empty())uiqi_map.create(src1.size(),CV_8U);

	if(mask.empty())
	{
		xcvCalcUIQI( &IplImage(src1), &IplImage(src2), channel, method, blocksize, NULL, &IplImage(uiqi_map));
		return xcvCalcUIQI( &IplImage(src1), &IplImage(src2), channel, method, blocksize, NULL, &IplImage(uiqi_map));
	}
	else
		return xcvCalcUIQI(& IplImage(src1), &IplImage(src2), channel, method, blocksize, &IplImage(mask), &IplImage(uiqi_map));
}
