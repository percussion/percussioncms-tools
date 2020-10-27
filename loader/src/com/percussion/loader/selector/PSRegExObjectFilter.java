/*[ PSRegExObjectFilter.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.PSItemContext;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * This class represents a regular expression and is used to exclude nodes.
 * If a node matches this regular expression it is not traversed by 
 * a {@link #PSCrawler}. The node is added to the model as existing and 
 * also being excluded from traversal.  
 * 
 * @see PSCrawler
 */
public class PSRegExObjectFilter
{
   /**
    * Accessor to set the regular expression string used 
    * for pattern matching.
    * 
    * @param s A regular expression string. May be <code>null</code> or
    *    empty. If <code>null</code> than {@link #m_strRegEx} will be empty.
    */
   public void setFilter(String s)
   {
      if (s == null)
         s = "";

      m_strRegEx = s;
   }

   /**
    * Accessor to get the regular expression string used for 
    * pattern matching.
    * 
    * @return A regular expression string. Never <code>null</code> but may 
    *    be empty.
    */
   public String getFilter()
   {
      return m_strRegEx;
   }
   
   /**
    * Checks this {@link #PSItemContext} node against the regular expression
    * defined by this class. If there is a match then this method returns
    * <code>true</code>, otherwise returns <code>false</code>. 
    * 
    * @param node An {@link #PSItemContext}. It may not be <code>null</code>.
    * 
    * @return boolean <code>false</code> represents no match 
    *    and thus this node should be traversed. 
    *    <code>true</code> represents a match 
    *    and thus the caller should not traverse this node.
    * 
    * @throws {@link org.apache.oro.text.regex.MalformedPatternException} if
    *    m_strRegEx is a malformed pattern.
    * 
    * @throws IllegalArgumentException if node is <code>null</code>.
    */
   public boolean accept(PSItemContext node)
      throws MalformedPatternException
   {
      if (node == null)
         throw new IllegalArgumentException(
            "item context node must not be null.");

      PatternCompiler compiler = new GlobCompiler();
      PatternMatcher matcher  = new Perl5Matcher();

      // may throw MalformedPatternException
      Pattern pattern = compiler.compile(m_strRegEx);

      return matcher.matches(node.getResourceId(), pattern);
   }

   /**
    * Regular expression string, intialized to empty in definition, 
    * set using <code>setFilter</code>. Never <code>null</code>
    */
   protected String m_strRegEx = "";
}
