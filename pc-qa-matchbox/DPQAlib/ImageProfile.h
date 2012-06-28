#pragma once

#include "stdafx.h"

#include <stdio.h>
#include <string>
#include <list>

#include "Level2Feature.h"
#include "ErrorOutputParameter.h"
#include "DoubleOutputParameter.h"

#include "opencv/cv.h"
#include <tclap/CmdLine.h>

using namespace cv;
using namespace std;

class ImageProfile :
	public Level2Feature
{
private:
	vector<Mat> profiles;
	list<Mat> vertProfiles;
	list<Mat> horProfiles;

protected:
	using Feature::name;

public:
	static const string TASK_NAME;

	ImageProfile(void);
	~ImageProfile(void);

	// implement virtual methods
	void execute(Mat& image);
	void compare(Feature *task);
	void parseCommandlineArguments();
	list<string>* getCmdlineArguments(void);
	void setCmdlineArguments(list<string>* args);
	void writeOutput(FileStorage& fs);
	void readData(FileNode& fs);

	vector<Mat> getProfiles(void);
};
