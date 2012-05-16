#include "TaskFactory.h"
#include "BOWHistogram.h"

TaskFactory::TaskFactory(void)
{
}

TaskFactory::~TaskFactory(void)
{
}

Feature* TaskFactory::createTask( FileNode node, int& level, bool& verbose )
{
	string name = node.name();
	Feature* feature = createFeature(name);
	feature->setVerbose(verbose);

	if ((feature != NULL) && (feature->getLevel() >= level))
	{
		feature->readData(node);
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




void TaskFactory::loadData( list<Feature*> tasks, FileStorage* fs )
{
	FileNode features1 = fs->root();
	for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
	{	
		FileNode node = *it;
		string name = node.name();

		list<Feature*>::iterator i;

		for(i=tasks.begin(); i != tasks.end(); ++i)
		{
			Feature* task = *i;

			if (name.compare(task->getName()) == 0)
			{
				task->readData(node);
				task->setDataLoaded(true);
			}
		}
	}
}


