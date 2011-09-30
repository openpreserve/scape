package eu.scape_project.pit.tools;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType( XmlAccessType.FIELD )
public class Parameter {
	
	@XmlAttribute
	String name;
	
	@XmlAttribute
	String var;
	
	@XmlElement( name = "default")
	String defaultValue;
	
	String datatype; // Default interpretation = "xsd:string"
	
	String documentation;

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

}
