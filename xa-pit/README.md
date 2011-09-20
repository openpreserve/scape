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

