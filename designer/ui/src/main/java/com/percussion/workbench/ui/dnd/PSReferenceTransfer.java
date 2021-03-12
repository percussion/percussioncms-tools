/******************************************************************************
 *
 * [ PSReferenceTransfer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.dnd;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import com.percussion.workbench.ui.util.PSUiUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

/**
 * This class converts a {@link com.percussion.client.IPSReference} to
 * and from a format that allows transfer via drag and drop. Instances of this
 * class can only be obtained using the {@link PSTransferFactory}.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSReferenceTransfer extends ByteArrayTransfer
{
   //see base class
   @Override
   protected int[] getTypeIds()
   {
      return new int[] {m_registeredId};
   }

   //see base class
   @Override
   protected String[] getTypeNames()
   {
      return new String[] {m_formatName};
   }

   /**
    * Returns the primary type that was used to create this instance Transfer.
    *
    * @return Never <code>null</code>.
    */
   public IPSPrimaryObjectType getPrimaryType()
   {
      if (m_objectType != null)
         return (IPSPrimaryObjectType) m_objectType.getPrimaryType();
      return m_primaryType;
   }
   
   /**
    * Converts the supplied data to fully qualified paths and saves them in a
    * serialized stream that is then converted to a format that conforms to the
    * <code>transferData</code> type.
    * 
    * @param object Expected to be a <code>Collection</code> of
    * <code>PSUiReference</code>s.
    * 
    * @param transferData The OS specific data type. Never <code>null</code>.
    * 
    * @throws ClassCastException if <code>object</code> not the expected type.
    */
   @Override
   protected void javaToNative(Object object, TransferData transferData)
   {
      if (null == transferData)
      {
         throw new IllegalArgumentException("transferData cannot be null");  
      }
      try
      {
         @SuppressWarnings("unchecked")
         Collection<PSUiReference> data = (Collection<PSUiReference>) object;
         String[] paths = new String[data.size()];
         int i = 0;
         for (PSUiReference node : data)
         {
            paths[i++] = PSDesignObjectHierarchy.getInstance()
                  .getFullyQualifiedPath(node);
         }
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream os = new ObjectOutputStream(bos);
         os.writeObject(paths);
         super.javaToNative(bos.toByteArray(), transferData);
      }
      catch (IOException e)
      {
         // should never happen because we're using byte array streams
         throw new RuntimeException(e);
      }

   }
   
   /**
    * @inheritDoc
    * 
    * @return Never <code>null</code>. Each entry may be <code>null</code> if
    * a node that matched the path was not found.
    */
   @Override
   protected Object nativeToJava(TransferData transferData)
   {
      try
      {
         final byte[] bytes = (byte[]) super.nativeToJava(transferData);
         final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
         final ObjectInputStream data = new ObjectInputStream(in);
         final Object results = data.readObject();
         final String[] paths = (String[]) results;
         final List<PSUiReference> nodes = PSDesignObjectHierarchy
               .getInstance().getNodes(paths);
         final StringBuffer failedPaths = new StringBuffer();
         for (int i = nodes.size()-1; i >= 0; i--)
         {
            if (nodes.get(i) == null)
            {
               nodes.remove(i);
               if (failedPaths.length() > 0)
                  failedPaths.append(", ");
               failedPaths.append(paths[i]);
            }
         }
         if (failedPaths.length() > 0)
         {
            Display.getDefault().syncExec(new Runnable()
            {
               @SuppressWarnings("synthetic-access")
               public void run()
               {
                  String title = PSMessages
                        .getString("PSReferenceTransfer.noNodeForPath.title");
                  String msg = PSMessages.getString(
                        "PSReferenceTransfer.noNodeForPath.message",
                        new Object[] { failedPaths.toString() });
                  MessageDialog.openWarning(PSUiUtils.getShell(), title, msg);
               }
            });            
         }
         return nodes;
      }
      catch (IOException e)
      {
         ms_logger.info("Should not happen on byte array.", e);
      }
      catch (ClassNotFoundException e)
      {
         ms_logger.info("Should not happen for String objects.", e);
      }
      catch (PSModelException e)
      {
         //the node has already been expanded in order to drag it
         ms_logger.info("Should not happen in this context.", e);
      }
      return null;
   }

   /**
    * Returns the name used to register this transfer type.
    */
   @Override
   public String toString()
   {
      return m_formatName;
   }

   /**
    * To be valid, the param must be a {@link Collection} of
    * {@link PSUiReference} and all the refs must have a primaty (and possibly
    * secondary) type that matches the type supplied in the ctor.
    * 
    * @param object The data to check.
    */
   @Override
   protected boolean validate(Object object)
   {
      if (object == null)
         return false;
      
      if (object instanceof Collection)
      {
         for (Object o : (Collection) object)
         {
            if ((o instanceof PSUiReference))
            {
               PSObjectType oType = ((PSUiReference) o).getObjectType();
               if (m_objectType != null && !m_objectType.equals(oType))
                  return false;
               else if (m_primaryType != null
                     && !oType.getPrimaryType().equals(m_primaryType))
               {
                  return false;
               }
            }
            else
               return false;
         }
         return true;
      }
      return false;
   }

   /**
    * Package access so only factory can access it. Use
    * {@link PSTransferFactory} to get instances.
    * 
    * @param type The design object type for this particular instance of the
    * transfer.
    * 
    * @param suffix An optional text string that is appended onto the name that
    * is used to register this transfer type.
    * 
    * @param useSecondary If <code>true</code>, the secondary type (if present)
    * is used as part of the unique identifier for the this transfer. This 
    * allows the creation of different transfers for the same primary type.
    */
   PSReferenceTransfer(PSObjectType type, String suffix, 
         boolean useSecondary)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type cannot be null");
      }
      
      if (StringUtils.isBlank(suffix))
         suffix = "";
      
      String formatName = type.getPrimaryType().name();
      if (useSecondary && type.getSecondaryType() != null)
         formatName += ":" + type.getSecondaryType().name();
      
      formatName += "-transfer-format";
      if (!StringUtils.isBlank(suffix)) 
         formatName += "-" + suffix;
      m_formatName = formatName;
      m_registeredId = registerType(m_formatName);
      if (ms_logger.isDebugEnabled())
      {
         String pattern = "Created Transfer: Name={0}, Id={1}";
         ms_logger.debug(MessageFormat.format(pattern, m_formatName, String
               .valueOf(m_registeredId)));
      }
      if (useSecondary)
      {
         m_objectType = type;
         m_primaryType = null;
      }
      else
      {
         m_objectType = null;
         m_primaryType = (IPSPrimaryObjectType) type.getPrimaryType();
      }
   }

   /**
    * Either this or {@link #m_primaryType} will be non-<code>null</code>. The
    * valid one is used for validation.
    */
   private final PSObjectType m_objectType;
   
   /**
    * See {@link #m_objectType}.
    */
   private final IPSPrimaryObjectType m_primaryType;
   
   /**
    * The text representation of this data format.
    */
   private final String m_formatName;
   
   /**
    * The numeric identifier for this format.
    */
   private final int m_registeredId;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager.getLogger(PSReferenceTransfer.class);
}
