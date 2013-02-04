# SCAPE Toolwrapper

The toolwrapper is a Java tool developed in the SCAPE Project to simplify the execution of the following tasks:

1. **Tool description** (through the toolspec);
2. **Tool invocation** (simplified) through command-line wrapping;
3. **Artifacts generation** (associated to a tool invocation, e.g., Taverna workflow);
4. **Packaging** of all the generated artifacts for easier distribution and installation.

## Toolwrapper and the toolspec

Tools, and tools invocations, are described using a machine-readable language (XML, respecting a XML schema) called toolspec. On this file, one can specify:

1. Tool information, i.e., name, version, homepage, etc;
2. Tool installation information, i.e., software dependencies, license, etc;
3. One or more concrete operations, pre-described, that can be executed for a particular input to generate a particular output.

**Example:**

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
* **CHANGELOG.txt**
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
                                       |  output_directory   |
                +----------------+     |---------------------|
                |                |     | ./bash/             |
 +--------+     |                |     |    ./bash_wrapper_1 |
 |toolspec|+---&gt;| bash-generator |+---&gt;|                     |
 +--------+     |                |     | ./workflow/         |
                |                |     |    ./workflow_1     |
                +----------------+     |                     |
                                       | ./install/          |
                                       +---------------------+
</pre>
  
Then, if one wants to generate a Debian package, for a given toolspec and for the previously generated artifacts, one executes the **bash-debian-generator**, as the following diagram explains.

<pre> +---------------------+                                   +---------------------+
 |  output_directory   |                                   |  output_directory   |
 |---------------------|                                   |---------------------|
 | ./bash/             |     +-----------------------+     | ./bash/             |
 |    ./bash_wrapper_1 |     |                       |     |    ./bash_wrapper_1 |
 |                     |+---&gt;|                       |     |                     |
 | ./workflow/         |     | bash-debian-generator |+---&gt;| ./workflow/         |
 |    ./workflow_1     |  --&gt;|                       |     |    ./workflow_1     |
 |                     |  |  |                       |     |                     |
 | ./install/          |  |  +-----------------------+     | ./install/          |
 +---------------------+  |                                |                     |
        +--------+        |                                | ./debian/           |
        |toolspec|+-------|                                |    ./debian_1       |
        +--------+                                         +---------------------+
</pre>
  
**Sum up:**  
1. Components can be combined, in the correct order, passing generated artifacts through a folder (i.e., the output folder of the **bash-generator** will be the input folder of the **bash-debian-generator**).  
2. An install folder is generated by the **bash-generator**, which can be used to place scripts/files/programs that should be installed alongside with the bash wrapper and workflow. These scripts/files/programs are going to be placed under **/usr/share/OPERATION-NAME/**.
  
### Different Debian package generation scenarios

1. **1 toolspec with 1 operation**  
This will generate 1 Debian package named OPERATION-NAME\_VERSION\_all.deb
2. **1 toolspec with n operations (n > 1)**
    1. Generate a Debian package with all artifacts (named DEB-NAME\_VERSION\_all.deb, where DEB-NAME is passed as a parameter through the command-line)
    2. Generate a Debian package per operation (named OPERATION-NAME\_VERSION\_all.deb) **DEFAULT**

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

TBA (e.g., generate RPM for Red Hat and others)

##Acknowledgements

Part of this work was supported by the European Union in the 7th Framework Program, IST, through the SCAPE project, Contract 270137.
