#pragma once

#include "stdafx.h"

#include "OutputParameter.h"
#include "StringConverter.h"

#include <string>
#include <sstream>

using namespace std;

class ListOutputParameter :
	public OutputParameter
{
public:
	ListOutputParameter(string nme);
	~ListOutputParameter(void);

	void setData(list<double> l);
};
