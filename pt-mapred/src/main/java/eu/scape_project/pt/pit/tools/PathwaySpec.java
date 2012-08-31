package eu.scape_project.pt.pit.tools;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 * @version 0.1
 * 
 * @deprecated use bindings from eu.scape_project.pt.tool instead
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class PathwaySpec {

	@XmlAttribute
	String in;
	
	@XmlAttribute
	String out;
	
}
