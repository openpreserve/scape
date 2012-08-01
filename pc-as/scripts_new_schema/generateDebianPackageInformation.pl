#!/usr/bin/perl

use strict;
use XML::XPath;
use Time::HiRes;

## hash containing the mapping between the information needed from the xml file 
# describing the web service and the information needed for the equivs program
my %info2xpath=(
	'dependencies' => '//tool/installation/dependency[@operatingSystemName=\'Debian\']/text()',
	'version' => '//tool/@version',
	'description' => '//tool/operations/operation/description/text()',
	'operation_name' => '//tool/operations/operation/@name',
	'url' => '//tool/@homepage',
);
## script parameters
my $projectName = shift or die("Must provide project name!");
die("Project name must not contain underscores!") if($projectName =~ m/.+_/);
my $fileName = shift or die("Must provide .xml file containing the toolspec web service description!");

## read the needed information in the .xml file and return it as a hash reference
my $hashResultReference = getValuesFromXPath();

## obtain and write information in the workflow files (rest and soap) with sed
my @now = Time::HiRes::gettimeofday();
my $version = $hashResultReference->{'version'};
$version =~ s/[\.\-]//g;
my $operationName = $hashResultReference->{'operation_name'};
$operationName =~ s/-//g;
my $operationPlusVersion="$operationName$version";
my $sedString = "sed -i -e 's/##OPERATION_NAME_WITH_VERSION##/$operationPlusVersion/g' -e 's/##UNIQ_ID##/".$now[0]."/g' -e 's/##OPERATION_NAME##/$operationName/g'";
system("$sedString $projectName"."_bash.t2flow");

## write information in the debian related files (folder debian)
$operationName = $hashResultReference->{'operation_name'};
$version = $hashResultReference->{'version'};
my $date_rfc_2822 = `date -R`;
chomp $date_rfc_2822;
$hashResultReference->{'url'} =~ s/\//\\\//g;
$hashResultReference->{'description'} =~ s/\//\\\//g;
$sedString = "sed -i -e 's/##NAME##/$projectName/g' -e 's/##VERSION##/$version/g' -e 's/##DATE##/$date_rfc_2822/g' -e 's/##DEPENDENCIES##/".$hashResultReference->{'dependencies'}."/g' -e 's/##DESCRIPTION##/".$hashResultReference->{'description'}."/g' -e 's/##URL##/".$hashResultReference->{'url'}."/g' -e 's/##XML_NAME_WO_EXT##/$projectName/g' debian/*";
system("$sedString");


##################### functions #####################
sub getValuesFromXPath{
	my %res=();
	my $value, my $retValue, my $nodeset;
	my $xp = XML::XPath->new(filename => $fileName);
	for my $info (keys %info2xpath){
		$nodeset = $xp->find($info2xpath{$info}); 
	   foreach my $node ($nodeset->get_nodelist){
			if($node->getNodeType == 3 ){
				$value=$node->string_value;
			}
			if($node->getNodeType == 2 ){
				$value=$node->getData;
			}
      	last;
	   }	
		chomp $value;
		$value =~ s/^\s+//;
		$value =~ s/\s+$//;
		$res{$info}=$value;
	}
	return \%res;
}

1;
