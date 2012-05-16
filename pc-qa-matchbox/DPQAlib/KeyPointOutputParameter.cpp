#include "KeyPointOutputParameter.h"

KeyPointOutputParameter::KeyPointOutputParameter(string name):OutputParameter(name)
{
}

KeyPointOutputParameter::~KeyPointOutputParameter(void)
{
}

void KeyPointOutputParameter::setData(vector<KeyPoint> keypoints)
{

	stringstream ssStream;

	ssStream << "\n";

	for(int i = 0; i < keypoints.size(); ++i)
	{
		ssStream << "<keypoint row=\"" << i << "\"";
		
		KeyPoint kp = keypoints[i];
		
		ssStream << " angle=\"" << kp.angle << "\"";
		//ssStream << " class_id=\"" << kp.class_id << "\" ";
		ssStream << " octave=\"" << kp.octave << "\"";
		ssStream << " locX=\"" << kp.pt.x << "\"";
		ssStream << " locY=\"" << kp.pt.y << "\"";
		ssStream << " response=\"" << kp.response << "\"";
		ssStream << " size=\"" << kp.size << "\"";
		ssStream << "/>\n";
	}

	OutputParameter::setData(ssStream.str());

}