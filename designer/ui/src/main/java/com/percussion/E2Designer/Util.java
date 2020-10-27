/******************************************************************************
 *
 * [ Util.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.util.PSProperties;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.workbench.ui.editors.form.PSFrameProvider;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class contains utility methods and constants. This class contains
 * local members used for caching. To make this work properly, the main class
 * of the program using this class should call <code>getInstance</code> and
 * keep a reference to this class so the GC doesn't remove it from memory.
 */
public class Util
{

    public static final String DEFAULT_DYNAMIC_TAG = "psx-";

    /**
    * Returns the single instance of this class, creating it if it hasn't been
    * created yet.
    *
    * @return The singleton instance of this class.
    */
   public static Util getInstance()
   {
      if ( null == ms_instance )
         ms_instance = new Util();
      return ms_instance;
   }

   /**
    * When integers are used as IDs, it is useful to have a value that indicates
    * it is not a valid ID.
    */
   public static final int INVALID_ID = -1;
   public static final int FIRST_VALID_ID = 0;


   /**
    * Like getComponentsAt, except it recurses through all children as well.
    *
    * @throws IllegalArgumentException if parent is null
    */
   public static Vector<Component> findComponentsAt(Component parent, Point pt)
   {
      if ( null == parent )
      {
         final Object[] astrParams =
         {
            "Component"
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "CantBeNull" ), astrParams ));
      }

      Vector<Component> hits = new Vector<Component>(10);
      if ( !parent.contains( pt ))
         return hits;

      Component [] comps;
      if ( parent instanceof Container )
         comps = ((Container) parent).getComponents();
      else
         comps = new Component[0];

//      System.out.println( "Parent contains " + Integer.toString( comps.length )
//            + " children" );
      Point loc = new Point();
      for ( int index = 0; index < comps.length; index++ )
      {
//         System.out.println( "trying index " + Integer.toString( index ));
         Point subloc = comps[index].getLocation();
         loc.setLocation( pt.x - subloc.x, pt.y - subloc.y );
         if ( comps[index].contains( loc ))
         {
            Vector<Component> v = findComponentsAt( comps[index], loc );
//            System.out.println( "Child contains " + Integer.toString( v.size())
//                  + " hit children" );
            hits.addAll( v );
         }
      }
      hits.add( parent );
      return hits;
   }

   /**
    * Keeps a persistent counter that is incremented and returned each time
    * the method is called. Since an int is used, it will eventually roll over,
    * but that shouldn't be a problem in our lifetime.
    *
    * @returns a unique number within the universe of the E2 server.
    */
   public static synchronized int getUniqueId()
   {
      UserConfig config = UserConfig.getConfig();
      int nextId = config.getInteger( "UtilNextUniqueId", 1 );
      config.setInteger( "UtilNextUniqueId", nextId + 1 );
      return nextId;
   }

   /**
    * Get application files.
    * 
    * @param requestRoot the application root
    * 
    * @return Vector a vector all files found in the application root
    */
   public static Vector<String> getApplicationFiles(String requestRoot)
   {
      Vector<String> files = new Vector<String>();
      
      try
      {
         PSObjectStore os = new PSObjectStore(
            E2Designer.getDesignerConnection());
         files.addAll(os.getApplicationFiles(requestRoot));
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
      }

      return files;
   }

  /**
   * This function returns a proposed location where to drop the new figure. It
   * makes sure that no other figure is already using the returned location.
   *
   * @param frame the figure frame
   * @param existingFigure the target figure
   * @param newFigure the drag source
   * @param xOffset the x offset for the new figure
   * @return Point the proposed location of the new figure
   */
  //////////////////////////////////////////////////////////////////////////////
  public static Point getNewFigureLocation(UIFigureFrame frame,
                                           UIConnectableFigure existingFigure,
                                           UIConnectableFigure newFigure,
                                           int xOffset)
  {
    Dimension existingSize = existingFigure.getBaseImageSize();
    Dimension newSize = newFigure.getSize();
    Point newLocation = new Point(existingFigure.getLocation());

    if (xOffset >= 0)
      newLocation.translate(existingSize.width + xOffset,
                            (existingSize.height - newSize.height) / 2);
    else
      newLocation.translate(-newSize.width + xOffset,
                            (existingSize.height - newSize.height) / 2);
    while (frame.getFigure(newLocation) != null)
      newLocation.translate(0, newSize.height);

    return newLocation;
  }

  /**
   * Strip off all path elements from the provided file name/path and return
   * the file name and extension only. The current file extension is removed
   * and the one provided is added.
   *
   * @param filePath the original file path
   * @param extension file extension
   * @return String the filename/extension only
   */
  public static String stripPath(String filePath, String extension)
  {
    File file = new File(filePath);
    String fileName = file.getName();

    // remove file extension
    int pos = fileName.lastIndexOf(".");
    if (pos != -1)
      fileName = fileName.substring (0, pos);

    return fileName + extension;
  }

  /**
   * Strip off all path elements from the provided file name/path and return
   * the file name and extension only.
   *
   * @param filePath the original file path
   * @return String the filename/extension only
   */
  public static String stripPath(String filePath)
  {
    File file = new File(filePath);
    return file.getName();
  }

  /**
   * Get the file extension (with the .).
   *
   * @param filePath the original file path
   * @return String the file extension
   */
  public static String getFileExtension(String filePath)
  {
    File file = new File(filePath);
    String fileName = file.getName();
    String fileExtension = "";

    int pos = fileName.lastIndexOf(".");
    if (pos != -1)
      fileExtension = fileName.substring(pos, fileName.length());

    return fileExtension;
  }

   /**
    * Convert the passed file specification int a URL string. The file extension
    * must be of .xml, .xsl or .dtd. Path specifications deeper than the E2 root
    * are allowed.
    *
    * @param file  the file (including path) the want to convert to a URL
    * @returns String the URL string or the error message if not valid
    */
   public static String createURLString(String file, String defaultExtension)
   {
      String url = new String(file.toLowerCase());

      // we do not allow path definition "outside" the E2 root directory
      if (url.startsWith("..") || url.startsWith(":\\", 1) || url.startsWith("\\"))
         return E2Designer.getResources().getString("InvalidURLFilepath");

    // the file name starts o.k. we also do not allow the .. operator within the path
    if (url.indexOf("..") != -1)
      return E2Designer.getResources().getString("");

    // create URL string
    url = "file:" + url;

    // add the default file extension if they did not provide one or they explicitly
    // don't want one (provided name ends wit a dot)
    if (url.endsWith("."))
      return url;
    else if (url.indexOf(".") == -1)
      url += defaultExtension;

    return url;
   }

  /**
   * This method crops the message passed in to a readable size by adding \n
   * (end of line) character into specified line lengths.
   *
   * @param msg The message sent in for cropping.
   */
  public static String cropErrorMessage(String msg)
  {
   if(msg == null)
      return null;

    StringTokenizer tokenString = new StringTokenizer(msg, new String(" "), true);
    int lineCount = 0;
    String errorMsg = "";

    while(tokenString.hasMoreTokens())
    {
      String s = tokenString.nextToken();
     if(s == null)
        break;

      lineCount += s.length();

      if (80 > lineCount)
        errorMsg = errorMsg + s;
      else
      {
        // reached eol so add eol char and reset lineCount
        errorMsg = errorMsg + "\n" + s;
        //System.out.println(lineCount);
        lineCount = s.length();
      }
    }

    return errorMsg;
  }

  /**
   * Get the dynamic splitter tag. If none is found in the workbench properties
   * it will return the splitter default.
   *
   * @return the dynamic splitter markup tag, never <code>null</code>.
   */
   public static String getXSplitDynamicPrefix()
   {
      String strPsxTag =
         UserConfig.getConfig().getValue(OptionsPropertyDialog.PSX_TAG);
      if ((strPsxTag == null) || (strPsxTag.trim().length() == 0))
         strPsxTag = DEFAULT_DYNAMIC_TAG;

      return strPsxTag;
   }

  /**
   * Get the XSL output encoding. If none is found in the workbench properties
   * it will return the splitter default.
   *
   * @return the XSL output encoding, never <code>null</code>.
   */
   public static String getXslOutputEncoding()
   {
      String xslEncoding =
         UserConfig.getConfig().getValue("xslOutputEncoding");
      if ((xslEncoding == null) || (xslEncoding.trim().length() == 0))
         xslEncoding = StandardCharsets.UTF_8.toString();

      return xslEncoding;
   }

  /**
   * Since only 1 figure can have dialog focus at one time, the figure that
   * spawned the current displaying dialog will be stored statically.
   * This provides a static way to provide UIFigure object to be accessed from
   * anywhere. <CODE>null</CODE> is default value. Lastly, to display the
   * params correctly from the Requestor dialog, this method is used in the
   * DatasetInputConnectorPropertyDialog.onEdit(), instead of its normal place
   * in the UIFigure.onEdit() call.
   *
   * @see UIFigure
   * @see DatasetInputConnectorPropertyDialog
   */
  public static void setFigure( UIFigure figure )
  {
    ms_figure = figure;
  }

  /**
   * Since only 1 figure can have dialog focus at one time, the figure that
   * spawned the current displaying dialog will be stored statically. This
   * provides a static way to access the UIFigure object stored from
   * anywhere. <CODE>null</CODE> will be returned if it has not been set.
   *
   * @returns UIFigure
   * @see UIFigure
   */
  public static UIFigure getFigure()
  {
    return ms_figure;
  }

   /**
    * Looks at the supplied filename and makes a guess as to whether it is
    * an HTML Doc based on the extension.
    *
    * @param filename A fully qualified or relative path. If null, <code>false
    * </code> is returned.
    *
    * @return <code>true</code> if the supplied filename appears to reference an HTML
    * file
   **/
   public static boolean isHtml( String filename )
   {
      if ( null == filename )
         return false;

      String lowerCased = filename.toLowerCase();
      return ( lowerCased.endsWith( ".htm" ) || lowerCased.endsWith( ".html" )
         || lowerCased.endsWith( ".xhtm" ) || lowerCased.endsWith( ".xhtml" ));
   }

   /**
    * Looks at the supplied filename and decides whether or not to split it.
    * All files with .htm,.html,.asp,.jsp are splitable
    * Filenames are case insensitive.
    * @param fileName A fully qualified or relative path.
    * @return <code>true</code> if the supplied filename is splitable.
    * @throws IllegalArgumentException if <code>fileName</code> is is null or
    * empty or file extension not found.
    */
   public static boolean isSplitable(String fileName)
   {
      if (fileName == null || fileName.length() == 0)
         throw new IllegalArgumentException("File name cannot be null.");
      int index = fileName.lastIndexOf('.');
      if (index == -1)
         throw new IllegalArgumentException("File extension not found.");
      String fileExt = fileName.substring(index+1, fileName.length()).toLowerCase();
      return  ms_splitExtList.contains(fileExt);
   }

   /**
    * Parses "Splitable_File_Extensions" string from designer.properties to populate
    */
   private static void loadExtTable()
   {
      if (ms_splitExtList.size() == 0)
      {
         PSProperties psProp = E2Designer.getDesignerProperties();
         if (psProp != null)
         {
            String strOuter = psProp.getProperty("Splitable_File_Extensions");
            if (strOuter != null)
            {
               StringTokenizer strTok = new StringTokenizer(strOuter, ";");
               while (strTok.hasMoreTokens())
               {
                  ms_splitExtList.add(strTok.nextToken().toLowerCase());
               }
            }
         }
      }


      if (ms_splitExtList.size() == 0)
      {
         ms_splitExtList.add("htm");
         ms_splitExtList.add("html");
         ms_splitExtList.add("xhtm");
         ms_splitExtList.add("xhtml");
         ms_splitExtList.add("asp");
         ms_splitExtList.add("jsp");
      }
   }

   /**
   * A utility method to get alphabetically sorted list of files' extension
   * that can be split.
   * @return sorted List of file extensions. Returns htm,html,xhtm,xhtml,asp.jsp
   * as entries by default.
   */
   public static List<String> getSplitableFileExts()
   {
      loadExtTable();
      Collections.sort(ms_splitExtList);
      return new ArrayList<String>(ms_splitExtList);
   }

  /**
   * Description should change accordingly.
   * A utility method to get sorted string array of files' extension
   * that can be split.
   * @param lst - The entries in the string should be String objects.
   * @return sorted String array of file extensions.
   * Returns htm,html,xhtm,xhtml,asp.jsp
   * as entries by default.
   */
   public static String[] listToArray(List<String> lst)
   {
      Collections.sort(lst);
      String [] fileTypes = new String[lst.size()];
      lst.toArray(fileTypes);
      return fileTypes;
   }

  /**
   * A utility method to quickly get the UIFigureFrame owner of the UIFigure
   * object passed in as the parameter. It keeps calling getParent() until the
   * Component object returned is an UIFigureFrame object.
   *
   * @param figure The UIFigure to traverse through its parent containers.
   */
  public static UIFigureFrame getOwnerFrameOf( UIFigure figure )
  {
     if (figure == null)
        throw new IllegalArgumentException("figure cannot be null");

     Component comp = figure.getParent();
     while(comp != null && comp.getParent() != null)
     {
        if(comp.getParent() instanceof UIFigureFrame)
        {
           return (UIFigureFrame)comp.getParent();
        }
        if (comp instanceof PSFrameProvider)
        {
           final PSFrameProvider provider = (PSFrameProvider) comp;
           if (provider.getFrame() instanceof UIFigureFrame)
           {
              return (UIFigureFrame) provider.getFrame();
           }
        }
        comp = comp.getParent();
     }

     return(null);
  }


   /**
    * Implements a caching mechanism for objects. If the supplied key is not
    * currently in the cache, a new mapping is added. If the key is present,
    * the overwrite flag determines whether the existing key is replaced.
    *
    * @param key The name to use as the key in the cache.
    *
    * @param value The object to use as the value in the mapping.
    *
    * @param overwrite If <code>true</code> and the key already has an entry
    * in the cache, that value will be overwritten. Otherwise, the key-value
    * mapping will only be added if it is not already present.
    *
    * @return <code>true</code> if the object was added to the cache, <code>
    * false</code> otherwise.
    *
    * @throws IllegalArgumentException If key is <code>null</code> or empty.
    *
    * @see #getCachedObject
    */
   public static boolean cacheObject( String key, Object value,
      boolean overwrite )
   {
      if ( null == key || key.trim().length() == 0 )
         throw new IllegalArgumentException( "invalid key for object cache" );

      boolean added = false;
      if ( !ms_objCache.containsKey( key ) || overwrite )
      {
         added = true;
         ms_objCache.put( key, value );
      }
      return added;
   }

   /**
    * Shortcut to htmlConverter util class, since this package has a util class
    * itself. See method makeXmlName(String) in the PSXMLDomUtil class of the package
    * <code>com.percussion.util</code>
    */
   public static String makeXmlName(String source)
   {
      return PSXMLDomUtil.makeXmlName(source);
   }


   /**
    * Extracts a specified mapping from the cache.
    *
    * @param key The map key used to lookup the desired value in the cache.
    *
    * @return The value found associated with the supplied key, or <code>null
    * </code> if no mapping has this key.
    */
   public static Object getCachedObject( String key )
   {
      return ms_objCache.get( key );
   }


   /**
    * Removes the mapping found using the supplied key.
    *
    * @param key The map key used to lookup the desired value in the cache.
    *
    * @return <code>true</code> if a mapping is removed, <code>false</code>
    * otherwise.
    */
   public static boolean removeCachedObject( String key )
   {
      boolean present = ms_objCache.containsKey( key );
      ms_objCache.remove( key );
      return present;
   }

   /** Do not construct new instances. Use the static functions. */
   private Util() {}


   ////////////////
   // private members
   private static UIFigure ms_figure;

   /**
    * An named object cache. Use <code>cacheObject</code> and <code>
    * getCachedObject</code> to access this variable.
    */
   private static HashMap<String, Object> ms_objCache = new HashMap<String, Object>();

   /**
    * The single instance of this singleton object. Use <code>getInstance</code>
    * to retrieve it.
    */
   private static Util ms_instance;

    // Sorted List of keys from  m_fileExtTable which are splitable.
   private static List<String> ms_splitExtList = new ArrayList<String>();

   /*
    * Default file extensions.
    */
   public static final String XML_FILE_EXTENSION = ".xml";
   public static final String XSL_FILE_EXTENSION = ".xsl";
   public static final String DTD_FILE_EXTENSION = ".dtd";
}
