#pragma once

#include "stdafx.h"

#include "Feature.h"


class Level2Feature :
	public Feature
{
protected:
	using Feature::level;

public:
	Level2Feature(void);
	~Level2Feature(void);
};
