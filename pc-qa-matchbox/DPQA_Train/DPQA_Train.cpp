
#include <stdio.h>
#include <string>
#include <sstream>
#include <map>

#include <sys/types.h>
#include <errno.h>
#include <dirent.h>
#include <vector>
#include <iostream>

#include "opencv/cv.h"
#include "opencv2/nonfree/features2d.hpp"
#include "opencv2/flann/flann.hpp"
#include "opencv/highgui.h"

#include "SIFTComparison.h"
#include "VisualBOW.h"

#include <tclap/CmdLine.h>
#include "VerboseOutput.h"

using namespace cv;
using namespace std;

vector<string> getdir(string dir, string filter)
{
    DIR *dp;
    struct dirent *dirp;
	vector<string> files = vector<string>();

    if((dp  = opendir(dir.c_str())) == NULL)
	{
        cout << "Error(" << errno << ") opening " << dir << endl;
        return files;
    }

    while ((dirp = readdir(dp)) != NULL)
	{
		string filename = string(dirp->d_name);

		if (filename.find(filter) != string::npos)
		{
			files.push_back(filename);
		}
    }
    
	closedir(dp);
    
	return files;
}

void writeVocabularyToFile( Mat vocab , string& outputFilename) 
{
	FileStorage fs(outputFilename.c_str() , FileStorage::WRITE);
	write(fs, "vocabulary", vocab);
	fs.release();
}

void drawPreclusteredKeypoints( string filename, Mat initDescriptors, Mat clusteredDescriptors, vector<KeyPoint> keypoints ) 
{
	// **************************************************
	// draw keypoints
	// **************************************************

	filename = StringUtils::getFilename(filename);

	Mat outputImage = imread("E:/test/" + filename + ".png");

	// knn search for clustered descriptors

	flann::KMeansIndexParams params;
	flann::Index flann_index(initDescriptors,params);

	Mat indices;
	Mat dists;

	for (int i = 0; i < clusteredDescriptors.rows; i++)
	{
		flann_index.knnSearch(clusteredDescriptors.row(i),indices,dists,1);
		int idx = indices.at<int>(0);

		circle(outputImage,keypoints.at(idx).pt,3,Scalar(0, 0, 255, 0),2);
	}

	vector<int> compression_params;
	compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
	compression_params.push_back(9);

	imwrite("E:/test/" + filename + ".precluster.png", outputImage, compression_params);



	// **************************************************
}

void getDescriptorsFromFile( string& dirName, string& filename, BOWKMeansTrainer& bow, int clusterCenters) 
{
	try
	{		
		stringstream ssStream;
		ssStream << dirName << "/" << filename;
		string filePath = ssStream.str();

		FileStorage fs1(filePath, FileStorage::READ);

		try
		{
			FileNode features1 = fs1.root();

			VerboseOutput::println(string("train"), string("processing file '" + filePath + "'"));

			for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
			{
				FileNode node = *it;

				if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
				{
					SIFTComparison* sComp = new SIFTComparison();
					sComp->readData(node);

					VerboseOutput::println(string("train"), "%i descriptors loaded", sComp->getDescriptors().rows);

					// used for drawing output
					Mat initDescriptors = sComp->getDescriptors().clone();

					if (clusterCenters > 0)
					{
						VerboseOutput::println(string("train"), string("preclustering"));
						sComp->precluster(clusterCenters);

						//drawPreclusteredKeypoints(sComp->getFilename(), initDescriptors, sComp->getDescriptors(), sComp->getKeypoints());
					}

					

					bow.add(sComp->getDescriptors());
					sComp->~SIFTComparison();
				}
			}
		}
		catch (exception& e)
		{
			stringstream ss;
			ss << "*** WARNING: failed to load descriptors: " << e.what();
			VerboseOutput::println(string("train"), ss.str() );
		}
		catch (cv::Exception& e)
		{
			stringstream ss;
			ss << "*** WARNING: failed to load descriptors: " << e.msg;
			VerboseOutput::println(string("train"), ss.str() );
		}

		if (fs1.isOpened())
		{
			fs1.release();
		}
	}
	catch(exception& e)
	{
		stringstream ss;
		ss << "*** WARNING: failed to load descriptors: " << e.what();
		VerboseOutput::println(string("train"), ss.str() );
	}
	catch (cv::Exception& e)
	{
		stringstream ss;
		ss << "*** WARNING: failed to load descriptors: " << e.msg;
		VerboseOutput::println(string("train"), ss.str() );
	}
}

Mat drawKeypointsForBoWIndex( string maxFilename, string dirName, Mat &vocab, int i );


void drawBowDescriptors( vector<string> dirNames, Mat vocab ) 
{
	Mat response_hist = Mat( 1, vocab.rows, CV_32FC1, Scalar::all(0.0) );
	int descCount = 0;

	vector<list<KeyPoint>> bowDist(vocab.rows);
	vector<map<string,int>> bowExamples(vocab.rows);
	int maxWidth = 0;
	int maxHeight = 0;

	string dirName; 

	Mat sumMat;
	list<string> filenames;

	for (vector<string>::iterator it = dirNames.begin(); it!=dirNames.end(); ++it)
	{
		dirName = *it;
		VerboseOutput::println(string("train"), "reading features of directory '%s'", dirName.c_str());

		vector<string> files = getdir(dirName, ".SIFTComparison.feat.xml.gz");

		int numFiles = files.size();
		int i = 1;

		sumMat = Mat::zeros(files.size(), vocab.rows, CV_16U);
		int fileID = 0;

		

		for (vector<string>::iterator it2 = files.begin(); it2!=files.end(); ++it2)
		{
			string filename = *it2;

			VerboseOutput::println("train", "[%i of %i in directory '%s']", i++, numFiles, dirName.c_str());
			stringstream ssStream;
			ssStream << dirName << "/" << filename;
			string filePath = ssStream.str();

			FileStorage fs1(filePath, FileStorage::READ);

			stringstream tmp;
			tmp << fileID << " - " << filePath;

			filenames.push_back(tmp.str());

			FileNode features1 = fs1.root();

			VerboseOutput::println(string("train"), string("processing file '" + filePath + "'"));

			for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
			{
				FileNode node = *it;

				if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
				{
					SIFTComparison* sComp = new SIFTComparison();
					sComp->readData(node);

					VerboseOutput::println(string("train"), "%i descriptors loaded", sComp->getDescriptors().rows);

					// knn search for clustered descriptors
					// calculate descriptors
					Ptr<DescriptorMatcher> dmatcher(new FlannBasedMatcher());

					int clusterCount = vocab.rows;

					// Match keypoint descriptors to cluster center (to vocabulary)
					vector<DMatch> matches;
					dmatcher->clear();
					dmatcher->add( vector<Mat>(1, vocab) );
					dmatcher->match( sComp->getDescriptors(), matches );

					// Compute image descriptor
					
					float *dptr = (float*)response_hist.data;
					for( size_t i = 0; i < matches.size(); i++ )
					{
						int trainIdx = matches[i].queryIdx;
						int bowIdx = matches[i].trainIdx; // cluster index
						
						dptr[bowIdx] = dptr[bowIdx] + 1.f;
						bowDist[bowIdx].push_back(sComp->getKeypoints().at(trainIdx));

						sumMat.at<UINT16>(fileID,bowIdx)++;

						map<string,int>::iterator m_it = bowExamples[bowIdx].find(filename);
						if (m_it != bowExamples[bowIdx].end())
						{
							m_it->second++;
						}
						else
						{
							bowExamples[bowIdx].insert(pair<string, int>(filename, 1));
						}

						descCount++;
					}

					sComp->~SIFTComparison();
				}
			}
			fileID++;
		}
	}

	cout << sumMat << endl;

	for (list<string>::iterator it = filenames.begin(); it != filenames.end(); it++)
	{
		cout << (*it) << endl;
	}


	response_hist /= descCount;
	
	double maxVal = 0;
	minMaxLoc(response_hist, NULL, &maxVal);

	response_hist *= ((float)1 / maxVal);

	for (vector<string>::iterator it = dirNames.begin(); it!=dirNames.end(); ++it)
	{
		string dirName = *it;
		VerboseOutput::println(string("train"), "reading features of directory '%s'", dirName.c_str());

		vector<string> files = getdir(dirName, ".SIFTComparison.feat.xml.gz");

		int numFiles = files.size();
		int i = 1;

		for (vector<string>::iterator it2 = files.begin(); it2!=files.end(); ++it2)
		{
			string filename = *it2;

			VerboseOutput::println("train", "[%i of %i in directory '%s']", i++, numFiles, dirName.c_str());
			stringstream ssStream;
			ssStream << dirName << "/" << filename;
			string filePath = ssStream.str();

			FileStorage fs1(filePath, FileStorage::READ);

			FileNode features1 = fs1.root();

			VerboseOutput::println(string("train"), string("processing file '" + filePath + "'"));

			for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
			{
				FileNode node = *it;

				if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
				{
					SIFTComparison* sComp = new SIFTComparison();
					sComp->readData(node);

					VerboseOutput::println(string("train"), "%i descriptors loaded", sComp->getDescriptors().rows);

					// used for drawing output


					filename = StringUtils::getFilename(sComp->getFilename());

					Mat outputImage = imread("E:/test/" + filename + ".precluster.png");

					if (outputImage.rows > maxHeight)
					{
						maxHeight = outputImage.rows;
					}

					if (outputImage.cols > maxWidth)
					{
						maxWidth = outputImage.cols;
					}

					// knn search for clustered descriptors
					// calculate descriptors
					Ptr<DescriptorMatcher> dmatcher(new FlannBasedMatcher());

					int clusterCount = vocab.rows;


					// Match keypoint descriptors to cluster center (to vocabulary)
					vector<DMatch> matches;
					dmatcher->clear();
					dmatcher->add( vector<Mat>(1, vocab) );
					dmatcher->match( sComp->getDescriptors(), matches );

					// Compute image descriptor

					float *dptr = (float*)response_hist.data;
					for( size_t i = 0; i < matches.size(); i++ )
					{
						int trainIdx = matches[i].queryIdx;
						int bowIdx = matches[i].trainIdx; // cluster index
						

						//float p = ((float)bowIdx / (float)vocab.rows);
						float colo = (float)255 * ((float)1 - dptr[bowIdx]);
						circle(outputImage,sComp->getKeypoints().at(trainIdx).pt,4,Scalar(255, (int)colo, 0, 0),3);

						stringstream ss;
						ss << bowIdx;
						putText(outputImage, ss.str().c_str(), sComp->getKeypoints().at(trainIdx).pt, FONT_HERSHEY_SIMPLEX,0.5,Scalar(255, (int)colo, 0, 0),1);
						
						//cout << bowIdx << endl;
						
						
						
					}

					vector<int> compression_params;
					compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
					compression_params.push_back(9);

					imwrite("E:/test/" + filename + ".bow.png", outputImage, compression_params);

					sComp->~SIFTComparison();
				}
			}
		}
	}

	for (int i = 0; i < vocab.rows; i++)
	{
		Mat outputImage(maxHeight,maxWidth + int(maxWidth / 5), CV_8UC3, Scalar(255,255,255));

		line(outputImage,Point(maxWidth,0), Point(maxWidth,maxHeight),Scalar(255,0,0),1);
		cout << bowDist[i].size() << endl;

		//bowExamples[bowIdx].

		for(list<KeyPoint>::iterator it = bowDist[i].begin(); it != bowDist[i].end(); it++)
		{
			KeyPoint currKP = *it;
			circle(outputImage,currKP.pt,3,Scalar(0,0, 255, 0),2);
		}

		int maxScore = 0;
		string maxFilename;
		string secFilename;
		string thirdFilename;

		map<string,int> m_m = bowExamples[i];
		map<int,string> m_m2;
		for(map<string,int>::iterator it = m_m.begin(); it != m_m.end(); it++)
		{
			m_m2.insert(pair<int,string>((*it).second, (*it).first));
		}

		int start = 0;
		int idx = 0;

		for(map<int,string>::reverse_iterator it = m_m2.rbegin(); it != m_m2.rend(); it++)
		{

			Mat ex1 = drawKeypointsForBoWIndex((*it).second, dirName, vocab, i);
			Mat roi1 = outputImage(Range(start, (start + ex1.rows)),Range((maxWidth),(maxWidth + ex1.cols)));
			ex1.copyTo(roi1);

			start += ex1.rows + 2;

			if (idx > 3)
			{
				break;
			}
			idx++;
		}

		


		vector<int> compression_params;
		compression_params.push_back(CV_IMWRITE_PNG_COMPRESSION);
		compression_params.push_back(9);

		stringstream sstream;
		sstream << "E:/test/BoWIndex_" << i << "_count_" << bowDist[i].size() << ".bowDist.png";

		imwrite(sstream.str().c_str(), outputImage, compression_params);
	}
}

int main(int argc, char* argv[])
{
	string outputFilename;

	try
	{  
		// init comandline parser
			TCLAP::CmdLine cmd("Command description message", ' ', "0.9");

			TCLAP::ValueArg<std::string> argOutputFile("o","output","Output file",true,"","string");
			cmd.add( argOutputFile );

			TCLAP::ValueArg<std::string> argFilter("f","filter","Filter files according to pattern",false,".SIFTComparison.feat.xml.gz","string");
			cmd.add( argFilter );

			TCLAP::ValueArg<int> argPRECLUSTER("p","precluster", "Number of descriptors to select in precluster-preprocessing (0 = no preclustering)",false,0   ,"int");
			cmd.add(argPRECLUSTER);

			TCLAP::ValueArg<int> argBOWSIZE("b","bowsize", "Size of the BoW Dictionary",false,1000   ,"int");
			cmd.add(argBOWSIZE);

			TCLAP::SwitchArg argVerbose("v","verbose","Provide additional debugging output",false);
			cmd.add( argVerbose );

			TCLAP::UnlabeledMultiArg<std::string> dirListArg("directories", "Directory containing files with extracted features",true,"directories",false,0);
			cmd.add( dirListArg );

		// parse arguments
			cmd.parse( argc, argv );

			// enable/disable verbose output
			VerboseOutput::verbose = argVerbose.getValue();

			outputFilename = argOutputFile.getValue();

			Ptr<DescriptorExtractor> extractor = new SiftFeatureDetector();

			vector<string>  dirNames = dirListArg.getValue();

			//VerboseOutput::println(string("train"), "Create BoW of size %d", verbose, argBOWSIZE.getValue());
			BOWKMeansTrainer bowtrainer(argBOWSIZE.getValue()); 

			for (vector<string>::iterator it = dirNames.begin(); it!=dirNames.end(); ++it)
			{
				string dirName = *it;
				VerboseOutput::println(string("train"), "reading features of directory '%s'", dirName.c_str());

				vector<string> files = getdir(dirName, argFilter.getValue());

				int numFiles = files.size();
				int i = 1;

				for (vector<string>::iterator it2 = files.begin(); it2!=files.end(); ++it2)
				{
					string filename = *it2;

					VerboseOutput::println("train", "[%i of %i in directory '%s']", i++, numFiles, dirName.c_str());
					getDescriptorsFromFile(dirName, filename, bowtrainer, argPRECLUSTER.getValue());
				}
			}

			// descriptors loaded ==> build BOW
			VerboseOutput::println(string("train"), string("Calculating Visual Bag of Words"));

			// calculate vocabulary
			VerboseOutput::println(string("train"), string("Creating Vocabulary"));
			
			if (bowtrainer.descripotorsCount() <= argBOWSIZE.getValue())
			{
				throw runtime_error("BoW size higher than number of loaded descriptors!");
			}
			
			Mat vocab = bowtrainer.cluster();

			//drawBowDescriptors(dirNames, vocab);

			// output results to file
			VerboseOutput::println(string("train"), string("Storing BoW to File"));
			writeVocabularyToFile(vocab, outputFilename);

			VerboseOutput::println(string("train"), string("Finished"));
	} 
	catch (exception &e)  // catch any exceptions
	{
		cout << "\n";
		cout << "*** Training aborted!\n";
		cout << "    Reason: " << e.what() << "\n\n";
		exit(1);
	}

	return 0;
}

Mat drawKeypointsForBoWIndex( string maxFilename, string dirName, Mat &vocab, int i )
{
	cout << "featurefile: " << maxFilename << endl;

	stringstream ssStream;
	ssStream << dirName << "/" << maxFilename;
	string filePath = ssStream.str();

	FileStorage fs1(filePath, FileStorage::READ);

	FileNode features1 = fs1.root();

	Mat example;

	for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
	{
		FileNode node = *it;

		if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
		{
			SIFTComparison* sComp = new SIFTComparison();
			sComp->readData(node);

			cout << "imagefile: " << "E:/test/" << StringUtils::getFilename(sComp->getFilename()) << ".png" << endl;
			example = imread("E:/test/" + StringUtils::getFilename(sComp->getFilename()) + ".png");

			VerboseOutput::println(string("train"), "%i descriptors loaded", sComp->getDescriptors().rows);

			// knn search for clustered descriptors
			// calculate descriptors
			Ptr<DescriptorMatcher> dmatcher(new FlannBasedMatcher());

			int clusterCount = vocab.rows;

			// Match keypoint descriptors to cluster center (to vocabulary)
			vector<DMatch> matches;
			dmatcher->clear();
			dmatcher->add( vector<Mat>(1, vocab) );
			dmatcher->match( sComp->getDescriptors(), matches );

			// Compute image descriptor

			for( size_t c_match = 0; c_match < matches.size(); c_match++ )
			{
				int trainIdx = matches[c_match].queryIdx;
				int bowIdx = matches[c_match].trainIdx; // cluster index

				if (bowIdx == i)
				{
					circle(example,sComp->getKeypoints().at(trainIdx).pt,3,Scalar(0,0, 255, 0),3);
				}
			}

			sComp->~SIFTComparison();
		}
	}

	resize(example,example,Size(int(example.cols / 5), int(example.rows/5)));

	return example;
}
