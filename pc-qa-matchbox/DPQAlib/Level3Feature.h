#pragma once

#include "stdafx.h"

#include "Feature.h"


class Level3Feature :
	public Feature
{
protected:
	using Feature::level;

public:
	Level3Feature(void);
	~Level3Feature(void);
};
