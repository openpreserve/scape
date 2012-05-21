#include "LoweSIFTMatcher.h"



LoweSIFTMatcher::LoweSIFTMatcher(void)
{
}

LoweSIFTMatcher::~LoweSIFTMatcher(void)
{
}

void LoweSIFTMatcher::match(Mat &queryDescriptors, Mat &trainDescriptors, vector<DMatch> &matches)
{

	for( int i = 0; i < queryDescriptors.rows; i++ )
	{
		int dsq, distsq1 = 10000000, distsq2 = 10000000;
		int minRowID = 0;

		for( int j = 0; j < trainDescriptors.rows; j++ )
		{

			Mat mdiff = queryDescriptors.row(i) - trainDescriptors.row(j);
			dsq = (int) mdiff.dot(mdiff);

			if (dsq < distsq1) {
				distsq2 = distsq1;
				distsq1 = dsq;
				minRowID = j;
			} else if (dsq < distsq2) {
				distsq2 = dsq;
			}
		}

		/* Check whether closest distance is less than 0.6 of second. */
		if (10 * 10 * distsq1 < 6 * 6 * distsq2)
		{
			DMatch* dmatch = new DMatch();
			dmatch->distance = distsq1;
			dmatch->trainIdx = minRowID;
			dmatch->queryIdx = i;

			matches.push_back(*dmatch);
		}  
	}
}
