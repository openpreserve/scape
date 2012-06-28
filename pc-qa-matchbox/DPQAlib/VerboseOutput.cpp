#include "VerboseOutput.h"

bool VerboseOutput::verbose = false;

VerboseOutput::VerboseOutput(void)
{
}

VerboseOutput::~VerboseOutput(void)
{
}

void VerboseOutput::println( string name, string msg, ... )
{
	if (VerboseOutput::verbose)
	{
		stringstream ss;
		ss << "[" << getTimeStamp() << " " << name << "] " << msg << "\n";

		va_list argptr;
		va_start(argptr, msg);
		vfprintf(stdout, ss.str().c_str(), argptr);
		va_end(argptr);
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
