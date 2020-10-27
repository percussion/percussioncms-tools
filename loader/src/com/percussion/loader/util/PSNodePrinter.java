/*[ PSNodePrinter.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.util;

import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * This class is separated from PSXmlTreeWaker.java to fix a specific bug for
 * xmldom package. The idea is to not jeopardise other areas (as treewalker is
 * used everywhere) by making this change for xmldom. The only difference is
 * that the pretty print via indentation is completely disabled. This is to
 * avoid problems with  mixing the content between xml (html) nodes which is
 * very common with any HTML editor control like Ektron control.
 */
public class PSNodePrinter
{
   /**
    * Only constructor. Takes the print writer as the argument.
    * @param out must not be <code>null</code>.
    * @throws IllegalArgumentException
    */
   public PSNodePrinter(Writer out)
   {
      if(out==null)
      {
         throw new IllegalArgumentException("output writer must not be null");
      }
      m_out = out;
   }

   /**
    * This method works in conjunction with {@link #printElement(Element,
    * String) printElement}.
    * For the passed in node and all of its children, either the node is
    * written to the writer associated with this object during construction,
    * or <code>printElement<code> is called to handle this writing. No
    * indentation is done. This method calls itself recursively, so only the
    * root-most element that needs to be printed is passed in.
    *
    * @param node The node to print. If <code>null</code>, the method returns
    * immediately.
    *
    * @throws IOException when it fails to print a node
    */
   public void printNode(Node node)
      throws IOException
   {
      if (node == null)
         return;
      String ent = "";
      switch (node.getNodeType())
      {
         case Node.ATTRIBUTE_NODE:
            m_out.write(" ");
            m_out.write(((Attr)node).getName());
            m_out.write("=\"");
            // #RVAI-4CDKN3 : now we handle entities in attribute values
            ent = PSXmlTreeWalker.convertToXmlEntities(
               ((Attr)node).getValue());
            m_out.write(ent);
            m_out.write("\"");
            break;

         case Node.CDATA_SECTION_NODE:
            m_out.write("<![CDATA[");
            ent = PSXmlTreeWalker.convertToXmlEntities(
               ((CDATASection)node).getData());
            m_out.write(ent);
            m_out.write("]]>");
            break;

         case Node.COMMENT_NODE:
            m_out.write("<!--");
            m_out.write(((Comment)node).getData());
            m_out.write("-->");
            break;

         case Node.DOCUMENT_NODE:
            Document dNode = (Document)node;

         /* go through the doc's children, which should be
          * the PI nodes, DTD nodes and then the root data node
          */
            Node kid = dNode.getFirstChild();
            if (kid != null)
            {
               if (kid.getNodeType() == Node.ELEMENT_NODE)
                  printElement((Element)kid);
               else
                  printNode(kid);

               while (null != (kid = kid.getNextSibling()))
               {
                  printNode(kid);
               }
            }
            break;

         case Node.DOCUMENT_TYPE_NODE:
            break;

         case Node.ELEMENT_NODE:
            Element eNode = (Element)node;
            printElement(eNode);
            break;

   /*
    *New code for entity references.  When "printing" the XML document,
    *we must include the "Name" of any entities we encounter.  This will
    *only happen in Element data, never in attribute data.
    */
         case Node.ENTITY_REFERENCE_NODE:
            m_out.write("&"+node.getNodeName()+";");
            break;

         case Node.PROCESSING_INSTRUCTION_NODE:
            ProcessingInstruction pi = (ProcessingInstruction)node;
            // tidy returns null for pi.getTarget()
            String pitarget = pi.getTarget();
            String pidata = pi.getData();
            m_out.write("<?");
            if (pitarget != null)
            {
               pitarget = pitarget.trim();
               m_out.write(pitarget);
               m_out.write(" ");
            }
            if (pidata != null)
            {
               pidata = pidata.trim();
               m_out.write(pidata);
            }
            String tmp = m_out.toString();
            if (tmp.endsWith("?"))
               m_out.write(">");
            else
               m_out.write("?>");
            break;

         case Node.TEXT_NODE:
            String data = ((Text)node).getData();
            if (data != null)
               ent = PSXmlTreeWalker.convertToXmlEntities(data);
            m_out.write(ent);
            break;
      }
   }

   /**
    * Method to print the Element node
    * @param eNode must not be <code>null</code>
    * @throws IOException when it fails to print a node
    */
   private void printElement(Element eNode)
      throws IOException
   {
      m_out.write("<");
      m_out.write(eNode.getTagName());

      /* print any attributes in the tag */
      // #RVAI-4CDKN3 : now we handle entities in attribute values
      NamedNodeMap attrList = eNode.getAttributes();
      if (attrList != null) {
         for (int i = 0; i < attrList.getLength(); i++) {
            printNode(attrList.item(i));
         }
      }

      /* if there are children, close the tag, print the kids
       * and print the end tag
       */
      if (eNode.hasChildNodes()) {
         /* close the tag */
         m_out.write(">");

         /* print the kids */
         for ( Node kid = eNode.getFirstChild(); kid != null;
            kid = kid.getNextSibling())
         {
               printNode(kid);
         }
         m_out.write("</");
         m_out.write(eNode.getTagName());
         m_out.write(">");
      }
      else {
         /* close this tag with the end tag */
         m_out.write("/>");
      }
   }

   /**
    * writer object to write the node tree. Never <code>null</code> after
    * the object is created.
    */
   private Writer m_out;
}
