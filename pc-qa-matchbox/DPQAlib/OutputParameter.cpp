#include "OutputParameter.h"


OutputParameter::OutputParameter(string nme)
{
	name = nme;
}

OutputParameter::~OutputParameter(void)
{
	// delete attributes
}

void OutputParameter::setName(string str)
{
	name = str;
}

string *OutputParameter::getName()
{
	return &name;
}

void OutputParameter::setData(string str)
{
	data = str;
}

string *OutputParameter::getData()
{
	return &data;
}

void OutputParameter::setType(int i)
{
	type = i;
}

int *OutputParameter::getType()
{
	return &type;
}

void OutputParameter::addAttribute(OutputAttribute attr)
{
	attrs.push_back(attr);
}

list<OutputAttribute> OutputParameter::getOutputAttributes(void)
{
	return attrs;
}