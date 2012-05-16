#pragma once

#include "stdafx.h"

#include "Feature.h"

class Level1Feature : 
	public Feature
{
protected:
	using Feature::level;

public:
	Level1Feature(void);
	~Level1Feature(void);
};
