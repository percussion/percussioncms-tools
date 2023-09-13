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
package com.percussion.htmlConverter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.tags.Span;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class provides all functionality to handle server page code for the
 * split and merge processes. The approach we took has two main steps:
 * <ol>
 * <li>The source HTML is passed through the preProcess method to replace
 * all server page source code with our own markup. While doing this the
 * original code is stored in a map together with the key of our markup.</li>
 * <li>This specially marked HTML file is now ready to be split using tidy.</li>
 * <li>After the split process we put back the original server page code.
 * If the original code was part of an attribute, it will be escaped before
 * put back so the input parser is happy. Otherwise the original code is wrapped with
 * '<xsl:text disable-output-escaping="yes"><![CDATA[' ... ']]></xsl:text>'
 * to tell the parser not to escape the generated output.</li>
 * </ol>
 */
public class ProcessServerPageTags extends Object
{
   /**
    * Constructs and initializes the state machine.
    *
    * @param serverPageTags the document containing the serverPageTags to be
    *    handled by this class.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    * @throws SplitterException if the server page tag file is invalid.
    */
   public ProcessServerPageTags(Document serverPageTags) throws SplitterException
   {
      if (serverPageTags == null)
         throw new IllegalArgumentException("serverPageTags cannot be null");

      m_serverPageTags = serverPageTags;
      initTagVectors();
   }

   /**
    * Call this to process the provided source for server page tags.
    *
    * @param htmlSource the source HTML page to pre-process server page tags.
    * @throws <code>SplitterException</code> when
    *    <code>preProcessSlotSpanTags</code> fails.
    * @return the processed HTML string.
    */
   public String preProcess(String htmlSource) throws SplitterException
   {
      m_htmlSource = htmlSource;

      // initialize the markup hash map and make sure our key is unique
      int counter = 0;
      m_codeMap.clear();
      m_escapeMap.clear();
      while (m_htmlSource.indexOf(m_keyPrefix) != -1)
         m_keyPrefix += counter;

      m_htmlTarget = new StringBuffer(m_htmlSource.length());

      m_current = 0;
      m_lastClose = 0;
      int closingTagIndex = setNextOpeningTag(m_current);
      while (m_nextOpen != -1)
      {
         int skipTagIndex = nextSkipTag(m_current);
         if (m_nextOpen < m_nextSkip || m_nextSkip == -1)
            markIt(closingTagIndex);
         else
            skipIt(skipTagIndex);

         closingTagIndex = setNextOpeningTag(m_current);
      }
      m_htmlTarget.append(m_htmlSource.substring(m_current,
                                                 m_htmlSource.length()));


      return preProcessSlotSpanTags(m_htmlTarget.toString());


   }

   /**
    * Removes sample content inside of slot span tags. This needs to occur
    * before we Tidy the document so the content in the span tags does not
    * get "moved".
    * @param htmlSource to be processed. May not be <code>null</code>.
    * @throws <code>SplitterException</code> when HTML parsing fails.
    * @return processed string. Never <code>null</code>.
    */
   private String preProcessSlotSpanTags(String htmlSource) throws SplitterException
   {
      if(null == htmlSource)
         throw new IllegalArgumentException("Html source cannot be null.");
      if(htmlSource.length() < 1)
         return "";

      StringBuffer sb = new StringBuffer();
      // Anything that precedes the <html> tag will be thrown away by the
      // HTML parser so we must capture it first.
      int pos = htmlSource.toLowerCase().indexOf("<html");
      if(pos > 0)
         sb.append(htmlSource.substring(0, pos));
      try
      {
         Parser parser = new Parser();
         SpanTagVisitor visitor = new SpanTagVisitor(sb);
         parser.setInputHTML(htmlSource);
         parser.visitAllNodesWith(visitor);
      }
      catch(ParserException e)
      {
         throw new SplitterException(e.getMessage());
      }

      return sb.toString();
   }

   /**
    * This goes through the map created in the pre process and replaces the
    * XSpLit markups with its original server page code.
    *
    * @param xslSource the source XSL string
    * @return the processed XSL string
    */
   public String postProcess(String xslSource)
   {
      StringBuffer xslTarget = new StringBuffer(xslSource);
      Vector topElements = new Vector();

      String key = "";
      String serverPageBlock = "";

      int stylesheetStart = 0;
      int stylesheetEnd = xslSource.length();
      int pos = 0;
      Iterator keys = m_codeMap.keySet().iterator();
      while (keys.hasNext())
      {
         stylesheetStart = xslTarget.toString().indexOf("<xsl:stylesheet");
         stylesheetEnd = xslTarget.toString().indexOf("</xsl:stylesheet>") +
            "</xsl:stylesheet>".length();
         key = (String) keys.next();
         pos = xslTarget.toString().indexOf(key);
         String strDisable = (String) m_escapeMap.get(key);
         if (strDisable != null && strDisable.equalsIgnoreCase("yes"))
            serverPageBlock = (String) m_codeMap.get(key);
         else
            serverPageBlock = escape(key, (String) m_codeMap.get(key),
                                     stylesheetStart, pos);

         if (serverPageBlock.startsWith("<xsl:include") ||
             serverPageBlock.startsWith("<xsl:import") ||
             ((serverPageBlock.indexOf("<xsl:") != -1) &&
              (pos < stylesheetStart || pos > stylesheetEnd)))
         {
            topElements.add(serverPageBlock);
            if (pos < 0)
            {
               System.out.println("ERROR - postProcess: missing XSpLit markup key!" + key);
            }
            else
            {
               xslTarget.replace(pos, pos+key.length(), "");
            }
         }
         else
         {
            if (pos != -1)
               xslTarget.replace(pos, pos+key.length(), serverPageBlock);
            else
               System.out.println("ERROR - postProcess: missing XSpLit markup key!" + key);
         }
      }

      // add top elements
      stylesheetStart = xslTarget.toString().indexOf("<xsl:stylesheet");
      pos = xslTarget.toString().indexOf(">", stylesheetStart +
         ("<xsl:stylesheet").length())+1;
      int lastImport = xslTarget.toString().lastIndexOf("<xsl:import");
      int lastInclude = xslTarget.toString().lastIndexOf("<xsl:include");
      if (lastImport > pos || lastInclude > pos)
      {
         if (lastImport > lastInclude)
            pos = xslTarget.toString().indexOf(">", lastImport +
               ("<xsl:import").length())+1;
         else
            pos = xslTarget.toString().indexOf(">", lastInclude +
               ("<xsl:include").length())+1;
      }
      if (pos != -1)
      {
         for (int i=0; i<topElements.size(); i++)
         {
            String strTop = (String) topElements.elementAt(i);

            xslTarget.insert(pos++, "\n");
            xslTarget.insert(pos, strTop);
            pos = pos+strTop.length();
         }
      }

      return xslTarget.toString();
   }

   /**
    * This goes through the map created in the pre process and replaces the
    * XSpLit markups with its original server page code. This is different from
    * {@link #postProcess(String)} in that this method does not assume the 
    * content is XSL. All that this does is to replace the server page tags that
    * were added previously using {@link #preProcess(String)} method.
    *
    * @param source the source XSL string, must not be <code>null</code> or empty.
    * @return the processed content, never <code>null</code> or empty.
    */
   public String replaceServerPageTags(String source)
   {
      if (source == null || source.length() < 1)
      {
         throw new IllegalArgumentException("source must not be null or empty");
      }
      StringBuffer target = new StringBuffer(source);

      String key = "";
      String serverPageBlock = "";

      int contentStart = 0;
      int contentEnd = source.length();
      int pos = 0;
      Iterator keys = m_codeMap.keySet().iterator();
      while (keys.hasNext())
      {
         key = (String) keys.next();
         pos = target.toString().indexOf(key);
         String strDisable = (String) m_escapeMap.get(key);
         if (strDisable != null && strDisable.equalsIgnoreCase("yes"))
            serverPageBlock = (String) m_codeMap.get(key);
         else
            serverPageBlock = escape(key, (String) m_codeMap.get(key),
                  contentStart, pos);

         if (pos != -1)
         {
            target.replace(pos, pos + key.length(), serverPageBlock);
         }
         else
         {
            System.out
                  .println("ERROR - postProcess: missing XSpLit markup key!"
                        + key);
         }
      }
      return target.toString();
   }

   /**
    * Returns the status whether the processed document contained any xsl:output
    * statements or not.
    *
    * @return <code>true</code> if the processed document contains any
    *    xsl:output statements, <code>false</code> otherwise.
    */
   public boolean hasOutputElement()
   {
      return (getOutputElement() != null);
   }
   
   /**
    * Returns the first xsl:output statement found.
    * 
    * @return the xsl:output statement or <code>null</code>
    * if none was found. Never empty.
    */
   public String getOutputElement()
   {
      Iterator entries = m_codeMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         if (((String) entry.getValue()).indexOf("xsl:output") >= 0)
            return (String)entry.getValue();
      }
      return null;      
   }

   /**
    * In case this key did replace an attribute entry, we need to escape the
    * code block.
    * Whether or not this was an attribute entry is determined by the key
    * used. If the key is wrapped with HTML comment opening/closing tags it
    * is not an attribute entry, otherwise it is.
    *
    * @param key the key used to mark up the code block.
    * @param the codeBlock to escape.
    * @param stylesheetStart the start position of the stylesheet declaration.
    * @param current the currently processed position.
    * @return the escaped code block if this is for an attribute entry.
    */
   private String escape(String key, String codeBlock,
                         int stylesheetStart, int current)
   {
      if (key.startsWith("<!--"))
      {
         String temp = ms_strCDATABegin + codeBlock + ms_strCDATAEnd;
         if (current < stylesheetStart)
            return temp;
         else
            return ms_strXslTextBegin + temp + ms_strXslTextEnd;
      }

      StringBuffer escapedBlock = new StringBuffer(codeBlock);
      escapedBlock.replace(0, 1, "&lt;");
      int length = escapedBlock.length();
      escapedBlock.replace(length-1, length, "&gt;");

      return escapedBlock.toString();
   }

   /**
    * This function will replace all server page blocks found with an
    * enumerated key and put it together with the key into a hash map. After
    * the splitting process we must put back the removed code into the XSL
    * document created, marking it as CDATA.
    *
    * @param tagIndex the closing tag index to use.
    */
   private void markIt(int tagIndex)
   {
      int oldCurrent = m_current;

      String strClosingTag = (String) m_closingTags.elementAt(tagIndex);
      String strOpeningTag = (String) m_openingTags.elementAt(tagIndex);
      int nextOpening = getNextOpeningTag(m_nextOpen+strOpeningTag.length(), tagIndex);
      int nextClosing = m_htmlSource.indexOf(strClosingTag,
                                             m_nextOpen+strOpeningTag.length());

      // try the default closing tag
      if (nextClosing == -1)
      {
         strClosingTag = "/>";
         nextClosing = m_htmlSource.indexOf(strClosingTag,
                                            m_nextOpen+strOpeningTag.length());
      }

      if (nextClosing == -1)
      {
         // skip this and report error
         m_current = m_nextOpen+strOpeningTag.length();
         System.out.println("ERROR - scriptIt: illegal source HTML! Missing closing tag.");
      }

      while (nextOpening != -1 && (nextClosing > nextOpening || nextClosing == -1))
      {
         // skip nested opening-closing pairs
         nextClosing = m_htmlSource.indexOf(strClosingTag,
                                            nextClosing+strClosingTag.length());
      }
      if (nextClosing != -1)
      {
         m_current = nextClosing+strClosingTag.length();

         // get the isXslTag attribute, defaults to 'no' if not found
         String isXslAttribute =
            ((Element) m_tags.elementAt(tagIndex)).getAttribute("isXslTag");

         boolean isXsl = isXslAttribute.equalsIgnoreCase("yes");
         boolean isAttr = isAttribute(oldCurrent);
         String key = getNextKey(isAttr, isXsl);
         m_htmlTarget.append(key);

         String strKey = getPostProcessKey(isAttr, isXsl);
         m_codeMap.put(strKey, m_htmlSource.substring(oldCurrent, m_current));
         m_escapeMap.put(strKey, (String) m_disableEscaping.elementAt(tagIndex));

         m_lastClose = m_current;
      }
      else
      {
         // make sure we skip this and report error
         m_current = m_nextOpen+strOpeningTag.length();
         System.out.println("ERROR - scriptIt: illegal source HTML! Unbalanced closing tags.");
      }
   }

   /**
    * This method returns whether the current handled server page block is
    * part of an attribut or not.
    *
    * @param end the end position until we search for the attribute closing tag.
    * @return <code>true</code> if part of an attribute, <code>false</code>
    *    otherwise.
    */
   private boolean isAttribute(int end)
   {
      int close = m_lastClose;
      int open = m_lastClose;
      while (close < end && close != -1)
      {
         open = m_htmlSource.indexOf("<", close);
         if (open < end && open != -1)
            close = m_htmlSource.indexOf(">", open);
         else
            break;
      }

      return (close > end && close != -1);
   }

   /**
    * This will return the next key to for our server page markup. It will be
    * the key prefix plus an incremented counter.
    *
    * @param isAttribute whether or not to get the attribute or regular key.
    * @param isXsl idicates that we should mark this key as XSL
    */
   private synchronized String getNextKey(boolean isAttribute,
                                          boolean isXsl)
   {
      String xslMark = "";
      if (isXsl)
         xslMark = XSL_MARKER;

      if (isAttribute)
         return xslMark + m_keyPrefix + "_" + (++ms_keyCount);

      return "<!--" + xslMark + m_keyPrefix + "_" + (++ms_keyCount) + "-->";
   }

   /**
    * The XSL output is adding 2 spaces to the open and close comment tag.
    * Use this function to save the key we are looking for in the post
    * process.
    *
    * @param isAttribute whether or not to get the attribute or regular key.
    * @param isXsl idicates that we should mark this key as XSL
    */
   private synchronized String getPostProcessKey(boolean isAttribute,
                                                 boolean isXsl)
   {

    String xslMark = "";
    if (isXsl)
         xslMark = XSL_MARKER;

     if (isAttribute)
         return xslMark + m_keyPrefix + "_" + ms_keyCount;

      return "<!--" + xslMark + m_keyPrefix + "_" + ms_keyCount + "-->";
   }

   /**
    * This will skip the current position to the next occurence of the skip
    * tag provided.
    *
    * @param tagIndex the index of the skip tag to use.
    */
   private void skipIt(int tagIndex)
   {
      int oldCurrent = m_current;
      if (m_skipTags.isEmpty())
      {
         m_nextSkip = -1;
         return;
      }

      String strTag = (String) m_skipTags.elementAt(tagIndex);
      int index = m_htmlSource.indexOf(strTag, m_nextSkip);
      if (index != -1)
      {
         m_current = index+strTag.length();
         m_htmlTarget.append(m_htmlSource.substring(oldCurrent, m_current));
      }
      else
         System.out.println("ERROR - skipIt: illegal state!");
   }

   /**
    * Calculates and setx the index of the next opening tag starting from
    * the provided index.
    *
    * @param start the index to start from.
    * @return the vector index of the openingTag for which we found the next
    *    position.
    */
   private int setNextOpeningTag(int start)
   {
      // assume there is no more opening tags
      m_nextOpen = -1;

      int tagIndex = -1;
      int temp = -1;
      for (int i=0, count=m_openingTags.size(); i<count; i++)
      {
         temp = m_htmlSource.indexOf((String) m_openingTags.elementAt(i),
                                     start);
         if (temp != -1)
         {
            if (m_nextOpen == -1 || temp < m_nextOpen)
            {
               tagIndex = i;
               m_nextOpen = temp;
            }
         }
      }

      if (m_nextOpen != -1)
      {
         m_current = m_nextOpen;
         m_htmlTarget.append(m_htmlSource.substring(start, m_current));
      }

      return tagIndex;
   }

   /**
    * Calculates the index of the next opening tag starting from the provided
    * index.
    *
    * @param start the index to start from.
    * @return the next found opening tag.
    */
   private int getNextOpeningTag(int start)
   {
      // assume there is no more opening tags
      int nextOpen = -1;

      int temp = -1;
      for (int i=0, count=m_openingTags.size(); i<count; i++)
      {
         temp = m_htmlSource.indexOf((String) m_openingTags.elementAt(i),
                                     start);
         if (temp != -1)
         {
            if (nextOpen == -1 || temp < nextOpen)
               nextOpen = temp;
         }
      }

      return nextOpen;
   }

   /**
    * Calculates the index of the next opening tag starting from the provided
    * index.
    *
    * @param start the index to start from.
    * @param tagIndex the tag index to use
    * @return the next found opening tag.
    */
   private int getNextOpeningTag(int start, int tagIndex)
   {
      return m_htmlSource.indexOf((String) m_openingTags.elementAt(tagIndex),
                                  start);
   }

   /**
    * Calculate the next skip opening tag starting at the given position.
    *
    * @param start the index to start from.
    * @return the vector index of the skip tag for which we found the next
    *    position.
    */
   private int nextSkipTag(int start)
   {
      // assume there is no more opening tags
      m_nextSkip = -1;

      if (m_skipTags.isEmpty())
      {
         // no skip tags defined
         m_nextSkip = -1;
         return -1;
      }

      int tagIndex = -1;
      int temp = -1;
      for (int i=0, count=m_skipTags.size(); i<count; i++)
      {
         temp = m_htmlSource.indexOf((String) m_skipTags.elementAt(i),
                                     start);
         if (temp != -1)
         {
            if (m_nextSkip == -1 || temp < m_nextSkip)
            {
               tagIndex = i;
               m_nextSkip = temp;
            }
         }
      }

      return tagIndex;
   }

   /**
    * Initialize the tag vectors. The vectors of opening and closing tags
    * are created from the external XML file 'serverPageTags.xxml'. The skip
    * vector is currently hardcoded.
    *
    * @throws SplitterException if the server page tag file is invalid.
    */
   private void initTagVectors() throws SplitterException
   {
      NodeList tags = m_serverPageTags.getElementsByTagName("tag");
      NodeList openings = m_serverPageTags.getElementsByTagName("opening");
      NodeList closings = m_serverPageTags.getElementsByTagName("closing");
      NodeList disableEscaping = m_serverPageTags.getElementsByTagName("disableEscaping");

      if (openings == null || closings == null || disableEscaping == null)
         throw new SplitterException(MainFrame.getRes().getString("invalidTagFile"));

      int count = openings.getLength();
      if (count != closings.getLength() || count != disableEscaping.getLength())
         throw new SplitterException(MainFrame.getRes().getString("unbalancedTagFile"));

      m_tags = new Vector(count);
      m_openingTags = new Vector(count);
      m_closingTags = new Vector(count);
      m_disableEscaping = new Vector(count);
      for (int i=0; i<count; i++)
      {
         m_tags.add(tags.item(i));

         Node openingNode = openings.item(i).getFirstChild();
         if (openingNode instanceof Text)
            m_openingTags.add(((Text) openingNode).getData());

         Node closingNode = closings.item(i).getFirstChild();
         if (closingNode instanceof Text)
            m_closingTags.add(((Text) closingNode).getData());

         Node disableEscapingNode = disableEscaping.item(i).getFirstChild();
         if (disableEscapingNode instanceof Text)
            m_disableEscaping.add(((Text) disableEscapingNode).getData());
      }

      m_skipTags = new Vector(2);
      m_skipTags.add("\"");
      m_skipTags.add("'");
   }

   /**
    * Visits each tag that is parsed by the htmlparser and removes sample
    * content inside of slot span tags.
    */
   private class SpanTagVisitor extends NodeVisitor
   {
     private SpanTagVisitor(StringBuffer sb)
     {
        super(true);
        m_buffer = sb;
     }

      /*
       * @see org.htmlparser.visitors.NodeVisitor#finishedParsing()
       */
      public void finishedParsing()
      {
         m_buffer.append(m_rootNode.toHtml());
      }

      /*
       * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
       */
      public void visitTag(Tag tag)
      {
         // Grab the first tag to visit, this is the root node.
         if(m_rootNode == null)
            m_rootNode = tag;

         if(tag instanceof Span)
         {
            Span span = (Span)tag;
            String slotname = span.getAttribute("slotname");
            // Remove dummy content if this is a slot span tag
            if(null != slotname && slotname.trim().length() > 0)
            {
               // We have to put back the snippet comments so an exception
               // does not get thrown. We also add a comment that informs the
               // user that the sample content was removed. This is only seen in
               // the Tidied Html window and does not end up in the XSL.
               org.htmlparser.util.NodeList nl = span.getChildren();
               if(nl != null)
               {

                  nl.removeAll();
                  if(!span.isEmptyXmlTag())
                  {
                     nl.add(new RemarkNode(" start snippet "));
                     nl.add(new RemarkNode(" Sample content removed "));
                     nl.add(new RemarkNode(" end snippet "));
                  }
               }

            }

         }

      }

      /**
       * String buffer that will hold ther modified html, passed in via
       * the ctor. Never <code>null</code>.
       */
      private StringBuffer m_buffer;

      /**
       * The root node of the html that is being modified, set on first visit
       * in {@link #visitTag(Tag)}, never <code>null</code> after that.
       */
      private org.htmlparser.Node m_rootNode;
}

   /**
    * This is the hash table which will be used to store the removed server
    * page code.
    */
   private Hashtable m_codeMap = new Hashtable();
   /**
    * This is the hash table which will be used to store the enable/disable
    * escape information. The keys correspond to the keys in the code map.
    */
   private Hashtable m_escapeMap = new Hashtable();
   /**
    * The key prefix used to mark removed server page code.
    */
   private String m_keyPrefix = "XSpLit_Server_Page_Block";
   /**
    * The key counter.
    */
   private static int ms_keyCount = 0;

   /**
    * A vector of server page tag elements, initialized in
    * {#link initTagVectors()}, never <code>null</code> or changed after that.
    */
   private Vector m_tags = null;
   /**
    * A vector of opening tags.
    */
   private Vector m_openingTags = null;
   /**
    * A vector of closing tags.
    */
   private Vector m_closingTags = null;
   /**
    * A vector of disable escaping information.
    */
   private Vector m_disableEscaping = null;
   /**
    * A vector of skip tags.
    */
   private Vector m_skipTags = null;
   /**
    * The source HTML string to pre-process server page tags for.
    */
   private String m_htmlSource = null;
   /**
    * The target HTML string to which we build the result to.
    */
   private StringBuffer m_htmlTarget = null;
   /**
    * The current index of the state machine.
    */
   private int m_current = 0;
   /**
    * The next index of opening tag found. -1 indicates there is no next
    * opening tag index.
    */
   private int m_nextOpen = 0;
   /**
    * The last closeing tag position marked.
    */
   private int m_lastClose = 0;
   /**
    * The next index of skip tag found. -1 indicates there is no next
    * skip tag index.
    */
   private int m_nextSkip = 0;
   /**
    * All documentation opening tags.
    */
   private static final Vector ms_openDocTags = new Vector();
   /**
    * All documentation closing tags.
    */
   private static final Vector ms_closeDocTags = new Vector();
   /**
    * Initialize the documentation tags.
    */
   static
   {
      ms_openDocTags.add("<!--");
      ms_closeDocTags.add("-->");

      ms_openDocTags.add("<%--");
      ms_closeDocTags.add("--%>");
   }

   /**
    * The document which holds the JSP / APS tags that need special handling
    * for tidy. Initialized during construction, never <code>null</code> after
    * that.
    */
    private static Document m_serverPageTags = null;

   /**
    * The CDATA wrapper opening part.
    */
   private static final String ms_strCDATABegin = "<![CDATA[\n";
   /**
    * The CDATA wrapper closing part.
    */
   private static final String ms_strCDATAEnd = "\n]]>";
   /**
    * The xsl:text wrapper opening part.
    */
   private static final String ms_strXslTextBegin = "\n<xsl:text disable-output-escaping=\"yes\">";
   /**
    * The xsl:text wrapper closing part.
    */
   private static final String ms_strXslTextEnd = "</xsl:text>";
   /**
    * Marker indicating the content is XSL
    */
   private static final String XSL_MARKER = "XSL_";
}
