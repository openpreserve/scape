package eu.scape_project.pit.tools;

import java.net.URL;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An Action may be backed by:
 *  - A command line invocation.
 *  - A web service (e.g. PLANETS or SCAPE services, autodiscovered and registered if possible.)
 *  - A Java class from a particular Maven POM/package.
 *  
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class Action {

	@XmlAttribute
	private String id;
	
	@XmlAttribute
	private String type;
	
	private String command;
	
	/* Best done as class, with room for more than URL (?), including supported types? */
	private URL service;
	
	@XmlElement()
	private Inputs inputs;
	
	private List<PathwaySpec> formats;

	
	public Action() {
		super();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/**
	 * @return the formats
	 */
	public List<PathwaySpec> getFormats() {
		return formats;
	}

	/**
	 * @param formats the formats to set
	 */
	public void setFormats(List<PathwaySpec> formats) {
		this.formats = formats;
	}

	/**
	 * @return the inputs
	 */
	public Inputs getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(Inputs inputs) {
		this.inputs = inputs;
	}

}