#pragma once

#include "stdafx.h"

#include "Feature.h"
#include "PixelwiseComparison.h"
#include "SIFTComparison.h"
#include "ImageProfile.h"
#include "ImageMetadata.h"
#include "ImageHistogram.h"
#include "BOWHistogram.h"

#include "TaskFactory.h"
#include "VerboseOutput.h"

#include <tclap/CmdLine.h>

#include <string>
#include <list>

using namespace cv;
using namespace std;

class Comparison
{
private:

	list<Feature*> tasks;
	list<Feature*> tasksFromXML1;
	list<Feature*> tasksFromXML2;
	int level;

	void setCommandlineArguments(Feature* task);
	void setTasksCmdArgs(list<Feature*>* tasks);
	bool canExecute( Feature* task1 );

public:

	Comparison(void);
	~Comparison(void);

	void read(string* filename1, string* filename2);
	void addCommandLineArgs(TCLAP::CmdLine* cmd);
	void parseCommandLineArgs();
	void execute();
	void level3(string *filename1, string *filename2);

	void writeOutput(void);
	void setLevel( int& level );
};
