#include "ImageMetadata.h"


const string ImageMetadata::TASK_NAME      = "ImageMetadata";
const string ImageMetadata::IMAGE_WIDTH    = "ImageWidth";
const string ImageMetadata::IMAGE_HEIGHT   = "ImageHeight";
const string ImageMetadata::IMAGE_CHANNELS = "ImageChannels";


ImageMetadata::ImageMetadata(void):Level1Feature()
{
	name = TASK_NAME;
}

ImageMetadata::~ImageMetadata(void)
{
}

void ImageMetadata::execute(Mat& image)
{
	imageWidth    = image.cols;
	imageHeight   = image.rows;
	imageChannels = image.channels();
}

void ImageMetadata::writeOutput(FileStorage& fs)
{
	fs << "ImageMetadata" << "{" << "imageWidth" << imageWidth << "imageHeight" << imageHeight << "imageChannels" << imageChannels << "}";
}

void ImageMetadata::readData(FileNode& node)
{
	imageWidth    = node["imageWidth"];
	imageHeight   = node["imageHeight"];
	imageChannels = node["imageChannels"];
}

void ImageMetadata::compare(Feature *task)
{
	verbosePrintln(string("comparing"));

	ImageMetadata* im = dynamic_cast< ImageMetadata* >( task );
	
	double result = 0;
	result += (im->imageHeight   - this->imageHeight);
	result += (im->imageWidth    - this->imageWidth);
	result += (im->imageChannels - this->imageChannels);

	DoubleOutputParameter *param1 = new DoubleOutputParameter("result");
	param1->setData(result);
	addOutputParameter(*param1);
}

void ImageMetadata::parseCommandlineArguments()
{
}

list<string>* ImageMetadata::getCmdlineArguments(void)
{
	return NULL;
}

void ImageMetadata::setCmdlineArguments(list<string>* args)
{
}