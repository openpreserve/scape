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
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement( name="toolspec" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ToolSpec {
	
	@XmlElement()
	private String id;

	@XmlElement()
	private String name;
	
	@XmlElement()
	private Version version;
	
	@XmlElement()
	private Installation installation;
	
	@XmlElement()
	private List<Var> env;
	
	@XmlElement()
	private List<Template> template;
	
	@XmlElement()
	private List<Parameter> param;
	
	@XmlElement()
	private Parameters parameters;
	
	@XmlElement( name="action" )
	private List<Action> actions;
		
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
	 * @return the tool
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the tool to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the install
	 */
	public Installation getInstallation() {
		return installation;
	}

	/**
	 * @param install the install to set
	 */
	public void setInstallation(Installation install) {
		this.installation = install;
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
	 * @return the param
	 */
	public List<Parameter> getParam() {
		return param;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(List<Parameter> param) {
		this.param = param;
	}

	/**
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
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
	
	public static ToolSpec fromInputStream( InputStream input ) throws FileNotFoundException, JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (ToolSpec) u.unmarshal(new StreamSource(input));
	}
	
	

}
