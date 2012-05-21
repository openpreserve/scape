#include "PixelwiseComparison.h"

const string PixelwiseComparison::TASK_NAME = "PixelwiseComparison";

PixelwiseComparison::PixelwiseComparison(void)
{
	name = TASK_NAME;
}

PixelwiseComparison::~PixelwiseComparison(void)
{
}

void PixelwiseComparison::compare(Feature *task)
{
	verbosePrintln(string("comparing"));

	CvSize sz = cvGetSize( image );

	Mat* otherImg  = ((PixelwiseComparison*)task)->getImage();
	Mat diffImage = cvCreateImage( sz, image->dims, 3 );

	cvAbsDiff( image , otherImg, &diffImage );
	
	Scalar s = mean(diffImage);

	DoubleOutputParameter *param1 = new DoubleOutputParameter("result");
	param1->setData(s.val[0] + s.val[1] + s.val[2]);
	addOutputParameter(*param1);
}

void PixelwiseComparison::writeOutput(FileStorage& fs)
{
}

void PixelwiseComparison::readData(FileNode& fs)
{
}

void PixelwiseComparison::execute(Mat& img)
{
	image = &img;
}

list<string>* PixelwiseComparison::getCmdlineArguments()
{
	return NULL;
}

void PixelwiseComparison::setCmdlineArguments(list<string> *args)
{
}

void PixelwiseComparison::parseCommandlineArguments()
{
}

Mat *PixelwiseComparison::getImage(void)
{
	return image;
}