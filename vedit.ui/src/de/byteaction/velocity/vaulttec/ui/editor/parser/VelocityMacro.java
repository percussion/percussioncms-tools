package de.byteaction.velocity.vaulttec.ui.editor.parser;

/**
 * Container used to store Velocity macro information.
 */
public class VelocityMacro
{

    protected String   fName;
    protected String[] fArguments;
    protected String   fTemplate;

    public VelocityMacro(String aName, String[] anArguments, String aTemplate)
    {
        fName = aName;
        fArguments = anArguments;
        fTemplate = aTemplate;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getName()
    {
        return fName;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String[] getArguments()
    {
        return fArguments;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getTemplate()
    {
        return fTemplate;
    }
}
