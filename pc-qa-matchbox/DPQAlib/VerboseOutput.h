#pragma once

#include "stdafx.h"
#include <time.h>

#include <string>
#include <sstream>
#include <stdio.h>
#include <iostream>

using namespace std;

class VerboseOutput
{
private:

	static string getTimeStamp();

public:

	VerboseOutput(void);
	~VerboseOutput(void);
	static void println( string name, string msg, bool verbose );
	
};
