/******************************************************************************
 *
 * [ UIContentEditorHandler.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.client.IPSReference;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSPageDataTank;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.design.objectstore.PSSortedColumn;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSWhereClause;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSCollection;
import com.percussion.workbench.ui.PSModelTracker;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.dnd.PSTransferFactory;
import com.percussion.workbench.ui.editors.form.ce.PSContentEditorDefinition;
import com.percussion.xml.PSDtdBuilder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an assembler query resource from a content editor present in
 * Eclipse clipboard.
 */
public class UIContentEditorHandler
{
   /**
    * Creates an assembler query resource from a content editor present in
    * Eclipse clipboard. 
    * @param frame The figure frame to which the generated figure is added,
    * may not be <code>null</code>.
    * @param action Must be one of the <code>PASTE_ASSEMBLER_XXX</code> actions, 
    * indicates the type of assembler to create.
    * @param point The point in the frame where the pasted assembler should be 
    * placed, may not be <code>null</code>.
    * 
    * @return The figure, may be <code>null</code> if one was not created.
    */
   public static UIConnectableFigure handlePasteAssembler(UIFigureFrame frame, 
      int action, Point point)
   {
      if (frame == null)
         throw new IllegalArgumentException("frame may not be null");
      
      if (!(action == PASTE_ASSEMBLER_PAGE || action == 
         PASTE_ASSEMBLER_SNIPPET))
            throw new IllegalArgumentException("invalid action");

      if (point == null)
         throw new IllegalArgumentException("point may not be null");
      
      try
      {
         final PSItemDefinition ce = getContentTypeFromClipboard();
         if (ce == null)
         {
            return null;
         }
         else
         {
            final UIConnectableFigure resultFig =
                  createFigureForAssembler(ce, frame, action);
            if (resultFig != null)
            {
               resultFig.setLocation(point);
            }
            return resultFig;
         }
            
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e);
         return null;
      }
   }

   /**
    * Indicates whether Eclipse clipboard contains content type.  
    * @return <code>true</code> if Eclipse clipboard contains reference to a
    * content type. Otherwise returns <code>false</code>.
    */
   public static boolean clipboardContainsContentType()
   {
      final boolean[] result = new boolean[1];
      getDisplay().syncExec(new Runnable()
      {
         public void run()
         {
            result[0] = false;
            final TransferData[] availableData = m_clipboard.getAvailableTypes();
            if (availableData == null)
            {
               return;
            }
            
            for (TransferData td : availableData)
            {
               if (ms_contentTypeTransfer.isSupportedType(td))
               {
                  result[0] = true;
                  return;
               }
            }
         }
      });
      return result[0];
   }

   /**
    * Creates an assembler dataset figure based on the supplied content editor 
    * and adds it to the supplied frame.
    *  
    * @param ce The content editor from which the assembler is generated, 
    * assumed not <code>null</code>.  
    * @param frame The frame in which the resulting figure is placed, assumed 
    * not <code>null</code>. 
    * @param action Assumed to be one of the <code>PASTE_ASSEMBLER_XXX</code>
    * action constants.
    * 
    * @return The figure, may be <code>null</code> if it could not be created.
    */
   private static UIConnectableFigure createFigureForAssembler(
      PSItemDefinition ce, UIFigureFrame frame, int action) throws Exception
   {
      // create a new figure and data object
      UIConnectableFigure uic = frame.add( AppFigureFactory.QUERY_DATASET );
      if(uic != null)
      {
         boolean isPage = (action == PASTE_ASSEMBLER_PAGE);
         String statusKey = isPage ? "statusNewPageAssembler" : 
            "statusNewSnippetAssembler";
         String statusMsg= E2Designer.getResources().getString(statusKey);

         // get the datasets data object and create the query pipe
         OSDataset dataset = (OSDataset) uic.getData();
         AutoCreateDataset.createQueryPipe(dataset, uic);
         
         String pageKey = isPage ? "pageAssemblerName" : "snippetAssemblerName";
         String pageName =  E2Designer.getResources().getString(pageKey);
         dataset.setName(generateUniqueDataSetName((UIAppFrame) frame, 
            pageName));
         AutoCreateDataset.setRequestorDefaults(frame, uic);
         
         // set the request name
         dataset.getRequestor().setRequestPage(pageName);
         
         // build the pieces
         generateAssemblerComponents((UIAppFrame) frame, ce, dataset, action);

         uic.invalidateLabel(); 
            
         showStatusMessage(statusMsg);
      }
      
      return uic;
   }   

   /**
    * Generates all dataset componenets for the specified assembler action
    * using the supplied content editor.
    * 
    * @param ce The content editor from which the assembler is generated, 
    * assumed not <code>null</code>.  
    * @param frame The frame in which the dataset's figure has been placed, 
    * assumed not <code>null</code>. 
    * @param dataset The dataset that has already been created and for which a 
    * figure has been added to the supplied frame.  Assumed not 
    * <code>null</code> and that a pipe has already been added, and that a
    * selector and mapper have already been added to the pipe.  
    * @param action Assumed to be one of the <code>PASTE_ASSEMBLER_XXX</code>
    * action constants.
    */
   private static void generateAssemblerComponents(UIAppFrame frame, 
      PSItemDefinition ce, OSDataset dataset, int action) throws Exception
   {
      // set up the components
      String dtdRoot = E2Designer.getResources().getString("whitebox");
      PSDtdBuilder dtdBuilder = new PSDtdBuilder(dtdRoot);

      // ensure all fields have complete locators
      PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getContentEditor().getPipe();
      PSContainerLocator pipeLocator = cePipe.getLocator();
      final List<String> sharedFieldIncludes = new ArrayList<String>();
      Iterator shared = cePipe.getMapper().getSharedFieldIncludes(); 
      while (shared.hasNext())
      {
         sharedFieldIncludes.add(((String)shared.next()).toUpperCase());
      }
      pipeLocator.mergeTableSets(
            PSContentEditorDefinition.getSystemDef(),
            PSContentEditorDefinition.getSharedDef(),
            sharedFieldIncludes);
      cePipe.getMapper().getFieldSet().fixupBackEndColumns(
         cePipe.getLocator().getBackEndTables());  
      
      // walk the mapper and fieldset, build map of tables to field list
      PSContentEditorMapper ceMapper = cePipe.getMapper();
      PSFieldSet fieldSet = ceMapper.getFieldSet();
      PSDisplayMapper dispMapper = 
         ceMapper.getUIDefinition().getDisplayMapper();
      
      Iterator dispMappings = dispMapper.iterator();
      final Map<String, List<PSField>> fieldMap =
            new HashMap<String, List<PSField>>();
      String parentTableAlias = null;
      final List<String> bodyFieldNames = new ArrayList<String>();
      
      while ( dispMappings.hasNext())
      {
         PSDisplayMapping dispMapping =
               (PSDisplayMapping) dispMappings.next();
         String fieldRef = dispMapping.getFieldRef();
         // field sets indicate a child which is not supported
         Object o = fieldSet.get(fieldRef);
         if ( o instanceof PSFieldSet )
            continue;

         String fsName = fieldSet.getName();
         if ( null == o )
         {
            o = fieldSet.getChildField( fieldRef,
                  PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD );
            if ( null == o )
               continue;
            PSFieldSet sharedSet = fieldSet.getChildsFieldSet(fieldRef, 
               PSFieldSet.TYPE_MULTI_PROPERTY_SIMPLE_CHILD);
            if (sharedSet == null)
               continue;  
            fsName = sharedSet.getName();
         }
         
         PSField field = (PSField) o;

         // skip binary cols
         if (field.isForceBinary())
            continue;
         else if (field.getDataType().equals(PSField.DT_BINARY))
            continue;
            
         // skip non-backend cols
         IPSBackEndMapping locator = field.getLocator();
         if (!( locator instanceof PSBackEndColumn ))
            continue;

         // skip body fields for snippets, save names for pages
         if (field.getDataType().equals(PSField.DT_TEXT) && 
            OSField.MAX_FORMAT.equals(field.getDataFormat()))
         {
            if (action == PASTE_ASSEMBLER_SNIPPET)
               continue;
            else
               bodyFieldNames.add(field.getSubmitName());
         }
         
         PSBackEndColumn col = (PSBackEndColumn) locator;
         String alias = col.getTable().getAlias().toLowerCase();
         if (parentTableAlias == null && 
            field.getType() == PSField.TYPE_LOCAL)
         {
            // need to have at least one local field
            parentTableAlias = alias;
         }
         
         final List<PSField> fields;
         if (fieldMap.containsKey(fsName))
         {
            fields = fieldMap.get(fsName);
         }
         else
         {
            fields = new ArrayList<PSField>();
            fieldMap.put(fsName, fields);
         }
         fields.add(field);
      }
      
      // build dtd and mapper
      buildXmlMappings(dtdBuilder, dataset.getPipe().getDataMapper(), 
         fieldMap);
      
      // create and set the pagetank
      OSPageDatatank pageTank = new OSPageDatatank();
      pageTank.autoCreatePageDatatank(dtdBuilder, generateUniqueDTDFileName(
         frame, dtdBuilder.getRootName()));
      dataset.setPageDataTank(pageTank);
      
      // build backendtabletank and selector
      addBackendMappings(dataset, parentTableAlias);
      
      // add the extensions
      addExtensions(dataset, action, bodyFieldNames);
   }

   /**
    * Adds required extensions to the suppliled dataset, creates a figure for
    * the extension call set and connects it to the dataset's figure.  
    * 
    * @param dataset The dataset to which the extensions are added, assumed not 
    * <code>null</code>.
    * @param action Assumed to be one of the <code>PASTE_ASSEMBLER_XXX</code>
    * action constants.
    * @param bodyFieldNames A list field namess as <code>String</code> objects
    * that are considered to be body fields mapped in the supplied dataset, 
    * assumed not <code>null</code>, may be empty. 
    */
   private static void addExtensions(OSDataset dataset, int action, 
      List bodyFieldNames)
   {
      OSExitCallSet callSet = new OSExitCallSet();
      
      // find the extension defs we care about
      String iFaceType = IPSResultDocumentProcessor.class.getName();
      IPSExtensionDef addInfoDef = null;
      IPSExtensionDef textToTreeDef = null;
      IPSExtensionDef navAutoSlotDef = null;
      Iterator exitWalker = CatalogServerExits.getCatalog(
         E2Designer.getDesignerConnection(),
         CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME, 
         false).iterator();
      while (exitWalker.hasNext() && (addInfoDef == null || 
         textToTreeDef == null || navAutoSlotDef == null))
      {
         IPSExtensionDef extensionDef = (IPSExtensionDef) exitWalker.next();
         if (extensionDef.implementsInterface(iFaceType))
         {
            String extName = extensionDef.getRef().getExtensionName(); 
            if (extName.equals("sys_casAddAssemblerInfo"))
               addInfoDef = extensionDef;
            else if (extName.equals("sys_xdTextToTree"))
               textToTreeDef = extensionDef;
            else if (extName.equals("rxs_NavAutoSlot"))
               navAutoSlotDef = extensionDef;
         }
      }
      
      // add addAssemblerInfo to all
      if (addInfoDef != null)
         callSet.add(new OSExtensionCall(addInfoDef, null), iFaceType);
      
      // do page specific
      if (action == PASTE_ASSEMBLER_PAGE)
      {
         if (textToTreeDef != null)
         {
            // add for each body field
            Iterator bodyfields = bodyFieldNames.iterator();
            while (bodyfields.hasNext())
            {
               String bodyField = (String) bodyfields.next();
               final List<PSExtensionParamValue> params =
                     new ArrayList<PSExtensionParamValue>();
               Iterator paramNames = textToTreeDef.getRuntimeParameterNames();               
               while (paramNames.hasNext())
               {
                  // don't care about name, do this ordinally as the name could
                  // change
                  paramNames.next();
                  
                  // add body field name as first param, others empty
                  String paramVal = (params.isEmpty()) ? bodyField : "";                  
                  PSExtensionParamValue val = new PSExtensionParamValue(
                     new PSTextLiteral((paramVal)));
                  params.add(val);
               }
               
               PSExtensionParamValue[] paramArray = 
                     params.toArray(new PSExtensionParamValue[params.size()]);
               callSet.add(new OSExtensionCall(textToTreeDef, paramArray), 
                  iFaceType);
            }
         }
         
         // add nav autoslot to every page if it is registered
         if (navAutoSlotDef != null)
            callSet.add(new OSExtensionCall(navAutoSlotDef, null), iFaceType);
      }
      
      callSet.setFigure(dataset.getFigure());
      dataset.getPipe().setResultDataExtensions(callSet);
      dataset.recreateExit(callSet, AppFigureFactory.POST_JAVA_EXIT);
   }

   /**
    * Using the datamapper in the supplied dataset, generates aback-end 
    * datatank, adds all mapped tables and generates joins, adds where clause 
    * conditions to the selector, and adds a pager to sort on the parent 
    * table's CONTENTID column.  
    * 
    * @param dataset The dataset from which the components are generated and
    * to which they are added, assumed not <code>null</code>.
    * @param parentTableAlias The alias of the parent item's backend table, may
    * be <code>null</code> if no local fields were found, in which case not all
    * components will be generated (there must always be a local field, but the
    * object's don't enforce this).
    */
   private static void addBackendMappings(OSDataset dataset,  
      String parentTableAlias)
   {
      OSDataSelector selector = (OSDataSelector) 
         ((OSQueryPipe)dataset.getPipe()).getDataSelector();
      OSBackendDatatank dataTank = new OSBackendDatatank();
      
      PSCollection mastertable = new PSCollection(OSBackendTable.class);
      PSCollection tables = new PSCollection(OSBackendTable.class);
      PSCollection whereClauses = new PSCollection(PSWhereClause.class);
      PSCollection sortedCols = new PSCollection(PSSortedColumn.class);
      
      final Set<String> addedTables = new HashSet<String>();
      Iterator mappings = dataset.getPipe().getDataMapper().iterator();
      while (mappings.hasNext())
      {
         PSDataMapping mapping = (PSDataMapping) mappings.next();
         PSBackEndColumn col = (PSBackEndColumn) mapping.getBackEndMapping();
         String alias = col.getTable().getAlias();
         if (addedTables.contains(alias))
            continue;
         else
            addedTables.add(alias);

         OSBackendTable table = new OSBackendTable(col.getTable());
         if (alias.equalsIgnoreCase(parentTableAlias))
         {
            // add as master table
            mastertable.add(table);
            
            // generate where clauses
            PSBackEndColumn cId = new PSBackEndColumn(table, 
               IPSConstants.ITEM_PKEY_CONTENTID);
            PSBackEndColumn rId = new PSBackEndColumn(table, 
               IPSConstants.ITEM_PKEY_REVISIONID);
            whereClauses.add(new PSWhereClause(cId, 
               PSWhereClause.OPTYPE_EQUALS, new PSSingleHtmlParameter(
                  IPSHtmlParameters.SYS_CONTENTID), false));
            whereClauses.add(new PSWhereClause(rId, 
               PSWhereClause.OPTYPE_EQUALS, new PSSingleHtmlParameter(
                  IPSHtmlParameters.SYS_REVISION), false));
            
            // add sort
            sortedCols.add(new PSSortedColumn(cId, true));
         }
         else
         {
            tables.add(table);
         }
      }
      
      // add tables to the tank, guess the joins
      dataTank.addTables(mastertable, false);
      dataTank.addTables(tables, true);
      dataset.getPipe().setBackEndDataTank(dataTank);

      // set where clauses and paging
      selector.setWhereClauses(whereClauses);
      selector.setSortedColumns(sortedCols);
      OSResultPager ospager = new OSResultPager(new PSResultPager());
      ospager.setSortedColumns(sortedCols);
      dataset.setResultPager(ospager);
   }
   
   
   /**
    * Builds the DTD and data mapper from the content editor fields.
    * 
    * @param dtdBuilder The dtd builder to use, assumed not <code>null</code>.
    * @param mapper The mapper to which mappings are added, assumed not 
    * <code>null</code>.
    * @param fieldMap The content editor fields, key is the fieldset name as a
    * <code>String</code>, value is the <code>PSField</code> object, assumed not 
    * <code>null</code>.
    */
   private static void buildXmlMappings(PSDtdBuilder dtdBuilder, 
      PSDataMapper mapper, Map fieldMap)
   {
      String root = dtdBuilder.getRootName();
      Iterator entries = fieldMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         String fsName = (String) entry.getKey();
         List fieldList = (List) entry.getValue();
         
         boolean addedSharedParent = false;
         Iterator fields = fieldList.iterator();
         while (fields.hasNext())
         {
            
            PSField field = (PSField) fields.next();
            String parent = root;
            String xmlField = root;
            if (field.getType() == PSField.TYPE_SHARED)
            {
               // shared group names must match the fieldset name               
               if (!addedSharedParent)
               {
                  dtdBuilder.addElement(fsName, PSDtdBuilder.OCCURS_ANY, 
                     parent);
                  addedSharedParent = true;
               }
               parent = fsName;
               xmlField = xmlField + "/" + parent;
            }
            
            xmlField = xmlField + "/" + field.getSubmitName(); 
            dtdBuilder.addElement(field.getSubmitName(), 
               PSDtdBuilder.OCCURS_ANY, parent);
            mapper.add(new PSDataMapping(xmlField, field.getLocator()));
         }
      }
   }

   /**
    * Generates a unique dataset within the supplied appframe by appending an
    * index to the end of the page name, starting from the value of 1 and 
    * incrementing it as needed to create a unique name.
    * 
    * @param frame The frame in which the name is to be unique, assumed not 
    * <code>null</code>
    * @param pageName The base page name used to generate the name and to which
    * the index is appended, assumed not <code>null</code> or empty.
    * 
    * @return The unique page name, never <code>null</code> or empty.
    */
   private static String generateUniqueDataSetName(UIAppFrame frame, 
      String pageName)
   {
      int index = 0;
      String uniqueName = null;
      
      while (uniqueName == null)
      {
         index++;
         boolean foundMatch = false;
         Iterator datasets = frame.getDatasets().iterator();         
         while (datasets.hasNext() && !foundMatch)
         {
            OSDataset ds = (OSDataset) datasets.next();
            if (ds.getName().equalsIgnoreCase(pageName + index))
               foundMatch = true;
         }
         
         if (!foundMatch)
            uniqueName = pageName + index;
      }
      
      return uniqueName;
   }
   
   /**
    * Generates a DTD filename within the supplied appframe by appending an
    * index to the end of the file name, starting from the value of 1 and 
    * incrementing it as needed to create a unique name.
    * 
    * @param frame The frame in which the name is to be unique, assumed not 
    * <code>null</code>
    * @param fileName The base file name used to generate the name and to which
    * the index is appended, assumed not <code>null</code> or empty, and to not
    * have an extension added.
    * 
    * @return The unique base file name, never <code>null</code> or empty.
    */
   private static String generateUniqueDTDFileName(UIAppFrame frame, 
      String fileName)
   {
      int index = 0;
      String uniqueName = null;
      
      while (uniqueName == null)
      {
         index++;
         boolean foundMatch = false;
         Iterator datasets = frame.getDatasets().iterator();         
         while (datasets.hasNext() && !foundMatch)
         {
            OSDataset ds = (OSDataset) datasets.next();
            PSPageDataTank pageTank = ds.getPageDataTank();
            if (pageTank == null)
               continue;
            URL source = pageTank.getSchemaSource();
            if (source == null)
               continue;
            if (Util.stripPath(source.getFile(), "").equalsIgnoreCase(fileName + 
               index))
            {
               foundMatch = true;
            }
         }
         
         if (!foundMatch)
            uniqueName = fileName + index;
      }
      
      return uniqueName;
   }   

   /**
    * Just sends the supplied message to the mainframe's status bar.
    * See {@link UIMainFrame#setStatusMessage(String) here} for more details.
    */
   private static void showStatusMessage( String msg )
   {
      E2Designer.getApp().getMainFrame().setStatusMessage(msg);
   }
   
   /**
    * Displays a parameterized error dialog to the user. If you don't have
    * params, use {@link #showError(String,String) this} one instead.
    *
    * @param titleKey The resource key name used to lookup the title text in
    *    the main resource bundle. Assumed not <code>null</code>.
    *
    * @param msgKey The resource key name used to lookup the message body text
    *    in the main resource bundle. Assumed not <code>null</code>.
    *
    * @param params These params and the body text is passed to the
    *    MessageFormat class to create the final text.
    */
   private static void showError( String titleKey, String msgKey,
         Object[] params)
   {
      PSDlgUtil.showErrorDialog(
            MessageFormat.format(
               E2Designer.getResources().getString( msgKey ), params ),
            E2Designer.getResources().getString( titleKey ));
   }


   /**
    * Displays a parameterized error dialog to the user. If you have
    * params, use {@link #showError(String,String,String[]) this} one instead.
    *
    * @param titleKey The resource key name used to lookup the title text in
    *    the main resource bundle. Assumed not <code>null</code>.
    *
    * @param msg The actual text of the body.
    */
   private static void showError( String titleKey, String msg )
   {
      PSDlgUtil.showErrorDialog(msg,
            E2Designer.getResources().getString(titleKey));
   }
   
   /**
    * Initializes SWT resources used by this class.
    * Must be called from SWT thread before any methods of this class are called.
    */
   public static void initSwtResources()
   {
      m_clipboard = new Clipboard(getDisplay());
   }

   /**
    * Extracts content type from SWT clipboard.
    * Changes to content type returned by this method won't affect content
    * type cached by model. 
    * @return the content type stored in the clipboard or <code>null</code>
    * if content type can't be extracted.
    */
   private static PSItemDefinition getContentTypeFromClipboard() throws Exception
   {
      final IPSReference[] result = new IPSReference[1];
      getDisplay().syncExec(new Runnable()
      {
         @SuppressWarnings("unchecked")
         public void run()
         {
            result[0] = null;
            final Object tmp = m_clipboard.getContents(ms_contentTypeTransfer);
            if (tmp == null)
            {
               return;
            }
            final Collection<PSUiReference> nodes =
                  (Collection<PSUiReference>) tmp;
            for (final PSUiReference node : nodes)
            {
               final IPSReference ref = node.getReference();
               if (ref != null && ref.getObjectType().getPrimaryType().equals(
                     PSObjectTypes.CONTENT_TYPE))
               {
                  result[0] = ref;
                  return;
               }
            }
         }
      });

      final IPSReference ref = result[0];
      if (ref != null)
      {
         final PSModelTracker tracker = PSModelTracker.getInstance();
         final PSItemDefinition clone;
         {
            final PSItemDefinition itemDef =
               (PSItemDefinition) tracker.getInstance().load(ref, false);
            if (itemDef == null)
            {
               return null;
            }
            clone = (PSItemDefinition) itemDef.clone();
         }
         PSContentEditorDefinition.mergeFields(clone);
         return clone;
      }
      else
      {
         return null;
      }
   }

   /**
    * Current workbench display.
    * @return current workbench display. Never <code>null</code>.
    * Fails if the workbench has not been created yet.
    */
   private static Display getDisplay()
   {
      return PlatformUI.getWorkbench().getDisplay();
   }
   
   /**
    * Private so no instances can be instantiated. All methods are static.
    */
   private UIContentEditorHandler()
   {}
   
   /**
    * Constant to indicate action is to generate a page assembler from a content
    * editor's XML
    */
   public static final int PASTE_ASSEMBLER_PAGE = 0;

   /**
    * Constant to indicate action is to generate a snippet assembler from a 
    * content editor's XML
    */
   public static final int PASTE_ASSEMBLER_SNIPPET = 1;
   
   /**
    * Eclipse clipboard. Initialized in {@link #initSwtResources()}.
    */
   private static Clipboard m_clipboard;
   
   private static Transfer ms_contentTypeTransfer = 
      PSTransferFactory.getInstance().getTransfer(
            new PSObjectType(PSObjectTypes.CONTENT_TYPE), null);
}
