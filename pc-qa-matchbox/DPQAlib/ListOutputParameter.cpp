#include "ListOutputParameter.h"

ListOutputParameter::ListOutputParameter(string nme):OutputParameter(nme)
{
}

ListOutputParameter::~ListOutputParameter(void)
{
}

void ListOutputParameter::setData(list<double> l)
{
	OutputParameter::setData(StringConverter::toString(l));
}