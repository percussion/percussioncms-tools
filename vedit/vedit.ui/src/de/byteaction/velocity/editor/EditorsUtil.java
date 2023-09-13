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
                                                      "&agrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Agrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&acirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&auml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Auml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Acirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aring;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Aring;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aelig;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&AElig;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ccedil;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ccedil;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&eacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Eacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&aacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Aacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&egrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Egrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ecirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ecirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&euml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Euml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&iuml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Iuml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&iacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Iacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&atilde;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Atilde;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&otilde;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Otilde;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&oacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Oacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ocirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ocirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ouml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ouml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&oslash;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Oslash;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&szlig;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ugrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ugrave;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&uacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Uacute;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&ucirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Ucirc;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&uuml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
                                                      "&Uuml;", "�" }, { //$NON-NLS-1$ //$NON-NLS-2$
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
            case '�':
                return "&agrave;"; //$NON-NLS-1$
            case '�':
                return "&aacute;"; //$NON-NLS-1$
            case '�':
                return "&Agrave;"; //$NON-NLS-1$
            case '�':
                return "&Aacute;"; //$NON-NLS-1$
            case '�':
                return "&acirc;"; //$NON-NLS-1$
            case '�':
                return "&Acirc;"; //$NON-NLS-1$
            case '�':
                return "&auml;"; //$NON-NLS-1$
            case '�':
                return "&Auml;"; //$NON-NLS-1$
            case '�':
                return "&aring;"; //$NON-NLS-1$
            case '�':
                return "&Aring;"; //$NON-NLS-1$
            case '�':
                return "&atilde;"; //$NON-NLS-1$
            case '�':
                return "&Atilde;"; //$NON-NLS-1$
            case '�':
                return "&aelig;"; //$NON-NLS-1$
            case '�':
                return "&AElig;"; //$NON-NLS-1$
            case '�':
                return "&ccedil;"; //$NON-NLS-1$
            case '�':
                return "&Ccedil;"; //$NON-NLS-1$
            case '�':
                return "&eacute;"; //$NON-NLS-1$
            case '�':
                return "&Eacute;"; //$NON-NLS-1$
            case '�':
                return "&egrave;"; //$NON-NLS-1$
            case '�':
                return "&Egrave;"; //$NON-NLS-1$
            case '�':
                return "&ecirc;"; //$NON-NLS-1$
            case '�':
                return "&Ecirc;"; //$NON-NLS-1$
            case '�':
                return "&euml;"; //$NON-NLS-1$
            case '�':
                return "&Euml;"; //$NON-NLS-1$
            case '�':
                return "&iacute;"; //$NON-NLS-1$
            case '�':
                return "&Iacute;"; //$NON-NLS-1$
            case '�':
                return "&iuml;"; //$NON-NLS-1$
            case '�':
                return "&Iuml;"; //$NON-NLS-1$
            case '�':
                return "&otilde;"; //$NON-NLS-1$
            case '�':
                return "&Otilde;"; //$NON-NLS-1$
            case '�':
                return "&oacute;"; //$NON-NLS-1$
            case '�':
                return "&ocirc;"; //$NON-NLS-1$
            case '�':
                return "&Oacute;"; //$NON-NLS-1$
            case '�':
                return "&Ocirc;"; //$NON-NLS-1$
            case '�':
                return "&ouml;"; //$NON-NLS-1$
            case '�':
                return "&Ouml;"; //$NON-NLS-1$
            case '�':
                return "&oslash;"; //$NON-NLS-1$
            case '�':
                return "&Oslash;"; //$NON-NLS-1$
            case '�':
                return "&szlig;"; //$NON-NLS-1$
            case '�':
                return "&ugrave;"; //$NON-NLS-1$
            case '�':
                return "&Ugrave;"; //$NON-NLS-1$
            case '�':
                return "&uacute;"; //$NON-NLS-1$
            case '�':
                return "&Uacute;"; //$NON-NLS-1$
            case '�':
                return "&ucirc;"; //$NON-NLS-1$
            case '�':
                return "&Ucirc;"; //$NON-NLS-1$
            case '�':
                return "&uuml;"; //$NON-NLS-1$
            case '�':
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
