MapReduce Wrappers
==================

CLIWrapper
----------

CLIWrapper stands for "Command-Line-Interface"-Wrapper. It wraps _command lines_ for parallel execution in a MapReduce job. The wrapper only leverages the Mapper of the MapReduce-framework. Per map task the wrapper 

* fetches the specified _toolspec_ from the _toolspec repository_
* maps the _command line_ to the specified _action_ 
* copies remote input files to the local file system
* executes the _action_'s command in the local runtime environment of the system
* deposits resulting output files to a remote location 

In order to avoid data-intensive duplications and temporary files streaming of data from/to files and between executions is supported too.

### The Command Line

At least a CL consists of a pair of a _toolspec_ name and an _action_ of that toolspec. An _action_ denotes a specific shell command with placeholders for parameters, input and output files and further restrictions. 

* See the [Toolspec XML Schema draft](https://github.com/openplanets/scape/blob/master/doc/WP.02.XA.Technical.Coordination/toolspec/tool-1.0_draft.xsd) and [example toolspecs](https://github.com/openplanets/scape/tree/master/pc-as/toolspecs) for deeper understanding of toolspecs
* See [CLIWrapper input file examples](

#### Basics

Beneath the _toolspec_-_action_ pair a CL may contain additional parameters for the _action_. These are mapped to the placeholders in the definition of the _action_. Parameters are specified by a list of --{placeholder}="{value}" strings after the _toolspec_-_action_ pair. For example:

    fancy-tool do-fancy-thing --fancy-parameter="foo" --another-fancy-parameter="bar"

For this CL to function there should be a _toolspec_ named `fancy-tool` containing the _action_ `do-fancy-thing`, which should have `fancy-parameter` and `another-fancy-parameter` defined in its parameters section. An action's input and output file parameters are specified the same way. For example: 

    fancy-tool do-fancy-file-stuff --input="hdfs:///fancy-file.foo" --output="hdfs:///fancy-output-file.bar"

Again, an input parameter `input` and an output parameter `output` needs to be defined in the correspondent sections of `do-fancy-file-stuff`.

#### File redirection and piping

As an _action_'s command may be reading from standard input and/or writing to standard output, a _stdin_ and/or _stdout_ section should be defined for the _action_. From the CL's perspective these properties are mapped by the `>` character. For example:

    "hdfs:///input-file.foo" > fancy-tool do-fancy-streaming > "hdfs:///output-file.bar"

Prior to the execution of the _action_, the wrapper will start reading an input stream of `hdfs:///input-file.foo` and feeding its contents to the command of `do-fancy-streaming`. Respectively, the output is redirected to an output stream of `hdfs:///output-file.bar`.

Instead of streaming the command's output to a file, it could be streamed to another _action_ of another _toolspec_ imitating pipes in the UNIX shell. For example:

    "hdfs:///input-file.foo" > fancy-tool do-fancy-streaming | funny-tool do-funny-streaming > "hdfs:///output-file.bar"

This CL results in the output of the command of `do-fancy-streaming` being piped to the command of `do-funny-streaming`. Then the output of the latter one will be redirected to `hdfs:///output-file.bar`. 

There can be numerous pipes in one CL but only one input file at the beginning and one output file at the end for file redirection. Independently from this, the piped _toolspec_-_action_ pairs may contain parameters as explained in the previous section, ie. input and output file parameters too.

If a CL produces standard output and there is not final redirection to an output file, then the output is written to Hadoop default output file `part-r-00000`. It contains the Job's output key-value pairs. Key is the hashcode of the CL.

### Usage

Having Hadoop set up you can run CLIWrapper:

    hadoop jar {path-to-jar} 
        -i {input-file-with-command-lines} 
        -o {output-dir-for-job} 
        -r {toolspec-repo-dir}

* *path-to-jar* leads to the jar file of the SCAPE subproject _pt-mapred_ which has to be built with dependencies.
* *input-file-with-command-lines* functions like a batch file containing a list of commands line by line. 
* *output-dir-for-job* is the directory on HDFS where output files will be written to.
* *toolspec-repo-dir* is a directory on HDFS containing available Toolspecs.

### Demostration

As a proof of concept the execution of CLIWrapper on 

1. file identification 
2. streamed file identification
3. postscript to PDF migration of an input ps-file to an output pdf-file
4. streamed in postscript to PDF migration of an input ps-file to an streamed out pdf-file
5. streamed in ps-to-pdf migration with consecutive piped file identification
6. streamed in ps-to-pdf migration with two consecutive piped file identifications

is described and demonstrated in this section. The input _command line_ files only contain one command each. Of course in a productive environment one would have thousends of such command lines.

#### Prerequisites

1. Make sure the commands `file` and `ps2pdf` are in the path of your system. 
2. Copy the toolspecs [file.xml](https://github.com/openplanets/scape/tree/master/pt-mapred/src/test/resources/toolspecs/file.xml) and [ps2pdf.xml](https://github.com/openplanets/scape/tree/master/pt-mapred/src/test/resources/toolspecs/ps2pdf.xml) to a directory of your choice on HDFS (eg. `/user/you/toolspecs/`).
3. Copy [ps2pdf-input.ps](https://github.com/openplanets/scape/tree/master/pt-mapred/src/test/resources/ps2pdf-input.ps) to a directory of your choice on HDFS (eg. `/user/you/input/`).

#### File identification 

Contents of job input file (CLs):

    file identify --input="hdfs:///user/you/input/ps2pdf-input.ps"

After running the job, contents of `part-r-00000` in output directory is:

    1407062753      PostScript document text conforming DSC level 3.0, Level 2

#### Streamed file identification   

Contents of job input file (CLs):

    "hdfs:///user/you/input/ps2pdf-input.ps" > file identify-stdin 

After running the job, contents of `part-r-00000` in output directory is:

    -238455161      PostScript document text conforming DSC level 3.0, Level 2

#### Postscript to PDF migration

Contents of job input file (CLs):

    ps2pdf convert --input="hdfs:///user/you/input/ps2pdf-input.ps" --output="hdfs:///user/you/output/ps2pdf-output.pdf"

After running the job, specified output file location references the migrated PDF.

#### Streamed postscript to PDF migration

Contents of job input file (CLs):

    "hdfs:///user/you/input/ps2pdf-input.ps" > ps2pdf convert-streamed > "hdfs:///user/you/output/ps2pdf-output.pdf"

After running the job, specified output file location references the migrated PDF.

#### Streamed postscript to PDF migration with consecutive piped file identification

Contents of job input file (CLs):

    "hdfs:///user/you/input/ps2pdf-input.ps" > ps2pdf convert-streamed | file identify-stdin > "hdfs:///user/you/output/file-identified.txt" 

After running the job, contents of `file-identified.txt` in output directory is:

    PDF document, version 1.4    

#### Streamed postscript to PDF migration with two consecutive piped file identifications

Contents of job input file (CLs):

    "hdfs:///user/you/input/ps2pdf-input.ps" > ps2pdf convert-streamed | file identify-stdin | file identify-stdin

After running the job, contents of `part-r-00000` in output directory is:

    -1771972640     ASCII text

### Caveat

As the input file with the list of command-lines serves as the MR Job input MapReduce cannot distribute workload. This input file gets assigned to one worker node which reads all lines and executes them, regardless of where the inwrite put or output files reside on HDFS. Ie. HDFS input files are copied from potentially remote nodes to the local filesystem what harms locality. However, the aim should be that nodes process primarily command-lines which operate on files that already reside locally on that HDFS-node.

Executing a Taverna Workflow
----------------------------

*NOT TESTED. Maybe deprecated.*

To execute a Taverna workflow, Taverna must be installed on the cluster. The wrapper can then be used to execute a Taverna workflow in parallel. 

### Prerequisites

Taverna must be installed on the cluster. On every machine, Taverna must be in the same path. If you want to execute the example workflow, convert (ImageMagic) and FITS must be installed. Convert should be in your path if you installed ImageMagic using a package manager. fits.sh must be added to the path in order for Taverna to find/execute it.

### Execution

You execute the wrapper like any other hadoop jar. The needed arguments are as follows:

    hadoop jar {path-to-jar} -i {input-file-with-workflow-inputs} -o {where-to-save-results} -v {taverna-home} -w {workflow-location}

Example:
    
    bin/hadoop jar /home/schenck/Workspaces/scape/scape/pt-mapred/target/pt-mapred-0.0.1-SNAPSHOT-jar-with-dependencies.jar -i hdfs:///user/schenck/inFile -o hdfs:///user/schenck/results/ -v /home/schenck/Programs/taverna-workbench-2.3.0/ -w hdfs:///user/schenck/tifWorkflow.tf2flow

Example input for example workflow TiffWorkflow_*.t2flow thatneeds to be specified in {input-file-with-workflow-inputs}, one per line
    
    -inputvalue image_location {image-location}

### URLs

Taverna: http://www.taverna.org.uk/download/workbench/
Fits: http://code.google.com/p/fits/
