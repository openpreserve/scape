#include "TaskFactory.h"
#include "BOWHistogram.h"

TaskFactory::TaskFactory(void)
{
}

TaskFactory::~TaskFactory(void)
{
}

Feature* TaskFactory::createTask( FileNode node, int& level )
{
	string name = node.name();
	Feature* feature = createFeature(name);

	if ((feature != NULL) && (feature->getLevel() >= level))
	{
		feature->loadData(node);
	}

	return feature;
}

Feature* TaskFactory::createFeature(string& name)
{
	if (name.compare(ImageMetadata::TASK_NAME) == 0)
	{
		return new ImageMetadata();
	}
	else if (name.compare(ImageHistogram::TASK_NAME) == 0)
	{
		return new ImageHistogram();
	}
	else if (name.compare(ImageProfile::TASK_NAME) == 0)
	{
		return new ImageProfile();
	}
	else if (name.compare(SIFTComparison::TASK_NAME) == 0)
	{
		return new SIFTComparison();
	}
	else if (name.compare(BOWHistogram::TASK_NAME) == 0)
	{
		return new BOWHistogram();
	}
}

void TaskFactory::loadData( list<Feature*> tasks)
{
	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;
		task->loadData();
	}
}