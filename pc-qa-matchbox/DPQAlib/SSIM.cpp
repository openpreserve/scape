#include "SSIM.h"


SSIM::SSIM(void)
{
}

SSIM::~SSIM(void)
{
}


double getSSIM(IplImage* src1, 
					  IplImage* src2, 
					  IplImage* mask,
					  const double K1,
					  const double K2,
					  const int L,
					  const int downsamplewidth,
					  const int gaussian_window,
					  const double gaussian_sigma,
					  IplImage* dest)
{
	// default settings
	const double C1 = (K1 * L) * (K1 * L); //6.5025 C1 = (K(1)*L)^2;
	const double C2 = (K2 * L) * (K2 * L); //58.5225 C2 = (K(2)*L)^2;

	//　get width and height
	int x=src1->width, y=src1->height;

	//　distance (down sampling) setting
	int rate_downsampling = std::max(1, int((std::min(x,y) / downsamplewidth) + 0.5));

	int nChan=1, d=IPL_DEPTH_32F;

	//　size before down sampling
	CvSize size_L = cvSize(x, y);

	//　size after down sampling
	CvSize size = cvSize(x / rate_downsampling, y / rate_downsampling);

	//　image allocation
	cv::Ptr<IplImage> img1 = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> img2 = cvCreateImage( size, d, nChan);


	//　convert 8 bit to 32 bit float value
	cv::Ptr<IplImage> img1_L = cvCreateImage( size_L, d, nChan);
	cv::Ptr<IplImage> img2_L = cvCreateImage( size_L, d, nChan);
	cvConvert(src1, img1_L);
	cvConvert(src2, img2_L);

	//　down sampling
	cvResize(img1_L, img1);
	cvResize(img2_L, img2);

	//　buffer alocation
	cv::Ptr<IplImage> img1_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> img2_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> img1_img2 = cvCreateImage( size, d, nChan);

	//　pow and mul
	cvPow( img1, img1_sq, 2 );
	cvPow( img2, img2_sq, 2 );
	cvMul( img1, img2, img1_img2, 1 );

	//　get sigma
	cv::Ptr<IplImage> mu1 = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> mu2 = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> mu1_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> mu2_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> mu1_mu2 = cvCreateImage( size, d, nChan);


	cv::Ptr<IplImage> sigma1_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> sigma2_sq = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> sigma12 = cvCreateImage( size, d, nChan);

	//　allocate buffer
	cv::Ptr<IplImage> temp1 = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> temp2 = cvCreateImage( size, d, nChan);
	cv::Ptr<IplImage> temp3 = cvCreateImage( size, d, nChan);

	//ssim map
	cv::Ptr<IplImage> ssim_map = cvCreateImage( size, d, nChan);


	//////////////////////////////////////////////////////////////////////////
	// 	// PRELIMINARY COMPUTING

	//　gaussian smooth
	cvSmooth( img1, mu1, CV_GAUSSIAN, gaussian_window, gaussian_window, gaussian_sigma );
	cvSmooth( img2, mu2, CV_GAUSSIAN, gaussian_window, gaussian_window, gaussian_sigma );

	//　get mu
	cvPow( mu1, mu1_sq, 2 );
	cvPow( mu2, mu2_sq, 2 );
	cvMul( mu1, mu2, mu1_mu2, 1 );

	//　calc sigma
	cvSmooth( img1_sq, sigma1_sq, CV_GAUSSIAN, gaussian_window, gaussian_window, gaussian_sigma );
	cvAddWeighted( sigma1_sq, 1, mu1_sq, -1, 0, sigma1_sq );

	cvSmooth( img2_sq, sigma2_sq, CV_GAUSSIAN, gaussian_window, gaussian_window, gaussian_sigma);
	cvAddWeighted( sigma2_sq, 1, mu2_sq, -1, 0, sigma2_sq );

	cvSmooth( img1_img2, sigma12, CV_GAUSSIAN, gaussian_window, gaussian_window, gaussian_sigma );
	cvAddWeighted( sigma12, 1, mu1_mu2, -1, 0, sigma12 );


	//////////////////////////////////////////////////////////////////////////
	// FORMULA

	// (2*mu1_mu2 + C1)
	cvScale( mu1_mu2, temp1, 2 );
	cvAddS( temp1, cvScalarAll(C1), temp1 );

	// (2*sigma12 + C2)
	cvScale( sigma12, temp2, 2 );
	cvAddS( temp2, cvScalarAll(C2), temp2 );

	// ((2*mu1_mu2 + C1).*(2*sigma12 + C2))
	cvMul( temp1, temp2, temp3, 1 );

	// (mu1_sq + mu2_sq + C1)
	cvAdd( mu1_sq, mu2_sq, temp1 );
	cvAddS( temp1, cvScalarAll(C1), temp1 );

	// (sigma1_sq + sigma2_sq + C2)
	cvAdd( sigma1_sq, sigma2_sq, temp2 );
	cvAddS( temp2, cvScalarAll(C2), temp2 );

	// ((mu1_sq + mu2_sq + C1).*(sigma1_sq + sigma2_sq + C2))
	cvMul( temp1, temp2, temp1, 1 );

	// ((2*mu1_mu2 + C1).*(2*sigma12 + C2))./((mu1_sq + mu2_sq + C1).*(sigma1_sq + sigma2_sq + C2))
	cvDiv( temp3, temp1, ssim_map, 1 );

	cv::Ptr<IplImage> stemp = cvCreateImage( size, IPL_DEPTH_8U, 1);
	cv::Ptr<IplImage> mask2 = cvCreateImage( size, IPL_DEPTH_8U, 1);

	cvConvertScale(ssim_map, stemp, 255.0, 0.0);
	cvResize(stemp,dest);
	cvResize(mask,mask2);

	CvScalar index_scalar = cvAvg( ssim_map, mask2 );

	// through observation, there is approximately 
	// 1% error max with the original matlab program

	return index_scalar.val[0];
}


double xcvCalcSSIM(IplImage* src, IplImage* dest, int channel, int method, IplImage* _mask,
				   const double K1,
				   const double K2,
				   const int L,
				   const int downsamplewidth,
				   const int gaussian_window,
				   const double gaussian_sigma,
				   IplImage* ssim_map
				   )
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
			sn+=getSSIM(sgray[i],gray[i],mask,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma,smap);
		}
		sn/=(double)src->nChannels; 
	}
	else
	{
		sn	= getSSIM(sgray[channel],gray[channel],mask,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma,smap);
	}

	for(int i=0;i<4;i++)
	{
		cvReleaseImage(&gray[i]);
		cvReleaseImage(&sgray[i]);
	}

	if(ssim_map!=NULL)cvCopy(smap,ssim_map);
	cvReleaseImage(&smap);
	cvReleaseImage(&__mask);
	cvReleaseImage(&ssrc);
	cvReleaseImage(&sdest);
	return sn;
}

double xcvCalcDSSIM(IplImage* src, IplImage* dest, int channel, int method, IplImage* _mask,
					const double K1,
					const double K2,
					const int L,
					const int downsamplewidth,
					const int gaussian_window,
					const double gaussian_sigma,
					IplImage* ssim_map
					)
{
	double ret = xcvCalcSSIM(src, dest, channel, method, _mask, K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma,ssim_map);
	if(ret==0)ret=-1.0;
	else ret =(1.0 / ret) - 1.0;
	return ret;
}
double xcvCalcSSIMBB(IplImage* src, IplImage* dest, int channel, int method, int boundx,int boundy,const double K1,	const double K2,	const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma, IplImage* ssim_map)
{
	IplImage* mask = cvCreateImage(cvGetSize(src),8,1);
	cvZero(mask);
	cvRectangle(mask,cvPoint(boundx,boundy),cvPoint(src->width-boundx,src->height-boundy),cvScalarAll(255),CV_FILLED);

	double ret = xcvCalcSSIM(src,dest,channel,method,mask,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma,ssim_map);
	cvReleaseImage(&mask);
	return ret;
}

double xcvCalcDSSIMBB(IplImage* src, IplImage* dest, int channel, int method, int boundx,int boundy,const double K1,	const double K2,	const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma, IplImage* ssim_map)
{
	IplImage* mask = cvCreateImage(cvGetSize(src),8,1);
	cvZero(mask);
	cvRectangle(mask,cvPoint(boundx,boundy),cvPoint(src->width-boundx,src->height-boundy),cvScalarAll(255),CV_FILLED);

	double ret = xcvCalcSSIM(src,dest,channel,method,mask,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma,ssim_map);
	cvReleaseImage(&mask);

	if(ret==0)ret=-1.0;
	else ret = (1.0 / ret) - 1.0;
	return ret;
}


double SSIM::calcSSIM(cv::Mat& src1, cv::Mat& src2, int channel, int method, const cv::Mat& mask, const double K1, const double K2,	const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma,  cv::Mat ssim_map)
{
	if(ssim_map.empty())ssim_map.create(src1.size(),CV_8U);

	if(mask.empty())
	{
		xcvCalcSSIM(&IplImage(src1),&IplImage(src2),channel,method,NULL,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
		return xcvCalcSSIM(&IplImage(src1),&IplImage(src2),channel,method,NULL,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
	}
	else
		return xcvCalcSSIM(&IplImage(src1),&IplImage(src2),channel,method,&IplImage(mask),K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
}

double SSIM::calcSSIMBB(cv::Mat& src1, cv::Mat& src2, int channel, int method, int boundx, int boundy, const double K1, const double K2, const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma,  cv::Mat ssim_map)
{
	if(ssim_map.empty())ssim_map.create(src1.size(),CV_8U);
	return xcvCalcSSIMBB(&IplImage(src1),&IplImage(src2),channel,method,boundx,boundy,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
}

double SSIM::calcDSSIM(cv::Mat& src1, cv::Mat& src2, int channel, int method, const cv::Mat& mask, const double K1, const double K2,	const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma,  cv::Mat ssim_map)
{
	if(ssim_map.empty())ssim_map.create(src1.size(),CV_8U);

	if(mask.empty())
	{
		xcvCalcSSIM(&IplImage(src1),&IplImage(src2),channel,method,NULL,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
		return xcvCalcDSSIM(&IplImage(src1),&IplImage(src2),channel,method,NULL,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
	}
	else
		return xcvCalcDSSIM(&IplImage(src1),&IplImage(src2),channel,method,&IplImage(mask),K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
}

double SSIM::calcDSSIMBB(cv::Mat& src1, cv::Mat& src2, int channel, int method, int boundx, int boundy, const double K1, const double K2, const int L, const int downsamplewidth, const int gaussian_window, const double gaussian_sigma, cv::Mat ssim_map)
{
	if(ssim_map.empty())ssim_map.create(src1.size(),CV_8U);
	return xcvCalcDSSIMBB(&IplImage(src1),&IplImage(src2),channel,method,boundx,boundy,K1,K2,L,downsamplewidth,gaussian_window,gaussian_sigma, &IplImage(ssim_map));
}

