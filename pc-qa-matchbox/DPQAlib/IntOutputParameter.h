#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"

#include <string>
#include <sstream>

using namespace std;

class IntOutputParameter : 
	public OutputParameter
{
public:
	IntOutputParameter(string nme);
	~IntOutputParameter(void);

	void setData(int i);
	int getData();
};
