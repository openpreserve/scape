#include "Characterization.h"

TCLAP::ValueArg<int>    argLevel("l","level","Characterization Level (1-3)"   , false, 0, "int");
TCLAP::MultiArg<string> argNO   ("n","no"   ,"exclude feature from extraction", false,    "string");
TCLAP::MultiArg<string> argOnly ("o","only" ,"extract only specified features", false,    "string");

Characterization::Characterization(void)
{
}

Characterization::~Characterization(void)
{
}

//FileStorage* Characterization::openFilestorage() 
//{
//	FileStorage* fs;
//
//	// create output filename
//	stringstream ssStreamFilename;
//	ssStreamFilename << filename->c_str() << ".feat.xml";
//
//	// load data from existing file
//	if (appendToFile || updateFile)
//	{
//		// check if this file exists
//		ifstream ifile(ssStreamFilename.str().c_str());
//
//		if (ifile)
//		{
//			VerboseOutput::println(string("Characterization"), "open filestorage to read data: '" + ssStreamFilename.str(), verbose);
//
//			// reading existing features
//			try
//			{
//				VerboseOutput::println(string("Characterization"), "Reading existing data from file", verbose);
//
//				//fs = new FileStorage(ssStreamFilename.str().c_str() , FileStorage::READ);
//				loadFeatures();
//				//fs->release();
//			}
//			catch (Exception& e)
//			{
//				stringstream msg;
//				msg << "Error while reading Feature file " << ssStreamFilename.str() << ": ";
//
//				// TODO: use StringUtils::removeSourceFromExceptionMessage() instead
//				vector<string> items = StringUtils::split(e.msg, ':');
//				for (int i = 1; i < items.size(); i++)
//				{
//					msg << items.at(i);
//				}
//
//				throw runtime_error(msg.str());
//			}
//		}
//		else
//		{
//			stringstream msg;
//			msg << "Feature file '" << ssStreamFilename.str() << "' not found. Cannot append new data!";
//			throw runtime_error(msg.str());
//		}
//
//		ifile.close();
//	}
//
//	if (appendToFile)
//	{
//
//		try
//		{
//			VerboseOutput::println(string("Characterization"), "open filestorage to append data: '" + ssStreamFilename.str(), verbose);
//			fs = new FileStorage(ssStreamFilename.str().c_str() , FileStorage::APPEND);
//		}
//		catch (Exception& e)
//		{
//			stringstream msg;
//			msg << "Can not open feature file '" << ssStreamFilename.str() << "': ";
//
//			// TODO: use StringUtils::removeSourceFromExceptionMessage() instead
//			vector<string> items = StringUtils::split(e.msg, ':');
//			for (int i = 1; i < items.size(); i++)
//			{
//				msg << items.at(i);
//			}
//			throw runtime_error(msg.str());
//		}
//	}
//	else
//	{
//		try
//		{
//			VerboseOutput::println(string("Characterization"), string("open new filestorage"), verbose);
//			fs = new FileStorage(ssStreamFilename.str().c_str() , FileStorage::WRITE);
//		}
//		catch (Exception& e)
//		{
//			stringstream msg;
//			msg << "Can not open feature file '" << ssStreamFilename.str() << "': ";
//
//			// TODO: use StringUtils::removeSourceFromExceptionMessage() instead
//			vector<string> items = StringUtils::split(e.msg, ':');
//			for (int i = 1; i < items.size(); i++)
//			{
//				msg << items.at(i);
//			}
//			throw runtime_error(msg.str());
//		}
//	}		
//	
//	return fs;
//}

void Characterization::addTask(Feature* task)
{
	tasks.push_back(task);
}

void Characterization::execute()
{
	
	Mat img;

	// load image
	try
	{
		img = imread(const_cast<char*>(filename->c_str()), CV_LOAD_IMAGE_COLOR);
	}
	catch (Exception& e)
	{
		throw runtime_error("Error while reading image");
	}

	// check image
	if ((img.rows == 0) || (img.cols == 0))
	{
		throw runtime_error("Corrupt Image");
	}

	// execute feature extraction
	try
	{
		list<Feature*>::iterator i;

		// iterate through all tasks
		for(i=tasks.begin(); i != tasks.end(); ++i)
		{
			Feature* task = *i;

			if(canExecute(task))
			{
				task->execute(img);
				task->persist(featureFileOutputDirectory);
			}
		}
	}
	catch (Exception& ex)
	{
		stringstream msg;
		msg << "Error while executing feature extraction: " << ex.msg;
		throw runtime_error(msg.str());
	}
}

void Characterization::writeOutput(void)
{
}

bool Characterization::canExecute(Feature* task)
{
	// is argOnly set?
	if (argOnly.getValue().size() > 0)
	{
		vector<string> onlies = argOnly.getValue();
		vector<string>::iterator i;

		for(i=onlies.begin(); i != onlies.end(); ++i)
		{
			string onlyFeat = *i;

			if (onlyFeat.compare(task->getName()) == 0)
			{
				VerboseOutput::println(string("Characterization"), "option --only: extract feature only '" + onlyFeat + "'");
				return true;
			}
		}
		return false;
	}

	bool result = true;

	// check if a certain level is requested
	if( (task->getLevel() == argLevel.getValue()) ||
		((argLevel.getValue() == 0) && (task->getLevel() < 4)) ) 
	{
		// check if certain features should be skipped
		vector<string> nos = argNO.getValue();
		
		vector<string>::iterator i;

		for(i=nos.begin(); i != nos.end(); ++i)
		{
			string no = *i;

			if (no.compare(task->getName()) == 0)
			{
				result = false;
			}
		}
	}
	else
	{
		result = false;
	}

	return result;
}

void Characterization::addCommandLineArgs(TCLAP::CmdLine *cmd)
{
	cmd->add(argLevel);
	cmd->add(argNO);
	cmd->add(argOnly);

	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;

		list<TCLAP::Arg*>* args = task->getCharacterizationCommandlineArguments();

		list<TCLAP::Arg*>::iterator j;

		for(j=args->begin(); j != args->end(); ++j)
		{
			TCLAP::Arg* arg = *j;
			cmd->add(arg);
		}
	}
}

void Characterization::parseCommandLineArgs()
{
	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;
		task->setFilename(filename);
		task->parseCommandlineArguments();
	}
}

void Characterization::setFilename(string* fname)
{
	filename = fname;
}

void Characterization::loadFeatures()
{
	TaskFactory::loadData(tasks);
}

void Characterization::setFeatureFileOutputDirectory( string& dir )
{
	list<Feature*>::iterator i;

	for(i=tasks.begin(); i != tasks.end(); ++i)
	{
		Feature* task = *i;
		task->setFeatureFilesDirectory(dir);
	}
}

