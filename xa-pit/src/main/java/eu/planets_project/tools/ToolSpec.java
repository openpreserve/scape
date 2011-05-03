package eu.planets_project.tools;

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
	private String tool;
	
	@XmlElement
	private Version version;
	
	@XmlElement
	private Install install;
	
	@XmlElement
	private List<Var> env;
	
	@XmlElement
	private List<Var> var;
	
	@XmlElement
	private List<Convert> convert;
	
	@XmlElement
	private List<Validate> validate;

	
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
	public String getTool() {
		return tool;
	}

	/**
	 * @param tool the tool to set
	 */
	public void setTool(String tool) {
		this.tool = tool;
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
	public List<Convert> getConvert() {
		return convert;
	}

	/**
	 * @param convert the convert to set
	 */
	public void setConvert(List<Convert> convert) {
		this.convert = convert;
	}

	/**
	 * @return the validate
	 */
	public List<Validate> getValidate() {
		return validate;
	}

	/**
	 * @param validate the validate to set
	 */
	public void setValidate(List<Validate> validate) {
		this.validate = validate;
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
