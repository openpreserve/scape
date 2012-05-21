#include "XMLReader.h"

XMLReader::XMLReader(void)
{
	//try
	//{
	//	XMLPlatformUtils::Initialize();  
	//}
	//catch( XMLException& e )
	//{
	//	char* message = XMLString::transcode( e.getMessage() );

	//	XMLString::release( &message );

	//}
}

XMLReader::~XMLReader(void)
{
}
//
//list<Feature*> XMLReader::read(std::string *filename)
//{
//	list<Feature*> result;
//
//	//XercesDOMParser *m_ConfigFileParser = new XercesDOMParser;
//
//	//m_ConfigFileParser->setValidationScheme( XercesDOMParser::Val_Never );
//	//m_ConfigFileParser->setDoNamespaces( false );
//	//m_ConfigFileParser->setDoSchema( false );
//	//m_ConfigFileParser->setLoadExternalDTD( false );
//
//	//try
//	//{
//	//	// parse file
//	//	m_ConfigFileParser->parse( filename->c_str() );
//
//	//	// get Document
//	//	xercesc::DOMDocument* xmlDoc = m_ConfigFileParser->getDocument();
//
//	//	// get root element
//	//	DOMElement* elementRoot = xmlDoc->getDocumentElement();
//
//	//	// if this element is not the root, this is not a proper xml document
//	//	stringstream ssStream;
//	//	ssStream << "empty or damaged XML document: " << filename->c_str();
//	//	if( !elementRoot ) throw(exception(ssStream.str().c_str()));
//
//	//	DOMNodeList*      children = elementRoot->getChildNodes();
//	//	const  XMLSize_t nodeCount = children->getLength();
//
//	//	for( XMLSize_t i = 0; i < nodeCount; ++i )
//	//	{
//	//		DOMNode* currentNode = children->item(i);
//
//	//		if( currentNode->getNodeType() &&  // true is not NULL
//	//			currentNode->getNodeType() == DOMNode::ELEMENT_NODE ) // is element
//	//		{
//	//			DOMElement* currentElement = dynamic_cast< xercesc::DOMElement* >( currentNode );	
//	//			Feature* task = TaskFactory::createTask(*currentElement);
//	//			result.push_back(task);
//	//		}
//	//	}
//
//	//}
//	//catch( exception& e )
//	//{
//	//	stringstream ssStream;
//	//	ssStream << "Error parsing file: " << e.what();
//	//	throw(exception(ssStream.str().c_str()));
//	//}
//	return result;
//}
//
//void XMLReader::close()
//{
//}