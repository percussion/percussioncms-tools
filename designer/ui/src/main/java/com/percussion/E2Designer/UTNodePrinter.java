/*[ UTNodePrinter.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.io.PrintWriter;


/* Utility class to printout the node and its children in an XML format.
 */
public class UTNodePrinter
{

   /* Default constructor
    */
   public UTNodePrinter()
   {
   }
   
   /* print node generates an XML representation of the passed in node and writes the 
    * text to the PrintWriter object.
    *@param node - the node for which xml output is desired
    *@param indent - the amount of desired whitespace for indenting
    *@param output - the PrintWriter to which output is to be written. For example,
    *a ByteArrayOutputStream can be used to construct a PrintWriter object then
    *the text can be retrieved by calling toString() on the ByteArrayOutputStream object.
    */
   public static void printNode(Node node, String indent, PrintWriter output)
   {
      if (node == null)
         return;

      PrintWriter m_out = output;

      switch (node.getNodeType())
      {
      case Node.ATTRIBUTE_NODE:
         m_out.print(" " + ((Attr)node).getName() + "=\"" +
            ((Attr)node).getValue() + "\"");
         break;

      case Node.CDATA_SECTION_NODE:
         m_out.print("<![CDATA[" +
            convertToXmlEntities(((CDATASection)node).getData()) +
            "]]>");
         break;

      case Node.COMMENT_NODE:
         m_out.print(indent + "<!-- " + ((Comment)node).getData() + " -->");
         break;
            
      case Node.DOCUMENT_NODE:
         Document dNode = (Document)node;
            
         /* go through the doc's children, which should be
          * the PI nodes, DTD nodes and then the root data node
          */
         for (   Node kid = dNode.getFirstChild();
         kid != null;
         kid = kid.getNextSibling()) {
            printNode(kid, indent, m_out);
         }
         break;
            
      case Node.ELEMENT_NODE:
         Element eNode = (Element)node;
            
         m_out.println();
         m_out.print(indent + "<" + eNode.getTagName());
            
         /* print any attributes in the tag */
         NamedNodeMap attrList = eNode.getAttributes();
         if (attrList != null) {
            Attr aNode;
            for (int i = 0; i < attrList.getLength(); i++) {
               aNode = (Attr)attrList.item(i);
               m_out.print(" " + aNode.getName() + "=\"" +
                  aNode.getValue() + "\"");
            }
         }
            
         /* if there are children, close the tag, print the kids
          * and print the end tag
          */
         if (eNode.hasChildNodes()) {
            /* close the tag */
            m_out.print(">");
               
            boolean bSawText    = false;
            boolean bSawElement   = false;
               
            /* first pass see if elements exist, in which case
             * no text will be printed (as it is likely whitespace)
             */
            for (   Node kid = eNode.getFirstChild();
            kid != null;
            kid = kid.getNextSibling()) {
               if (kid instanceof Element) {
                  bSawElement = true;
                  break;
               }
            }
               
            /* print the kids */
            for (   Node kid = eNode.getFirstChild();
            kid != null;
            kid = kid.getNextSibling()) {
               if (kid instanceof CharacterData)
                  if (bSawElement)
                  continue;
               else
                  bSawText = true;
                  
               printNode(kid, indent + "  ", m_out);
            }
               
            /* and the end tag */
            if (!bSawText) {
               m_out.println();
               m_out.print(indent);
            }
            m_out.print("</" + eNode.getTagName() + ">");
         }
         else {
            /* close this tag with the end tag */
            m_out.print("/>");
         }
         break;
            
      case Node.PROCESSING_INSTRUCTION_NODE:
         ProcessingInstruction pi = (ProcessingInstruction)node;
         m_out.print("<?" + pi.getTarget() + " " + pi.getData() + "?>");
         break;
            
      case Node.TEXT_NODE:
         // made change here to print text node as is and not convert to Xml Entities
         // if it is desired later to convert to Xml entities for some cases, use a flag to 
         m_out.print(((Text)node).getData());
         break;
      }
   }

   private static String convertToXmlEntities(String input)
   {
      /* This implementation should be fairly efficient in that
       * it minimizes the number of function calls. Hence, we
       * operate on an array rather than a String object. We
       * collect the output in a string buffer, and here too
       * we try to minimize the number of function calls.
       *
       * We do not add each normal character to the output
       * buffer one at a time. Instead, we build a "run" of
       * normal characters and, upon encountering a special
       * character, we write the previous normal run before
       * we write the special replacement.
       */
      char[] chars = input.toCharArray();
      int len = chars.length;

      StringBuffer buf = new StringBuffer((int)(chars.length * 1.5));

      char c;

      // the start of the latest run of normal characters
      int startNormal = 0;
      
      int i = 0;
      while (true)
      {
         if (i == len)
         {
            if (startNormal != i)
               buf.append(chars, startNormal, i - startNormal);
            break;
         }
         c = chars[i];
         switch (c)
         {
            case '&' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&amp;");
               break;
            case '<' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&lt;");
               break;
            case '>' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&gt;");
               break;
            case '\'' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&apos;");
               break;
            case '"' :
               if (startNormal != i)
                  buf.append(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               buf.append("&quot;");
               break;
            default:
               // do nothing...this char becomes part of the normal run
         }
         i++;
      }
      return buf.toString();
   }
}
