#pragma once

#include "OutputParameter.h"
#include "stdafx.h"

class ErrorOutputParameter :
	public OutputParameter
{
public:
	ErrorOutputParameter(string nme = "ERROR");
	~ErrorOutputParameter(void);

	void setErrorMessage(string msg);
};
