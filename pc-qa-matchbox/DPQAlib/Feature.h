#pragma once

#include "stdafx.h"

#include <list>
#include <string>

#include <cv.h>

#include <tclap/CmdLine.h>
#include <tclap/Arg.h>

#include "OutputParameter.h"
#include "OutputAttribute.h"
#include "VerboseOutput.h"

using namespace std;
using namespace cv;

class Feature
{

private:

	list<OutputParameter> outputParams;
	list<TCLAP::Arg*>     characterizationArguments;
	list<TCLAP::Arg*>     comparisonArguments;
	list<FileNode>        fileNodes;

protected:

	string    name;
	string    filename;
	int       level;
	bool      verbose;
	bool      dataLoaded;

	// protected methods
	void verbosePrintln(string msg);

public:

	static const string TAG_NAME;
	static const string ATTR_LEVEL;
	static const string ATTR_NAME;

	// constructors / destructors
	Feature(void);
	~Feature(void);

	// virtual methods
	virtual void           execute(Mat& image)                     = 0;
	virtual void           compare(Feature* task)                  = 0;
	virtual void           parseCommandlineArguments()             = 0;
	virtual list<string>*  getCmdlineArguments(void)               = 0;
	virtual void           setCmdlineArguments(list<string>* args) = 0;
	virtual void           writeOutput(FileStorage& fs)            = 0;
	virtual void           readData(FileNode& fs)                  = 0;

	// output methods
	void                   addOutputParameter(OutputParameter param);
	list<OutputParameter>* getOutputParameters();
	void                   addFilenode(FileNode node);

	// commandline arguments
	void                   addCharacterizationCommandlineArgument(TCLAP::Arg* arg);
	void                   addComparisonCommandlineArgument(TCLAP::Arg* arg);
	list<TCLAP::Arg*>*     getCharacterizationCommandlineArguments();
	list<TCLAP::Arg*>*     getComparisonCommandlineArguments();

	// getter/setter
	string                 getName(void);
	int                    getLevel(void);
	bool                   hasDataLoaded(void);
	void                   setVerbose(bool v);
	void                   setFilename( string* filename );
	string                 getFilename(void);

	void                   testOutput(void);	
	void                   setDataLoaded( bool param1 );
};