/******************************************************************************
*
* [ PSProblemSet.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple class that represent a list of field problems in an 
 * editor or wizard page.
 */
public class PSProblemSet
{
   
  /**
   * Adds a new problem to the set
   * @param type on of the TYPE_XXX constants
   * @param fieldname the name of the field where the problem
   * occurred. Cannot be <code>null</code> or empty.
   * @param description the description of the problem. May be
   * <code>null</code> or empty.
   * @param pagename may be <code>null</code> or empty.
   */
   public void addProblem(int type, String fieldname, String description,
      String pagename)
   {
      if(StringUtils.isBlank(fieldname))
         throw new IllegalArgumentException("fieldname cannot be null or empty.");
      m_problems.add(new Problem(type, fieldname, description, pagename));   
   }
   
   /**
    * @return iterator of all problems in this set.
    * Never <code>null</code>, may be empty.
    */
   public Iterator<Problem> getProblems()
   {
      return m_problems.iterator();
   }
   
   /**
    * Determine if the problem set is empty.
    * @return <code>true</code> if empty.
    */
   public boolean isEmpty()
   {
      return m_problems.isEmpty();
   }
   
   /**
    * @return the number of problem entries in this set
    */
   public int size()
   {
      return m_problems.size();
   }
   
   /* 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(obj == null)
         return false;
      if(!(obj instanceof PSProblemSet))
         return false;
      PSProblemSet set = (PSProblemSet)obj;
      if(set.size() != size())
         return false;
      Iterator it = set.getProblems();
      while(it.hasNext())
      {
         if(!m_problems.contains(it.next()))
            return false;
      }
      return true;
   }
   
   /* 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }
   
   
   /**
    * Simple class that represents a field level problem in the
    * editors and wizards.
    */
   public class Problem
   {
      /**
       * Create a new problem object
       * @param type one of the TYPE_XXX constants
       * @param fieldname the name of the field where the problem
       * occurred.
       * @param description the description of the problem.
       * @param pagename the name of the page the error occurred on.
       * May be <code>null</code> or empty.
       */
      public Problem(int type, String fieldname, String description,
         String pagename)
      {
         if(type < 0 || type > 2)
            type = 0;
         mi_type = type;
         mi_fieldname = fieldname;
         mi_description = description;
         mi_pagename = pagename;
      }
      
      /** 
       * @return the problem type
       */
      public int getType()
      {
         return mi_type;
      }
      
      /** 
       * @return the problem field name, never <code>null</code>,
       * may be empty.
       */
      public String getFieldname()
      {
         return StringUtils.defaultString(mi_fieldname);
      }
      
      /** 
       * @return the problem description, never <code>null</code>,
       * may be empty.
       */
      public String getDescription()
      {
         return StringUtils.defaultString(mi_description);
      }
      
      /** 
       * @return the problem pagename, never <code>null</code>,
       * may be empty.
       */
      public String getPagename()
      {
         if(mi_pagename == null)
            return "";
         return mi_pagename.replaceAll("&", "");
      }
      
      /* 
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(Object obj)
      {
         if(obj == null)
            return false;
         if(!(obj instanceof Problem))
            return false;
         Problem problem = (Problem)obj;
         return (problem.getType() == getType() &&
            problem.getDescription().equals(getDescription()) &&
            problem.getFieldname().equals(getFieldname()) &&
            problem.getPagename().equals(getPagename()));         
      }
      
      /* 
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return super.hashCode();
      }
      
      
      private int mi_type;
      private String mi_fieldname;
      private String mi_description;
      private String mi_pagename;
      
      
   }
   
   private List<Problem> m_problems = new ArrayList<Problem>();
   
   public static final int TYPE_ERROR = 0;
   public static final int TYPE_WARN = 1;
   public static final int TYPE_INFO = 2;
   
   
}


