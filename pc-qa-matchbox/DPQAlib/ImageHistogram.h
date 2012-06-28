#pragma once

#include "stdafx.h"

#include <stdio.h>
#include <string>
#include <list>

#include "opencv/cv.h"
#include <tclap/CmdLine.h>

#include "Level2Feature.h"
#include "ErrorOutputParameter.h"
#include "DoubleOutputParameter.h"

using namespace cv;
using namespace std;


class ImageHistogram :
	public Level2Feature
{
private:
	int    binSize;
	string metric;

	vector<Mat> bins;
	void normalizeHist(Mat hist, int size);

protected:
	using Feature::name;

public:
	static const string TASK_NAME;
	static const string TAG_BINS;

	ImageHistogram(void);
	~ImageHistogram(void);

	// implement virtual methods
	void execute(Mat& image);
	void compare(Feature *task);
	void parseCommandlineArguments();
	list<string>* getCmdlineArguments(void);
	void setCmdlineArguments(list<string>* args);
	void writeOutput(FileStorage& fs);
	void readData(FileNode& fs);

	vector<Mat> getBins(void);

};
