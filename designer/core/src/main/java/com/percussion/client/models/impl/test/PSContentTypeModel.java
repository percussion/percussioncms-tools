/******************************************************************************
 *
 * [ PSContentTypeModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl.test;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import com.percussion.client.models.impl.PSCmsModel;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.xml.serialization.PSObjectSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSContentTypeModel extends PSCmsModel implements
   IPSContentTypeModel
{

   public PSContentTypeModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   /*
    * @see com.percussion.client.models.IPSContentTypeModel#getControls()
    */
   public List<PSControlMeta> getControls() throws PSModelException
   {
      List<PSControlMeta> ctrlList = new ArrayList<PSControlMeta>();
      Document controlXML;
      controlXML = getDocument("sys_Templates.xsl");
      if (controlXML != null)
      {
         try
         {
            NodeList controlNodes = controlXML
               .getElementsByTagName(PSControlMeta.XML_NODE_NAME);
            for (int i = 0; controlNodes != null
               && i < controlNodes.getLength(); i++)
            {
               ctrlList.add(new PSControlMeta((Element) controlNodes.item(i)));
            }
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new PSModelException(e);
         }
      }
      return ctrlList;
   }

   /*
    * @see com.percussion.client.models.IPSContentTypeModel#getCEFieldCatalog()
    */
   @SuppressWarnings("unused")
   public PSContentEditorFieldCataloger getCEFieldCatalog(
          boolean force, boolean forDisplayFormat)
      throws PSModelException
   {
      try
      {
         Document doc = getDocument("MockContentEditorFieldCatalogResults.xml");
         final Element root = doc.getDocumentElement();
         IPSFieldCataloger cat = new IPSFieldCataloger() {
            
            public Element getCEFieldXml(int controlFlags, Set<String> fields) 
               throws PSCmsException
            {
               return root;
            }

            public Element getCEFieldXml(int controlFlags) throws PSCmsException
            {
               return root;
            }};
         return new PSContentEditorFieldCataloger(cat, null, 0);
      }
      catch (PSCmsException e)
      {
         throw new PSModelException(e);
      }
   }

   //see interface
   public Collection<IPSReference> getUseableContentTypes(boolean force)
      throws PSModelException
   {
      Collection<IPSReference> ctypes = catalog(force);
      for (Iterator<IPSReference> iter = ctypes.iterator(); iter.hasNext(); )
      {
         IPSReference ref = iter.next();
         if (ref.getName().equalsIgnoreCase("folder"))
         {
            iter.remove();
            break;
         }
      }
      return ctypes;
   }
   //see interface
   @SuppressWarnings("unused") //params, throws
   public Collection<IPSReference> getWorkflowAssociations(
         IPSReference ctypeRef, boolean force)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   //see interface
   @SuppressWarnings("unused") //params, throws
   public Map<IPSReference, Collection<IPSReference>> getWorkflowAssociations(
         Collection<IPSReference> workflowFilter, boolean force)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   // see base class method for details
   public Map<IPSReference, Collection<IPSReference>> getTemplateAssociations(
      Collection<IPSReference> contentTypeFilter, 
      @SuppressWarnings("unused") boolean force,
      boolean lock) 
      throws PSModelException
   {
      maybeLoadRepository();

      if (lock)
         force = true;

      try
      {
         Map<IPSReference, Collection<IPSReference>> results = 
            new HashMap<IPSReference, Collection<IPSReference>>();
         for (IPSReference ctypeKey : m_templateAssociations.keySet())
         {
            if (contentTypeFilter == null
               || contentTypeFilter.contains(ctypeKey))
            {
               Collection<IPSReference> templateRefs = 
                  new ArrayList<IPSReference>();
               templateRefs.addAll(m_templateAssociations.get(ctypeKey));
               if (!templateRefs.isEmpty())
                  results.put(ctypeKey, templateRefs);
            }
         }
         return results;
      }
      catch (Exception e)
      {
         if (e instanceof PSModelException)
            throw (PSModelException) e;
         throw new PSModelException(e);
      }
   }

   /**
    * Load repository if it is not loaded yet.
    */
   private void maybeLoadRepository() throws PSModelException
   {
      if (m_templateAssociations == null)
      {
         try
         {
            loadRepository();
         }
         catch (Exception e)
         {
            System.out.println("Load from repository failed. "
               + "Delete the repository file and try again");
            throw new PSModelException(e);
         }
      }
   }

   // see interface
   public void setTemplateAssociations(
      Map<IPSReference, Collection<IPSReference>> associations)
      throws PSMultiOperationException
   {
      try
      {
         maybeLoadRepository();
      }
      catch (PSModelException e1)
      {
         throw new PSMultiOperationException(e1);         
      }

      if (associations != null)
      {
         for (IPSReference ctypeKey : associations.keySet())
         {
            Collection<IPSReference> templateRefs = new ArrayList<IPSReference>();
            templateRefs.addAll(associations.get(ctypeKey));
            for (final IPSReference ref : templateRefs)
            {
               if (ref == null)
               {
                  throw new IllegalArgumentException("Attempt to pass null template reference");
               }
            }
            m_templateAssociations.put(ctypeKey, templateRefs);
         }
      }
      try
      {
         saveTemplateAssociations();
      }
      catch (Exception e)
      {
         System.out.println("Save to repository failed.");
         throw new PSMultiOperationException(e);
      }
   }

   /**
    * Initializes data needed by this handler by loading it from a file (if one
    * can be found,) or starting w/ a set of hard-coded data.
    */
   private void loadRepository() throws Exception
   {
      Map<String, Collection<String>> testData = new HashMap<String, Collection<String>>();
      if (ms_cTypeTemplateAssocRepFile.exists())
      {
         Reader r = null;
         m_templateAssociations = new HashMap<IPSReference, Collection<IPSReference>>();
         try
         {
            r = new FileReader(ms_cTypeTemplateAssocRepFile);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
            NodeList nl = doc.getElementsByTagName("entry");
            for (int i = 0; i < nl.getLength(); i++)
            {
               Element entry = (Element) nl.item(i);
               Element key = (Element) entry.getElementsByTagName("key")
                  .item(0);
               IPSReference refContentType = refFromXml(key);
               Collection<IPSReference> templateColl = new HashSet<IPSReference>();
               m_templateAssociations.put(refContentType, templateColl);
               NodeList tempElems = ((Element) entry.getElementsByTagName(
                  "value").item(0)).getElementsByTagName("Reference");
               for (int j = 0; j < tempElems.getLength(); j++)
               {
                  IPSReference refTemplate = refFromXml((Element) tempElems
                     .item(j));
                  templateColl.add(refTemplate);
               }
            }
            return;
         }
         finally
         {
            IOUtils.closeQuietly(r);
         }
      }

      // fixme try to get this data from a file first
      // Brief -> Test Body (local)
      // Brief -> S_Callout (shared)
      Collection<String> templateIds = new ArrayList<String>();
      templateIds.add("1-4-100");
      templateIds.add("1-4-210");
      testData.put("0-2-10", templateIds);

      // Generic -> Test Snippet (local)
      // Generic -> P_CI_Generic (shared)
      templateIds = new ArrayList<String>();
      templateIds.add("1-4-110");
      templateIds.add("1-4-200");
      testData.put("0-2-30", templateIds);

      // Press Release -> S_Callout (shared)
      testData.put("0-2-50", Collections.singleton("1-4-210"));
      // convert the string ids to refs
      try
      {
         m_templateAssociations = new HashMap<IPSReference, Collection<IPSReference>>();
         IPSCmsModel templateModel = getModel(PSObjectTypes.TEMPLATE);

         Collection<IPSReference> ctypes = catalog();
         for (IPSReference ctype : ctypes)
         {
            templateIds = testData.get(ctype.getId().toString());
            if (templateIds == null)
               continue;
            Collection<IPSReference> associatedTemplateRefs = new ArrayList<IPSReference>();
            for (String templateIdString : templateIds)
            {
               IPSGuid templateGuid = new PSGuid(templateIdString);
               IPSReference templateRef = templateModel
                  .getReference(templateGuid);
               if (templateRef == null)
               {
                  throw new RuntimeException(
                     "Couldn't find template for id: " + templateIdString);
               }
               associatedTemplateRefs.add(templateRef);
            }
            if (!associatedTemplateRefs.isEmpty())
               m_templateAssociations.put(ctype, associatedTemplateRefs);
         }
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
      saveTemplateAssociations();
   }

   private void saveTemplateAssociations() throws Exception
   {
      Writer w = null;
      try
      {
         Element elem = PSObjectSerializer.getInstance().toXml(
            m_templateAssociations);
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         doc.appendChild(doc.importNode(elem, true));
         w = new FileWriter(ms_cTypeTemplateAssocRepFile);
         PSXmlDocumentBuilder.write(doc, w);
      }
      finally
      {
         IOUtils.closeQuietly(w);
      }

   }

   /**
    * Key is the guid for the ctype, value is a set of guids for the associated
    * templates. Never <code>null</code>, may be empty.
    */
   private Map<IPSReference, Collection<IPSReference>> m_templateAssociations = null;

   /**
    * Simple method to wrap the checked exception in an unchecked one.
    * 
    * @return Never <code>null</code>.
    */
   private static IPSCmsModel getModel(PSObjectTypes type)
   {
      try
      {
         return PSCoreFactory.getInstance().getModel(type);
      }
      catch (PSModelException e)
      {
         // shouldn't happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Reads sys_Templates file and creates a dom document returns
    * 
    * @return Document of sys_Templates.xsl file
    */
   private Document getDocument(String file)
   {
      Document doc = null;
      InputStream is = null;
      try
      {
         Class clazz = getClass();
         is = clazz.getResourceAsStream(file);
         InputSource source = new InputSource(is);
         doc = PSXmlDocumentBuilder.createXmlDocument(source, false);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         IOUtils.closeQuietly(is);
      }
      return doc;
   }

   private IPSReference refFromXml(Element elem) throws PSModelException
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(elem);
      String name = walker.getElementData("name");
      String label = walker.getElementData("label");
      String description = walker.getElementData("description");
      walker.setCurrent(walker.getNextElement("id", true));
      String uuid = walker.getElementData("UUID", false);
      String hostid = walker.getElementData("hostId", false);
      String type = walker.getElementData("type", false);
      walker.setCurrent(walker.getNextElement("objecttype", false));
      String pType = walker.getElementData("primaryType", false);
      String sType = walker.getElementData("secondaryType", false);
      String ts = pType;
      if (!StringUtils.isEmpty(sType))
         ts += ":" + sType;

      final PSReference ref =
            new PSReference(name, label, description,
                  PSObjectTypeFactory.getType(ts),
                  new PSGuid(hostid + "-" + type + "-" + uuid));
      ref.setPersisted();
      return ref;
   }

   private File ms_cTypeTemplateAssocRepFile = new File(
      "content_type_template_assoc_repository.xml");
}
