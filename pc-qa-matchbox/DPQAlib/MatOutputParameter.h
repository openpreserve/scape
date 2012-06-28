#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"
#include "opencv/cv.h"

using namespace cv;

class MatOutputParameter :
	public OutputParameter
{
public:
	MatOutputParameter(string nme);
	~MatOutputParameter(void);

	void setData(Mat m);
};
