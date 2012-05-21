#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"

#include <string>
#include <sstream>

using namespace std;

class DoubleOutputParameter :
	public OutputParameter
{
public:
	DoubleOutputParameter(string nme);
	~DoubleOutputParameter(void);

	void setData(double i);
	double getData();
};
