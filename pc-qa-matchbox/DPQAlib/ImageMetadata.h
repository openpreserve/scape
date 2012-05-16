#pragma once

#include "stdafx.h"

#include "Level1Feature.h"
#include "IntOutputParameter.h"
#include "DoubleOutputParameter.h"

#include <tclap/CmdLine.h>


class ImageMetadata :
	public Level1Feature
{

private:
	// properties
	int imageWidth;
	int imageHeight;
	int imageChannels;

protected:
	using Feature::name;

public:
	// constants
	static const string TASK_NAME;
	static const string IMAGE_WIDTH;
	static const string IMAGE_HEIGHT;
	static const string IMAGE_CHANNELS;

	// constructors
	ImageMetadata(void);
	~ImageMetadata(void);

	// members
	// implement virtual methods
	void execute(Mat& image);
	void compare(Feature *task);
	void parseCommandlineArguments();
	list<string>* getCmdlineArguments(void);
	void setCmdlineArguments(list<string>* args);
	void writeOutput(FileStorage& fs);
	void readData(FileNode& fs);
};

