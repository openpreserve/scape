#include "XMLUtils.h"

XMLUtils::XMLUtils(void)
{
}

XMLUtils::~XMLUtils(void)
{
}

//string XMLUtils::getElementValue(DOMElement *rootelem, const string *name)
//{
//	XMLCh* elemName    = XMLString::transcode(name->c_str());
//
//	DOMNodeList* nodes = rootelem->getElementsByTagName(elemName);
//	DOMElement* elem = dynamic_cast< DOMElement* >( nodes->item(0) );
//
//	const XMLCh* xmlstr = elem->getTextContent();
//
//	return XMLString::transcode(xmlstr);
//}
//
//string XMLUtils::getElementValue(DOMElement *rootelem)
//{
//	const XMLCh* xmlstr = rootelem->getTextContent();
//	return XMLString::transcode(xmlstr);
//}
//
//string XMLUtils::xmlChToString(const XMLCh* toTranscode)
//{ 
//	char* test = XMLString::transcode(toTranscode);
//
//	return test;
//}