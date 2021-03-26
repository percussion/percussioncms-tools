/*[ IPSExtractorConfigChangeListener.java ]************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;


/**
 * This interface may be implemented by one of the sub-panels within a
 * configuration panel of a extractor. It is used to listen for
 * {@link PSExtractorConfigChangeEvent} fired by certain tab panels.
 *
 * @see IPSExtractorConfigTabPanel
 * @see PSAbstractExtractorConfigTabPanel
 */
public interface IPSExtractorConfigChangeListener
{

   /**
    * This method is called when a configuration change occurs for the extractor
    * tab panel that this listener is registered to.
    *
    * @param event the event object that was passed to this listener.
    */
   public void configChanged(PSExtractorConfigChangeEvent event);
}
