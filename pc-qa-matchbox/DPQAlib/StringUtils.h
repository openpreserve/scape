#pragma once

#include <string>
#include <sstream>
#include <vector>

#include "opencv/cv.h"

using namespace std;
using namespace cv;

class StringUtils
{
public:
	StringUtils(void);
	~StringUtils(void);

	static vector<string> split(string &s, char delim);
	static string removeSourceFromExceptionMessage( string msg );
	static string getFilename( string fullpath );
};
