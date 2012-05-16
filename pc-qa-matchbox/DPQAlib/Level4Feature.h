#pragma once

#include "stdafx.h"

#include "Feature.h"

class Level4Feature:
	public Feature
{
protected:
	using Feature::level;

public:
	Level4Feature(void);
	~Level4Feature(void);
};
