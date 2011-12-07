/**
 * 
 */
package eu.scape_project.core;

/**
 * Enum class, to enforce static instance.  Meant for project constants.
 * 
 * @author <a href="mailto:carl.wilson.bl@gmail.com">Carl Wilson</a>
 *	   <a href="http://sourceforge.net/users/carlwilson-bl">carlwilson-bl@SourceForge</a>
 *	   <a href="https://github.com/carlwilson-bl">carlwilson-bl@github</a>
 *
 */
public enum Constants {
    /** Enforce a static instance */
    INSTANCE;
    /** SCAPE URI scheme prefix */
    public final static String SCAPE_URI_SCHEME = "scape:";
    /** SCAPE algorithm id URI scheme prefix */
    public final static String ALGID_URI_PREFIX = SCAPE_URI_SCHEME + "id.algorithm.";
    /** Path to the toolspec schema in the resources */
    public final static String TOOLSPEC_SCHEMA_RESOURCE_PATH = "eu/scape_project/core/model/toolspec/toolspec.xsd";
}
