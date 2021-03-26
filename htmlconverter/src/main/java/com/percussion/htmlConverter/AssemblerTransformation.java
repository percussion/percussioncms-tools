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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class provides all functionality to transfrom the new splitter markup
 * tags introduced for content assemblers.
 */
public class AssemblerTransformation
{
   /**
    * Ctor. Takes an optional argument that is template variable/mode name
    * suffix which in turn can serve as an addiotnal key to generating unique
    * template names.
    * 
    * @param templateNameSuffix template name suffix. This will be part of
    *           variable names or template modes of all templates generated.
    *           This can be used to avoid template variable/mode name conflicts
    *           when two HTML source files are split to produce two XSL
    *           stylesheets in that one includes/imports the other. The normal
    *           template variable/mode naming scheme uses numbers incremeneted
    *           for unique name. This can easily lead to template call
    *           conflicts. For example, A global template includes a local
    *           template each of which was split at a diffrent time and hence may
    *           end up with conflicting template matches. This additional suffix
    *           can help avoiding such a situation. May be <code>null</code>
    *           or empty.
    */
   public AssemblerTransformation(String templateNameSuffix)
   {
      if (templateNameSuffix == null)
         templateNameSuffix = "";
      m_templateNameSuffix = templateNameSuffix;
   }

   /**
    * Convert the provided attribute from a dynamic field mark-up to an
    * assembler property mark-up. This is necessary because our mark-up
    * contains a '$' to start the property, which is not a valid XML name (for
    * elements and attributes).
    *
    * @param elem the element containing the attribute which needs to be
    *    converted, not <code>null</code>.
    * @param attr the attribute to be converted, not <code>null</code>.
    * @return <code>true</code> if the provided attribute was converted,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any of the provided parameters is
    *    <code>null</code>.
    */
   public static boolean convertToRxMarkup(Element elem, Attr attr)
   {
      if (elem == null || attr == null)
         throw new IllegalArgumentException("parameters cannot be null");

      String name = attr.getName();
      String value = attr.getValue();

      int pos = value.indexOf(
         Splitter.getConfig().getProperty("propertyPrefix"));
      if (pos >= 0 && name.startsWith(
         Splitter.getConfig().getProperty("dynamicTag")))
      {
         elem.setAttribute(Splitter.getConfig().getProperty("propertyTag") +
            getRealAttrName(name), value.substring(pos+1));

         elem.removeAttribute(name);

         return true;
      }

      return false;
   }

   /**
    * Transforms all assembler property mark-ups found in the provided
    * element.
    * <p>
    * <img src="/images/nav_start.gif" psx-src="$rximage/nav_start.gif"/> will
    * be translated into
    * <p>
    * <img>
    *    <xsl:call-template name="rx-context">
    *       <xsl:with-param name="attribute-name" select="'src'"/>
    *       <xsl:with-param name="attribute-value" select="
    *          concat($rximage, '/nav_start.gif')"/>
    *    </xsl:call-template>
    * </img>
    *
    * @param doc the document for which we transform the assembler properties,
    *    not <code>null</code>.
    * @param elem the element to transform assembler properties for, not
    *    <code>null</code>.
    * @param contexts a map of global variables to be created, not
    *    <code>null</code>, may be empty.
    * @return <code>true</code> if an assembler property has been transformed,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any parameter provided is
    *    <code>null</code>.
    */
   public boolean transformProperties(Document doc, Element elem,
      Map contexts)
   {
      if (doc == null || elem == null || contexts == null)
         throw new IllegalArgumentException("parameters cannot be null");

      boolean transformed = false;
      NamedNodeMap attrs = elem.getAttributes();
      for (int i=0; i<attrs.getLength(); i++)
      {
         Attr attr = (Attr) attrs.item(i);
         if (isPropertyAttr(attr))
         {
            String value = getSelectValue(attr);
            if (value != null)
            {
               String propertyName = getPropertyName(attr);
               String variable = createPropertyLookup(propertyName);
               contexts.put(propertyName, variable);

               Element callTemplate = doc.createElement("xsl:call-template");
               callTemplate.setAttribute("name", "rx-context");
               callTemplate.appendChild(
                  createWithParam(doc, "attribute-name", "'" +
                     getRealAttrName(attr.getName()) + "'"));
               callTemplate.appendChild(
                  createWithParam(doc, "attribute-value", value));

               Element ifElem = doc.createElement("xsl:if");
               ifElem.setAttribute("test", "$" + propertyName);
               ifElem.appendChild(callTemplate);

               elem.removeAttribute(attr.getName());
               elem.insertBefore(ifElem, elem.getFirstChild());

               transformed = true;
            }
         }
      }

      return transformed;
   }

   /**
    * Test if the provided attribute is makekedup as an assembler property.
    *
    * @param attr the attribute to test, assumed not <code>null</code>.
    * @return <code>true</code> if the provided attribute is a property
    *    markup, <code>false</code> otherwise.
    */
   private boolean isPropertyAttr(Attr attr)
   {
      return attr.getName().startsWith(
         Splitter.getConfig().getProperty("propertyTag"));
   }

   /**
    * Get the real attribute name for the provided attribute name. If the
    * provided name has a percussion markup, its removed and the bare
    * attribute name returned. Otherwise the provided name is returned as
    * provided.
    *
    * @param name the attribute name we need the real name for, assumed not
    *    <code>null</code>.
    * @return the attribute name with its "psx-" or "rx-" prefix removed,
    *    never <code>null</code>.
    */
   private static String getRealAttrName(String name)
   {
      if (name.startsWith(Splitter.getConfig().getProperty("dynamicTag")))
         return name.substring(
            Splitter.getConfig().getProperty("dynamicTag").length());

      if (name.startsWith(Splitter.getConfig().getProperty("propertyTag")))
         return name.substring(
            Splitter.getConfig().getProperty("propertyTag").length());

      return name;
   }

   /**
    * Transforms slot markups for the provided element to XSL into the
    * supplied document.
    *
    * @param doc the document in which to create the slot XSL, not
    *    <code>null</code>.
    * @param node the element to translate into XSL, not <code>null</code>.
    * @return <code>true</code> if the provided element was translated or
    *    <code>false</code> if the element was not markedup as slot.
    * @throws IllegalArgumentException for any <code>null</code> parameter
    *    provied.
    * @throws SplitterException if the transformation failed unexpectedly.
    */
   public boolean transformSlots(Document doc, Node node)
      throws SplitterException
   {
      if (doc == null || node == null)
         throw new IllegalArgumentException("parameters cannot be null");

      int slotType = isSlotMarkup(node);
      if (slotType == -1)
         return false;

      if (slotType == SLOT)
         transformSlot(doc, (Comment) node);
      else if (slotType == SNIPPET_WRAPPER)
         transformSnippetWrapper(doc, (Comment) node);
      else if (slotType == SNIPPET)
      {
         // only necessary for markups where template is provided
         if (node.getNodeType() != Node.COMMENT_NODE)
         {
            Element elem = (Element) node;
            if (elem.getAttributeNode(TEMPLATE_ATTR) != null)
            {
               boolean changeMode = true;
               String slotName = elem.getAttribute(SLOTNAME_ATTR).trim();
               String templateName = elem.getAttribute(TEMPLATE_ATTR).trim();
               if (slotName.length() > 0 && templateName.length() > 0
                     && !slotName.equalsIgnoreCase(templateName))
                  changeMode = false;
               addSlotTemplateCall(doc, elem.getAttribute(SLOTNAME_ATTR),
                  elem.getAttribute(TEMPLATE_ATTR),
                  makeUnique(SLOT_NAME_PREFIX), elem, changeMode);
            }
         }
      }

      return true;
   }

   /**
    * Create and add the node-set variable for related content and apply the
    * slot template.
    * @param doc the document in which to add the node-set variable and apply
    *    the slot template, assumed not <code>null</code>.
    * @param slotName the slot name for which this will be added, assumed
    *    not <code>null</code> or empty.
    * @param template the template name to be called. If <code>null</code> is
    *    provided, the slotName will be used as template name.
    * @param nodeSetName the name of the node-set variable, assumed not
    *    <code>null</code> or empty.
    * @param container the container element to which the template call will
    *    be added, assumed not <code>null</code>.
    * @param changeMode TODO
    *
    * @throws SplitterException if anything failed adding the solt template
    *    call.
    */
   private void addSlotTemplateCall(Document doc, String slotName,
         String template, String nodeSetName, Node container, 
         boolean changeMode)
         throws SplitterException
   {
      Element variable = createNodeSetVariable(doc, slotName, template,
         nodeSetName);

      Element apply = doc.createElement("xsl:apply-templates");
      apply.setAttribute("select", "$" + nodeSetName + "/*");
      String mode = SLOT_NAME_PREFIX;
      if(changeMode)
         mode = createTemplateMode(SLOT_NAME_PREFIX);
      apply.setAttribute("mode", mode);

      Node parent = container.getParentNode();
      parent.insertBefore(apply, container.getNextSibling());
      parent.insertBefore(variable, container.getNextSibling());

      if (container.getNodeType() == Node.ELEMENT_NODE)
      {
         Iterator params =
            getSlotParameters((Element) container).entrySet().iterator();
         while (params.hasNext())
         {
            Map.Entry param = (Map.Entry) params.next();
            apply.appendChild(createWithParam(doc, (String) param.getKey(),
               "'" + (String) param.getValue() + "'"));
         }
      }
   }

   /**
    * Creates the releated content node-set for the provided slot.
    *
    * @param doc the document for which to create the node-set variable,
    *    assumed not <code>null</code>.
    * @param slotName the slot name, assumed not <code>null</code> or empty.
    * @param template the template name to be called. If <code>null</code> is
    *    provided, the slotName will be used as template name.
    * @param the nodeSetName the name of the node-set variable, assumed not
    *    <code>null</code> or empty.
    * @return an Element containing the related content node-set variable for
    *    the provided slot, never <code>null</code>.
    */
   private Element createNodeSetVariable(Document doc, String slotName,
      String template, String nodeSetName)
   {
      String templateName = template;
      if (templateName == null)
         templateName = slotName;

      Element copyOf = doc.createElement("xsl:copy-of");
      copyOf.setAttribute("select", "$" + RELATED_NODE_SET +
         "/linkurl[@slotname='" + slotName + "']");

      Element rxslot = doc.createElement(SLOT_NAME_PREFIX);
      rxslot.setAttribute("template", templateName);
      if (ms_slotAttributes != null)
      {
         for (int i=0; i<ms_slotAttributes.getLength(); i++)
         {
            Node attr = ms_slotAttributes.item(i);
            if (!attr.getNodeName().equals(templateName) &&
                !attr.getNodeName().equals("id"))
               rxslot.setAttribute(attr.getNodeName(), attr.getNodeValue());
         }
      }
      rxslot.appendChild(copyOf);

      Element variable = doc.createElement("xsl:variable");
      variable.setAttribute("name", nodeSetName);
      variable.appendChild(rxslot);

      return variable;
   }

   /**
    * Transforms the provided slot.
    *
    * @param doc the document in which to transform the slot, assumed not
    *    <code>null</code>.
    * @param openingTag the opening slot tag, assumed not <code>null</code>.
    * @throws SplitterException if no snippet was found.
    */
   private void transformSlot(Document doc, Comment openingTag)
      throws SplitterException
   {
      // create the slot template
      Element slot = doc.createElement("xsl:template");
      slot.setAttribute("match", "rxslot[@template='" +
         getSlotName(openingTag) + "']");
      slot.setAttribute("mode", createTemplateMode(SLOT_NAME_PREFIX));

      Element testEmpty = doc.createElement("xsl:if");
      testEmpty.setAttribute("test", "linkurl");

      wrapWithElement(testEmpty, openingTag,
         getClosingTag(openingTag, END_SLOT), true);
      slot.appendChild(testEmpty);

      String rxcas = makeUnique(TEMPATE_MODE_PREFIX);
      Element apply = doc.createElement("xsl:apply-templates");
      apply.setAttribute("select", "linkurl");
      apply.setAttribute("mode", rxcas);

      Node snippetWrapperNode = null;
      transformSlotComments(doc, slot);

      NodeList divs = slot.getElementsByTagName("div");
      for (int i=0; i<divs.getLength(); i++)
      {
         Node div = divs.item(i);
         if (((Element) div).getAttributeNode(SLOTNAME_ATTR) != null)
         {
            Node parent = div.getParentNode();
            snippetWrapperNode = parent.replaceChild(apply, div);
            break;
         }
      }

      // add slot template to stylesheet
      doc.getDocumentElement().appendChild(slot);

      Element forEach = null;
      Node snippet = getSnippet((Element) slot);
      if (snippetWrapperNode != null)
      {
         // create the snippet wrapper template
         Element snippetWrapper = doc.createElement("xsl:template");
         snippetWrapper.setAttribute("match", "linkurl");
         snippetWrapper.setAttribute("mode", rxcas);

         NodeList children = snippetWrapperNode.getChildNodes();
         for (int i=0; i<children.getLength(); i++)
         {
            Node child = children.item(i);
            snippetWrapper.appendChild(child.cloneNode(true));
         }

         // add snippet wrapper template to the stylesheet
         doc.getDocumentElement().appendChild(snippetWrapper);

         if (snippet == null)
            snippet = getSnippet((Element) snippetWrapper);
      }
      else
      {
         forEach = doc.createElement("xsl:for-each");
         forEach.setAttribute("select", "linkurl");
      }

      // apply the body template for the snippet
      if (snippet != null)
      {
         String slotName = ((Element) snippet).getAttribute(SLOTNAME_ATTR)
               .trim();
         if (((Element) snippet).getAttributeNode(TEMPLATE_ATTR) == null)
         {
            Element body = doc.createElement("xsl:copy-of");
            body.setAttribute("select", "document(Value/@current)/*/body/node()");

            Node parent = snippet.getParentNode();
            if (forEach == null)
               parent.replaceChild(body, snippet);
            else
            {
               forEach.appendChild(body);
               parent.replaceChild(forEach, snippet);
            }
         }
         boolean changeMode = true;
         String templateName = ((Element) snippet).getAttribute(TEMPLATE_ATTR).trim();
         if (slotName.length() > 0 && templateName.length() > 0
               && !slotName.equalsIgnoreCase(templateName))
            changeMode = false;

         addSlotTemplateCall(doc, slotName, null,
            makeUnique(SLOT_NAME_PREFIX), openingTag, changeMode);
      }
      else
      {
         // we must have a snippet
         throw new SplitterException("Missing snippet");
      }
   }

   /**
    * Transforms the provided snippet wrapper (wraps it with div tags).
    *
    * @param doc the document in which to transform the snippet wrapper,
    *    assumed not <code>null</code>.
    * @param openingTag the snippet wrapper opening tag, assumed not
    *    <code>null</code>.
    * @throws SplitterException if no slot name was found in the
    *    provided opening tag.
    */
   private void transformSnippetWrapper(Document doc,
      Comment openingTag) throws SplitterException
   {
      Element snippetWrapper = doc.createElement("div");
      snippetWrapper.setAttribute(SLOTNAME_ATTR, getSlotName(openingTag));

      wrapWithElement(snippetWrapper, openingTag,
         getClosingTag(openingTag, END_SNIPPET_WRAPPER), true);
   }

   /**
    * Get the slot name from the provided node.
    *
    * @param node the node to get the slot name from, assumed not
    *    <code>null</code>.
    * @return the slot name, never <code>null</code> or empty.
    * @throws SplitterException if no slot name (snippet) was found in the
    *    provided node.
    */
   private String getSlotName(Node node) throws SplitterException
   {
      Node snippet = getSnippet((Element) node.getParentNode());
      if (snippet == null)
         throw new SplitterException("Missing snippet");

      saveSlotAttributes(snippet);
      return ((Element) snippet).getAttribute(SLOTNAME_ATTR);
   }

   /**
    * Get the node markedup as snippet.
    *
    * @param elem to element in which to search for the snippet markup,
    *    assumed not <code>null</code>.
    * @return the node markedup as snippet or <code>null</code> if not found.
    */
   private Node getSnippet(Element elem)
   {
      NodeList nodes = elem.getElementsByTagName("div");
      Node snippet = isSnippet(nodes);
      if (snippet == null)
      {
         nodes = elem.getElementsByTagName("span");
         snippet = isSnippet(nodes);
      }

      return snippet;
   }

   /**
    * Test if the provided node list contains a node markedup as snippet.
    *
    * @param nodes the node list to test, assumed not <code>null</code>.
    * @return Node the node markedup as snippet or <code>null</code> if not
    *    found.
    */
   private Node isSnippet(NodeList nodes)
   {
      for (int i=0; i<nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (node.getNodeType() != Node.ELEMENT_NODE)
            continue;

         String id = ((Element) node).getAttribute("id");
         String slotName = ((Element) node).getAttribute(SLOTNAME_ATTR);
         if (id != null && slotName != null && id.startsWith(
            Splitter.getConfig().getProperty("dynamicTag")))
            return node;
      }

      return null;
   }

   /**
    * Transforms all slot comments of the provided node.
    *
    * @param doc the document in which to transform the node, assumed not
    *    <code>null</code>.
    * @param node the node to transform, assumed not <code>null</code>.
    * @throws SplitterException if the transformation failed.
    */
   private void transformSlotComments(Document doc, Node node)
      throws SplitterException
   {
      NodeList nodeList = null;
      Node nodeTemp = null;
      if (node.hasChildNodes())
      {
         // transform all child nodes first
         nodeList = node.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++)
         {
            nodeTemp = nodeList.item(i);
            transformSlotComments(doc, nodeTemp);
         }
      }

      // transform all comment nodes
      if (node.getNodeType() == Node.COMMENT_NODE)
         transformSlots(doc, node);
   }
   
   /**
    * Creates the mode attribute value used for <code>xsl:template</code> and
    * <code>xsl:apply-templates</code> elements.
    * 
    * @param base the base name to use, assumed not <code>null</code> or
    *    empty.
    * @return the supplied base name with the template name suffix appended
    *    separated by <code>-</code>, never <code>null</code> or empty.
    */
   private String createTemplateMode(String base)
   {
      String temp = base;
      if (m_templateNameSuffix.length() > 0)
         temp += "-" + m_templateNameSuffix;
      
      return temp;
   }

   /**
    * Makes the provided string unique within this class by adding a unique
    * number. The string is result of concatinating the supplied base string,
    * the template name suffix {@link #m_templateNameSuffix} and a unique number
    * within the template separated by "-". The template suffix (and its
    * preceding "-") is added only if it is non empty.
    * 
    * @param base the string to make unique, assumed not <code>null</code>.
    * @return a unique string, never <code>null</code> or empty.
    */
   private String makeUnique(String base)
   {
      String temp =  base;
      if (m_templateNameSuffix.length() > 0)
         temp += "-" + m_templateNameSuffix;
      temp += "-" + m_uniqueNumber++;
      
      return temp;
   }

   /**
    * Wraps the code between the provided opening and closing comments with
    * the supplied wrapper.
    *
    * @param wrapper the element with which the opening and closing comments
    *    will be wrapped, assumed not <code>null</code>.
    * @param openingTag the opening comment to be wrapped, assumed not
    *    <code>null</code>.
    * @param closingTag the closing comment to be wrapped, assumed not
    *    <code>null</code>.
    * @param remove if <code>true</code> is provided the wrapped code will
    *    be removed, otherwise not.
    * @throws SplitterException if the element wrapping failed.
    */
   private void wrapWithElement(Element wrapper, Comment openingTag,
      Comment closingTag, boolean remove)
      throws SplitterException
   {
      List removeList = new ArrayList();
      Node sibling = openingTag.getNextSibling();
      while (sibling != null)
      {
         wrapper.appendChild(sibling.cloneNode(true));
         removeList.add(sibling);

         sibling = sibling.getNextSibling();
         if (sibling != null && sibling.getNodeType() == Node.COMMENT_NODE)
         {
            if (closingTag.getData().equals(((Comment) sibling).getData()))
               break;
         }
      }

      Node parent = openingTag.getParentNode();
      for (int i=0; i<removeList.size() && remove; i++)
         parent.removeChild((Node) removeList.get(i));

      parent.insertBefore(wrapper, openingTag.getNextSibling());
   }

   /**
    * This method tests if the provided element is markedup as slot and
    * returns the slot type.
    *
    * @param elem the element to test, assumed not <code>null</code>.
    * @return -1 if the provided element is not markedup as slot, the slot
    *    type otherwise.
    */
   private int isSlotMarkup(Node node)
   {
      int type = node.getNodeType();
      if (type == Node.COMMENT_NODE)
      {
         String comment = ((Comment) node).getData();
         if (comment != null)
         {
            if (matchesPattern(comment, START_SLOT))
               return SLOT;

            if (matchesPattern(comment, START_SNIPPET_WRAPPER))
               return SNIPPET_WRAPPER;
         }
      }
      else if (type == Node.ELEMENT_NODE)
      {
         Element elem = (Element) node;

         if (elem.getTagName().equalsIgnoreCase("div"))
         {
            if (elem.getAttributeNode(SLOT_MARKUP[0]) != null)
               return SLOT_DIV;

            if (elem.getAttributeNode(SNIPPET_WRAPPER_MARKUP[0]) != null)
               return SNIPPET_WRAPPER_DIV;
         }

         if (elem.getTagName().equalsIgnoreCase("span") ||
             elem.getTagName().equalsIgnoreCase("div"))
         {
            /* SLOTNAME_ATTR attribute has an alias of SLOTNAME_ATTR_ALIAS to
             * use consistent notation. If present normalize it. Note that the
             * attribute SLOTNAME_ATTR_ALIAS overrides SLOTNAME_ATTR when both
             * are present
             */
            Attr slotname = elem.getAttributeNode(SLOTNAME_ATTR_ALIAS);
            if(slotname != null)
            {
               elem.setAttribute(SLOTNAME_ATTR, slotname.getValue());
               elem.removeAttributeNode(slotname);
            }
            slotname = elem.getAttributeNode(SLOTNAME_ATTR);
            if (slotname != null && slotname.getValue().trim().length() > 0)
            {
               saveSlotAttributes(elem);
               return SNIPPET;
            }
         }
      }

      return -1;
   }

   /**
    * If no dynamic id attribute was provided in the HTML mark-up, we add the
    * standard id as psx-sys_AssemblerInfo to create the apply-templates call.
    *
    * @param elem the element to test for the id, not <code>null</code>.
    * @throws IllegalArgumentException if the provided parameter is
    *    <code>null</code>.
    */
   public static void addDynamicSlotId(Element elem)
   {
      if (elem == null)
         throw new IllegalArgumentException("element cannot be null");

      if (elem.getTagName().equalsIgnoreCase("span") ||
          elem.getTagName().equalsIgnoreCase("div"))
      {
         /* normalize the SLOTNAME_ATTR_ALIAS(if exists) to SLOTNAME_ATTR before
          * processing.
          */
         Attr slotname = elem.getAttributeNode(SLOTNAME_ATTR_ALIAS);
         if(slotname != null)
         {
            elem.setAttribute(SLOTNAME_ATTR, slotname.getValue());
            elem.removeAttributeNode(slotname);
         }

         slotname = elem.getAttributeNode(SLOTNAME_ATTR);
         if (slotname == null)
            return;

         String dynamicTag = Splitter.getConfig().getProperty("dynamicTag");
         Attr id = elem.getAttributeNode("id");
         if (id == null || !id.getValue().startsWith(dynamicTag))
            elem.setAttribute("id", dynamicTag + ASSEMBLER_INFO_ELEM);
      }
   }

   /**
    * Finds and returns the closing tag for the provided opening tag.
    *
    * @param openingTag the opening tag to find the closing tag for, assumed
    *    not <code>null</code>.
    * @param pattern the marku-up pattern used, assumed not <code>null</code>.
    * @return the closing tag found, never <code>null</code>.
    * @throws SplitterException if no closing tag was found.
    */
   private Comment getClosingTag(Comment openingTag, String[] pattern)
      throws SplitterException
   {
      Node sibling = openingTag.getNextSibling();
      while (sibling != null)
      {
         if (sibling.getNodeType() == Node.COMMENT_NODE)
         {
            if (matchesPattern(((Comment) sibling).getData(), pattern))
               return (Comment) sibling;
         }

         sibling = sibling.getNextSibling();
      }

      Object[] args = { openingTag.getData() };
      String msg = MessageFormat.format(
         "Missing closing tag for opening tag: {0}", args);
      throw new SplitterException(msg);
   }

   /**
    * Tests if the provided comment matches the supplied pattern. A match is
    * found if all pattern elements are found in array order in the provided
    * comment. Whitespace, case and any additional strings after the
    * pattern are ignored.
    *
    * @param comment the comment tested to match the provided pattern, might
    *    be <code>null</code> or empty.
    * @param pattern an array of String objects specifying the pattern, not
    *    <code>null</code> or empty.
    * @return <code>true</code> if the provided comment matches the pattern,
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if the provided pattern array is
    *    <code>null</code> or empty.
    */
   private boolean matchesPattern(String comment, String[] pattern)
   {
      if (pattern == null || pattern.length == 0)
         throw new IllegalArgumentException("pattern cannot be null or empty");

      int foundPatternElements = 0;
      int pos = 0;
      StringTokenizer tok = new StringTokenizer(comment.trim(), DELIMITER);
      while (tok.hasMoreElements() && pos<pattern.length)
      {
         String element = tok.nextToken();

         // skip whitespace
         if (element.equals(DELIMITER))
            continue;

         if (!element.equalsIgnoreCase(pattern[pos++]))
            return false;

         ++foundPatternElements;
      }

      return (foundPatternElements == pattern.length);
   }

   /**
    * The delimiter used to separate pattern strings, never <code>null</code>.
    */
   private static String DELIMITER = " ";

   /**
    * An array of String objects specifying the start of a slot, never
    * <code>null</code> or empty.
    */
   public static String[] START_SLOT =
   {
      "start",
      "slot"
   };

   /**
    * An array of String objects specifying the end of a slot, never
    * <code>null</code> or empty.
    */
   public static String[] END_SLOT =
   {
      "end",
      "slot"
   };

   /**
    * An array of String objects specifying the slot markup within <div> tags,
    * never <code>null</code> or empty.
    */
   private static String[] SLOT_MARKUP =
   {
      "slot",  // the attribute name
      "start"  // the attribute value
   };

   /**
    * An array of String objects specifying the start of a snippet wrapper,
    * never <code>null</code> or empty.
    */
   public static String[] START_SNIPPET_WRAPPER =
   {
      "start",
      "snippet",
      "wrapper"
   };

   /**
    * An array of String objects specifying the end of a snippet wrapper,
    * never <code>null</code> or empty.
    */
   public static String[] END_SNIPPET_WRAPPER =
   {
      "end",
      "snippet",
      "wrapper"
   };

   /**
    * An array of String objects specifying the snippet wrapper markup
    * within <div> tags, never <code>null</code> or empty.
    */
   private static String[] SNIPPET_WRAPPER_MARKUP =
   {
      "snippetWrapper", // the attribute name
      "start"           // the attribute value
   };

   /**
    * Get a map of all slot parameters out of the provided element.
    * To be recognized as slot parameters, they must be marked-up like
    * paramN="'name', 'value'", where N is a number >= 1.
    *
    * @param elem the element from where to get the slot parameters,
    *    assumed not <code>null</code>.
    * @return a map of parameters found, never <code>null</code>, might be
    *    empty. The map key is the parameter name while the map value is the
    *    parameter value.
    * @throws SplitterException if the slot markup is invalid.
    */
   private Map getSlotParameters(Element elem) throws SplitterException
   {
      NamedNodeMap attrs = elem.getAttributes();
      Map params = new HashMap();
      for (int i=0; i<attrs.getLength(); i++)
      {
         Attr attr = (Attr) attrs.item(i);
         String name = attr.getName();
         if (name != null && name.startsWith(SLOT_PARAM))
         {
            int paramNumber = 0;
            try
            {
               paramNumber = Integer.parseInt(name.substring(SLOT_PARAM.length()));
            }
            catch (NumberFormatException e)
            {
               // no-op
            }

            // guess its not a slot parameter
            if (paramNumber <= 0)
               continue;

            StringTokenizer tok = new StringTokenizer(
               attr.getValue().trim(), "'");
            try
            {
               String key = tok.nextToken().trim();
               tok.nextToken().trim(); // read over the comma
               String value = tok.nextToken().trim();

               params.put(key, value);
            }
            catch (NoSuchElementException e)
            {
               throw new SplitterException("Invalid slot mark-up: " +
                  e.getLocalizedMessage());
            }
         }
      }

      return params;
   }

   /**
    * Adds the global variable for related content and all xsl:import
    * elements used for content assemblers to the provided document.
    *
    * @param doc the document to which to add, not <code>null</code>.
    * @param addGlobalTemplates supply <code>true</code> if the imports for
    *    global templates should be added, <code>false</code> otherwise.
    * @return <code>true</code> if the includes were added, <code>false</code>
    *    otherwise.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public boolean addAssemblerImports(Document doc, boolean addGlobalTemplates)
   {
      if (doc == null)
         throw new IllegalArgumentException("document cannot be null");

      List templates =
         Splitter.getConfig().getStylesheetLocations("Globals");

      if (addGlobalTemplates)
         templates.addAll(
            Splitter.getConfig().getStylesheetLocations("GlobalTemplates"));

      templates.addAll(
         Splitter.getConfig().getStylesheetLocations("ContextTemplates"));

      templates.addAll(
         Splitter.getConfig().getStylesheetLocations("SlotTemplates"));

      templates.addAll(
         Splitter.getConfig().getStylesheetLocations("InlineLinkTemplates"));

      templates.addAll(
         Splitter.getConfig().getStylesheetLocations("I18NTemplates"));
      
      Element docElem = doc.getDocumentElement();
      Element related = doc.createElement("xsl:variable");
      related.setAttribute("name", RELATED_NODE_SET);
      related.setAttribute("select",
         "/*/" + ASSEMBLER_INFO_ELEM + "/RelatedContent");
      docElem.insertBefore(related, docElem.getFirstChild());
      for (int i=0; i<templates.size(); i++)
      {
         Element include = doc.createElement("xsl:import");
         include.setAttribute("href", (String) templates.get(i));

         docElem.insertBefore(include, docElem.getFirstChild());
      }

      return true;
   }

   /**
    * A template is added to be processed for all elements (*) to add a
    * non breaking space if the element is empty. This is to show table cells
    * correct in case there is no data.
    * <p>
    * This adds a template excluding the provided element from this
    * behavier. Otherwise the NBSP template would add an NBSP for all slots.
    *
    * @param doc the document to which this will add the template, not
    *    <code>null</code>.
    * @param exclude the element name to be excluded from adding NBSP's if
    *    empty, not <code>null</code> or empty.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code> or the exclude element is <code>null</code> or empty.
    */
   public void excludeNBSPTemplate(Document doc, String exclude)
   {
      if (doc == null)
         throw new IllegalArgumentException("document cannot be null");
      if (exclude == null || exclude.trim().length() == 0)
         throw new IllegalArgumentException(
            "exclude element can not be null or empty");

      Element template = doc.createElement("xsl:template");
      template.setAttribute("match", exclude);
      doc.getDocumentElement().appendChild(template);
   }

   /**
    * This prepares the value for the select attribute out of the supplied
    * attribute markedup as assembler property.
    *
    * @param attr the attribute markedup as assembler property to create the
    *    select value for, assumed not <code>null</code>.
    * @return the select value for the provided assembler property attribute,
    *    never <code>null</code> or empty.
    */
   private String getSelectValue(Attr attr)
   {
      String value = attr.getValue();
      int pos = value.indexOf('/');
      if (pos >= 0)
      {
         StringBuffer selectValue = new StringBuffer("concat($");
         selectValue.append(value.substring(0, pos));
         selectValue.append(", '");
         selectValue.append(value.substring(pos));
         selectValue.append("')");

         return selectValue.toString();
      }

      return value;
   }

   /**
    * Get the assembler property name of the supplied attribute. The attribute
    * value is assumed in the format "rxproperty/file.ext", where the
    * substring before the first "/" is considerd the property name.
    *
    * @param attr the attribute we want the property name from, assumed not
    *    <code>null</code>.
    * @return the property name, never <code>null</code>, might be empty.
    */
   private String getPropertyName(Attr attr)
   {
      String value = attr.getValue();
      int pos = value.indexOf('/');
      if (pos >= 0)
         return value.substring(0, pos);

      return "";
   }

   /**
    * Creates a <xsl:param-with ...> element with the attribute 'name' set to
    * the provided name and the attribute 'select' set to the supplied select.
    *
    * @param doc the document to create the element for, not <code>null</code>.
    * @param name the value for the name attribute, not <code>null</code> or
    *    empty.
    * @param select the value for the select attribute, not
    *    <code>null</code> or empty.
    * @return the xsl:param-with element created, never <code>null</code>.
    * @throws IllegalArgumentException if any parameter is <code>null</code>
    *    or any of the provided attribute values is empty.
    */
   public Element createWithParam(Document doc, String name,
      String select)
   {
      if (doc == null)
         throw new IllegalArgumentException("document cannot be null");
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      if (select == null || select.trim().length() == 0)
         throw new IllegalArgumentException("select cannot be null or empty");

      Element param = doc.createElement("xsl:with-param");
      param.setAttribute("name", name);
      param.setAttribute("select", select);

      return param;
   }

   /**
    * Creates and adds the inline link template to the provided document.
    *
    * @param doc the document into which to add the template, not
    *    <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public void addInlineLinkTemplate(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      Element apply = doc.createElement("xsl:apply-templates");
      apply.setAttribute("select", "*");
      apply.setAttribute("mode", RX_BODY_FIELD);

      Element template = doc.createElement("xsl:template");
      template.setAttribute("match", "*[div/@class='" + RX_BODY_FIELD + "']");
      template.appendChild(apply);

      Element docElem = doc.getDocumentElement();
      docElem.appendChild(template);
   }
   
   /**
    * Creates and adds the correct global template to the supplied document.
    * 
    * @param markup the markup decribes what template to create, <code>*</code>
    *    for adding the global template dispatching template, the template
    *    name for adding a local override global template, may be 
    *    <code>null</code> or empty for which case no template will be added.
    * @param doc the document into which to add the template, not
    *    <code>null</code>.
    */
   public void addGlobalTemplate(String markup, Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc cannot be null");
      
      if (markup != null && markup.trim().length() > 0)
      {
         Element stylesheet = doc.getDocumentElement();
         Element template = null;
         if (markup.equals("*"))
            template = createGlobalTemplate(
               "psx-global-template-dispatcher", doc);
         else
            template = createGlobalTemplate(markup, doc);
         
         stylesheet.insertBefore(template, stylesheet.getFirstChild());
      }
   }
   
   /**
    * Creates the template which calls the global template as specified by
    * the supplied name.
    * 
    * @param name the global template name, assumed not <code>null</code>.
    * @param doc the document for which to create the template, assumed not
    *    <code>null</code>.
    * @return the template element, never <code>null</code>.
    */
   private Element createGlobalTemplate(String name, Document doc)
   {
      Element template = doc.createElement("xsl:template");
      template.setAttribute("match", "/");
      template.setAttribute("priority", "100");
      
      Element applyTemplate = doc.createElement("xsl:call-template");
      applyTemplate.setAttribute("name", name);
      template.appendChild(applyTemplate);
      
      return template;
   }

   /**
    * Create the property lookup mark-up string for the provided property.
    *
    * @param property the property to create the lookup string for, assumed
    *    not <code>null</code>.
    * @return the lookup string, never <code>null</code>.
    */
   private static String createPropertyLookup(String property)
   {
      return "psx-" + ASSEMBLER_INFO_ELEM +
         "/AssemblerProperties/Property[@name='" + property +
         "']/Value/@current";
   }

   /**
    * Save the attributes for the supplied slot node. This will overwrite any
    * existing attributes.
    *
    * @param node the slot not for which to save the attributes, assumed not
    *    <code>null</code>.
    */
   private static void saveSlotAttributes(Node node)
   {
      ms_slotAttributes = node.getAttributes();
   }

   /**
    * A storage to save all attributes from the slot markup so they can be
    * recreated in the slot template. This will be reset for each slot
    * processed.
    */
   private static NamedNodeMap ms_slotAttributes = null;

   /**
    * The constant used to mark-up the slot name. Initialized during
    * construction, never <code>null</code> after that.
    */
   public static final String SLOTNAME_ATTR = "slotname";

   /**
    * Alias for SLOTNAME_ATTR to use the convention of reserving attribute names
    * starting with psx (no hyphen) for internal use of the system. Initialized
    * during construction, never <code>null</code> after that.
    */
   private static final String SLOTNAME_ATTR_ALIAS = "psxslotname";

   /**
    * The constant used to mark-up the slot template. Initialized during
    * construction, never <code>null</code> after that.
    */
   private static final String TEMPLATE_ATTR = "template";

   /**
    * ID to identify slot start for comment tags.
    */
   private static final int SLOT = 0;

   /**
    * ID to identify slot start for <div> tags.
    */
   private static final int SLOT_DIV = 1;

   /**
    * ID to identify snippet wrapper start for comment tags.
    */
   private static final int SNIPPET_WRAPPER = 2;

   /**
    * ID to identify snippet wrapper start for <div> tags.
    */
   private static final int SNIPPET_WRAPPER_DIV = 3;

   /**
    * ID to identify snippet start tags.
    */
   private static final int SNIPPET = 4;

   /**
    * The constant used to mark-up a slot parameter. Slot parameters are
    * enumerated starting at 1. Initialized during construction, never
    * <code>null</code> after that.
    */
   private static final String SLOT_PARAM = "param";

   /**
    * A counter to create unique numbers. Must be incremented each time the
    * current number was used.
    */
   private static int m_uniqueNumber = 1;

   /**
    * The prefix string used to name the mode attribute for content assembler
    * templates, never <code>null</code>.
    */
   private static final String TEMPATE_MODE_PREFIX = "rxcas";

   /**
    * The prefix string used to match content assembler templates,
    * never <code>null</code>.
    */
   private static final String SLOT_NAME_PREFIX = "rxslot";

   /**
    * The name used for the variable created for the related content node-set,
    * never <code>null</code>.
    */
   private static final String RELATED_NODE_SET = "related";

   /**
    * The body field markup tag used for inline links, never
    * <code>null</code>.
    */
   private static final String RX_BODY_FIELD = "rxbodyfield";

   /**
    * Field to store the template name suffix. Initialized in the ctor
    * {@link #AssemblerTransformation(String)} the usage of which is also
    * described there. Never <code>null</code> may be empty.
    */
   private String m_templateNameSuffix = "";

   /**
    * The element name for the assembler info added to the result document.
    */
   public static final String ASSEMBLER_INFO_ELEM = "sys_AssemblerInfo";
}
