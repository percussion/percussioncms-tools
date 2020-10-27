package de.byteaction.velocity.vaulttec.ui.editor.text;

import de.byteaction.velocity.utils.IdUtils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Guesses the start/end and the type of Velocity content (directive or
 * identifier) from a given offset.
 */
public class VelocityTextGuesser
{
   public enum Type {
      TYPE_END, TYPE_APOSTROPHE, TYPE_INVALID, TYPE_DIRECTIVE, TAG_DIRECTIVE, TAG_CLOSE, TYPE_VARIABLE, TYPE_FIELD
   }

   private Type fType;

   private String fText;

   private int fLine;

   private int tagoffset = -1;

   /**
    * Create an invalid text guesser.
    */
   public VelocityTextGuesser() {
      fType = Type.TYPE_INVALID;
      fText = "";
      fLine = -1;
   }

   public VelocityTextGuesser(IDocument aDocument, int anOffset,
         boolean aGuessEnd) {
      String f = "";
      try
      {
         // Guess start position
         int start = anOffset;
         while ((start >= 1) && isWordPart(aDocument.getChar(start - 1)))
         {
            start--;
         }
         // Guess end position
         int end = anOffset;
         if (aGuessEnd)
         {
            int len = aDocument.getLength() - 1;
            while ((end < len) && isWordPart(aDocument.getChar(end)))
            {
               end++;
            }
         }
         fText = aDocument.get(start, (end - start));
         fLine = aDocument.getLineOfOffset(start) + 1;
         // Now guess fType of completion
         if (start >= 1)
         {
            // Directive or shorthand reference
            char c1 = aDocument.getChar(start - 1);
            char c0 = 0;
            try
            {
               c0 = aDocument.getChar(start - 2);
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            try
            {
               f = IdUtils.getIdentifier(anOffset, aDocument);
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
            String fieldref = getFieldRef(anOffset, aDocument);
            if (fieldref != null)
            {
               fType = Type.TYPE_FIELD;
               fText = fieldref;
            }
            else if (f.equals("#end"))
            {
               fType = Type.TYPE_END;
               tagoffset = start - 1;
            }
            else if (c1 == '#')
            {
               fType = Type.TYPE_DIRECTIVE;
            }
            else if (c1 == '\"')
            {
               fType = Type.TYPE_APOSTROPHE;
            }
            else if (c1 == '$')
            {
               fType = Type.TYPE_VARIABLE;
            }
            else if (c1 == '<')
            {
               fType = Type.TAG_DIRECTIVE;
            }
            else if ((c1 == '/') && (c0 == '<'))
            {
               fType = Type.TAG_CLOSE;
            }
            else
            {
               if (f != null && f.trim().length() > 0)
               {
                  fType = Type.TYPE_VARIABLE;
                  fText = f;
               }
            }
         }
      }
      catch (BadLocationException e)
      {
         fType = Type.TYPE_INVALID;
         fText = "";
         fLine = -1;
      }
   }

   /**
    * Get a field ref, if there is one. A field reference is a set of alpha
    * characters that ends with a colon like jcr: or rx: and is used as part of
    * completing a JSR-170 reference.
    * 
    * @param start the current start location
    * @param document the document
    * @return the field prefix or <code>null</code> if there is no field
    * @throws BadLocationException
    */
   private String getFieldRef(int start, IDocument document)
         throws BadLocationException
   {
      int pos = start - 1;
      int end = start;

      // Skip over id characters (letters, numbers and underscore)
      while (true)
      {
         char ch = document.getChar(pos);
         if (!Character.isLetterOrDigit(ch) && ch != '_')
            break;
         pos--;
      }

      if (document.getChar(pos) != ':')
         return null;

      while (pos > 0)
      {
         pos--;
         char ch = document.getChar(pos);
         if (!Character.isLetter(ch))
         {
            pos++;
            break;
         }
      }

      if (end - pos == 0)
         return null;
      else
         return document.get(pos, end - pos);
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public int getTagOffset()
   {
      return tagoffset;
   }

   /**
    * Return the type being completed
    * 
    * @return the type of completion to perform
    */
   public Type getType()
   {
      return fType;
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public String getText()
   {
      return fText;
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public int getLine()
   {
      return fLine;
   }

   /**
    * Determines if the specified character may be part of a Velocity identifier
    * as other than the first character. A character may be part of a Velocity
    * identifier if and only if it is one of the following:
    * <ul>
    * <li>a letter (a..z, A..Z)
    * <li>a digit (0..9)
    * <li>a hyphen ("-")
    * <li>a connecting punctuation character ("_")
    * <li>a period (".")
    * </ul>
    * 
    * @param aChar the character to be tested.
    * @return true if the character may be part of a Velocity identifier; false
    *         otherwise.
    * @see java.lang.Character#isLetterOrDigit(char)
    */
   private static final boolean isWordPart(char aChar)
   {
      return Character.isLetterOrDigit(aChar) || (aChar == '-')
            || (aChar == '_') || (aChar == '.');
   }

   /**
    * DOCUMENT ME!
    * 
    * @return DOCUMENT ME!
    */
   public String toString()
   {
      return "type=" + fType + ", text=" + fText + ", line=" + fLine;
   }
}
