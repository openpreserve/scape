package eu.scape_project.pit.tools;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *  
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class Tool {

	private Version version;
	
	private String name;
	
	private Installation installation;
	
	/** For JAXB */
	public Tool() { }


	/**
	 * @return the version
	 */
	public Version getVersion() {
		return version;
	}


	/**
	 * @param version the version to set
	 */
	public void setVersion(Version version) {
		this.version = version;
	}


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
	 * @return the installation
	 */
	public Installation getInstallation() {
		return installation;
	}


	/**
	 * @param installation the installation to set
	 */
	public void setInstallation(Installation installation) {
		this.installation = installation;
	}	

}