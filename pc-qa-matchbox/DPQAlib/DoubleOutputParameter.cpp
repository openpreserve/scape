#include "DoubleOutputParameter.h"

DoubleOutputParameter::DoubleOutputParameter(string nme):OutputParameter(nme)
{
	setType(0);
}

DoubleOutputParameter::~DoubleOutputParameter(void)
{
}

void DoubleOutputParameter::setData(double i)
{
	OutputParameter::setData(StringConverter::toString(i));
}

double DoubleOutputParameter::getData()
{
	return StringConverter::toDouble(OutputParameter::getData());
}