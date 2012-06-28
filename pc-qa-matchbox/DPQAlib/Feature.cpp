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
		string filepath = getFilepath(featureFileOutputDirectory);

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

void Feature::loadData( void )
{
	string filepath = getFilepath("");

	// check if this file exists
	ifstream ifile(filepath.c_str());

	if (ifile)
	{
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

	ifile.close();
}



string Feature::getFilepath(string featureFileOutputDirectory)
{
	// create output filename
	stringstream ssStreamFilename;

	if (featureFileOutputDirectory.length() > 0)
	{
		string file = StringUtils::getFilename(filename);
		ssStreamFilename << featureFileOutputDirectory.c_str() << "/" << file.c_str() << "." << name.c_str() <<  ".feat.xml.gz";
	}
	else
	{
		ssStreamFilename << filename.c_str() << "." << name.c_str() <<  ".feat.xml.gz";
	}

	return ssStreamFilename.str();
}

