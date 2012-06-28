
#include <stdio.h>
#include <string>

#include "ImageMetadata.h"
#include "ImageHistogram.h"
#include "ImageProfile.h"
#include "Characterization.h"
#include "Comparison.h"
#include "SIFTComparison.h"
#include "BOWHistogram.h"

#include <tclap/CmdLine.h>

using namespace std;

int main(int argc, char* argv[])
{
	int exitcode = 0;

	try
	{  
		// init command line parser
			TCLAP::CmdLine cmd("Command description message", ' ', "0.9");

			TCLAP::ValueArg<string> argOutput("d","dir","Output directory for feature files.",false,"","string");
			cmd.add( argOutput );

			TCLAP::SwitchArg argVerbose("v","verbose","Provide additional debugging output",false);
			cmd.add( argVerbose );

			TCLAP::UnlabeledValueArg<std::string> file1Arg("file", "image file to extract features from", true, "", "file", false);
			cmd.add( file1Arg );

		// init characterization
			Characterization* c     = new Characterization();
	
		// add tasks
			ImageMetadata* metadata = new ImageMetadata();
			c->addTask(metadata); 

			ImageHistogram* hist    = new ImageHistogram();
			c->addTask(hist);

			ImageProfile* profile   = new ImageProfile();
			c->addTask(profile);

			SIFTComparison* sift    = new SIFTComparison();
			c->addTask(sift);

			BOWHistogram* bowHist   = new BOWHistogram(sift);
			c->addTask(bowHist);

		// add task command line parameters
			c->addCommandLineArgs(&cmd);

		// parse arguments
			cmd.parse( argc, argv );

			// enable/disable verbose output
			VerboseOutput::verbose = argVerbose.getValue();

			c->setFilename(&file1Arg.getValue());
			c->setFeatureFileOutputDirectory(argOutput.getValue());

			c->parseCommandLineArgs();

		// execute characterization
			c->execute();
		
	}
	catch (TCLAP::ArgException &e)
	{
		cerr << "*** ERROR : Exception in Argument Handling" << endl;
		cerr << "    Reason: " << e.error() << " for arg " << e.argId() << endl;
		cerr << endl;
		exitcode = 1;
	}
	catch (exception &e)  // catch any exceptions
	{
		cerr << "\n";
		cerr << "*** ERROR : Feature extraction aborted!" << endl;
		cerr << "    Reason: " << e.what() << endl;
		cerr << endl;
		exitcode = 2;
	}

	exit(exitcode);
}