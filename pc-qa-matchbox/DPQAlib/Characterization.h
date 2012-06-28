#pragma once

#include "stdafx.h"

#include "Feature.h"
#include "TaskFactory.h"

#include <tclap/CmdLine.h>
#include <list>

#include "opencv2/imgproc/imgproc_c.h"
#include <highgui.h>
#include <fstream>

#include "StringUtils.h"


using namespace std;
using namespace cv;

class Characterization
{

private:
	list<Feature*> tasks;
	string* filename;
	string featureFileOutputDirectory;

	bool         canExecute(Feature* task);
	//FileStorage* openFilestorage();

public:
	Characterization(void);
	~Characterization(void);

	void addTask(Feature* task);
	void execute();
	void writeOutput(void);
	void addCommandLineArgs(TCLAP::CmdLine* cmd);
	void parseCommandLineArgs();
	void setFilename(string* fname);
	void setAppendToFile( bool append );
	void loadFeatures();
	void setUpdateFile( bool param1 );
	void setFeatureFileOutputDirectory( string& featureFileOutputDirectory );
};
