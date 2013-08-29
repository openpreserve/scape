#include "Feature.h"


const string Feature::ATTR_LEVEL   = "level";
const string Feature::ATTR_NAME    = "name";
const string Feature::TAG_NAME     = "task";


Feature::Feature(void)
{
}

Feature::~Feature(void)
{
	list<OutputParameter>::iterator i;

	for(i=outputParams.begin(); i != outputParams.end(); ++i)
	{
		OutputParameter param = *i;
	}
}

void Feature::addOutputParameter(OutputParameter param)
{
	outputParams.push_back(param);
}

list<OutputParameter> *Feature::getOutputParameters()
{
	return &outputParams;
}

void Feature::printXML(void)
{
	list<OutputParameter>::iterator i;
	
	for(i=outputParams.begin(); i != outputParams.end(); ++i)
	{
		OutputParameter param = *i;
		
		std::string name = *param.getName();
		std::string data = *param.getData();

		printf("  <%s",name.c_str());

		if (param.getOutputAttributes().size() > 0)
		{
			list<OutputAttribute> attrs = param.getOutputAttributes();
			list<OutputAttribute>::iterator j;

			for(j=attrs.begin(); j != attrs.end(); ++j)
			{
				OutputAttribute attr = *j;
				printf(" %s=\"%s\"",attr.getName()->c_str(),attr.getValue()->c_str());
			}
		}
		
		printf(">%s</%s>\n",data.c_str(),name.c_str());
	}
}

string Feature::getName(void)
{
	return name;
}

int Feature::getLevel(void)
{
	return level;
}

void Feature::addCharacterizationCommandlineArgument(TCLAP::Arg* arg)
{
	characterizationArguments.push_back(arg);
}

void Feature::addComparisonCommandlineArgument(TCLAP::Arg* arg)
{
	comparisonArguments.push_back(arg);
}

list<TCLAP::Arg*>* Feature::getCharacterizationCommandlineArguments(void)
{
	return &characterizationArguments;
}

list<TCLAP::Arg*>* Feature::getComparisonCommandlineArguments(void)
{
	return &comparisonArguments;
}

void Feature::verbosePrintln(string msg)
{
	VerboseOutput::println(name, msg);
}

void Feature::verboseError(string msg)
{
	VerboseOutput::printError(name, msg);
}

void Feature::setFilename( string* fname )
{
	filename = *fname;
}

string Feature::getFilename()
{
	return filename;
}

void Feature::persist(string featureFileOutputDirectory)
{
	try
	{
		string filepath = getFilepath(".feat.xml.gz");

		// open filestorage for writing
		FileStorage* fs = new FileStorage(filepath.c_str() , FileStorage::WRITE);

		// call child implementation to write data to file
		writeOutput(*fs);

		// close filestorage
		fs->release();
	}
	catch (Exception& ex)
	{
		stringstream msg;
		msg << "Error while persisting feature data: " << ex.msg;
		throw runtime_error(msg.str());
	}
}

void Feature::loadData()
{
	verbosePrintln("reading data");

	string filepath = getFilepath(".feat.xml.gz");

	// open filestorage for reading
	FileStorage* fs = new FileStorage(filepath.c_str() , FileStorage::READ);

	FileNode features1 = fs->root();
	for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
	{	
		FileNode node = *it;
		string name = node.name();

		if (name.compare(name) == 0)
		{
			readData(node);
		}
	}

}

void Feature::loadData( FileNode node )
{
	verbosePrintln("reading data");
	readData(node);
}

string Feature::getFilepath(string extension)
{
	// create output filename
	stringstream ssStreamFilename;

	if (directory.length() > 0)
	{
		string file = StringUtils::getFilename(filename);

		stringstream msg;
		msg << "Reading data from feature file directory: " << file;
		
		
		verbosePrintln(msg.str());


		ssStreamFilename << directory.c_str() << "/" << file.c_str() << "." << name.c_str() << extension;
	}
	else
	{
		ssStreamFilename << filename.c_str() << "." << name.c_str() << extension;
	}

	return ssStreamFilename.str();
}

void Feature::setFeatureFilesDirectory( string dir )
{
	directory = dir;
}

