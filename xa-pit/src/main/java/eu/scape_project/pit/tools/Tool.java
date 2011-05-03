package eu.scape_project.pit.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
public class Tool {

	@XmlAttribute
	private String id;
	
	@XmlAttribute
	private String type;
	
	private String command;

	List<PathwaySpec> formats;

	List<PathwaySpec> fmt;

	public Tool() {
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

	

}