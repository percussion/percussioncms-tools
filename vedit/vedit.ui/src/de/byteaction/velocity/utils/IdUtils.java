/******************************************************************************
 *
 * [ IdUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package de.byteaction.velocity.utils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Utilities for dealing with identifiers
 * 
 * @author dougrand
 */
public class IdUtils
{
   /**
    * Is the given character part of an identifier
    * 
    * @param ch character
    * @return <code>true</code> if the character is part of an id
    */
   public static boolean isIdChar(char ch)
   {
      return Character.isLetterOrDigit(ch) || ch == '.' || ch == '-'
            || ch == '_';
   }

   /**
    * Find and return an identifier if one is present. Identifiers start with a
    * dollar sign. That is followed by an optional exclamation point and an
    * optional open curly brace. The text of the identifier is a series of
    * letters, digits, hyphens, underscores and periods.
    * <p>
    * Secondly, this code needs to recognize macro references. A macro reference
    * starts with a pound sign and is followed by letters, digits, etc. So that
    * is OK too.
    * <p>
    * So this method searches backward over the letters, digits, etc. Then it
    * checks for the start sequence. It returns only the name of the identifier.
    * The replacement code will only replace that portion of the id being
    * referenced.
    * 
    * @param anOffset the start position
    * @param document the document, never <code>null</code>
    * @return the variable reference or an empty string if there isn't a
    *         variable found
    */
   public static String getIdentifier(int anOffset, IDocument document)
   {
      if (document == null)
      {
         throw new IllegalArgumentException("document may not be null");
      }

      int end = anOffset;
      int pos = anOffset - 1;

      try
      {
         // Skip variable characters
         while (pos > 0 && isIdChar(document.getChar(pos)))
         {
            pos--;
         }
         if (pos < 0)
            return "";

         // Validate the sequence
         char c = pos > 0 ? document.getChar(pos) : 0;
         char b = pos - 1 > 0 ? document.getChar(pos - 1) : 0;
         char a = pos - 2 > 0 ? document.getChar(pos - 2) : 0;

         if (c == '$' || c == '#'
               || (c == '{' && ((b == '$') || (b == '!' && a == '$'))))
         {
            return document.get(pos + 1, end - pos).trim();
         }
         else
            return "";
      }
      catch (BadLocationException e)
      {
         return "";
      }
   }

   /**
    * Find end of identifier
    * 
    * @param doc the document, never <code>null</code>
    * @param start the starting position
    * @param method if <code>true</code> then look for a parameter list after
    *           the id and skip that as well
    * @return the end position
    * @throws BadLocationException
    */
   public static int findEndOfIdentifier(IDocument doc, int pos, boolean method)
   {
      try
      {
         if (doc == null)
         {
            throw new IllegalArgumentException("doc may not be null");
         }
         int end = pos;

         // Skip to end of id.
         while (isIdChar(doc.getChar(end)))
         {
            end++;
            if (end >= doc.getLength())
               return doc.getLength();
         }

         // Only search if we aren't where we started
         if (method && end < doc.getLength() && end != pos)
         {
            // Skip any whitespace
            while (Character.isWhitespace(doc.getChar(end)))
            {
               if (end >= doc.getLength())
                  return doc.getLength();
               end++;
            }
            // If method then skip over parameters. First find a paren
            if (doc.getChar(end) == '(')
            {
               end = findMatchingParen(doc, end);
            }
         }

         return end;
      }
      catch (BadLocationException e)
      {
         return doc.getLength();
      }
   }

   /**
    * Find a matching parenthesis in the document. Just skip over quoted strings
    * and find the paren, no concern with correctness.
    * 
    * @param doc
    * @param end
    * @return
    * @throws BadLocationException
    */
   private static int findMatchingParen(IDocument doc, int end)
         throws BadLocationException
   {
      while (doc.getChar(end) != ')')
      {

         if (doc.getChar(end) == '"')
         {
            end = findMatchingQuote(doc, end + 1, '"');
         }
         else if (doc.getChar(end) == '\'')
         {
            end = findMatchingQuote(doc, end + 1, '\'');
         }
         else
         {
            end++;
         }
         if (end >= doc.getLength())
            return doc.getLength();
      }
      return end+1;
   }

   /**
    * Skip quoted string
    * 
    * @param doc
    * @param pos
    * @param quote
    * @return
    * @throws BadLocationException
    */
   private static int findMatchingQuote(IDocument doc, int pos, char quote)
         throws BadLocationException
   {
      while (doc.getChar(pos) != quote)
      {
         if (doc.getChar(pos) == '\\')
            pos++; // Skip next character
         pos++;
         if (pos >= doc.getLength())
            return doc.getLength();
      }
      return pos;
   }

   /**
    * Skip over special characters until we're pointing at the first id
    * character. Don't search beyond end of line
    * 
    * @param pos
    * @param document
    * @return
    */
   public static int skipToIdStart(int pos, IDocument document)
   {
      if (document == null)
      {
         throw new IllegalArgumentException("document may not be null");
      }

      try
      {
         while (!isIdChar(document.getChar(pos)))
         {
            if (document.getChar(pos) == '\n') break;
            pos++;
            if (pos >= document.getLength())
               return document.getLength();
         }

         return pos;
      }
      catch (BadLocationException e)
      {
         return pos;
      }
   }

}
