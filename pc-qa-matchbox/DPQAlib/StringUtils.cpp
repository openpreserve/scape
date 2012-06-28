#include "StringUtils.h"

StringUtils::StringUtils(void)
{
}

StringUtils::~StringUtils(void)
{
}

vector<string> StringUtils::split(string &s, char delim )
{
	vector<string> elems;
	stringstream   ss(s);
	string         item;

	while(getline(ss, item, delim)) {
		elems.push_back(item);
	}

	return elems;
}

cv::string StringUtils::getFilename( string fullpath )
{
	size_t pos;
	string file;

	// check path seperator
	if (fullpath.find("\\") != -1)
	{
		// windows path seperator
		pos = fullpath.find_last_of("\\");
	}
	else
	{
		// unix path seperator
		pos = fullpath.find_last_of("/");
	}

	if(pos != string::npos)
	{
		file = fullpath.substr(pos + 1, fullpath.length());
	}
	else
	{
		file = fullpath;
	}

	return file;
}

string removeSourceFromExceptionMessage( string msg ) 
{
	stringstream result;

	vector<string> elems;
	stringstream   ss(msg);
	string         item;

	while(getline(ss, item, ':')) {
		elems.push_back(item);
	}

	for (int i = 1; i < elems.size(); i++)
	{
		result << elems.at(i);
	}

	return result.str();
}