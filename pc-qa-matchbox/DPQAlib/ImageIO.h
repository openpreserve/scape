#pragma once

#include "stdafx.h"

#include "opencv2/imgproc/imgproc_c.h"

class ImageIO
{
public:
	ImageIO(void);
	~ImageIO(void);

	static IplImage* loadImage(char* path);
	
};
