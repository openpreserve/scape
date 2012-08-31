#!/usr/bin/perl

use strict;
use XML::XPath;
use Time::HiRes;

## hash containing the mapping between the information needed from the xml file 
# describing the web service and the information needed for the equivs program
my %info2xpath=(
	'dependencies' => '//toolspec/installation/os[@type=\'linux\']/text()',
	'version' => '//toolspec/version/text()',
	'description' => '//toolspec/services/service/description/text()',
	'service_name' => '//toolspec/services/service/@name',
	'service_context_path' => '//toolspec/services/service/@contextpathprefix',
	'service_url' => '//toolspec/deployments/deployment/identifier/text()',
);
## script parameters
my $projectName = shift or die("Must provide project name!");
die("Project name must not contain underscores!") if($projectName =~ m/.+_/);
my $warName = shift or dir("Must provide .war name!");
my $fileName = shift or die("Must provide .xml file containing the toolspec web service description!");

## read the needed information in the .xml file and return it as a hash reference
my $hashResultReference = getValuesFromXPath();
#for my $key (keys %$hashResultReference){
#	print $key.">".$hashResultReference->{$key}."\n";
#}


## obtain and write information in the workflow files (rest and soap) with sed
my @now = Time::HiRes::gettimeofday();
my $version = $hashResultReference->{'version'};
$version =~ s/[\.\-]//g;
my $urlWithPath=$hashResultReference->{'service_url'}."".$hashResultReference->{'service_context_path'};
$urlWithPath =~ s/\//\\\//g;
my $serviceName = $hashResultReference->{'service_name'};
$serviceName =~ s/-//g;
my $servicePlusVersion="$serviceName$version";
my $sedString = "sed -i -e 's/##SERVICE_NAME_WITH_VERSION##/$servicePlusVersion/g' -e 's/##UNIQ_ID##/".$now[0]."/g' -e 's/##SERVICE_NAME##/$serviceName/g' -e 's/##SERVICE_URL_PLUS_INITIAL_PATH##/$urlWithPath/g'";
system("$sedString $projectName"."_rest.t2flow");
system("$sedString $projectName"."_soap.t2flow");

## write information in the debian related files (folder debian)
$serviceName = $hashResultReference->{'service_name'};
$version = $hashResultReference->{'version'};
my $date_rfc_2822 = `date -R`;
chomp $date_rfc_2822;
$sedString = "sed -i -e 's/##NAME##/$projectName/g' -e 's/##VERSION##/$version/g' -e 's/##DATE##/$date_rfc_2822/g' -e 's/##DEPENDENCIES##/".$hashResultReference->{'dependencies'}."/g' -e 's/##DESCRIPTION##/".$hashResultReference->{'description'}."/g' -e 's/##URL##/".$hashResultReference->{'url'}."/g' -e 's/##XML_NAME_WO_EXT##/$projectName/g' -e 's/##WAR##/$warName/g' debian/*";
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
































#use strict;
#use XML::XPath;
#use Time::HiRes;
#
### hash containing the mapping between the information needed from the xml file 
## describing the web service and the information needed for the equivs program
#my %info2xpath=(
#	'dependencies' => '//toolspec/installation/os[@type=\'linux\']/text()',
#	'version' => '//toolspec/version/text()',
#	'description' => '//toolspec/services/service/description/text()',
#	'service_name' => '//toolspec/services/service/@name',
#	'service_context_path' => '//toolspec/services/service/@contextpathprefix',
#	'service_url' => '//toolspec/deployments/deployment/identifier/text()',
#);
### script parameters
#my $projectName = shift or die("Must provide project name!");
#die("Project name must not contain underscores!") if($projectName =~ m/.+_/);
#my $warName = shift or dir("Must provide .war name!");
#my $fileName = shift or die("Must provide .xml file containing the toolspec web service description!");
#
### read the needed information in the .xml file and return it as a hash reference
#my $hashResultReference = getValuesFromXPath();
##for my $key (keys %$hashResultReference){
##	print $key.">".$hashResultReference->{$key}."\n";
##}
#
### write file needed by equivs-build to build the debian package
#open F,">","$projectName";
#print F "Section: misc\n";
#print F "Priority: optional\n";
#print F "Homepage: http://www.scape-project.eu/\n";
#print F "Package: $projectName\n";
#print F "Version: $hashResultReference->{'version'}\n";
#print F "Maintainer: Hélder Silva, Rui Castro <[hsilva,rcastro]\@keep.pt>\n";
#print F "Depends: $hashResultReference->{'dependencies'}\n";
#print F "Architecture: all\n";
#print F "Files: $warName /var/lib/tomcat6/webapps/$warName\n";
#print F "Extra-Files: ".$projectName."_rest.t2flow, ".$projectName."_soap.t2flow, README\n";
#print F "Description: $hashResultReference->{'description'}\n";
#close F;
#
### obtain and write information in the workflow files (rest and soap) with sed
#my @now = Time::HiRes::gettimeofday();
#my $version = $hashResultReference->{'version'};
#$version =~ s/[\.\-]//g;
#my $urlWithPath=$hashResultReference->{'service_url'}."".$hashResultReference->{'service_context_path'};
#$urlWithPath =~ s/\//\\\//g;
#my $serviceName = $hashResultReference->{'service_name'};
#$serviceName =~ s/-//g;
#my $servicePlusVersion="$serviceName$version";
#my $sedString = "sed -i -e 's/##SERVICE_NAME_WITH_VERSION##/$servicePlusVersion/g' -e 's/##UNIQ_ID##/".$now[0]."/g' -e 's/##SERVICE_NAME##/$serviceName/g' -e 's/##SERVICE_URL_PLUS_INITIAL_PATH##/$urlWithPath/g'";
#system("$sedString $projectName"."_rest.t2flow");
#system("$sedString $projectName"."_soap.t2flow");
#
#
###################### functions #####################
#sub getValuesFromXPath{
#	my %res=();
#	my $value, my $retValue, my $nodeset;
#	my $xp = XML::XPath->new(filename => $fileName);
#	for my $info (keys %info2xpath){
#		$nodeset = $xp->find($info2xpath{$info}); 
#	   foreach my $node ($nodeset->get_nodelist){
#			if($node->getNodeType == 3 ){
#				$value=$node->string_value;
#			}
#			if($node->getNodeType == 2 ){
#				$value=$node->getData;
#			}
#      	last;
#	   }	
#		chomp $value;
#		$value =~ s/^\s+//;
#		$value =~ s/\s+$//;
#		$res{$info}=$value;
#	}
#	return \%res;
#}
#
#1;
