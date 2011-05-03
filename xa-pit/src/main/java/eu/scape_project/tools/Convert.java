package eu.scape_project.tools;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType( XmlAccessType.FIELD )
public class Convert extends AbstractCommand {

	List<PathwaySpec> formats;

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
