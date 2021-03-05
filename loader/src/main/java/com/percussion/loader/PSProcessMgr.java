/*[ PSProcessMgr.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.error.PSException;
import com.percussion.loader.objectstore.PSContentSelectorDef;
import com.percussion.loader.objectstore.PSEmailDef;
import com.percussion.loader.objectstore.PSErrorHandlingDef;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSFieldTransformationsDef;
import com.percussion.loader.objectstore.PSItemTransformationsDef;
import com.percussion.loader.objectstore.PSLoaderDef;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.objectstore.PSLogDef;
import com.percussion.loader.objectstore.PSParamDef;
import com.percussion.loader.objectstore.PSTransformationDef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class manages one or more processes. The derived classes may override
 * the <code>run()</code> method for its own operation.
 */
public abstract class PSProcessMgr extends Thread
{
   /**
    * Constructs a <code>PSProcessMgr</code> object from a loader descriptor
    *
    * @param ld The loader descriptor, may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>ld</code> is <code>null</code>.
    * @throws PSLoaderException if any error occurs.
    */
   public PSProcessMgr(PSLoaderDescriptor ld) throws PSLoaderException
   {
      if (ld == null)
         throw new IllegalStateException("ld may not be null");

      m_loaderDesc = ld;
      initPlugins();
   }

   /**
    * Initialize all plugins based on the loader descriptor.
    *
    * @throws PSLoaderException if any error occurs
    */
   private void initPlugins() throws PSLoaderException
   {
      // get the selector
      PSContentSelectorDef selectorDef;
      selectorDef = m_loaderDesc.getContentSelectorDef();
      PSPluginFactory factory = PSPluginFactory.getInstance();
      m_selector = factory.newContentSelector(selectorDef);

      // get the loader
      PSLoaderDef loaderDef;
      loaderDef = m_loaderDesc.getLoaderDef();
      m_loader = factory.newContentLoader(loaderDef);

      // get the static extractors
      m_staticExtractors.clear();
      Iterator extractorDefs = m_loaderDesc.getStaticExtractorDefs();
      while (extractorDefs.hasNext())
      {
         PSExtractorDef extractorDef = (PSExtractorDef)extractorDefs.next();
         IPSItemExtractor extractor = factory.newItemExtractor(extractorDef);
         m_staticExtractors.put(extractorDef.getName(), extractor);
      }

      // get the item extractors
      m_itemExtractors.clear();
      extractorDefs = m_loaderDesc.getItemExtractorDefs();
      while (extractorDefs.hasNext())
      {
         PSExtractorDef extractorDef = (PSExtractorDef)extractorDefs.next();
         IPSItemExtractor extractor = factory.newItemExtractor(extractorDef);
         m_itemExtractors.put(extractorDef.getName(), extractor);
      }

      // get the transformers
      initTransformers();

   }

   /**
    * Initializes the maps between the name of the transformers to both its
    * parameters and its plugin objects for all transformations in the
    * descriptor.
    */
   private void initTransformers() throws PSLoaderException
   {
      m_transParamsMap = new HashMap();
      m_transMap = new HashMap();

      Iterator trans;

      // init the transformations within the extractors
      Iterator extractorDefs = m_loaderDesc.getItemExtractorDefs();
      while (extractorDefs.hasNext())
      {
         PSExtractorDef extractDef = (PSExtractorDef) extractorDefs.next();
         trans = extractDef.getFieldTransformations();
         mappingTransformationAndParams(trans, false);

         trans = extractDef.getItemTransformations();
         mappingTransformationAndParams(trans, true);
      }

      // init the global transformations
      PSFieldTransformationsDef fieldDefs = m_loaderDesc.getFieldTransDef();
      if (fieldDefs != null)
      {
         trans = fieldDefs.getTransformations();
         mappingTransformationAndParams(trans, false);
      }

      PSItemTransformationsDef itemDefs = m_loaderDesc.getItemTransDef();
      if (itemDefs != null)
      {
         trans = itemDefs.getTransformations();
         mappingTransformationAndParams(trans, true);
      }
   }

   /**
    * Mapping the name of the transformation to both its parameters and its
    * plugin object for a given list of transformation definitions.
    *
    * @param trans The to be mapped list of transformation definitions,
    *    assume not <code>null</code>.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private void mappingTransformationAndParams(Iterator trans,
      boolean isItemTransformer) throws PSLoaderException
   {
      PSPluginFactory factory = PSPluginFactory.getInstance();

      while (trans.hasNext())
      {
         PSTransformationDef def = (PSTransformationDef) trans.next();

         Object[] params = getParamsFromTransDef(def);

         // make sure the names are unique
         if (m_transParamsMap.get(def.getName()) != null)
         {
            throw new PSLoaderException(
               IPSLoaderErrors.NON_UNIQUE_TRANSFORMER_NAME, def.getName());
         }

         // map the parameters to its name
         m_transParamsMap.put(def.getName(), params);

         // map the plugins to its name
         if (isItemTransformer)
         {
            IPSItemTransformer itemTrans = factory.newItemTransformer(def);
            m_transMap.put(def.getName(), itemTrans);
         }
         else
         {
            IPSFieldTransformer fieldTrans = factory.newFieldTransformer(def);
            m_transMap.put(def.getName(), fieldTrans);
         }
      }
   }

   /**
    * Gets the log
    *
    * @return, log, never <code>null</code>, may contain two newlines only.
    */
   protected String getLog()
   {
      PSLogDef logDef = m_loaderDesc.getLogDef();
      String logFilePath = logDef.getFileParamValue(PSLogDef.FILE_APPENDER,
            PSLogDef.FILE);
      StringBuffer logs = new StringBuffer();
      File file = new File(logFilePath);
      BufferedReader bufferReader = null;
      try
      {
         bufferReader = new BufferedReader(new FileReader(file));
         String str = null;
         while ((str = bufferReader.readLine()) != null)
         {
            logs.append(str);
         }
      }
      catch(IOException e)
      {

      }
      finally
      {
         if (bufferReader != null)
         {
            try
            {
               bufferReader.close();
            }
            catch(IOException e)
            {
               //should never happen
            }
         }
      }
      return logs.toString();
   }


   /**
    * Get the parameters from a given transformation definition.
    *
    * @param transDef The transformation definition, assume not
    *    <code>null</code>
    *
    * @return The parameter objects, never <code>null</code>, but may be empty.
    *
    * @throws PSLoaderException if an error occurs.
    */
   private Object[] getParamsFromTransDef(PSTransformationDef transDef)
      throws PSLoaderException
   {
      List paramList = transDef.getParameterList();
      if (paramList.size() > 0)
      {
         Iterator params = paramList.iterator();
         Object[] objs = new Object[paramList.size()];

         try
         {
            for (int i=0; i < paramList.size(); i++)
            {
               PSParamDef def = (PSParamDef) paramList.get(i);
               String value = def.getValue();
               if (value == null || value.trim().length() == 0)
               {
                  objs[i] = null;
               }
               else
               {
                  Class paramClass = Class.forName(def.getType());
                  Class[] classes = new Class[] {String.class};
                  Constructor paramCtr = paramClass.getConstructor(classes);
                  objs[i] = paramCtr.newInstance(new Object[] {def.getValue()});
               }
            }
         }
         catch (Exception e)
         {
            throw new PSLoaderException(
               IPSLoaderErrors.UNEXPECTED_ERROR, e.toString());
         }
         return objs;
      }
      else
      {
         return new Object[0];
      }
   }

   /**
    * Aborting current process
    */
   public void abort()
   {
      if (isAlive() && m_status == STATUS_STARTED)
      {
         m_status = STATUS_ABORTING;
      }
   }

   /**
    * Determines whether the manager is in the aborting process or its
    * <code>abort()</code> has been called.
    *
    * @return <code>true</code> if the manager is in the aborting process;
    *    <code>false</code> otherwise.
    */
   protected boolean isAborting()
   {
      return (m_status == STATUS_ABORTING);
   }

   /**
    * Get current status.
    *
    * @return The current status, will be one of the <code>STATUS_XXX</code>
    *    values.
    */
   public int getStatus()
   {
      return m_status;
   }

   /**
    * Adds a status listener to receive the status events. It is caller's
    * responsibility to remove the listeners by
    * {@link #removeStatusListener(IPSStatusListener)} or
    * {@link #clearStatusListener(IPSStatusListener)}
    *
    * @param listener The status listener, may not <code>null</code>
    */
   public void addStatusListener(IPSStatusListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not null");

      m_statusListeners.add(listener);
   }

   /**
    * Removes a status listener from the registered listener list.
    *
    * @param listener The status listener, may not <code>null</code>
    */
   public void removeStatusListener(IPSStatusListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not null");

      m_statusListeners.remove(listener);
   }

   /**
    * Removes all registered status listeners
    */
   public void clearStatusListeners()
   {
      m_statusListeners.clear();
   }

   /**
    * Adds a progress listener to receive the progress events. It is caller's
    * responsibility to remove the listeners by
    * {@link #removeProgressListener(IPSProgressListener)} or
    * {@link #clearProgressListener(IPSProgressListener)}
    *
    * @param listener The progress listener, may not <code>null</code>
    */
   public void addProgressListener(IPSProgressListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not null");

      m_progressListeners.add(listener);
   }

   /**
    * Removes a progress listener from the registered listener list.
    *
    * @param listener The progress listener, may not <code>null</code>
    */
   public void removeProgressListener(IPSProgressListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not null");

      m_progressListeners.remove(listener);
   }

   /**
    * Removes all registered progress listeners
    */
   public void clearProgressListeners()
   {
      m_progressListeners.clear();
   }

   /**
    * Get the exception that was caught by the <code>run()</code> method
    *
    * @return The exception described above, it may be <code>null</code> if
    *    there was no error and didn't throw any exception during the
    *    <code>run()</code> operation, or the operation was aborted by user.
    */
   public PSLoaderException getRunException()
   {
      return m_runException;
   }

   /**
    * Set the (unexpected) exception that was caught during executing 
    * <code>run()</code> method.
    *
    * @param e The unexpected exception, may not be <code>null</code>.
    */
   protected void setRunException(Exception ex)
   {
      if (ex == null)
         throw new IllegalArgumentException("ex may not null");

      if (ex instanceof PSLoaderException)
      {
         m_runException = (PSLoaderException) ex;
      }
      else if (ex instanceof PSException)
      {
         m_runException = new PSLoaderException((PSException)ex);
      }
      else
      {
         m_runException = new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
               ex.toString());
      }
      
      setErrorOccured();
      
      boolean stopOnError = true;
      try {
         stopOnError = m_loaderDesc.getErrorHandlingDef().getStopOnError();
      }
      catch (PSLoaderException ignore){} // not possible
      
      if (stopOnError)
         abort();
   }

   /**
    * Notify the operation of the manager has been started. This
    * should be called at the beginning of the <code>run()</code> method.
    */
   protected void operationStarted()
   {
      m_status = STATUS_STARTED;
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_STARTED,
         PSStatusEvent.PROCESS_MANAGER));
   }

   /**
    * Notify the operation of the manager has been aborted. This should be
    * called at the end of the <code>run()</code> method when the operation is
    * aborted.
    */
   protected void operationAborted()
   {
      m_status = STATUS_ABORTED;
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_ABORTED,
         PSStatusEvent.PROCESS_MANAGER));
   }

   /**
    * Determines whether the manager is aborted.
    *
    * @return <code>true</code> if the manager is aborted; <code>false</code>
    *    otherwise.
    */
   public boolean isAborted()
   {
      return (m_status == STATUS_ABORTED);
   }

   /**
    * Recording an error has occurred.
    */
   public void setErrorOccured()
   {
      m_hasErrorOccured = true;
   }

   /**
    * Determines whether an error has occurred.
    *
    * @return <code>true</code> if an error has occurred; otherwise return
    *    <code>false</code>.
    */
   public boolean hasErrorOccured()
   {
      return m_hasErrorOccured;
   }

   /**
    * Notify the operation of the manager is successfully completed.
    * This should be called at the end of the <code>run()</code> method
    */
   protected void operationCompleted()
   {
      m_status = STATUS_COMPLETED;
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_COMPLETED,
         PSStatusEvent.PROCESS_MANAGER));
   }


   /**
    * Notify a process has been started.
    *
    * @param processId The process id that has been started. It must be one
    *    of the <code>PSStatusEvent.PROCESS_XXX</code> values.
    */
   protected void processStarted(int processId)
   {
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_STARTED,
         processId));
   }

   /**
    * Notify a process is aborted.
    *
    * @param processId The process id that has been aborted. It must be one
    *    of the <code>PSStatusEvent.PROCESS_XXX</code> values.
    */
   protected void processAborted(int processId)
   {
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_ABORTED,
         processId));
   }

   /**
    * Notify a process is completed.
    *
    * @param processId The process id that has been started. It must be one
    *    of the <code>PSStatusEvent.PROCESS_XXX</code> values.
    */
   protected void processCompleted(int processId)
   {
      fireStatusEvent(new PSStatusEvent(this, PSStatusEvent.STATUS_COMPLETED,
         processId));
   }

   /**
    * Fires a status event to the registers listeners.
    *
    * @param e The status event, may not <code>null</code>
    */
   protected void fireStatusEvent(PSStatusEvent e)
   {
      Iterator listeners = m_statusListeners.iterator();
      while (listeners.hasNext())
      {
         Object obj = listeners.next();
         if (obj instanceof IPSStatusListener)
         {
            IPSStatusListener listener = (IPSStatusListener) obj;
            listener.statusChanged(e);
         }
      }
   }

   /**
    * Fires a progess event with percentage info to the registers listeners.
    *
    * @param e The progress event, may not <code>null</code>
    */
   protected void fireProgressEvent(int percent)
   {
      if (m_prevEventPercent == percent)
         return; // have send the same event already

      m_prevEventPercent = percent;

      Iterator listeners = m_progressListeners.iterator();
      PSProgressEvent e = new PSProgressEvent(this, percent);

      while (listeners.hasNext())
      {
         Object obj = listeners.next();
         if (obj instanceof IPSProgressListener)
         {
            IPSProgressListener listener = (IPSProgressListener) obj;
            listener.progressChanged(e);
         }
      }
   }

   /**
    * Get a list from a tree node from the specified content tree.
    *
    * @param tree The that contains a list of node, may not be <code>null</code>.
    *
    * @return The list nodes, <code>IPSContentTreeNode</code> objects, of the
    *    tree, never <code>null</code>, but may be empty.
    */
   protected List getListFromTree(IPSContentTree tree)
   {
      if (tree == null)
         throw new IllegalArgumentException("tree has not been set");

      List nodeList = new ArrayList();
      nodeList = tree.getNodes();
      //printNodeList(tree, nodeList);  // used for debugging

      return nodeList;
   }

   /**
    * Debugging method to print the tree list.
    *
    * @param tree The to be printed tree, assume not <code>null</code>.
    * @param treeList The list to be printed, a list over zero or more
    *    <code>IPSContentTreeNode</code> object, assume not <code>null</code>.
    */
   private void printNodeList(IPSContentTree tree, List nodeList)
   {
      // print the roots
      Iterator roots = tree.getRoots();
      while (roots.hasNext())
      {
         IPSContentTreeNode root = (IPSContentTreeNode) roots.next();
         PSItemContext item = root.getItemContext();
         System.out.println("printNodeList [ROOT]: " + item.getResourceId());
      }

      // print the generated node list
      Iterator nodes = nodeList.iterator();
      int i=0;
      while (nodes.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode)nodes.next();
         PSItemContext item = node.getItemContext();

         if (node.isBranchTerminated())
            System.out.println("ERROR node [" + i + "] " +
               node.getItemContext().getResourceId());
         else
            System.out.println("printNodeList[" + i  + "] " +
               node.getItemContext().getResourceId());

         // print the parent nodes
         Iterator parents = node.getParents();
         while (parents.hasNext())
         {
            IPSContentTreeNode pnode = (IPSContentTreeNode)parents.next();
            item = pnode.getItemContext();
            System.out.println("                      PARENT = " +
               item.getResourceId());
         }
         // print the children nodes
         Iterator children = node.getChildren();
         while (children.hasNext())
         {
            IPSContentTreeNode cnode = (IPSContentTreeNode)children.next();
            item = cnode.getItemContext();
            System.out.println("                       CHILD = " +
               item.getResourceId());
         }
         i++;
      }
   }

   /**
    * Get an extractor among a list of extractors, so that the given item
    * contains content which can be extracted by the extractor.
    *
    * @param extractorList The list of extractors, assume not <code>null</code>,
    *    but may be empty.
    * @param item The item that will be interrogated by the list of extractors.
    *
    * @return An extractor object if find one that is interested the
    *    <code>item</code>; <code>null</code> if cannot find an extractor that
    *    is interested in the <code>item</code>.
    */
   protected IPSItemExtractor getExtractorForItem(Iterator extractors,
      PSItemContext item)
   {
      IPSItemExtractor extractor = null;
      int count = 0;

      while (extractors.hasNext() && count == 0)
      {
         extractor = (IPSItemExtractor)extractors.next();
         count = extractor.containsInstances(item);
      }

      return (count > 0) ? extractor : null;
   }

   /**
    * Sends a email to inform a successful operation is done.
    *
    * @param logs, log data, may be empty or <code>null</code>
    */
   protected void sendSuccessfulEmail(String logs)
   {
      String subject = "Content Loader run successful...";
      if (logs == null || logs.length() == 0)
         logs = "";
      sendEmail(subject, getSuccessfulEmailMessage() + logs);
   }

   /**
    * Sends a email to inform a failure operation.
    *
    * @param logs, log data, may be empty or <code>null</code>
    */
   protected void sendFailedEmail(String logs)
   {
      String subject = "Content Loader run failed...";
      if (logs == null || logs.length() == 0)
         logs = "";
      sendEmail(subject, getFailedEmailMessage() + logs);
   }

   /**
    * Sends a email to inform completed operation with error.
    *
    * @param logs, log data, may be empty or <code>null</code>
    */
   protected void sendErrorEmail(String logs)
   {
      String subject = "Content Loader run finished error...";
      if (logs == null || logs.length() == 0)
         logs = "";
      sendEmail(subject, getErrorEmailMessage() + logs);
   }

   /**
    * Creates a email message for a successful operation.
    *
    * @return The created email message, never <code>null</code> or empty.
    */
   private String getSuccessfulEmailMessage()
   {
      return "The Content Loader successfully run for Loader Descriptor " +
         m_loaderDesc.getName() +
         ". Please consult the log for further information";
   }

   /**
    * Creates a email message for a failure operation.
    *
    * @return The created email message, never <code>null</code> or empty.
    */
   private String getFailedEmailMessage()
   {
      return "The Content Loader failed run for Loader Descriptor " +
         m_loaderDesc.getName() +
         ". Please consult the log for further information";
   }

   /**
    * Creates a email message for an operation with error.
    *
    * @return The created email message, never <code>null</code> or empty.
    */
   private String getErrorEmailMessage()
   {
      return "The Content Loader completed with error for Loader Descriptor " +
         m_loaderDesc.getName() +
         ". Please consult the log for further information";
   }

   /**
    * Sends a email with the given subject and message.
    *
    * @param subject The subject of the email, may not <code>null</code> or
    *    empty.
    * @param message The message of the email, may not <code>null</code> or
    *    empty.
    */
   private void sendEmail(String subject, String message)
   {
      PSErrorHandlingDef handlingDef = m_loaderDesc.getErrorHandlingDef();

      try
      {
         if ((! handlingDef.getEmailOnError()) &&
             (! handlingDef.getEmailOnSuccess()))
         {
            return; // do nothing if did not specify to send email.
         }
      }
      catch (PSLoaderException e)
      {
         e.printStackTrace(); // should never be here, ignore if it does for now
         return;
      }

      PSEmailDef mailDef = handlingDef.getEmail();

      // get recipients
      List recipients = mailDef.getRecipients();
      String[] toAddrs = new String[recipients.size()];
      for (int i=0; i < toAddrs.length; i++)
         toAddrs[i] = (String) recipients.get(i);

      // get CC recipients
      List ccRecipients = mailDef.getCCRecipients();
      String[] ccAddrs = new String[ccRecipients.size()];
      for (int i=0; i < ccAddrs.length; i++)
         ccAddrs[i] = (String) ccRecipients.get(i);

      PSMail.send(mailDef.getSmtpHost(), subject, message, toAddrs, ccAddrs,
         mailDef.getFromAddr());
   }

   /**
    * A broken link constant, used to replace the inline link that is point to
    * an excluded node.
    */
   public final static String BROKEN_LINK =
      "&lt;a href=&quot;BROKEN LINK&quot;&gt;&lt;/a&gt;";

   /**
    * Indicating the manager has started its operation
    */
   public final static int STATUS_STARTED = 1;

   /**
    * Indicating the operation has been aborted, by user or on error.
    */
   public final static int STATUS_ABORTED = 2;

   /**
    * Indicating the manager has successfully completed its operation.
    */
   public final static int STATUS_COMPLETED = 3;

   /**
    * Indicating the manager has not started its operation yet.
    */
   public final static int STATUS_NONE = 4;

   /**
    * Indicating the manager is in aborting process.
    */
   public final static int STATUS_ABORTING = 5;

   /**
    * The status of the event, it will always be one of the
    * <code>STATUS_XXX</code> values.
    */
   private int m_status = STATUS_NONE;

   /**
    * A list of <code>IPSStatusListener</code> objects, never <code>null</code>,
    * but may be empty.
    */
   private List m_statusListeners = new ArrayList();

   /**
    * A list of <code>IPSProgressListener</code> objects, never
    * <code>null</code>, but may be empty.
    */
   protected List m_progressListeners = new ArrayList();

   /**
    * The loader descriptor, initialized by constructors, never
    * <code>null</code> after that.
    */
   protected PSLoaderDescriptor m_loaderDesc;

   /**
    * The content selector, initialized by constructor, never <code>null</code>
    * after that.
    */
   protected IPSContentSelector m_selector = null;

   /**
    * Map the name of the static extractors to its extractor object. The key
    * of the map is the name (as <code>String</code>), the value of the map is
    * the extractor's object (as <code>IPSItemExtractor</code>).
    * initialized by constructor, never <code>null</code>, but may be empty
    * after that.
    */
   protected Map m_staticExtractors = new HashMap();

   /**
    * Map the name of the item extractors to its extractor object. The key
    * of the map is the name (as <code>String</code>), the value of the map is
    * the extractor's object (as <code>IPSItemExtractor</code>).
    * initialized by constructor, never <code>null</code>, but may be empty
    * after that.
    */
   protected Map m_itemExtractors = new HashMap();

   /**
    * The content loader object, initialized by constructor, never
    * <code>null</code> after that.
    */
   protected IPSContentLoader m_loader = null;

   /**
    * The percent info of the progress event that has send previously.
    * Used by {@link #fireProgressEvent(int)} to not send the same event
    * more than once.
    */
   private int m_prevEventPercent = -1;

   /**
    * The unexpected exception caught in the <code>run()</code> method.
    * Initialized to <code>null</code>, only modified by
    * <code>setRunException</code>.
    */
   private PSLoaderException m_runException = null;

   /**
    * The Map that maps the name of the transformation and its array of
    * parameters. The map key is <code>String</code> and map value is
    * <code>Object[]</code>. Initialized by the constructor, never
    * <code>null</code>, may be empty after that.
    */
   protected Map m_transParamsMap;

   /**
    * The mapping between the name of the item transformation (as map key
    * in <code>String</code>) and the item transformation plugin (as
    * map value in either <code>IPSItemTransformer</code> or
    * <code>IPSFieldTransformer</code>). Initialized by the constructor,
    * never <code>null</code>, may be empty after that.
    */
   protected Map m_transMap;


   /**
    * <code>true</code> if error occurred during one of the operations. Only
    * modified by <code>setErrorOccured()</code>. Default to <code>false</code>.
    */
   private boolean m_hasErrorOccured = false;
}
