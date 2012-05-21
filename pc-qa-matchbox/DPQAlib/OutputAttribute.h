#pragma once

#include "stdafx.h"

#include <string>

using namespace std;

class OutputAttribute
{
private:
	string name;
	string value;

public:
	OutputAttribute(string nme, string val);
	~OutputAttribute(void);

	void setName(string str);
	string *getName();

	void setValue(string str);
	string *getValue();
	
};
