#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"
#include <cv.h>

using namespace cv;

class Mat2DOutputParameter :
	public OutputParameter
{
public:
	Mat2DOutputParameter(string nme);
	~Mat2DOutputParameter(void);

	void setData(Mat m);
};
