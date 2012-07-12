#include "RobustMatcher.h"

// Clear matches for which NN ratio is > than threshold
// return the number of removed points
// (corresponding entries being cleared,
// i.e. size will be 0)
int RobustMatcher::ratioTest(vector<vector<DMatch> > &matches) {

	int removed=0;

	// for all matches
	for (vector<vector<DMatch> >::iterator matchIterator = matches.begin(); matchIterator != matches.end(); ++matchIterator) {

		// if 2 NN has been identified
		if (matchIterator->size() > 1) {

			// check distance ratio
			if ((*matchIterator)[0].distance / (*matchIterator)[1].distance > ratio) {
				matchIterator->clear(); // remove match
				removed++;
			}

		} else { // does not have 2 neighbors

			matchIterator->clear(); // remove match
			removed++;
		}
	}
	return removed;
}

// Identify good matches using RANSAC
// Return fundemental matrix
Mat RobustMatcher::ransacTest(const vector<DMatch>& matches,
							  const vector<KeyPoint>& keypoints1,
							  const vector<KeyPoint>& keypoints2,
							  vector<DMatch>& outMatches) {

								  // Convert keypoints into Point2f
								  vector<Point2f> points1, points2;

								  for (vector<DMatch>::const_iterator it = matches.begin(); it != matches.end(); ++it) 
								  {	
									  // Get the position of left keypoints
									  float x = keypoints1[it->queryIdx].pt.x;
									  float y = keypoints1[it->queryIdx].pt.y;
									  points1.push_back(Point2f(x,y));

									  // Get the position of right keypoints
									  x = keypoints2[it->trainIdx].pt.x;
									  y = keypoints2[it->trainIdx].pt.y;
									  points2.push_back(Point2f(x,y));
								  }

								  // Compute F matrix using RANSAC
								  vector<uchar> inliers(points1.size(),0);

								  Mat fundemental= findFundamentalMat(
									  Mat(points1), Mat(points2), // matching points
									  inliers,                    // match status (inlier or outlier)
									  CV_FM_RANSAC,               // RANSAC method
									  distance,                   // distance to epipolar line
									  confidence);                // confidence probability

								  // extract the surviving (inliers) matches
								  vector<uchar>::const_iterator  itIn = inliers.begin();
								  vector<DMatch>::const_iterator itM  = matches.begin();

								  // for all matches
								  for ( ; itIn != inliers.end(); ++itIn, ++itM) 
								  {
									  if (*itIn) // it is a valid match
									  { 
										  outMatches.push_back(*itM);
									  }
								  }

								  if (refineF) 
								  {
									  // The F matrix will be recomputed with
									  // all accepted matches
									  // Convert keypoints into Point2f
									  // for final F computation
									  points1.clear();
									  points2.clear();

									  for (vector<DMatch>::const_iterator it = outMatches.begin(); it != outMatches.end(); ++it)
									  {
										  // Get the position of left keypoints
										  float x = keypoints1[it->queryIdx].pt.x;
										  float y = keypoints1[it->queryIdx].pt.y;
										  points1.push_back(Point2f(x,y));

										  // Get the position of right keypoints
										  x = keypoints2[it->trainIdx].pt.x;
										  y = keypoints2[it->trainIdx].pt.y;
										  points2.push_back(Point2f(x,y));
									  }

									  // Compute 8-point F from all accepted matches
									  fundemental = findFundamentalMat(Mat(points1),Mat(points2), CV_FM_8POINT);
								  }

								  return fundemental;
}

// Insert symmetrical matches in symMatches vector
void RobustMatcher::symmetryTest(const vector<vector<DMatch> >& matches1,
								 const vector<vector<DMatch> >& matches2,
								 vector<DMatch>& symMatches) {

									 // for all matches image 1 -> image 2
									 for (vector<vector<DMatch> >::const_iterator matchIterator1 = matches1.begin(); matchIterator1 != matches1.end(); ++matchIterator1) {

										 // ignore deleted matches
										 if (matchIterator1->size() < 2)
										 {
											 continue;
										 }

										 // for all matches image 2 -> image 1
										 for (vector<vector<DMatch> >::const_iterator matchIterator2 = matches2.begin(); matchIterator2 != matches2.end(); ++matchIterator2)
										 {
											 // ignore deleted matches
											 if (matchIterator2->size() < 2)
											 {
												 continue;
											 }

											 // Match symmetry test
											 if ((*matchIterator1)[0].queryIdx == (*matchIterator2)[0].trainIdx &&
												 (*matchIterator2)[0].queryIdx == (*matchIterator1)[0].trainIdx) {

													 // add symmetrical match
													 symMatches.push_back(
														 DMatch((*matchIterator1)[0].queryIdx,
														 (*matchIterator1)[0].trainIdx,
														 (*matchIterator1)[0].distance));

													 break; // next match in image 1 -> image 2
											 }
										 }
									 }
}


// Match feature points using symmetry test and RANSAC
// returns fundemental matrix
Mat RobustMatcher::match(vector<KeyPoint>& keypoints1,
						 vector<KeyPoint>& keypoints2,
						 Mat& descriptors1,
						 Mat& descriptors2,
						 vector<DMatch>& matches) {

							 // Construction of the matcher
							 FlannBasedMatcher matcher;

							 vector<DMatch> symMatches;


							 bool findMatches = true;

							 while(findMatches)
							 {
								 symMatches.clear();

								 // from image 1 to image 2
								 // based on k nearest neighbors (with k=2)
								 vector<vector<DMatch> > matches1;

								 // return 2 nearest neighbours
								 matcher.knnMatch(descriptors1,descriptors2, matches1, 2);

								 // from image 2 to image 1
								 // based on k nearest neighbours (with k=2)
								 vector<vector<DMatch> > matches2;

								 // return 2 nearest neighbours
								 matcher.knnMatch(descriptors2,descriptors1, matches2, 2);

								 
								 // 3. Remove matches for which NN ratio is > than threshold
								 // clean image 1 -> image 2 matches
								 int removed = ratioTest(matches1);

								 // clean image 2 -> image 1 matches
								 removed = ratioTest(matches2);

								 // 4. Remove non-symmetrical matches
								 symmetryTest(matches1,matches2,symMatches);

								 if (symMatches.size() > 6)
								 {
									 findMatches = false;
								 }
								 else
								 {
									 if (ratio > 1)
									 {
										 throw runtime_error("RobustMatcher cannot find enough matches to create fundamental matrix");
									 }

									 ratio += 0.01f;
								 }
							 }

							 // 5. Validate matches using RANSAC
							 Mat fundemental= ransacTest(symMatches,
								 keypoints1, keypoints2, matches);

							 // return the found fundemental matrix
							 return fundemental;
}

