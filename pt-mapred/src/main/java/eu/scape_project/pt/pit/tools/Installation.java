package eu.scape_project.pt.pit.tools;

import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
@XmlAccessorType( XmlAccessType.FIELD )
public class Installation {

	String type;
	
	URL url;
	
	String md5;
	
}
