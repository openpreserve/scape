MapReduce Wrappers
==================

Usage
-----

Having Hadoop set up you can run CLIWrapper:

    hadoop jar {path-to-jar} -i {input-file-with-command-lines} -t {toolspec-name} -a {action-id} -p {processor}

CLIWrapper
----------

This wrapper depends essentially on eu.scape_project.pit.invoke.processor (Subproject xa-pit), which wraps command-line tools for invocation through Toolspec actions. It takes a Toolspec name and an action id and creates the Processor (use "toolspec" or "taverna" as input argument). 

So these two parameters are one part of a MapReduce Job's input. The other part is a text file listing command-lines with various arguments for the specified Toolspec/action-id. See an example here: https://github.com/openplanets/scape/blob/master/pt-mapred/files/hinput.txt (ignore comment lines).

Per command-line one *map* is executed which 

1. Copies needed input files (from eg. HDFS) into a temporary execution directory.
2. Executes the command-line tool with the given command-line arguments.
3. Copies potential output files back to a remote filesystem (eg. HDFS)

There are some TODOs:

* Input parameters of command-lines should be mapped and checked to parameters in the Toolspec commands generically. For instance placeholder "${input}" in a Toolspec command definition should be replaced by the argument of the input parameter "--input". 

* A great deal of a MapReduce Wrapper is concerned with getting the needed files (input, output, ...) into a temporary execution directory on the working node and back to the remote filesystem (eg. HDFS). So what could be useful is an additional flag in the input parameters specification of the Toolspec, which denotes whether a parameter is (1) a file and (2) whether it is an input or an output file. For MR Wrappers need input files to be copied to the temporary execution directory they need to know which of the input parameters carry input files. Consequently, MR Wrappers also need to know which of these are output destinations to copy locally and temporarily created output files there to.

* Reading from std-in and using InputStreams.

* Piped commands

* As the input file with the list of command-lines serves as the MR Job input MapReduce cannot distribute workload. This input file gets assigned to one worker node which reads all lines and executes them, regardless of where the input or output files reside on HDFS. Ie. HDFS input files are copied from potentially remote nodes to the local filesystem which results in loss of parallelity. However, the aim should be that nodes process primarily command-lines which operate on files that already reside locally on that HDFS-node.
