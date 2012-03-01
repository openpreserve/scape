package eu.scape_project.pit.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class Input {
	
	@XmlAttribute
	String name;
	
	@XmlAttribute
	String var;
	
	@XmlElement( name = "default")
	String defaultValue;
	
	String datatype; // Default interpretation = "xsd:string"
	
	String documentation;

	@XmlAnyElement
	private List<Element> xml;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the var
	 */
	public String getVar() {
		return var;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefault() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the xml
	 */
	public List<Element> getXml() {
		return xml;
	}

	/**
	 * @param xml the xml to set
	 */
	public void setXml(List<Element> xml) {
		this.xml = xml;
	}

}
