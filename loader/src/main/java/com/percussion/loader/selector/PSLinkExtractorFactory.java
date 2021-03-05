/*[ PSLinkExtractorFactory.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSItemContext;

import java.util.Vector;

import websphinx.Link;
import websphinx.Page;

/**
 * This class is responsible for providing the correct
 * (@link #IPSLinkExtractor} given a particular {@link #PSItemContext}. 
 * <p>
 * For now, the pairing of node <=> link
 * extractors is configured in the content loader configuration. 
*/

public class PSLinkExtractorFactory
{  
   /**
    * Gets the correct link extractor based on the node that is
    * provided. 
    * 
    * @param node An {@link #PSItemContext} node to be extracted.
    *    Never <code>null</code>.
    * 
    * @return {@link #IPSLinkExtractor} if one exists for this 
    *    node. Returns <code>null</code> if no proper link extractor
    *    exists.
    * 
    * @throws IllegalArgumentException if node is <code>null</code>
    * 
    */
   public IPSLinkExtractor getLinkExtractor(PSItemContext node)
   {
      if (node == null)
         throw new IllegalArgumentException(
            "Link extractor factory: item context cannot be null.");

      IPSLinkExtractor lEx = null;
      Object o = node.getDataObject();
      Link l = null;
      Page p = null;
      String strCmp = "";

      if (o != null)
      {
         if (o instanceof Link)
         {
            l = (Link) o;
         }
         else if (o instanceof Page)
         {
            p = (Page) o;
         }
      }
      
      for (int i=0; i<m_vExtractors.size(); i++)
      {
         lEx = (IPSLinkExtractor) m_vExtractors.elementAt(i);

         if (l != null)
         {
            strCmp = l.getPage().getContentType();
         }
         else if (p != null)
         {
            strCmp = p.getContentType();
         }
         else
         {
            strCmp = node.getResourceMimeType();
         }
         
         if (lEx.getContentType().equalsIgnoreCase(strCmp))
            return lEx;
      }

      return null;
   }

   /**
    * Adds a {@link #IPSLinkExtractor} to the list of 
    * available extractors.
    * 
    * @param l a {@link #IPSLinkExtractor} to add. 
    *    Never <code>null</code>.
    * 
    * @throws IllegalArgumentException when {@link #l} is <code>null</code>
    */
   public void addLinkExtractor(IPSLinkExtractor l)
   {
      if (l == null)
         throw new IllegalArgumentException("link extractor must not be null.");

      if (!m_vExtractors.contains(l))
         m_vExtractors.addElement(l);
   }

   /**
    * Removes the specified link extractor from the list.
    * 
    * @param l a {@link #IPSLinkExtractor} to remove.
    *    Never <code>null</code>.
    * 
    * @throws IllegalArgumentException when {@link #l} is <code>null</code>.
    */
   public IPSLinkExtractor removeLinkExtractor(IPSLinkExtractor l)
   {
      if (l == null)
         throw new IllegalArgumentException("link extractor must not be null.");

      int nIndex = m_vExtractors.indexOf(l);

      if (nIndex <= 0)
         return null;

      IPSLinkExtractor lEx = (IPSLinkExtractor) m_vExtractors.elementAt(nIndex);
      m_vExtractors.removeElementAt(nIndex);
      return lEx;
   }
   
   /**
    * Attributes
    */

   /**
    * A Collection of the link extractors.
    */
   private Vector m_vExtractors = new Vector();
}
