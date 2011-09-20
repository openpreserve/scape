PIT: Preservation action Invocation Tool
========================================

The purpose...

TODO
----
SCAPE xa-toolwrapper
Need to separate deployment descriptor from tool spec.
Move tool spec. into PIT, allow local deployment.
Focus WSDL and REST exposure in xa-toolwrapper (->xa-toolserver?)
Consider separating REST from WSDL, to build some richness on the REST.
Use standard names for input, output, and do all that automatically.
Use template to define required parameters, declarations are optional. i.e. do we really need the 'Required' field when we have 'Default'?
Declarations can be at the top level, so that they can be shared.
Do inputs/parameters need a name and a cliMapping?
Datatypes default to string.

Whether to handle INSTALLING at all, roadmap it?
Use local command, allow absolute?
Use action instead of operation or tool. Possible 'process' to keep it sufficiently general?
Use parameter instead of input - but what of the results?
Use result instead of output - stdout/etc, ${output}, 
ROADMAP: Allow commands instead of literals for version returns, etc.
ROADMAP: Allow download URL etc, for some. platform not os.
Support tempFile, tempFolder, create if present.
NOT Clear whether ENV should be supported/encouraged, because we want to record the expanded value, not the variable.

Define default process invocation behaviour and returns.
Add code to extract all parameters (${.*}) and use that to auto-construct interface and invocation.
Add 'typed' action support, allow parameters to be validated as far as possible.
TODO Generate XSD from classes? Or otherwise support integration with xa-toolwrapper, perhaps by published schema AND Maven dependency? Is that necessary?

How to cope with parameters that map to files.
In the deployed context, this means moving files to the right place or piping into stdin.
In the local command context, this could 'passthru' the filename, or send to stdin.

WSDL wrapper consumes URIs.
CLI interface need to spot parameters that are files and pass them appropriately.
Invocation tool needs to map URLs to local resources.
Cases:
* Identify, Validate, Characterise, etc. One ${input}
* Migrate: One ${input} and one ${output}
* Compare: Two ${input1} ${input2}
* ALL: May also use logFile, tempFile, tempFolder - ???

So, special names are handled specially. 
e.g. if CLI passes a parameter mapped to one of these, 
then turn into a local URI to pass to the invocation,
but the invocation layer scans for ${input}, ${input1}, ${input2} and maps the URIs into local files 
(file URLs pass through and become absolute pathnames)
Outputs are created by the invoker, 
CLI Client moves the output to the specified (local) location.
Service wrapper moves the output to the exposed folder and passes back the new URL.

So, process invocation needs to populate a result object that records what happened, 
(inc. output file location).


