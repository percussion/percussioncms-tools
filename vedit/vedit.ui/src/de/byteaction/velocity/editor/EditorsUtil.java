/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.byteaction.velocity.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IWordDetector;

/**
 * DOCUMENT ME!
 * 
 * @version $Revision: 1.1 $
 * @author <a href="mailto:as@byteaction.de">Akmal Sarhan </a>
 */
public class EditorsUtil
{

    private static final char[]     CR                = { '\r' };
    private static final char[]     LF                = { '\n' };
    private static final char[]     CRLF              = { '\r', '\n' };
    private static final char[]     EMPTY             = {};
    /**
     * Characters used for escape operations
     */
    private static final String[][] HTML_ESCAPE_CHARS = { { "&lt;", "<" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&gt;", ">" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&amp;", "&" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&quot;", "\"" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&agrave;", "à" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Agrave;", "À" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&acirc;", "â" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&auml;", "ä" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Auml;", "Ä" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Acirc;", "Â" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aring;", "å" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Aring;", "Å" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aelig;", "æ" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&AElig;", "Æ" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ccedil;", "ç" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ccedil;", "Ç" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&eacute;", "é" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Eacute;", "É" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aacute;", "á" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Aacute;", "Á" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&egrave;", "è" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Egrave;", "È" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ecirc;", "ê" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ecirc;", "Ê" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&euml;", "ë" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Euml;", "Ë" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&iuml;", "ï" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Iuml;", "Ï" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&iacute;", "í" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Iacute;", "Í" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&atilde;", "ã" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Atilde;", "Ã" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&otilde;", "õ" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Otilde;", "Õ" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&oacute;", "ó" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Oacute;", "Ó" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ocirc;", "ô" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ocirc;", "Ô" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ouml;", "ö" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ouml;", "Ö" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&oslash;", "ø" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Oslash;", "Ø" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&szlig;", "ß" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ugrave;", "ù" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ugrave;", "Ù" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&uacute;", "ú" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Uacute;", "Ú" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ucirc;", "û" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ucirc;", "Û" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&uuml;", "ü" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Uuml;", "Ü" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&nbsp;", " " }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&reg;", "\u00AE" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&copy;", "\u00A9" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&euro;", "\u20A0" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&#8364;", "\u20AC" } //$NON-NLS-1$ //$NON-NLS-2$
                                                      };

    /**
     * Get html entity for escape character
     * @return null, if no entity found for given character
     */
    public static final String getEntityForChar(char ch)
    {
        switch (ch)
        {
            case '<':
                return "&lt;"; //$NON-NLS-1$
            case '>':
                return "&gt;"; //$NON-NLS-1$
            case '&':
                return "&amp;"; //$NON-NLS-1$
            case '"':
                return "&quot;"; //$NON-NLS-1$
            case 'à':
                return "&agrave;"; //$NON-NLS-1$
            case 'á':
                return "&aacute;"; //$NON-NLS-1$
            case 'À':
                return "&Agrave;"; //$NON-NLS-1$
            case 'Á':
                return "&Aacute;"; //$NON-NLS-1$
            case 'â':
                return "&acirc;"; //$NON-NLS-1$
            case 'Â':
                return "&Acirc;"; //$NON-NLS-1$
            case 'ä':
                return "&auml;"; //$NON-NLS-1$
            case 'Ä':
                return "&Auml;"; //$NON-NLS-1$
            case 'å':
                return "&aring;"; //$NON-NLS-1$
            case 'Å':
                return "&Aring;"; //$NON-NLS-1$
            case 'ã':
                return "&atilde;"; //$NON-NLS-1$
            case 'Ã':
                return "&Atilde;"; //$NON-NLS-1$
            case 'æ':
                return "&aelig;"; //$NON-NLS-1$
            case 'Æ':
                return "&AElig;"; //$NON-NLS-1$
            case 'ç':
                return "&ccedil;"; //$NON-NLS-1$
            case 'Ç':
                return "&Ccedil;"; //$NON-NLS-1$
            case 'é':
                return "&eacute;"; //$NON-NLS-1$
            case 'É':
                return "&Eacute;"; //$NON-NLS-1$
            case 'è':
                return "&egrave;"; //$NON-NLS-1$
            case 'È':
                return "&Egrave;"; //$NON-NLS-1$
            case 'ê':
                return "&ecirc;"; //$NON-NLS-1$
            case 'Ê':
                return "&Ecirc;"; //$NON-NLS-1$
            case 'ë':
                return "&euml;"; //$NON-NLS-1$
            case 'Ë':
                return "&Euml;"; //$NON-NLS-1$
            case 'í':
                return "&iacute;"; //$NON-NLS-1$
            case 'Í':
                return "&Iacute;"; //$NON-NLS-1$
            case 'ï':
                return "&iuml;"; //$NON-NLS-1$
            case 'Ï':
                return "&Iuml;"; //$NON-NLS-1$
            case 'õ':
                return "&otilde;"; //$NON-NLS-1$
            case 'Õ':
                return "&Otilde;"; //$NON-NLS-1$
            case 'ó':
                return "&oacute;"; //$NON-NLS-1$
            case 'ô':
                return "&ocirc;"; //$NON-NLS-1$
            case 'Ó':
                return "&Oacute;"; //$NON-NLS-1$
            case 'Ô':
                return "&Ocirc;"; //$NON-NLS-1$
            case 'ö':
                return "&ouml;"; //$NON-NLS-1$
            case 'Ö':
                return "&Ouml;"; //$NON-NLS-1$
            case 'ø':
                return "&oslash;"; //$NON-NLS-1$
            case 'Ø':
                return "&Oslash;"; //$NON-NLS-1$
            case 'ß':
                return "&szlig;"; //$NON-NLS-1$
            case 'ù':
                return "&ugrave;"; //$NON-NLS-1$
            case 'Ù':
                return "&Ugrave;"; //$NON-NLS-1$
            case 'ú':
                return "&uacute;"; //$NON-NLS-1$
            case 'Ú':
                return "&Uacute;"; //$NON-NLS-1$
            case 'û':
                return "&ucirc;"; //$NON-NLS-1$
            case 'Û':
                return "&Ucirc;"; //$NON-NLS-1$
            case 'ü':
                return "&uuml;"; //$NON-NLS-1$
            case 'Ü':
                return "&Uuml;"; //$NON-NLS-1$
            case '\u00AE':
                return "&reg;"; //$NON-NLS-1$
            case '\u00A9':
                return "&copy;"; //$NON-NLS-1$
            case '\u20A0':
                return "&euro;"; //$NON-NLS-1$
            case '\u20AC':
                return "&#8364;"; //$NON-NLS-1$
            // case '' : return "&euro;"; //$NON-NLS-1$
            // case '\u20AC': return "&#x20AC;"; // euro
            // be carefull with this one (non-breaking white space)
            // case ' ' : return "&nbsp;"; //$NON-NLS-1$
            default:
            {
                int ci = 0xffff & ch;
                if (ci < 160)
                {
                    // nothing special only 7 Bit
                    return null;
                }
                // Not 7 Bit use the unicode system
                return "&#" + ci + ";"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * 
     * @param s
     *            string to be modified
     * @return string with escape characters, changed to html entities
     */
    public static final String escapeText(String s)
    {
        if (s == null) { return null; }
        StringBuffer sb = new StringBuffer();
        int n = s.length();
        char c;
        String entity;
        for (int i = 0; i < n; i++)
        {
            c = s.charAt(i);
            entity = getEntityForChar(c);
            if (entity != null)
            {
                sb.append(entity);
            } else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 
     * @param s
     *            string to unescape
     * @return new string with html entities changed to escape characters
     */
    public static final String unescapeText(String s)
    {
        int i, j, k;
        int arraySize = HTML_ESCAPE_CHARS.length;
        if (s != null && (i = s.indexOf("&")) > -1) { //$NON-NLS-1$
            j = s.indexOf(";", i); //$NON-NLS-1$
            if (j > i)
            {
                String temp = s.substring(i, j + 1);
                // search in escape[][] if temp is there
                k = 0;
                while (k < arraySize)
                {
                    if (HTML_ESCAPE_CHARS[k][0].equals(temp))
                    {
                        break;
                    }
                    k++;
                }
                // now we found html escape character
                if (k < arraySize)
                {
                    // replace it to ASCII
                    s = new StringBuffer(s.substring(0, i)).append(HTML_ESCAPE_CHARS[k][1]).append(s.substring(j + 1)).toString();
                    return unescapeText(s); // recursive call
                } else if (k == arraySize)
                {
                    s = new StringBuffer(s.substring(0, i)).append("&") //$NON-NLS-1$
                            .append(unescapeText(s.substring(i + 1))).toString();
                    return s;
                }
            }
        }
        return s;
    }

    public EditorsUtil()
    {
    }

    /**
     * DOCUMENT ME!
     * 
     * @param d
     *            DOCUMENT ME!
     * @param txt
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isLineDelimiter(IDocument d, String txt)
    {
        String[] delimiters = d.getLegalLineDelimiters();
        for (int i = 0; i < delimiters.length; i++)
        {
            if (txt.equals(delimiters[i])) { return true; }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param d
     *            DOCUMENT ME!
     * @param txt
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean endsWithDelimiter(IDocument d, String txt)
    {
        String[] delimiters = d.getLegalLineDelimiters();
        for (int i = 0; i < delimiters.length; i++)
        {
            if (txt.equals(delimiters[i])) { return true; }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param document
     *            DOCUMENT ME!
     * @param offset
     *            DOCUMENT ME!
     * @param tabwidth
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static int getIndentLength(IDocument document, int offset, int tabwidth)
    {
        int ret = 0;
        try
        {
            int lineNr = document.getLineOfOffset(offset);
            int start = document.getLineOffset(lineNr);
            int len = document.getLineLength(lineNr);
            for (int i = start; i < (start + len);)
            {
                char c = document.getChar(start);
                if (c == '\t')
                {
                    ret += (tabwidth - (ret % tabwidth));
                } else if (c == ' ')
                {
                    ret++;
                } else
                {
                    return ret;
                }
            }
            return ret;
        }
        catch (BadLocationException badlocationexception)
        {
            return ret;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param scanner
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static int skipWhitespace(ICharacterScanner scanner)
    {
        for (int c = scanner.read(); c != -1; c = scanner.read())
        {
            if (!Character.isWhitespace((char) c)) { return c; }
        }
        return -1;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param delim
     *            DOCUMENT ME!
     * @param scanner
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean skipString(int delim, ICharacterScanner scanner)
    {
        for (int c = scanner.read(); c != -1; c = scanner.read())
        {
            if (c == delim) { return true; }
            if ((c == 92) && (scanner.read() == -1))
            {
                scanner.unread();
            }
        }
        scanner.unread();
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param detector
     *            DOCUMENT ME!
     * @param scanner
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static String getWord(IWordDetector detector, ICharacterScanner scanner)
    {
        StringBuffer ret = new StringBuffer();
        int c = scanner.read();
        if (detector.isWordStart((char) c))
        {
            ret.append((char) c);
            for (c = scanner.read(); c != -1; c = scanner.read())
            {
                if (!detector.isWordPart((char) c))
                {
                    break;
                }
                ret.append((char) c);
            }
        }
        scanner.unread();
        return ret.toString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param d
     *            DOCUMENT ME!
     * @param line
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isBlankLine(IDocument d, int line)
    {
        try
        {
            int start = d.getLineOffset(line);
            int end;
            for (end = start + d.getLineLength(line); start < end; start++)
            {
                if (!Character.isWhitespace(d.getChar(start))) { return false; }
            }
            return start >= end;
        }
        catch (BadLocationException badlocationexception)
        {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param offset
     *            DOCUMENT ME!
     * @param doc
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isEscapedPartition(int offset, IDocument doc)
    {
        try
        {
            ITypedRegion partition = doc.getPartition(offset);
            String type = partition.getType();
            if ((offset != partition.getOffset())
                    && (type.equals("__javadoc_partition") || type.equals("__comment_partition") || type.equals("__comment1_partition") || type.equals("__string_partition"))) { return true; }
        }
        catch (BadLocationException e)
        {
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param offset
     *            DOCUMENT ME!
     * @param partitions
     *            DOCUMENT ME!
     * @param doc
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isInsidePartition(int offset, String[] partitions, IDocument doc)
    {
        return isInsidePartition(offset, partitions, false, doc);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param offset
     *            DOCUMENT ME!
     * @param partitions
     *            DOCUMENT ME!
     * @param include_start
     *            DOCUMENT ME!
     * @param doc
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public static boolean isInsidePartition(int offset, String[] partitions, boolean include_start, IDocument doc)
    {
        try
        {
            ITypedRegion partition = doc.getPartition(offset);
            if (include_start || (offset != partition.getOffset()))
            {
                String type = partition.getType();
                for (int i = 0; i < partitions.length; i++)
                {
                    if (type.equals(partitions[i])) { return true; }
                }
            }
        }
        catch (BadLocationException e)
        {
        }
        return false;
    }

    private static char[] getLineEnd(StringBuffer line)
    {
        if (line == null) { return EMPTY; }
        int lastIdx = line.length() - 1;
        if (lastIdx < 0) { return EMPTY; }
        char last = line.charAt(lastIdx);
        if (last == '\n')
        {
            if (lastIdx > 0)
            {
                if (line.charAt(lastIdx - 1) == '\r') { return CRLF; // windows
                }
            }
            return LF; // unix
        } else if (last == '\r')
        {
            return CR; // mac
        } else
        {
            return EMPTY;
        }
    }

    private static boolean removeTrailingSpace(StringBuffer line)
    {
        boolean changed = false;
        char lastChar;
        int lineLength = line.length();
        int lastCharsLength = getLineEnd(line).length;
        int lastIdx = lineLength - lastCharsLength - 1;
        while (lastIdx >= 0)
        {
            lastChar = line.charAt(lastIdx);
            if (lastChar != ' ' && lastChar != '\t')
            {
                break;
            }
            lastIdx--;
        }
        if (lastIdx != lineLength - lastCharsLength - 1)
        {
            line.delete(lastIdx + 1, lineLength - lastCharsLength);
            changed = true;
        }
        return changed;
    }

    public static boolean convertSpacesToTabs(StringBuffer line, int tabWidth, boolean removeTrailing)
    {
        char lastChar;
        boolean changed = false;
        if (removeTrailing)
        {
            changed = removeTrailingSpace(line);
        }
        int lineLength = line.length();
        int spacesCount = 0;
        int tabsCount = 0;
        int i = 0;
        for (; i < lineLength; i++)
        {
            lastChar = line.charAt(i);
            if (lastChar == ' ')
            {
                changed = true;
                spacesCount++;
            } else if (lastChar == '\t')
            {
                tabsCount++;
            } else
            {
                break;
            }
        }
        if (spacesCount > 0)
        {
            tabsCount += spacesCount / tabWidth;
            // modulo rest
            int extraSpaces = spacesCount % tabWidth;
            if (i - extraSpaces <= 0 || spacesCount - extraSpaces <= 0) { return false; }
            // delete whitespace to 'i' index, replace with tabs
            line.delete(0, i);
            line.insert(0, fillWith(tabsCount, '\t'));
            // if some last spaces exists, add them back
            if (extraSpaces > 0)
            {
                line.insert(tabsCount, fillWith(extraSpaces, ' '));
            }
        }
        return changed;
    }

    private static char[] fillWith(int length, char c)
    {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++)
        {
            chars[i] = c;
        }
        return chars;
    }
}
