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