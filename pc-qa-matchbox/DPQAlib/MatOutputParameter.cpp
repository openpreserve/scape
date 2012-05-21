#include "MatOutputParameter.h"

MatOutputParameter::MatOutputParameter(string nme):OutputParameter(nme)
{
}

MatOutputParameter::~MatOutputParameter(void)
{
}


void MatOutputParameter::setData(Mat m)
{
	OutputParameter::setData(StringConverter::toString(m));
}