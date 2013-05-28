#pragma once

#include "stdafx.h"

#include <string>

#include "ImageMetadata.h"
#include "ImageHistogram.h"
#include "ImageProfile.h"
#include "SIFTComparison.h"

using namespace std;

class TaskFactory
{
private:
	static Feature* createFeature(string& name);

public:
	TaskFactory(void);
	~TaskFactory(void);

	static Feature* createTask(FileNode node, int& level);
	static void     copyCmdlineArgument(Feature* src, Feature* dest);
	static void     loadData( list<Feature*> tasks );
	static Feature* createTaskfromFilename( string* file1 );
};
