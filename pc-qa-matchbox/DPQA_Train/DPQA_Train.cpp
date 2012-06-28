
#include <stdio.h>
#include <string>

#include <sys/types.h>
#include <errno.h>
#include <dirent.h>
#include <vector>
#include <iostream>

#include "opencv/cv.h"
#include "opencv2/nonfree/features2d.hpp"

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

					if (clusterCenters > 0)
					{
						VerboseOutput::println(string("train"), string("preclustering"));
						sComp->precluster(clusterCenters);
					}
					bow.add(sComp->getDescriptors());
					sComp->~SIFTComparison();
				}
			}
		}
		catch (exception& e)
		{
			VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "));
			cout << e.what();
		}
		catch (cv::Exception& e)
		{
			VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "));
			cout << e.what();
		}

		if (fs1.isOpened())
		{
			fs1.release();
		}
	}
	catch(exception& e)
	{
		VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "));
		cout << e.what();
	}
	catch (cv::Exception& e)
	{
		VerboseOutput::println(string("train"), string("*** ERROR loading descriptors: "));
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