package eu.scape_project.pit.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

@XmlAccessorType( XmlAccessType.FIELD )
public class Parameters {

	@XmlAnyElement
	private List<Element> parameters;

	/**
	 * @return the parameters
	 */
	public List<Element> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<Element> parameters) {
		this.parameters = parameters;
	}

}
