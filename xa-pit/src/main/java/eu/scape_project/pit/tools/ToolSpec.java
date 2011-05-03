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
import javax.xml.transform.stream.StreamSource;

@XmlRootElement( name="toolspec" )
@XmlAccessorType( XmlAccessType.FIELD )
public class ToolSpec {

	@XmlElement
	private String name;
	
	@XmlElement
	private Version version;
	
	@XmlElement
	private Install install;
	
	@XmlElement
	private List<Var> env;
	
	@XmlElement
	private List<Var> var;
	
	@XmlElement
	private List<Tool> tools;
		
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
	public Install getInstall() {
		return install;
	}

	/**
	 * @param install the install to set
	 */
	public void setInstall(Install install) {
		this.install = install;
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
	 * @return the var
	 */
	public List<Var> getVar() {
		return var;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(List<Var> var) {
		this.var = var;
	}

	/**
	 * @return the convert
	 */
	public List<Tool> getTools() {
		return tools;
	}

	/**
	 * @param tools the convert to set
	 */
	public void setTools(List<Tool> tools) {
		this.tools = tools;
	}

	String toXMlFormatted() throws JAXBException, UnsupportedEncodingException {
		//Create marshaller
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m.marshal(this, bos);
		return bos.toString("UTF-8");
	}
	
	public static ToolSpec fromInputStream( InputStream input ) throws FileNotFoundException, JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (ToolSpec) u.unmarshal(new StreamSource(input));
	}
	
	

}
