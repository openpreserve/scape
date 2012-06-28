#include "Comparison.h"

Comparison::Comparison(void)
{
	tasks.push_back(new ImageMetadata());
	tasks.push_back(new ImageHistogram());
	tasks.push_back(new ImageProfile());

	SIFTComparison* sift = new SIFTComparison();
	tasks.push_back(sift);
	tasks.push_back(new BOWHistogram(sift));

	level   = 0;
}

Comparison::~Comparison(void)
{
}

void Comparison::read(string *filename1, string *filename2)
{
	try
	{
		VerboseOutput::println("Comparison", "reading data from file 1: '" + *filename1 + "'");

		FileStorage fs1(*filename1, FileStorage::READ);
		FileNode features1 = fs1.root();		

		for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
		{	
			FileNode node = *it;

			Feature* task = TaskFactory::createTask(node,level);
			tasksFromXML1.push_back(task);
		}

		fs1.release();

		VerboseOutput::println(string("Comparison"), "reading data from file 2: '" + *filename2 + "'");

		FileStorage fs2(*filename2, FileStorage::READ);
		FileNode features2 = fs2.root();

		for( FileNodeIterator it = features2.begin() ; it != features2.end(); ++it )
		{			
			Feature* task = TaskFactory::createTask(*it, level);
			tasksFromXML2.push_back(task);
		}

		fs2.release();

		VerboseOutput::println(string("Comparison"), string("reading data complete"));

	}
	catch (exception& e)
	{
		throw e;
	}
}

void Comparison::level3(string *filename1, string *filename2)
{
	
	Mat img1 = imread(const_cast<char*>(filename1->c_str()), CV_LOAD_IMAGE_COLOR);
	PixelwiseComparison* pwComp1 = new PixelwiseComparison();
	pwComp1->execute(img1);
	tasksFromXML1.push_back(pwComp1);

	Mat img2 = imread(const_cast<char*>(filename2->c_str()), CV_LOAD_IMAGE_COLOR);
	PixelwiseComparison* pwComp2 = new PixelwiseComparison();
	pwComp2->execute(img2);
	tasksFromXML2.push_back(pwComp2);
}

void Comparison::execute()
{
	VerboseOutput::println(string("Comparison"), string("start comparison"));

	double sumDistance = 0;

	int size = tasksFromXML1.size();

	list<Feature*>::iterator i;

	for(i=tasksFromXML1.begin(); i != tasksFromXML1.end(); ++i)
	{
		Feature* task1 = *i;

		if (!canExecute(task1))
		{
			continue;
		}

		list<Feature*>::iterator j;

		for(j=tasksFromXML2.begin(); j != tasksFromXML2.end(); ++j)
		{
			Feature* task2 = *j;

			if(task1->getName().compare(task2->getName()) == 0)
			{
				task1->compare(task2);
			}
		}
	}
}


void Comparison::addCommandLineArgs(TCLAP::CmdLine *cmd)
{
	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;

		list<TCLAP::Arg*>* args = task->getComparisonCommandlineArguments();

		list<TCLAP::Arg*>::iterator j;

		for(j=args->begin(); j != args->end(); ++j)
		{
			TCLAP::Arg* arg = *j;
			cmd->add(arg);
		}
	}
}

void Comparison::parseCommandLineArgs()
{
	VerboseOutput::println(string("Comparison"), string("parsing commandline arguments"));

	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;
		task->parseCommandlineArguments();
	}

	setTasksCmdArgs(&tasksFromXML1);
	setTasksCmdArgs(&tasksFromXML2);
}

void Comparison::setTasksCmdArgs(list<Feature*>* tsks)
{
	list<Feature*>::iterator i;

	for(i=tsks->begin(); i != tsks->end(); ++i)
	{
		Feature* xmltask = *i;

		list<Feature*>::iterator j;

		for(j=tasks.begin(); j != tasks.end(); ++j)
		{
			Feature* task = *j;
			
			if (task->getName().compare(xmltask->getName()) == 0)
			{
				xmltask->setCmdlineArguments(task->getCmdlineArguments());
			}			
		}
	}
}

void Comparison::writeOutput(void)
{
	printf("<?xml version=\"1.0\"?>\n");
	printf("<comparison>\n");

	list<Feature*>::iterator i;

	for(i=tasksFromXML1.begin(); i != tasksFromXML1.end(); ++i)
	{
		Feature* task = *i;

			printf("<%s %s=\"%d\" %s=\"%s\">\n",Feature::TAG_NAME.c_str(), Feature::ATTR_LEVEL.c_str(), task->getLevel(), Feature::ATTR_NAME.c_str(), task->getName().c_str());
			task->printXML();
			printf("</%s>\n",Feature::TAG_NAME.c_str());

	}

	printf("</comparison>\n");
}

void Comparison::setLevel( int& _level )
{
	level = _level;
}

bool Comparison::canExecute( Feature* task )
{
	if (level > 0)
	{
		if (task->getLevel() != level)
		{
			return false;
		}
	}

	return true;
}
