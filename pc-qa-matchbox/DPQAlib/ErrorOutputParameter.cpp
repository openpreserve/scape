#include "ErrorOutputParameter.h"

ErrorOutputParameter::ErrorOutputParameter(string nme):OutputParameter(nme)
{
}

ErrorOutputParameter::~ErrorOutputParameter(void)
{
}

void ErrorOutputParameter::setErrorMessage(string msg)
{
	OutputParameter::setData(msg);
}