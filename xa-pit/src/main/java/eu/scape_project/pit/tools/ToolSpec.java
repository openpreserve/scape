package eu.scape_project.pit.tools;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

/**
 * @author  <a href="mailto:andrew.jackson@bl.uk">Andrew Jackson</a>
 */
@XmlRootElement( name="toolspec" )
@XmlType( namespace=ToolSpec.NS )
@XmlAccessorType( XmlAccessType.FIELD )
public class ToolSpec {
	/** The namespace for the toolspec */
	public static final String NS = "http://www.scape-project.eu/schemas/2011/11/16/toolspec";
	
	private String id;
	
	private Version version;
	
	private Tool tool;

	private List<Var> env;
	
	private List<Template> template;
	
	private Inputs inputs;
	
	@XmlElement( name="action" )
	private List<Action> actions;

	@XmlTransient
	private static JAXBContext jc;
	
	static {
		try {
			jc  = JAXBContext.newInstance(ToolSpec.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

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
	 * @return the tool
	 */
	public Tool getTool() {
		return tool;
	}

	/**
	 * @param tool
	 */
	public void setTool(Tool tool) {
		this.tool = tool;
	}

	/**
	 * @return the env
	 */
	public List<Var> getEnv() {
		return env;
	}

	/**
	 * @param env the env to set
	 */
	public void setEnv(List<Var> env) {
		this.env = env;
	}

	/**
	 * @return the template
	 */
	public List<Template> getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(List<Template> template) {
		this.template = template;
	}

	/**
	 * @return the parameters
	 */
	public Inputs getInputs() {
		return inputs;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setInputs(Inputs parameters) {
		this.inputs = parameters;
	}

	/**
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	String toXMlFormatted() throws JAXBException, UnsupportedEncodingException {
		//Create marshaller
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		m.setProperty( Marshaller.JAXB_ENCODING, "UTF-8" );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m.marshal(this, bos);
		return bos.toString("UTF-8");
	}
	
	/**
	 * Deserializes a ToolSpec from an XML stream
	 * @param input
	 * @return a new ToolSpec object created from the XML Stream data
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	public static ToolSpec fromInputStream( InputStream input ) throws FileNotFoundException, JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (ToolSpec) u.unmarshal(new StreamSource(input));
	}
	
	

}
