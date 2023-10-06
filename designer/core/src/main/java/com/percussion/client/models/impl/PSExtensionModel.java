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
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.client.models.impl;

import com.percussion.client.IPSPrimaryObjectType;
import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.models.IPSExtensionModel;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionDef;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Only needed to provide the
 * {@link com.percussion.client.models.IPSExtensionModel} interface.
 * 
 * @author paulhoward
 */
public class PSExtensionModel extends PSCmsModel implements IPSExtensionModel
{
   /**
    * Ctor required by framework. See
    * {@link com.percussion.client.models.IPSCmsModel} for details.
    */
   public PSExtensionModel(String name, String description,
      IPSPrimaryObjectType supportedType)
   {
      super(name, description, supportedType);
   }

   /* (non-Javadoc)
    * @see PSCmsModel#objectsSaved(Object[], boolean)
    */
   @Override
   protected void objectsSaved(Object[] refs, boolean isAcl)
   {
      // flush cache for all saved extensions
      if (!isAcl)
      {
         for (Object obj : refs)
         {
            if (obj != null && !(obj instanceof Throwable))
            {
               IPSReference ref = (IPSReference) obj;
               flush(ref);
            }
         }
      }

      super.objectsSaved(refs, isAcl);
   }

   // see base
    public String getAssemblerSourceExt(final String assembler)
          throws PSModelException
   {
      if (StringUtils.isBlank(assembler))
      {
         throw new IllegalArgumentException("Assembler should be specified");
      }
      final List<IPSReference> extensionRefs =
            new ArrayList<>(catalog());
      Object[] extensions;
      try
      {
         extensions = load(
               extensionRefs.toArray(new IPSReference[0]), false, false);
      }
      catch (PSMultiOperationException e)
      {
         final Throwable cause = e.getCause();
         if (cause instanceof PSModelException)
         {
            throw (PSModelException) cause;
         }
         else if (cause != null)
         {
            throw new PSModelException(cause);
         }
         else
         {
            throw new PSModelException(e);
         }
      }

      for (final Object o : extensions)
      {
         final PSExtensionDef extension = (PSExtensionDef) o;
         if (extension.getRef().getFQN().equals(assembler))
         {
            final String param = extension.getInitParameter(
                              IPSExtensionDef.INIT_PARAM_ASSEMBLY_FILE_SUFFIX);
            return StringUtils.isBlank(param) ? null : param;
         }
      }
      return null;
   }
}
