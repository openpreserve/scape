#pragma once

#include "stdafx.h"

#include <string>
#include <list>

#include "Level3Feature.h"
#include "DoubleOutputParameter.h"

#include <cv.h>
#include <highgui.h>
#include "opencv2/imgproc/imgproc_c.h"


using namespace cv;
using namespace std;

class PixelwiseComparison :
	public Level3Feature
{
private:
	Mat* image;

public:
	static const string TASK_NAME;

	PixelwiseComparison(void);
	~PixelwiseComparison(void);

	// implement virtual methods
	void execute(Mat& image);
	void compare(Feature *task);
	void parseCommandlineArguments();
	list<string>* getCmdlineArguments(void);
	void setCmdlineArguments(list<string>* args);
	void writeOutput(FileStorage& fs);
	void readData(FileNode& fs);

	Mat* getImage(void);
};
