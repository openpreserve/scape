#include "Feature.h"

const string Feature::ATTR_LEVEL   = "level";
const string Feature::ATTR_NAME    = "name";
const string Feature::TAG_NAME     = "task";


Feature::Feature(void)
{
	verbose = false;
	dataLoaded = false;
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

void Feature::testOutput(void)
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

void Feature::setVerbose(bool v)
{
	verbose = v;
}

void Feature::verbosePrintln(string msg)
{
	VerboseOutput::println(name, msg, verbose);
}

void Feature::setFilename( string* fname )
{
	filename = *fname;
}

string Feature::getFilename()
{
	return filename;
}

bool Feature::hasDataLoaded( void )
{
	return dataLoaded;
}

void Feature::setDataLoaded( bool param1 )
{
	dataLoaded = param1;
}
