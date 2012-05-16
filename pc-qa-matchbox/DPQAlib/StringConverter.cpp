#include "StringConverter.h"

StringConverter::StringConverter(void)
{
}

StringConverter::~StringConverter(void)
{
}

int StringConverter::toInt(string* str)
{
	stringstream ssStream(*str);
    int iReturn;
    ssStream >> iReturn;

	return iReturn;
}

int StringConverter::toInt(char* chr)
{
	string str = chr;
	return toInt(&str);
}

float StringConverter::toFloat(string* str)
{
	stringstream ssStream(*str);
    float iReturn;
    ssStream >> iReturn;

	return iReturn;
}

double StringConverter::toDouble(string* str)
{
	stringstream ssStream(*str);
    double iReturn;
    ssStream >> iReturn;

	return iReturn;
}

Mat StringConverter::toMat(string* str)
{
	// values have to be comma separated
	list<string>  tokens;
	istringstream iss(*str);

	copy(istream_iterator<string>(iss),
         istream_iterator<string>(),
         back_inserter<list<string> >(tokens));

	int idx = 0;
	
	CvMat* result = cvCreateMat(1,tokens.size(), CV_32F);
	cvSetZero(result);

	for (list<string>::iterator i = tokens.begin(); i != tokens.end(); ++i)
    {
		string& str = *i;
		cvmSet(result, 0, idx++, toFloat(&str));
    }
	return result;
}


string StringConverter::toString(int i)
{
	stringstream ssStream;
    ssStream << i;
	return ssStream.str();
}

string StringConverter::toString(double i)
{
	stringstream ssStream;
    ssStream << i;
	return ssStream.str();
}

string StringConverter::toString(Mat m)
{
	stringstream ssStream;
	float binVal = m.at<float>(0);

	ssStream << binVal;

	for( int i = 1; i < m.rows; i++ )
    {
        binVal = m.at<float>(i);
		ssStream << ", " << binVal;
    }

	return ssStream.str();
}

string StringConverter::toString(list<double> l)
{
	stringstream ssStream;
	
	list<double>::iterator i = l.begin();

	ssStream << *i++;

	for(i; i != l.end(); ++i)
    {
		ssStream << ", " << *i;
    }

	return ssStream.str();
}