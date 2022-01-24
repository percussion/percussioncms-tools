package de.byteaction.velocity.vaulttec.ui.editor.parser;

import de.byteaction.velocity.vaulttec.ui.IPreferencesConstants;
import de.byteaction.velocity.vaulttec.ui.VelocityPlugin;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.DirectiveConstants;
import org.apache.velocity.runtime.parser.Parser;
import org.eclipse.jface.preference.IPreferenceStore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class VelocityParser extends RuntimeInstance
{

    /**
     * Indicate whether the Parser has been fully initialized.
     */
    private boolean   fIsInitialized = false;
    /**
     * This is a hashtable of initialized Velocity directives. This hashtable is
     * passed to each parser that is created.
     */
    private Hashtable fDirectives;
    private List      fUserDirectives;
    private Hashtable fMacros        = new Hashtable();

/* (non-Javadoc)
 * @see org.apache.velocity.runtime.RuntimeInstance#addProperty(java.lang.String, java.lang.Object)
 */

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Collection getUserDirectives()
    {
        return fUserDirectives;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public VelocityMacro getLibraryMacro(String aName)
    { 
        return (fMacros.containsKey(aName) ? (VelocityMacro) fMacros.get(aName) : null);
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Collection getLibraryMacros()
    {
        return fMacros.values();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param aName
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean isUserDirective(String aName)
    {
        return fUserDirectives.contains(aName);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    @Override
    public synchronized void init()
    {
        if (!fIsInitialized)
        {
            // Set Velocity library
            IPreferenceStore store = VelocityPlugin.getDefault().getPreferenceStore();
            setProperty("file.resource.loader.path", store.getString(IPreferencesConstants.LIBRARY_PATH));
            setProperty("velocimacro.library", store.getString(IPreferencesConstants.LIBRARY_LIST));
            // Initialize system and user directives
            try {
                initializeDirectives();
            } catch (Exception e) {
                java.util.logging.Logger.getAnonymousLogger().severe(e.getMessage());
            }
            // Call super implementation last because it calls createNewParser()
            super.init();
            fIsInitialized = true;
        }
    }

    /**
     * Returns a JavaCC generated Parser.
     */
    public Parser createNewParser()
    {
        return  super.createNewParser();
    }
//    public Map get Resources
    /**
     * This methods initializes all the directives that are used by the Velocity
     * Runtime. The directives to be initialized are listed in the
     * RUNTIME_DEFAULT_DIRECTIVES properties file.
     */
    private void initializeDirectives() throws Exception {
        /*
         * Initialize the runtime directive table. This will be used for
         * creating parsers.
         */
        fDirectives = new Hashtable();
        Properties directiveProperties = new Properties();
        /*
         * Grab the properties file with the list of directives that we should
         * initialize.
         */
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(DEFAULT_RUNTIME_DIRECTIVES);
        if (inputStream == null) { throw new Exception("Error loading directive.properties! " + "Something is very wrong if these properties " + "aren't being located. Either your Velocity "
                + "distribution is incomplete or your Velocity " + "jar file is corrupted!"); }
        directiveProperties.load(inputStream);
        /*
         * Grab all the values of the properties. These are all class names for
         * example:
         * 
         * org.apache.velocity.runtime.directive.Foreach
         */
        Enumeration directiveClasses = directiveProperties.elements();
        while (directiveClasses.hasMoreElements())
        {
            String directiveClass = (String) directiveClasses.nextElement();
            loadDirective(directiveClass, "System");
        }
        /*
         * now the user's directives
         */
        fUserDirectives = new ArrayList();
        Iterator userDirectives = VelocityPlugin.getVelocityUserDirectives().iterator();
        while (userDirectives.hasNext())
        {
            String directive = (String) userDirectives.next();
            String name = directive.substring(0, directive.indexOf(' '));
            int type = (directive.endsWith("[Block]") ? DirectiveConstants.BLOCK : DirectiveConstants.LINE);
            fUserDirectives.add('#' + name);
            fDirectives.put(name, new VelocityDirective(name, type));
        }
    }

    /**
     * instantiates and loads the directive with some basic checks
     * 
     * @param directiveClass
     *            classname of directive to load
     */
    private void loadDirective(String directiveClass, String caption)
    {
        try
        {
            Object o = Class.forName(directiveClass).newInstance();
            if (o instanceof Directive)
            {
                Directive directive = (Directive) o;
                fDirectives.put(directive.getName(), directive);
                info("Loaded " + caption + " Directive: " + directiveClass);
            } else
            {
                error(caption + " Directive " + directiveClass + " is not org.apache.velocity.runtime.directive.Directive." + " Ignoring. ");
            }
        }
        catch (Exception e)
        {
            error("Exception Loading " + caption + " Directive: " + directiveClass + " : " + e);
        }
    }

    /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     * 
     * @param name Name of velocimacro
     * @param macro String form of macro body
     * @param argArray Array of strings, containing the #macro() arguments.
     *            the 0th is the name.
     * @return boolean True if added, false if rejected for some reason (either
     *         parameters or permission settings)
     */
    public boolean addVelocimacro(String name, String macro, String[] argArray, String sourceTemplate)
    {
        fMacros.put(name, new VelocityMacro(name, argArray, sourceTemplate));
        return super.addVelocimacro(name, macro, argArray, sourceTemplate);
    }
}
