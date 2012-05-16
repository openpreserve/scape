#include "VerboseOutput.h"


VerboseOutput::VerboseOutput(void)
{
}

VerboseOutput::~VerboseOutput(void)
{
}

void VerboseOutput::println( string name, string msg, bool verbose )
{
	if (verbose)
	{
		cout << "[" << getTimeStamp() << " " << name << "] " << msg.c_str() << "\n";
	}
}

string VerboseOutput::getTimeStamp()
{
	time_t ltime;
	struct tm *Tm;

	ltime=time(NULL);
	Tm=localtime(&ltime);

	stringstream sStream;
	sStream  << " " << Tm->tm_hour  << ":" << Tm->tm_min  << ":" << Tm->tm_sec ;

	return sStream.str();
}
