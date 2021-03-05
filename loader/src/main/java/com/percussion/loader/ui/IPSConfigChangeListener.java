
package com.percussion.loader.ui;

import java.util.EventListener;

/**
 * This interface specifies the listener interface to be implemented to 
 * receive configuration change events in the content loader UI.
 */
public interface IPSConfigChangeListener extends EventListener
{
   /**
    * Notifies all listeners of a configuration change.
    * 
    * @param event the event that occurred, never <code>null</code>.
    */
   void configurationChanged(PSConfigurationChangeEvent event);
}
