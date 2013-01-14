# SCAPE Toolwrapper

The toolwrapper is a Java tool developed in the SCAPE Project to simplify the execution of the following tasks:

1. __Tool description__ (through the toolspec);
2. __Tool invocation__ (simplified) through command-line wrapping;
3. __Artifacts generation__ (associated to a tool invocation, e.g., Taverna workflow);
4. __Packaging__ of all the generated artifacts for easier distribution and installation.

## Toolwrapper and the toolspec

Tools, and tools invocations, are described using a machine-readable language (XML, respecting a XML schema) called toolspec. On this file, one can specify:

1. Tool information, i.e., name, version, homepage, etc;
2. Tool installation information, i.e., software dependencies, license, etc;
3. One or more concrete operations, pre-described, that can be executed for a particular input to generate a particular output.

__Example:__

This example, even if simplified for presentation purpose, demonstrates how one could describe a image file format conversion using ImageMagick.

<pre style='color:#000000;background:#ffffff;'><span style='color:#004a43; '>&lt;?</span><span style='color:#800000; font-weight:bold; '>xml</span><span style='color:#004a43; '> </span><span style='color:#074726; '>version</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#7d0045; '>1.0</span><span style='color:#0000e6; '>"</span><span style='color:#004a43; '> </span><span style='color:#074726; '>encoding</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>utf-8</span><span style='color:#0000e6; '>"</span><span style='color:#004a43; '> </span><span style='color:#004a43; '>?></span>
<span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>tool</span> <span style='color:#274796; '>name</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>ImageMagick</span><span style='color:#0000e6; '>"</span> <span style='color:#274796; '>version</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>1.0.2</span><span style='color:#0000e6; '>"</span> <span style='color:#274796; '>homepage</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>http://www.imagemagick.org/script/convert.php</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>
  <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>installation</span><span style='color:#a65700; '>></span>
    <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>dependency</span> <span style='color:#274796; '>operatingSystemName</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>Debian</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>imagemagick<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>dependency</span><span style='color:#a65700; '>></span>
    <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>license</span> <span style='color:#274796; '>type</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>Apache Licence 2.0</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>Apache License, Version 2.0<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>license</span><span style='color:#a65700; '>></span>
  <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>installation</span><span style='color:#a65700; '>></span>
  <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>operations</span><span style='color:#a65700; '>></span>
    <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>operation</span> <span style='color:#274796; '>name</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>digital-preservation-migration-image-imagemagick-image2jp2</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>Converts any ImageMagick supported image format to JPEG2000<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>command</span><span style='color:#a65700; '>></span>/usr/bin/convert ${input} jp2:${output}<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>command</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>inputs</span><span style='color:#a65700; '>></span>
        <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>input</span> <span style='color:#274796; '>name</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>input</span><span style='color:#0000e6; '>"</span> <span style='color:#274796; '>required</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>true</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>     
          <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>Reference to input file<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>
        <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>input</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>inputs</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>outputs</span><span style='color:#a65700; '>></span>
        <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>output</span> <span style='color:#274796; '>name</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>output</span><span style='color:#0000e6; '>"</span> <span style='color:#274796; '>required</span><span style='color:#808030; '>=</span><span style='color:#0000e6; '>"</span><span style='color:#0000e6; '>true</span><span style='color:#0000e6; '>"</span><span style='color:#a65700; '>></span>   
          <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>Reference to output file<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>description</span><span style='color:#a65700; '>></span>
          <span style='color:#a65700; '>&lt;</span><span style='color:#5f5035; '>extension</span><span style='color:#a65700; '>></span>jp2<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>extension</span><span style='color:#a65700; '>></span>
        <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>output</span><span style='color:#a65700; '>></span>
      <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>outputs</span><span style='color:#a65700; '>></span>
    <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>operation</span><span style='color:#a65700; '>></span>
  <span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>operations</span><span style='color:#a65700; '>></span>
<span style='color:#a65700; '>&lt;/</span><span style='color:#5f5035; '>tool</span><span style='color:#a65700; '>></span>
</pre>

## Getting started

### Requirements

1. Unix/linux operating system;
2. Java SDK (version >= 1.6)
    * Debian/ubuntu: *sudo apt-get install openjdk-6-jdk*
3. Build tools (for Java and Debian packaging)
    * Debian/ubuntu: *sudo apt-get install build-essential dh-make devscripts debhelper lintian maven*
4. Clone of scape github repository
    * Unix/linux: *git clone https://github.com/openplanets/scape.git*

### Project directory structure

* _**bash-debian-generator**_ component that generates, from a set of bash wrappers, one or more Debian packages
    * **bin** folder with script that eases the component execution
    * **pom.xml**
    * **src** java source code and other resources (templates for debian package generation)
* _**bash-generator**_ component that generates a set of bash wrappers and the correspondent Taverna workflows
    * **bin** folder with script that eases the component execution
    * **pom.xml**
    * **src** java source code and other resources (templates for bash wrapper and Taverna workflow)
* **core** component with common core functionalities
    * **pom.xml**
    * **src** java source code and other resources (log4j.xml)
* **data**
    * **pom.xml**
    * **src** java source code and other resources (toolspec XML Schema)
* **LICENSE**
* **pom.xml**
* **README_FILES** folder with files mentioned on the README file
* **README.md**

### Compilation process

Execute the following on the command-line ($SCAPE\_GITHUB\_FOLDER denotes the path to the folder where the scape repository was cloned):

	$> cd $SCAPE_GITHUB_FOLDER/pc-as/toolwrapper/
	$> mvn package

### How toolwrapper works

In the project directory, there are 2 components (for now) whose name ends up with "generator". These, when executed in a certain sequence, generate different outputs.  
If one executes the **bash-generator** first, for a given toolspec, one will end up with a bash wrapper and a Taverna workflow, as the following diagram explains.

<pre>                                       +---------------------+
                +----------------+     |  output_directory   |
                |                |     |---------------------|
 +--------+     |                |     | ./bash/             |
 |toolspec|+---&gt;| bash-generator |+---&gt;|    ./bash_wrapper_1 |
 +--------+     |                |     |                     |
                |                |     | ./workflow/         |
                +----------------+     |    ./workflow_1     |
                                       +---------------------+</pre>

Then, if one wants to generate a Debian package, for a given toolspec and for the previously generated artifacts, one executes the **bash-debian-generator**, as the following diagram explains.

<pre>                                                           +---------------------+
 +---------------------+                                   |  output_directory   |
 |  output_directory   |                                   |---------------------|
 |---------------------|     +-----------------------+     | ./bash/             |
 | ./bash/             |     |                       |     |    ./bash_wrapper_1 |
 |    ./bash_wrapper_1 |+---&gt;|                       |     |                     |
 |                     |     | bash-debian-generator |+---&gt;| ./workflow/         |
 | ./workflow/         |  --&gt;|                       |     |    ./workflow_1     |
 |    ./workflow_1     |  |  |                       |     |                     |
 +---------------------+  |  +-----------------------+     | ./debian/w/         |
                          |                                |    ./debian_1       |
        +--------+        |                                +---------------------+
        |toolspec|+-------|
        +--------+</pre>


### Different Debian package generation scenarios                                                                                                                                                             

**1 toolspec with 1 operation**

This will generate 1 Debian package named OPERATION-NAME\_VERSION                                                                                                                                             

**1 toolspec with n operations (n > 1)**

There are 2 possibilities:

1. Generate a Debian package with all operations (1 bash wrapper/Taverna workflow per operation)                                                                                                              
2. Generate a Debian package per operation **DEFAULT**

### How to generate a Debian package from a toolspec

Files required:

* toolspec (e.g., digital-preservation-migration-image-imagemagick-image2jp2.xml)
* changelog (e.g., digital-preservation-migration-image-imagemagick-image2jp2.changelog)

Execute the following on the command-line ($SCAPE\_GITHUB\_FOLDER denotes the path to the folder where the scape repository was cloned):

<pre>$> cd $SCAPE_GITHUB_FOLDER/pc-as/toolwrapper/
$> ./bash-generator/bin/generate.sh -t README_FILES/digital-preservation-migration-image-imagemagick-image2jp2.xml -o output_dir
$> ./bash-debian-generator/bin/generate.sh -ch README_FILES/digital-preservation-migration-image-imagemagick-image2jp2.changelog -e hsilva@keep.pt
 -i output_dir -o output_dir -t README_FILES/digital-preservation-migration-image-imagemagick-image2jp2.xml
</pre>

### How to develop a specific functionality for the toolwrapper

(e.g., generate RPM for Red Hat and others)

##Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.
