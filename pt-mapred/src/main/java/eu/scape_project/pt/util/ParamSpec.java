package eu.scape_project.pt.util;

/**
 * Specification Class for input parameters of command line tools
 *
 * @author Matthias Rella, DME-AIT [my_rho]
 */
public class ParamSpec {

    public static class Direction { 
        public static int IN = 0;
        public static int OUT = 1;
    }

    private boolean required;
    private Class type;
    private int direction;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    
}
