#include "Mat2DOutputParameter.h"

Mat2DOutputParameter::Mat2DOutputParameter(string nme):OutputParameter(nme)
{
}

Mat2DOutputParameter::~Mat2DOutputParameter(void)
{
}

void Mat2DOutputParameter::setData(Mat m)
{
	stringstream ssStream;

	for(int i = 0; i < m.rows; ++i)
	{
		ssStream << "<mat row=\"" << i << "\">";

		float binVal = m.at<float>(i,0);

		ssStream << binVal;

		for( int j = 1; j < m.cols; j++ )
		{
			binVal = m.at<float>(i,j);
			ssStream << ", " << binVal;
		}
		
		ssStream << "</mat>\n";
	}

	OutputParameter::setData(ssStream.str());
}
