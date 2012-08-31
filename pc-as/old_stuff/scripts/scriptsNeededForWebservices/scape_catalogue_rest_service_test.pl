#!/usr/bin/perl

use XML::LibXML;
use strict;

my $dom=XML::LibXML->load_xml(location => "http://scape.keep.pt/scapeservices/scape-gimptiff2jpg10-service/services/gimptiff2jpg10/convert?input=http://scape.keep.pt/scape/testdata/elephant.tiff",);
my $xc=XML::LibXML::XPathContext->new($dom);
$xc->registerNs("ns", "http://scape-project.eu/pc/services");
$xc->registerNs("tns", "http://scape-project.eu/pc/services");

my $returnCode=$xc->findvalue('/ns:convertResponse/ns:return/tns:result/tns:returncode');
if($returnCode == 0){
	exit 0;
}else{
	exit 1;
}

1;
