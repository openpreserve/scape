#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"
#include "opencv/cv.h"

using namespace cv;

class KeyPointOutputParameter :
	public OutputParameter
{
public:
	KeyPointOutputParameter(string name);
	~KeyPointOutputParameter(void);

	void setData(vector<KeyPoint> keypoints);
};
