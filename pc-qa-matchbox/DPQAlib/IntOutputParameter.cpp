#include "IntOutputParameter.h"


IntOutputParameter::IntOutputParameter(string nme):OutputParameter(nme)
{
	setType(0);
}

IntOutputParameter::~IntOutputParameter(void)
{
}

void IntOutputParameter::setData(int i)
{
	OutputParameter::setData(StringConverter::toString(i));
}

int IntOutputParameter::getData()
{
	return StringConverter::toInt(OutputParameter::getData());
}