#pragma once

#include "stdafx.h"

#include <string>
#include <list>
#include "OutputAttribute.h"

using namespace std;

class OutputParameter
{
private:
	string name;
	string data;
	int    type;

	list<OutputAttribute> attrs;

public:

	OutputParameter(const string nme);
	~OutputParameter(void);

	void setName(string str);
	string *getName();

	void setData(string str);
	string *getData();

	void setType(int i);
	int *getType();

	void addAttribute(OutputAttribute attr);
	list<OutputAttribute> getOutputAttributes(void);
};
