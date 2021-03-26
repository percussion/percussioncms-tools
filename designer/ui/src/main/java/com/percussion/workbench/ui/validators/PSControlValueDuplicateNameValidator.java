/******************************************************************************
*
* [ PSControlValueDuplicateNameValidator.java ]
*
* COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.workbench.ui.validators;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.workbench.ui.PSMessages;
import com.percussion.workbench.ui.util.PSControlInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSControlValueDuplicateNameValidator
   implements
      IPSControlValueValidator
{

   public PSControlValueDuplicateNameValidator(IPSCmsModel model)
   {
     init(model);
   }

   /* 
    * @see com.percussion.workbench.ui.validators.IPSControlValueValidator#
    * validate(com.percussion.workbench.ui.util.PSControlInfo)
    */
   public String validate(PSControlInfo controlInfo)
   {
      if(controlInfo == null)
         throw new IllegalArgumentException("The control cannot be null."); //$NON-NLS-1$
         
         Control control = controlInfo.getControl();
         boolean exists = false;
         if(control instanceof Text)
         {
            String value = ((Text)control).getText();
            if(StringUtils.isBlank(value))
               return null;
            if(isDuplicateName(value))
               exists = true;
         }         
         else
         {
            throw new IllegalArgumentException(
               "The control must be a Text type control"); //$NON-NLS-1$
         }
         if(exists)
         {
            return PSMessages.getString(
               "PSControlValueDuplicateNameValidator.error.duplicateName"); //$NON-NLS-1$
         }
         return null;
   }
   
   /**
    * Checks to see if the specified name already exists for
    * this object type.
    * @param value the name cannot be <code>null</code>, or empty.
    * @return <code>true</code> if the name is duplicate.
    */
   public boolean isDuplicateName(String value)
   {
      if(StringUtils.isBlank(value))
         throw new IllegalArgumentException("value cannot be null or empty.");
      return m_names.contains(value.toLowerCase());
   }
   
   /**
    * Retrieves all the existing names for this object type
    * @param model the appropriate model for this type passed in
    * by the calling wizard page.
    */
   private void init(IPSCmsModel model)
   {
      try
      {
         Collection<IPSReference> refs = model.catalog(false);
         for(IPSReference ref : refs)
            m_names.add(ref.getName().toLowerCase());
      }
      catch (PSModelException e)
      {
         e.printStackTrace();
      }
   }
   
   private List<String> m_names = new ArrayList<String>();

}
