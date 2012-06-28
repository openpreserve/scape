#pragma once

#include "stdafx.h"

#include "Level4Feature.h"
#include "ErrorOutputParameter.h"
#include "DoubleOutputParameter.h"

#include "RobustMatcher.h"
#include "SIFTComparison.h"

#include <string>
#include "opencv/cv.h"

using namespace cv;
using namespace std;

class BOWHistogram :
	public Level4Feature
{
private:

	// private properties
	Mat               vocabulary;
	string            vocabularyFilename;
	Mat               response_hist;
	string            metric;
	SIFTComparison*   sift;

	Mat               descriptors;
	vector<KeyPoint>  keypoints;

	// private members
	Mat loadVocabulary(string filename);

protected:
	using Feature::name;

public:
	static const string TASK_NAME;		
	BOWHistogram(SIFTComparison* sift);
	BOWHistogram(void);
	~BOWHistogram(void);

	// implement virtual methods
	void             execute(Mat& image);
	void             compare(Feature *task);
	void             parseCommandlineArguments();
	list<string>*    getCmdlineArguments(void);
	void             setCmdlineArguments(list<string>* args);
	void             writeOutput(FileStorage& fs);
	void             readData(FileNode& fs);
	Mat&             getResponseHistogram();
};
