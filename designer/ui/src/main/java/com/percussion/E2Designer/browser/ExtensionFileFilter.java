/******************************************************************************
 *
 * [ ExtensionFileFilter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;


import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.Hashtable;

/*[ ExtensionFileFilter ]**********************************************************
 *
 * $Id: ExtensionFileFilter.java 1.6 2001/07/10 18:33:34Z JedMcGraw Release $
 *
 * Version Labels   : $Name: Pre_CEEditorUI RX_40_REL $
 *
 * Locked By      : $Locker: $
 *
 * Revision History:
 *   $Log: ExtensionFileFilter.java $
 *   Revision 1.6  2001/07/10 18:33:34Z  JedMcGraw
 *   Revised documentation to eliminate warnings when making javadocs.
 *   Revision 1.5  1999/08/09 13:24:29Z  paulhoward
 *   Added getExtension() and made the extension methods public so
 *   we can have a consistent way to determine a files extension.
 *   
 *   Revision 1.4  1999/06/30 16:41:47Z  markdandrea
 *   The files tab now allows file types with wild cards.  It is also sorted
 *   correctly.  
 *   
 *   Revision 1.3  1999/06/22 20:25:16  markdandrea
 *   File tab now local.  Added filter.
 *   
 *   Revision 1.2  1999/06/22 18:10:00  markdandrea
 *   Now part of browser package.
 *   Revision 1.1  1999/06/22 18:01:08  markdandrea
 *   Initial revision
 *
 ***************************************************************************/

/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 * Example - create a new filter that filerts out all files
 * but gif and jpg image files:
 *
 *     JFileChooser chooser = new JFileChooser();
 *     ExampleFileFilter filter = new ExampleFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *     chooser.addChoosableFileFilter(filter);
 *     chooser.showOpenDialog(this);
 *
 * @version 1.8 08/26/98
 * @author Jeff Dinkins
 */
public class ExtensionFileFilter implements FileFilter {

    private Hashtable m_filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter() {
   this.m_filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExampleFileFilter("jpg");
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String extension) {
   this(extension,null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new ExampleFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String extension, String description) {
   this();
   if(extension!=null) addExtension(extension);
    if(description!=null) setDescription(description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new ExampleFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String[] filters) {
   this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public ExtensionFileFilter(String[] filters, String description) {
   this();
   for (int i = 0; i < filters.length; i++) {
       // add filters one by one
       addExtension(filters[i]);
   }
    if(description!=null) setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane,
     * false if it shouldn't.
     *
     * Files that begin with "." are ignored.
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
    public boolean accept(File f) 
   {
      if(f != null) 
      {
          if(f.isDirectory()) 
         {
            return true;
          }
       
         String extension = getExtension(f);
//         System.out.println("checking " + extension);
         if(extension != null) 
         {
            Enumeration e = m_filters.keys();
            if(e != null)
            {
               while(e.hasMoreElements())
               {
                  String strElement = e.nextElement().toString();
                  if(strElement == null)
                     continue;
                     
//                  System.out.println("against " + strElement);
                  //no wild cards just do compare
                  if(strElement.indexOf(new String("?")) == -1 &&
                     strElement.indexOf(new String("*")) == -1)
                  {
//                     System.out.println("no wild cards");
                     if(strElement.equals(extension))
                        return(true);
                  }
                  else//do wild card check
                  {
                     char[] filters = strElement.toCharArray();
                     char[] extensions = extension.toCharArray();
                     boolean bEquals = true;
                     if(filters != null && extensions != null)
                     {
                        for(int iFilter = 0; bEquals && iFilter < filters.length; ++iFilter)
                        {
                           char cFilter = filters[iFilter];
                              
//                           System.out.println("checking filter " + cFilter);
                           if(iFilter < extensions.length)
                           {
                              char cExtension = extensions[iFilter];
                                 
//                              System.out.println("against " + cExtension);
                              //if the char in the filter list is a ? then contiue checking
                              if(cFilter == '?')
                              {
//                                 System.out.println("continue on ?");
                                 continue;
                              }
                              else if(cFilter == '*')
                              {
                                 //if * go to next filter char and find it in the 
                                 //element
//                                 System.out.println("found *");
                                 int iNextFilter = iFilter + 1;
                                 //if * is the last then we made it
                                 if(!(iNextFilter < filters.length))                                 
                                 {
//                                    System.out.println("* is last");
                                    break;
                                 }
                                 
                                 char cNextFilter = filters[iNextFilter];
                                    
//                                 System.out.println("looking for " + cNextFilter);
                                 boolean bFoundNext = false;
                                 for(int iFind = iFilter; iFind < extensions.length; ++iFind)
                                 {
                                       if(extensions[iFind] == cNextFilter)
                                       {
                                          bFoundNext = true;
//                                       System.out.println("found it");
                                       break;
                                    }
                                 }
                                 
                                 bEquals = bFoundNext;
                              }
                              else
                              {
//                                 System.out.println("doing regular compare");
                                 if(cFilter != cExtension)
                                 {
                                    bEquals = false;
                                    break;
                                 }
                              }
                           }
                        }
                     }
                     
                     if(bEquals)
                        return(true);
                  }
               }
            }
         }
      }
      
      return false;
    }

   /**
    * @return A string that contains the extension of the filename of the supplied
    * file. If f is null, or its filename no extension, null is returned.
    *
    * @see #getExtension
    * @see FileFilter#accept
   **/
    public static String getExtension(File f) {
      if(f != null)
      {
         String filename = f.getName();
         return getExtension( filename );
      }
      return null;
   }

   /**
    * @return A string that contains the extension of the supplied filename.
    * If filename is null, or has no extension, null is returned.
   **/
   public static String getExtension( String filename )
   {
      if ( null != filename )
      {
         int i = filename.lastIndexOf('.');
         if(i>0 && i<filename.length()-1)
         {
            return filename.substring(i+1).toLowerCase();
         };
      }
      return null;
   }


   /**
    * Adds a filetype "dot" extension to filter against.
    *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   ExampleFileFilter filter = new ExampleFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     */
    public void addExtension(String extension) {
   if(m_filters == null) {
       m_filters = new Hashtable(5);
   }
   m_filters.put(extension.toLowerCase(), this);
   fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     */
    public String getDescription() {
   if(fullDescription == null) {
       if(description == null || isExtensionListInDescription()) {
       fullDescription = description==null ? "(" : description + " (";
      // build the description from the extension list
      Enumeration extensions = m_filters.keys();
      if(extensions != null) {
          fullDescription += "." + (String) extensions.nextElement();
          while (extensions.hasMoreElements()) {
         fullDescription += ", " + (String) extensions.nextElement();
          }
      }
      fullDescription += ")";
       } else {
      fullDescription = description;
       }
   }
   return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     */
    public void setDescription(String description) {
   this.description = description;
   fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription()
     * @see #setDescription(String)
     * @see #isExtensionListInDescription()
     */
    public void setExtensionListInDescription(boolean b) {
   useExtensionsInDescription = b;
   fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     * @see #getDescription()
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     */
    public boolean isExtensionListInDescription() {
   return useExtensionsInDescription;
    }
}
