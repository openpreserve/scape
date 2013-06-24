#include "VerboseOutput.h"
#include "boost/date_time/posix_time/posix_time.hpp"

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
	//time_t ltime;
	//struct tm *Tm;

	//ltime=time(NULL);
	//Tm=localtime(&ltime);

	boost::posix_time::ptime now  = boost::posix_time::microsec_clock::local_time();

	stringstream sStream;
	//sStream  << " " << Tm->tm_hour  << ":" << Tm->tm_min  << ":" << Tm->tm_sec ;
	sStream  << " " << now ;

	return sStream.str();
}
