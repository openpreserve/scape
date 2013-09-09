
#include <stdio.h>
#include <string>
#include <sstream>

#include <sys/types.h>
#include <errno.h>
#include <dirent.h>
#include <vector>

#include "opencv/cv.h"
#include "opencv2/nonfree/features2d.hpp"
//#include "opencv2/imgproc/imgproc.hpp"

#include "SIFTComparison.h"

#include <tclap/CmdLine.h>
#include "VerboseOutput.h"

#include "BoostSerializer.h"

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

void loadDescriptorsFromOpenCVFilestorage( Mat& descriptors, string& filePath ) 
{
	VerboseOutput::println(string("train"), "Loading descriptors from OpenCV Filestorage");

	try
	{

		VerboseOutput::println(string("train"), "...open filestorage");
		FileStorage fs1(filePath, FileStorage::READ);
		VerboseOutput::println(string("train"), "...filestorage opened");

		try
		{	
			VerboseOutput::println(string("train"), "...read root node");
			FileNode features1 = fs1.root();

			VerboseOutput::println(string("train"), "...find SIFTComparison node");
			for( FileNodeIterator it = features1.begin() ; it != features1.end(); ++it )
			{
				FileNode node = *it;

				if (node.name().compare(SIFTComparison::TASK_NAME) == 0)
				{
					read(node["descriptors"], descriptors);
				}
			}

		}
		catch (exception& e)
		{
			stringstream ss;
			ss << "*** WARNING: failed to load descriptors from OpenCV FileStorage: " << e.what();
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
		ss << "*** WARNING: failed to close Filestorage: " << e.what();
		VerboseOutput::println(string("train"), ss.str() );
	}
}

void loadDescriptorsFromBinaryArchives( Mat& descriptors, string& filePath ) 
{
	VerboseOutput::println(string("train"), "Loading descriptors from binary archive");

	try
	{	
		std::ifstream ifs(filePath.c_str(), std::fstream::binary | std::fstream::in);
		boost::archive::binary_iarchive ia(ifs);
		ia >> descriptors;
	}
	catch (exception& e)
	{
		stringstream ss;
		ss << "*** WARNING: failed to load descriptors from binary archive: " << e.what();
		VerboseOutput::println(string("train"), ss.str() );
	}
}


int main(int argc, char* argv[])
{
	try
	{  
		// init comandline parser
			TCLAP::CmdLine cmd("Command description message", ' ', "1.0");

			TCLAP::ValueArg<string>          argOutputFile  ("o", "output",     "Output file",                                        true,  "",   "string");
			TCLAP::ValueArg<string>          argFilter      ("f", "filter",     "Filter files according to pattern",                  false, ".SIFTComparison.feat.xml.gz","string");
			TCLAP::ValueArg<int>             argPRECLUSTER  ("p", "precluster", "Number of descriptors to select in precluster-preprocessing (0 = no preclustering)",false,0   ,"int");
			TCLAP::ValueArg<int>             argBOWSIZE     ("b", "bowsize",    "Size of the BoW Dictionary",                         false, 1000, "int");
			TCLAP::SwitchArg                 argBinaryInput ("i", "binary",     "Read descriptors from binary archives",              false);
			TCLAP::SwitchArg                 argVerbose     ("v", "verbose",    "Provide additional debugging output",                false);
			TCLAP::UnlabeledValueArg<string> dirArg         (     "directory",  "Directory containing files with extracted features", true,  "directory","string");

			cmd.add( argOutputFile  );
			cmd.add( argFilter      );
			cmd.add( argPRECLUSTER  );
			cmd.add( argBOWSIZE     );
			cmd.add( argBinaryInput );
			cmd.add( argVerbose     );
			cmd.add( dirArg         );

		// parse arguments
			cmd.parse( argc, argv );

			// enable/disable verbose output
			VerboseOutput::verbose = argVerbose.getValue();

			VerboseOutput::println(string("train"), "Create BoW of size %d", argBOWSIZE.getValue());
			TermCriteria tcrit;
			tcrit.epsilon = 10;
			tcrit.maxCount = 10;
			tcrit.type = 1;

			BOWKMeansTrainer* bowtrainer = new BOWKMeansTrainer(argBOWSIZE.getValue(),tcrit,1,KMEANS_PP_CENTERS);

			VerboseOutput::println(string("train"), "Creating Visual Bag of Words");
			
			string filter = argFilter.getValue();

			if (argBinaryInput.getValue())
			{
				filter = ".SIFTComparison.descriptors.dat";
			}

			vector<string> files    = getdir(dirArg.getValue(), filter);
			int            i        = 1;

			VerboseOutput::println(string("train"), "Reading features of directory '%s'", dirArg.getValue().c_str());
			VerboseOutput::println(string("train"), "with extension '%s'", filter.c_str());

			for (vector<string>::iterator filename = files.begin(); filename!=files.end(); ++filename)
			{
				VerboseOutput::println("train", "[%i of %i in directory '%s']", i++, files.size(), dirArg.getValue().c_str());
				
				Mat          descriptors;
				stringstream filePathss;

				filePathss << dirArg.getValue() << "/" << *filename;

				string filePath = filePathss.str();
				VerboseOutput::println(string("train"), string("processing file '" + filePath + "'"));

				

				if (argBinaryInput.getValue())
				{
					loadDescriptorsFromBinaryArchives(descriptors, filePath);
				}
				else
				{
					loadDescriptorsFromOpenCVFilestorage(descriptors, filePath);
				}
				

				if ((descriptors.rows == 0) || (descriptors.cols == 0))
				{
					VerboseOutput::println(string("train"), string("No Descriptors read!"));
					continue;
				}

				VerboseOutput::println(string("train"), "%i descriptors loaded", descriptors.rows);

				if ((argPRECLUSTER.getValue() > 0) && (argPRECLUSTER.getValue() < descriptors.rows - 100))
				{
					VerboseOutput::println(string("train"), string("pre-clustering"));
					
					Mat          labels;
					Mat          centers;

					kmeans(descriptors,argPRECLUSTER.getValue(),labels,tcrit,1, KMEANS_PP_CENTERS, centers);

					VerboseOutput::println(string("train"), "...add cluster centers of pre-clustering to bow");
					bowtrainer->add(centers);
				}
				else
				{
					VerboseOutput::println(string("train"), "...add descriptors to bow");
					bowtrainer->add(descriptors);
				}

				VerboseOutput::println(string("train"), "...current bow-size: %i", bowtrainer->descripotorsCount());
			}

			// calculate vocabulary
			VerboseOutput::println(string("train"), string("Creating Vocabulary"));
			
			// check if number of descriptors is less than BoW size
			if (bowtrainer->descripotorsCount() <= argBOWSIZE.getValue())
			{
				// automatically reduce BoW size

				// reduce in 100 words intervals
				int newclusterCount = int((bowtrainer->descripotorsCount() - 1) / 100) * 100;
				VerboseOutput::println(string("train"), "***Warning: BoW size higher than number of loaded descriptors!");
				VerboseOutput::println(string("train"), "            reducing BoW size to %i", newclusterCount);

				// if target size is 0 than abort
				if (newclusterCount == 0)
				{
					throw runtime_error("Too few descriptors loaded to calculate an expressive vocabulary! Consider adding more images to the collection");
				}

				// create new trainer
				BOWKMeansTrainer* newbow = new BOWKMeansTrainer(newclusterCount,tcrit,1,KMEANS_PP_CENTERS);

				// copy data from the old trainer to the new one
				for (vector<Mat>::const_iterator descriptor = bowtrainer->getDescriptors().begin(); descriptor!=bowtrainer->getDescriptors().end(); ++descriptor)
				{
					newbow->add(*descriptor);
				}
				
				// release old one and set new reference
				bowtrainer->~BOWKMeansTrainer();
				bowtrainer = newbow;
			}
			
			Mat vocab = bowtrainer->cluster();

			// output results to file
			VerboseOutput::println(string("train"), string("Storing BoW to File"));
			writeVocabularyToFile(vocab, argOutputFile.getValue());

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
