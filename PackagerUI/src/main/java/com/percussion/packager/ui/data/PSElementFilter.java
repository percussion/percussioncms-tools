/******************************************************************************
 *
 * [ PSElementFilter.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui.data;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element node filtering class.
 */
public class PSElementFilter
{
   
   /**
    * Ctor
    * @param expression the filter expression, may be <code>null</code>
    * or empty.
    * @param isRegEx flag indicating usage of regEx for filter matching.
    * If <code>false</code> the Glob Type matching will be used.
    * @param selectedOnly flag indicating that only selected elements
    * will be allowed.
    */
   public PSElementFilter(
      String expression, boolean isRegEx, boolean selectedOnly,
      boolean isCaseSensitive)
   {
      m_expression = expression;
      m_isRegEx = isRegEx;
      m_selectedOnly = selectedOnly;
      m_isCaseSensitive = isCaseSensitive;
   }
   
   /**
    * Decide whether the given element name is accepted by this filter.
    * @param node element node, may be <code>null</code>.
    * @return <code>true</code> if accepted.
    */
   public boolean accept(PSElementNode node)
   {
      if(node == null)
         return false;
      if(m_selectedOnly && !node.isSelected())
         return false;
      if(StringUtils.isNotBlank(m_expression))
      {
         if(m_isRegEx)
            return doRegEx(node);
         try
         {
            return doGlob(node);
         }
         catch (MalformedPatternException e)
         {
            return false;
         }
         
      }
      return true;
   }
   
   /**
    * DoesRegEx match on the element name.
    * @param node assumed not <code>null</code>.
    * @return <code>true</code> if matches.
    */
   private boolean doRegEx(PSElementNode node)
   {
      int mask = m_isCaseSensitive ? 
               GlobCompiler.DEFAULT_MASK :
                  GlobCompiler.CASE_INSENSITIVE_MASK;
      Pattern pattern = m_isCaseSensitive ?
         Pattern.compile(m_expression) : 
            Pattern.compile(m_expression, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(node.getName());
      return matcher.matches();
   }
   
   /**
    * Does Glob type match on the element name.
    * @param node assumed not <code>null</code>.
    * @return <code>true</code> if matches.
    * @throws MalformedPatternException
    */
   private boolean doGlob(PSElementNode node) throws MalformedPatternException
   {
      PatternCompiler compiler = new GlobCompiler();
      PatternMatcher matcher = new Perl5Matcher();
      int mask = m_isCaseSensitive ? 
         GlobCompiler.DEFAULT_MASK :
            GlobCompiler.CASE_INSENSITIVE_MASK;

      return matcher.matches(node.getName(), 
         compiler.compile(m_expression, mask));
   }
   
   /**
    * The filter expression, may be <code>null</code>.
    */
   private String m_expression;
   
   /**
    * Flag indicating that regex matching should be used, else
    * use Glob type matching.
    */
   private boolean m_isRegEx;
   
   /**
    * Flag indicating the filter is case sensitive.
    */
   private boolean m_isCaseSensitive;
   
   /**
    * Flag indicating that only selected elements should show.
    */
   private boolean m_selectedOnly;
}
