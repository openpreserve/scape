package eu.scape_project.pit.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

@XmlAccessorType( XmlAccessType.FIELD )
public class Inputs {
	
	@XmlAttribute
	private Boolean useStdin = false;

	@XmlElement
	private List<Input> inputs;

	@XmlAnyElement
	private List<Element> elements;

	/**
	 * @return the useStdin
	 */
	public Boolean getUseStdin() {
		return useStdin;
	}

	/**
	 * @param useStdin the useStdin to set
	 */
	public void setUseStdin(Boolean useStdin) {
		this.useStdin = useStdin;
	}

	/**
	 * @return the parameters
	 */
	public List<Input> getInputs() {
		return inputs;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setInputs(List<Input> parameters) {
		this.inputs = parameters;
	}

	/**
	 * @return the elements
	 */
	public List<Element> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<Element> elements) {
		this.elements = elements;
	}
	
}
