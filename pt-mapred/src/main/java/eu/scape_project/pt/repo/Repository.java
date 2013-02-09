package eu.scape_project.pt.repo;

/**
 *
 * @author Matthias Rella
 */
public interface Repository {

    /**
     * The toolspecs contained in the repository.
     * @return a string array of toolspecs
     */
    public String[] getToolList();

    /**
     * Whether a toolspec exists in the repository.
     * @param toolspec to look for
     * @return true if the toolspec exists
     */
    public boolean toolspecExists(String toolspec);
    
}
