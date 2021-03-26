/*[ PSPage.java ]**************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.util.PSStringOperation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import websphinx.Link;
import websphinx.Page;
import websphinx.Tag;

/**
 * Derived from {@link websphinx.Page} to add some convience methods
 * for processing html (e.g. <code>getBody</code>} and providing
 * value substitution.
 * 
 * @see websphinx.Page for description of base class.   
 */
public class PSPage extends Page
{
   /**
    * Constructs this object from a String representation of 
    * the pages content and a base url.
    * 
    * @param strBase a String representation of the base url to 
    *    use for resolving relative paths encountered in the parsing 
    *    of <code>strContent</code>. Assumed not <code>null</code>.
    * 
    * @param strContent a String representation of an html pages
    *    content. Assumed not <code>null</code> or empty.
    * 
    * @throws MalformedURLException if <code>strBase</code> is invalid.
    */
   public PSPage(String strBase, String strContent)
      throws MalformedURLException
   {
      super(new URL(strBase), strContent);    
   }

   /**
    * Constructs this object from a String representation of 
    * the pages content.
    * 
    * @param strContent a String representation of an html pages
    *    content. Assumed not <code>null</code> or empty.
    */
   public PSPage(String strContent)      
   {
      super(null, strContent);    
   }

   /**
    * Constructs this object from a Link. The page that corresponds to
    * this link will be downloaded and parsed.  
    * 
    * @see base class for more description
    */
   public PSPage(websphinx.Link l) throws IOException
   {
      super(l);
   }

   /**
    * Returns only the body tag of this HTML document
    * 
    * @return a String of the body tag of this document. 
    *    Never <code>null</code> may be empty.
    */
   public String getBody()
   {
      Tag [] tags = getTags();

      if (tags == null)
         return "";

      for (int i=0; i<tags.length; i++)
      {
         Tag t = tags[i];

         if (t.getTagName().equals(Tag.BODY))
         {
           return t.getElement().toHTML();
         }
      }
      
      return "";
   }

   /**
    * Substitutes url destination and resets the result of calling 
    * <code>getContent</code> for this page.
    * 
    * @param origContent the content of the page to modify. Never <code>
    *    null</code> may be empty.
    * 
    * @param l a Link to substitute its destination with 
    *    <code>strUrl</code>. Never <code>null</code>.
    * 
    * @param strUrl the new string to substitute. Never <code>
    *    null</code> or empty.
    * 
    * @throws IllegalArgumentException if any invalid parameters. 
    * 
    * @return String the content of new page. Never <code>null</code> or
    *    empty.
    */
   public String substituteLink(String origContent, Link l, String strNew)
   {  
      if (origContent == null || l == null || strNew == null)
         throw new IllegalArgumentException(
            "origContent, l and strNew must not be null");

      if (strNew.trim().length() < 1)
         throw new IllegalArgumentException(
            "strNew must not be empty");

      return substituteString(origContent, l.toHTML(), strNew);
   }

   /**
    * Substitutes <code>strMatch</code> with <code>strNew</code> within 
    * its links.
    * 
    * @param origContent the source string. Never <code>null</code> may
    * be empty.
    * 
    * @param strMatch a String to match. Never <code>null</code> or empty.
    * 
    * @param strNew the new String the will appear if any and all matches 
    *    are found. Never <code>null</code> but may be empty to effectively 
    *    remove <code>strMatch</code>.
    *    
    * @return the String result of the substitution
    * 
    * @throws IllegalArgumentException if parameters are invalid.
    */
   public String substituteString(String origContent, 
      String strMatch, String strNew)
   {
      if (origContent == null || 
         strMatch == null || strMatch.trim().length() < 1)
         throw new IllegalArgumentException(
            "origContent, strMatch must not be null or empty");
      
      if (strNew == null)
         throw new IllegalArgumentException(
            "strNew must not be null");

      return PSStringOperation.replace(
         origContent, strMatch, strNew);
   }
}
      