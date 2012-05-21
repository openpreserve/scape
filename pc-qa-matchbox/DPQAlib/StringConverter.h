#pragma once

#include "stdafx.h"

#include <iostream>
#include <algorithm>
#include <iterator>
#include <string>
#include <sstream>
#include <list>

#include <cv.h>

using namespace cv;
using namespace std;

class StringConverter
{
public:
	StringConverter(void);
	~StringConverter(void);

	static int toInt(string* str);
	static int toInt(char* str);
	static float toFloat(string* str);
	static double toDouble(string* str);

	static Mat toMat(string* str);

	static string toString(int i);
	static string toString(double i);
	static string toString(Mat m);
	static string toString(list<double> l);
};
