#include "ImageIO.h"
#include <stdio.h>
#include "opencv2/imgproc/imgproc_c.h"
#include "opencv/highgui.h"

ImageIO::ImageIO(void)
{
}

ImageIO::~ImageIO(void)
{
}

IplImage* ImageIO::loadImage(char* path)
{
	char imagepath[1024];
	IplImage* img;

	strcpy( imagepath, path );
	img = cvLoadImage( imagepath, CV_LOAD_IMAGE_COLOR );

	if (!img)
	{
		printf( "\nUnable to load image %s\n\n", imagepath );
		exit(1);
	}	

	return img;
}
