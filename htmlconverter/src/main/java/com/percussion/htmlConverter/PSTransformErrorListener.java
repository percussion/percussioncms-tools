/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.htmlConverter;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * An error handler for JAXP Transformers which keeps the errors in
 * a list accessible after the transformation is complete.
 */
public class PSTransformErrorListener implements ErrorListener
{
   /**
    * Use this constructor when parser errors should be written usually to
    * a log or trace file, Otherwise can use default constructor.
    * Wraps the passed in writer instance to <code>PrintWriter</code> if it is
    * not of type <code>PrintWriter</code> and sets autoflushing to
    * <code>true</code>.
    *
    * @param pw The writer to log the warnings, errors and fatal errors, may
    *    not be <code>null</code>.
    *
    * @throws IllegalArgumentException if the writer is <code>null</code>.
    *
    * @see #throwOnWarnings
    * @see #throwOnErrors
    * @see #throwOnFatalErrors
    */
   public PSTransformErrorListener(Writer pw)
   {
      if (pw == null)
         throw new IllegalArgumentException(
            "The writer to which errors should be written can not be null.");

      if (pw instanceof PrintWriter)
         m_printWriter = (PrintWriter) pw;
      else
         m_printWriter = new PrintWriter(pw, true);
   }

   /**
    * Receive notification of an error during transformation. Adds the error 
    * to the writer provided in the constructor.
    *
    * @see ErrorListener#error error
    * @throws TransformerException is never thrown.
    */
   public void error(TransformerException e) throws TransformerException
   {
      m_errors.add(e);
      System.out.println("Error: " + e.getLocalizedMessage());
      m_printWriter.println("Error: " + e.getLocalizedMessage());
   }

   /**
    * Receive notification of a fatal error during transformation. Adds the 
    * to the writer provided in the constructor.
    *
    * @see ErrorListener#fatalError fatalError
    * @throws TransformerException is never thrown.
    */
   public void fatalError(TransformerException e) throws TransformerException
   {
      m_fatalErrors.add(e);
      System.out.println("Fatal Error: " + e.getLocalizedMessage());
      m_printWriter.println("Fatal Error: " + e.getLocalizedMessage());
   }

   /**
    * Receive notification of a warning during transformation.
    * Logs the exception to the writer if it is specified. Throws
    * TransformerException if throw exception on warning flag is set to
    * <code>true</code>.
    *
    * @see ErrorListener#warning warning
    * @throws TransformerException is never thrown.
    **/
   public void warning(TransformerException e)
      throws TransformerException
   {
      m_warnings.add(e);
      System.out.println("Warning: " + e.getLocalizedMessage());
      m_printWriter.println("Warning: " + e.getLocalizedMessage());
   }

   /**
    * Did the transformation produce errors.
    *
    * @return <code>true</code> if errors occurred, <code>false</code>
    *    otherwise.
    */
   public boolean hasErrors()
   {
      return (numErrors() > 0 || numFatalErrors() > 0);
   }
   
   /**
    * Did the transformation produce warnings.
    *
    * @return <code>true</code> if warnings occurred, <code>false</code>
    *    otherwise.
    */
   public boolean hasWarnings()
   {
      return (numWarnings() > 0);
   }
   
   /**
    * Returns number of errors.
    *
    * @return number of errors
    */
   public int numErrors()
   {
      return m_errors.size();
   }

   /**
    * Returns number of fatal errors.
    *
     * @return number of fatal errors
    */
   public int numFatalErrors()
   {
      return m_fatalErrors.size();
   }

   /**
    * Returns number of warnings.
    *
     * @return number of warnings
    */
   public int numWarnings()
   {
      return m_warnings.size();
   }

   /**
    * Returns iterator of errors list.
    *
    * @return iterator of errors list, never <code>null</code>.
    */
   public Iterator errors()
   {
      return m_errors.iterator();
   }

   /**
    * Returns iterator of fatal errors list.
    *
    * @return iterator of fatal errors list, never <code>null</code>.
    */
   public Iterator fatalErrors()
   {
      return m_fatalErrors.iterator();
   }

   /**
    * Returns iterator of warnings list.
    *
    * @return iterator of warnings list, never <code>null</code>.
    */
   public Iterator warnings()
   {
      return m_warnings.iterator();
   }

   /**
    * The list of errors, initialized to empty array list and never
    * <code>null</code> after that.
    */
    private List m_errors = new ArrayList();

   /**
    * The list of fatal errors, initialized to empty array list and never
    * <code>null</code> after that.
    */
   private List m_fatalErrors = new ArrayList();

   /**
    * The list of warnings, initialized to empty array list and never
    * <code>null</code> after that.
    */
   private List m_warnings = new ArrayList();

   /**
    * The Print Writer to which warnings, errors and fatal errors should be
    * written if it is specified. Initialized in constructor, never
    * <code>null</code> after that.
    */
   private PrintWriter m_printWriter = null;
}
