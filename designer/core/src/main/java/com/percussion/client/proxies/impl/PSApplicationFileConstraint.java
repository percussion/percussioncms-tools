/******************************************************************************
 *
 * [ PSApplicationFileConstraint.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.design.objectstore.PSApplicationFile;
import org.jmock.core.Constraint;

import java.io.File;

/**
 * Constraint which insures correct expected PSApplicationFile.
 * Used for testing with jmock library. 
 */
public class PSApplicationFileConstraint implements Constraint
{
   /**
    * Creates new constraint.
    * @param expectedPath path to check the file for.
    */
   public PSApplicationFileConstraint(String expectedPath)
   {
      m_expectedPath = expectedPath;
   }

   /**
    * Performs the validation.
    * @see Constraint#eval(java.lang.Object)
    */
   public boolean eval(Object obj)
   {
      if (!(obj instanceof PSApplicationFile))
      {
         return false;
      }
      final PSApplicationFile file = (PSApplicationFile) obj;
      return new File(m_expectedPath).equals(file.getFileName());
   }

   /**
    * Constraint description
    */
   public StringBuffer describeTo(StringBuffer buf)
   {
      return buf.append("application file constraint with path \"")
            .append(m_expectedPath)
            .append("\"");
   }
   
   /**
    * Path the application file is checked for.
    */
   final private String m_expectedPath;
}