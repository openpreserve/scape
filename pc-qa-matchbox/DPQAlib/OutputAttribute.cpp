#include "OutputAttribute.h"

OutputAttribute::OutputAttribute(string nme, string val)
{
	name = nme;
	value = val;
}

OutputAttribute::~OutputAttribute(void)
{
}

void OutputAttribute::setName(string nme)
{
	name = nme;
}

string* OutputAttribute::getName()
{
	return &name;
}

void OutputAttribute::setValue(string val)
{
	value = val;
}

string* OutputAttribute::getValue()
{
	return &value;
}