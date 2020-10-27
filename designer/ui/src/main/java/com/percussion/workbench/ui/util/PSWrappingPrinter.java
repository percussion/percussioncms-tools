/******************************************************************************
 *
 * [ PSWrappingPrinter.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;

/**
 * Helper class that performs the printing, wrapping text as necessary.
 * Example usage:
 * <code>
 * <pre>
 *       PrintDialog dlg = new PrintDialog(getShell());
 *       PrinterData pData = dlg.open();
 *       if(pData != null)
 *       {
 *           
 *          Printer printer = new Printer(pData);
 *          try
 *          {                        
 *             new PSWrappingPrinter(printer, "Printing stack trace...",
 *                mi_text.getText()).print();                        
 *          }
 *          finally
 *          {
 *             printer.dispose();
 *          }
 *       }
 *             
 * </pre>
 * </code>
 * This object was taken from publically available code originally written
 * by Rob Warner and Robert Harris. There is no usage license for this code.
 */
class PSWrappingPrinter 
{   
   /**
    * WrappingPrinter constructor
    * @param printer the printer, cannot be <code>null</code>.
    * @param fileName the fileName, cannot be <code>null</code> or
    * empty.
    * @param contents the contents cannot be <code>null</code>, may
    * be empty.
    */
   PSWrappingPrinter(Printer printer, String fileName, String contents)
   {
      if(printer == null)
         throw new IllegalArgumentException("printer cannot be null.");
      if(StringUtils.isBlank(fileName))
         throw new IllegalArgumentException(
            "fileName cannot be null or empty.");
      if(contents ==  null)
         throw new IllegalArgumentException("contents cannot be null.");
      m_printer = printer;
      m_fileName = fileName;
      m_contents = contents;
   }
   
   /**
    * Prints the file
    */
   void print() 
   {
      // Start the print job
      if (m_printer.startJob(m_fileName)) {
         // Determine print area, with margins
         m_bounds = computePrintArea(m_printer);
         m_xPos = m_bounds.x;
         m_yPos = m_bounds.y;
         
         // Create the GC
         m_gc = new GC(m_printer);
         
         // Determine line height
         m_lineHeight = m_gc.getFontMetrics().getHeight();
         
         // Determine tab width--use three spaces for tabs
         int tabWidth = m_gc.stringExtent("   ").x;
         
         // Print the text
         m_printer.startPage();
         m_buffer = new StringBuilder();
         char c;
         for (int i = 0, n = m_contents.length(); i < n; i++) {
            // Get the next character
            c = m_contents.charAt(i);
            
            // Check for newline
            if (c == '\n') {
               printBuffer();
               printNewline();
            }
            // Check for tab
            else if (c == '\t') {
               m_xPos += tabWidth;
            }
            else {
               m_buffer.append(c);
               // Check for space
               if (Character.isWhitespace(c)) {
                  printBuffer();
               }
            }
         }
         m_printer.endPage();
         m_printer.endJob();
         m_gc.dispose();
      }
   }
   
   /**
    * Prints the contents of the buffer
    */
   void printBuffer() 
   {
      // Get the width of the rendered buffer
      int width = m_gc.stringExtent(m_buffer.toString()).x;
      
      // Determine if it fits
      if (m_xPos + width > m_bounds.x + m_bounds.width) {
         // Doesn't fit--wrap
         printNewline();
      }
      
      // Print the buffer
      m_gc.drawString(m_buffer.toString(), m_xPos, m_yPos, false);
      m_xPos += width;
      m_buffer.setLength(0);
   }
   
   /**
    * Prints a newline
    */
   void printNewline() 
   {
      // Reset x and y locations to next line
      m_xPos = m_bounds.x;
      m_yPos += m_lineHeight;
      
      // Have we gone to the next page?
      if (m_yPos > m_bounds.y + m_bounds.height) {
         m_yPos = m_bounds.y;
         m_printer.endPage();
         m_printer.startPage();
      }
   }
   
   /**
    * Computes the print area, including margins
    * @param printer the printer
    * @return Rectangle
    */
   Rectangle computePrintArea(Printer printer) 
   {
      // Get the printable area
      Rectangle rect = printer.getClientArea();
      
      // Compute the trim
      Rectangle trim = printer.computeTrim(0, 0, 0, 0);
      
      // Get the printer's DPI
      Point dpi = printer.getDPI();
      
      // Calculate the printable area, using 1 inch margins
      int left = trim.x + dpi.x;
      if (left < rect.x) left = rect.x;
      
      int right = (rect.width + trim.x + trim.width) - dpi.x;
      if (right > rect.width) right = rect.width;
      
      int top = trim.y + dpi.y;
      if (top < rect.y) top = rect.y;
      
      int bottom = (rect.height + trim.y + trim.height) - dpi.y;
      if (bottom > rect.height) bottom = rect.height;
      
      return new Rectangle(left, top, right - left, bottom - top);
   }
   
   /**
    * The printer, never <code>null</code>, initialized in ctor.
    */
   private Printer m_printer;
   
   /**
    * The name of the file that will be displayed to the user while
    * printing. Initialized in ctor. Never <code>null</code> or empty.
    */
   private String m_fileName;
   
   /**
    * The file contents to be printed. Initialized in the ctor.
    * Never <code>null</code>, may be empty.
    */
   private String m_contents; 
   
   /**
    * The graphics "canvas" that will be used to print to. Initialized
    * in {@link #print()}. This object is also disposed at the end
    * of that method.
    */
   private GC m_gc; 
   
   /**
    * The buffer used in building the print content. Initialized
    * in {@link #print()}.
    */
   private StringBuilder m_buffer; 
   
   // Various variables used to hold posistioning info
   private int m_xPos; 
   private int m_yPos; 
   private Rectangle m_bounds;   
   private int m_lineHeight; 
}