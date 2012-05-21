#pragma once

#include "stdafx.h"

#include "OutputParameter.h"

class StringOutputParameter :
	public OutputParameter
{
public:
	StringOutputParameter(string nme);
	~StringOutputParameter(void);
};
