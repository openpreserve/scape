
#include <stdio.h>
#include <string>

#include <sys/types.h>
#include <errno.h>
#include <dirent.h>
#include <vector>
#include <iostream>

#include <cv.h>
#include "opencv2/nonfree/features2d.hpp"

#include "SIFTComparison.h"
#include "VisualBOW.h"

#include <tclap/CmdLine.h>
#include "VerboseOutput.h"

using namespace cv;
using namespace std;

bool verbose = false;

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

void getDescriptorsFromFile( string& dirName, string& filename, BOWKMeansTrainer& bow) 
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

			VerboseOutput::println(string("train"), string("processing file '" + filePath + "'"), verbose);

			for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
			{
				FileNode node = *it;

				if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
				{
					VerboseOutput::println(string("train"), string("loading descriptors"), verbose);

					SIFTComparison* sComp = new SIFTComparison();
					sComp->readData(node);
					bow.add(sComp->getDescriptors());
					sComp->~SIFTComparison();
				}
			}
		}
		catch (exception& e)
		{
			VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "), verbose);
			cout << e.what();
		}
		catch (cv::Exception& e)
		{
			VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "), verbose);
			cout << e.what();
		}

		if (fs1.isOpened())
		{
			fs1.release();
		}
	}
	catch(exception& e)
	{
		VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "), verbose);
		cout << e.what();
	}
	catch (cv::Exception& e)
	{
		VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "), verbose);
		cout << e.what();
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

			TCLAP::ValueArg<std::string> argFilter("f","filter","Filter files according to pattern",false,".feat.xml","string");
			cmd.add( argFilter );

			TCLAP::SwitchArg argVerbose("v","verbose","Provide additional debugging output",false);
			cmd.add( argVerbose );

			TCLAP::UnlabeledMultiArg<std::string> dirListArg("directories", "Directory containing files with extracted features",true,"directories",false,0);
			cmd.add( dirListArg );

		// parse arguments
			cmd.parse( argc, argv );

			verbose = argVerbose.getValue();
			outputFilename = argOutputFile.getValue();

			Ptr<DescriptorExtractor> extractor = new SiftFeatureDetector();

			vector<string>  dirNames = dirListArg.getValue();

			BOWKMeansTrainer bowtrainer(1000); 

			for (vector<string>::iterator it = dirNames.begin(); it!=dirNames.end(); ++it)
			{
				string dirName = *it;
				VerboseOutput::println(string("train"), string("reading features of directory '" + dirName + "'"), verbose);

				vector<string> files = getdir(dirName, argFilter.getValue());

				int numFiles = files.size();
				int i = 1;

				for (vector<string>::iterator it2 = files.begin(); it2!=files.end(); ++it2)
				{
					string filename = *it2;

					if (verbose)
					{
						stringstream ss;
						ss << "[" << i++ << " of " << numFiles << " in directory '" << dirName << "']";
						VerboseOutput::println(string("train"), ss.str() , verbose);
					}
					
					getDescriptorsFromFile(dirName, filename, bowtrainer);
				}
			}

			// descriptors loaded ==> build BOW
			VerboseOutput::println(string("train"), string("Calculating Visual Bag of Words"), verbose);

			// calculate vocabulary
			VerboseOutput::println(string("train"), string("Creating Vocabulary"), verbose);
			Mat vocab = bowtrainer.cluster();

			// output results to file
			VerboseOutput::println(string("train"), string("Storing BoW to File"), verbose);
			writeVocabularyToFile(vocab, outputFilename);

			VerboseOutput::println(string("train"), string("Finished"), verbose);
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