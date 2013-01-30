package eu.scape_project.pt.repo;

/**
 *
 * @author Matthias Rella
 */
public interface Repository {

    public String[] getToolList();

    public boolean toolspecExists(String get);
    
}
