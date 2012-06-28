#pragma once

#include "stdafx.h"

#include <iostream>
#include "opencv/cv.h"

using namespace cv;
using namespace std;

class VisualBOW
{
private:
	Mat vocabulary;

public:
	VisualBOW(void);
	~VisualBOW(void);

	void createVocabulary(Mat& descriptors);
	Mat getVocabulary(void);
	void release(void);
};
