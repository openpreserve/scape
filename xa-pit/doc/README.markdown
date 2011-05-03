The Planets Invocation Tool
===========================

This is a command-line-oriented tool specification and invocation system. It aims
to make it easy to describe the preservation actions and the tools used to perform
them, and also make is simple to invoke preservation actions defined by others, 
both locally and over the web.

TODO Wrap pdfbox as jhove2 module. Include our pw spotter logic etc. And then blog about how to do it.


Tool Specifications
-------------------

As well as preservation action tools, a tool spec. can also describe a remote 
service registry through which hosted services can be invoked.

* Lightweight tool spec as XML, speccing command line pattern, optional env etc.
* Or java cp and main class and args. Allows stack analysis.
* Java tools specs optionally generated from Maven POM via a magic mojo.
* ID used in invokation, but binary hash used to spot uniqueness of tools.
* Resist parameter enumeration as this is not really necessary to work out what's going on.
* Pass args as command line or string array, split to string array when analysing.
* i.e. minimal enforced spec in tool spec. Even pathways are only for discovery - can capture usage without them.
* Indeed, how do we use these specs?
* Extend to other cases? Identity or validate etc, by regexing the output?
* Tool spec (.ptspec) may include version.command eg 'file -v'. Response is included verbatim.
* Tool spec may be derived from another explicitly. eg multiple versions of same tool by modifing env.path only.
* Install text, parameter descriptions, in or out formats as optional but recommended.
* If install package then install-dir available as field for env description, eg FITS_HOME or JHove etc.
* Spec can define where additional parameters go, defaults to eol.
* Allows var definitions so that common values, e.g. sets of parameters, do not need to be repeated.
* Platforms specified as e.g. Java/1.6, not dissimilar to User Agents or os.name/arch or GCC or perhaps http://www.csee.wvu.edu/~jdm/classes/cs258/OScat/


### Instrumentation & Measurement ###

* Extend the approach from Plato work.
* Hash of input files and outputs too, of course. Of course, similarity indexes would be very useful here.
* Inject code into java calls automatically. Fix up proxy, measure performance etc,
* Freak out if the stack changes during a run.
* bg thread to monitor mem Use over time, especiallly for file set runs.

PIT Actions
-----------

### Tool Spec Management ###

Add a spec to the local register of known tools:
    pit add <file-or-url.ptspec>

Remove a spec from the local register:
    pit [rm|remove] <toolspec-id>

List all known tool specs:
    pit [ls|list]

Check if a tool spec is valid/up to scratch etc:
    pit [tt|tooltest] <toolspec-id>

### Preservation Actions ###

pit [id|identify] <toolspec-id> <file> [<extra parameters>]

pit [val|validate] <toolspec-id> <file> [<extra parameters>]

pit [tr|transform|cv|convert] <toolspec-id> <input> <output> [<extra parameters>]

pit [pf|perform] <toolspec-id> <input>

??? pit [vw|view] <toolspec-id> <input>

When these actions are called, the framework records lots of useful metadata 
about the process and the content. This data is stored in PIT reports (see later).

* Extra pit flags to turn off heavy profiling (total time always included)
* Extra pit flags to avoid/enforce recording the result report.

### Querying Reports ###

* Export to CSV, etc. 
* Can do some aggregation.


### Sharing Tool Specs and Reports ###

The tool also allows toolspecs and reports to be shared between users via a website.

* Tool specs get username based uris
* Env is not included in report or published spec by default, as may be private. Http proxy password? Allow mode with exclusions as env can affect performance.
* Download specs and test?
* Allow test inputs in spec to validate configuration. Can compare with output. Can publish if a licence is given.
* Auto-install mode? Attempt to look at all known tool specs and match up with local sw?
* Easy way to upload to shared space? A servlet that auths against opf drupal? Or open id, or Github backend?
* Pit knows about the OPF tool registry from the start. Tools can include install info, eg mvn or download this package and expand? Do it auto?


### Hosting Preservation Services ###

* Pit serve attempts to push tool spec into a service registry. Requires selftest is in place.

PIT Reports
-----------

XML breakdown of data about presevation action executions.

Includes timing, resource usage, exe digests and analysis.

Effective spec is in report.



