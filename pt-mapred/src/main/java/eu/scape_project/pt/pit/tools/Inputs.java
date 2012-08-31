package eu.scape_project.pt.pit.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 * 
 * @deprecated use bindings from eu.scape_project.pt.tool instead
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class Inputs {
	
	@XmlAttribute
	private Boolean useStdin = false;

	@XmlElement( name="input" )
	private List<Input> inputs;

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
	 * @param inputs the list of inputs to use 
	 */
	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}

}
