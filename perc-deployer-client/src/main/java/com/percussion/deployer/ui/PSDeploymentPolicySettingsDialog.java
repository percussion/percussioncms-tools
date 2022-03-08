/******************************************************************************
 *
 * [ PSDeploymentPolicySettingsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.objectstore.PSAppPolicySetting;
import com.percussion.deployer.objectstore.PSAppPolicySettings;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog to set policy settings for application logging, tracing and
 * activation.
 */
public class PSDeploymentPolicySettingsDialog extends PSDialog
{
   /**
    * Constructs this dialog.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    *
    * @param deploymentServer the deployment server from which policy settings
    * are got and set, may not be <code>null</code> and must be connected.
    *
    * @throws IllegalArgumentException if the required paramers are invalid or
    * the server is not connected.
    */
   public PSDeploymentPolicySettingsDialog(Frame parent,
      PSDeploymentServer deploymentServer) throws PSDeployException
   {
      super(parent);
      if (deploymentServer == null)
         throw new IllegalArgumentException("server cannot be null");
      if (!deploymentServer.isConnected())
         throw new IllegalArgumentException("server is not connected");
      m_deploymentServer = deploymentServer;
      initDialog();
   }

   /**
    * Creates the dialog framework.
    */
   private void initDialog() throws PSDeployException
   {
      PSDeploymentManager depMgr = m_deploymentServer.getDeploymentManager();

      m_policyApp = depMgr.getAppPolicySettings();
      m_logSetting = m_policyApp.getLogSetting();
      m_traceSetting = m_policyApp.getTraceSetting();
      m_activationSetting = m_policyApp.getEnabledSetting();

      RadioListener rdListener = new RadioListener();
      JPanel topPane = new JPanel();
      setTitle(getResourceString("title"));
      topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS ));
      topPane.setBorder(BorderFactory.createEmptyBorder(20,10,10,10));

      //log Pane
      JPanel logPane = new JPanel();
      logPane.setLayout(new BoxLayout(logPane, BoxLayout.Y_AXIS ));
      logPane.setBorder(BorderFactory.createTitledBorder(
         getResourceString("logging")));

      JRadioButton disableLogBtn = new JRadioButton(
         getResourceString("disableLogging"));
      disableLogBtn.setMnemonic(
            getResourceString("disableLogging.mn").charAt(0));
      disableLogBtn.setActionCommand(CMD_DISABLELOG);
      JRadioButton defaultLogBtn = new JRadioButton(
         getResourceString("default"));
      defaultLogBtn.setMnemonic(getResourceString("default.mn").charAt(0));
      defaultLogBtn.setActionCommand(CMD_DO_NOT_MODIFYLOG);
      if (m_logSetting.useSetting())
         disableLogBtn.setSelected(true);
      else
         defaultLogBtn.setSelected(true);
      disableLogBtn.addActionListener(rdListener);         
      defaultLogBtn.addActionListener(rdListener);      

      disableLogBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      defaultLogBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      ButtonGroup group1 = new ButtonGroup();
      group1.add(disableLogBtn);
      group1.add(defaultLogBtn);

      logPane.add(Box.createRigidArea(new Dimension(10,0)));
      logPane.add(disableLogBtn);
      logPane.add(Box.createRigidArea(new Dimension(10,0)));
      logPane.add(defaultLogBtn);
      logPane.add(Box.createHorizontalGlue());

      //trace pane
      JPanel tracePane = new JPanel();
      tracePane.setLayout(new BoxLayout(tracePane, BoxLayout.Y_AXIS ));
      tracePane.setBorder(BorderFactory.createTitledBorder(
         getResourceString("tracing")));

      JRadioButton disableTraceBtn = new JRadioButton(
         getResourceString("disableTracing"));
      disableTraceBtn.setMnemonic(
            getResourceString("disableTracing.mn").charAt(0));
      disableTraceBtn.setActionCommand(CMD_DISABLETRACE);
      JRadioButton defaultTraceBtn = new JRadioButton(
         getResourceString("default"));
      defaultTraceBtn.setMnemonic(getResourceString("default.mn").charAt(0));
      defaultTraceBtn.setActionCommand(CMD_DO_NOT_MODIFYTRACE);
      if (m_traceSetting.useSetting())
         disableTraceBtn.setSelected(true);
      else
         defaultTraceBtn.setSelected(true);
      disableTraceBtn.addActionListener(rdListener);         
      defaultTraceBtn.addActionListener(rdListener);      

      disableTraceBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      defaultTraceBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      ButtonGroup group2 = new ButtonGroup();
      group2.add(disableTraceBtn);
      group2.add(defaultTraceBtn);

      tracePane.add(Box.createRigidArea(new Dimension(10,0)));
      tracePane.add(disableTraceBtn);
      tracePane.add(Box.createRigidArea(new Dimension(10,0)));
      tracePane.add(defaultTraceBtn);
      tracePane.add(Box.createHorizontalGlue());


      //Application activation pane
      JPanel activePane = new JPanel();
      activePane.setLayout(new BoxLayout(activePane, BoxLayout.Y_AXIS ));
      activePane.setBorder(BorderFactory.createTitledBorder(
         getResourceString("activation")));

      JRadioButton enableAppBtn = new JRadioButton(
         getResourceString("enableApplication"));
      enableAppBtn.setMnemonic(
                      getResourceString("enableApplication.mn").charAt(0));
      enableAppBtn.setActionCommand(CMD_ENABLEAPP);

      JRadioButton defaultAppBtn = new JRadioButton(
         getResourceString("default"));
      defaultAppBtn.setMnemonic(getResourceString("default.mn").charAt(0));
      defaultAppBtn.setActionCommand(CMD_DO_NOT_MODIFYTRACE);

      if (m_activationSetting.useSetting())
         enableAppBtn.setSelected(true);
      else
         defaultAppBtn.setSelected(true);
      enableAppBtn.addActionListener(rdListener);
      defaultAppBtn.addActionListener(rdListener);
            
      enableAppBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      defaultAppBtn.setAlignmentX(Box.LEFT_ALIGNMENT);
      ButtonGroup group3 = new ButtonGroup();
      group3.add(enableAppBtn);
      group3.add(defaultAppBtn);

      activePane.add(Box.createRigidArea(new Dimension(10,0)));
      activePane.add(enableAppBtn);
      activePane.add(Box.createRigidArea(new Dimension(10,0)));
      activePane.add(defaultAppBtn);
      activePane.add(Box.createHorizontalGlue());

      topPane.add(logPane);
      topPane.add(Box.createRigidArea(new Dimension(0,10)));
      topPane.add(tracePane);
      topPane.add(Box.createRigidArea(new Dimension(0,10)));
      topPane.add(activePane);
      topPane.add(Box.createRigidArea(new Dimension(0,10)));
      
      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true), 
                   BorderLayout.EAST);
      topPane.add(cmdPanel);
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(topPane, BorderLayout.CENTER);
      mainPanel.add(cmdPanel, BorderLayout.SOUTH);
      getContentPane().add(mainPanel);
      
      pack();            
      center();
      setResizable(true);
   }

   /**
    * The action performed by the 'Ok' button. Application policy settings are
    * saved here. If there is an exception while saving error message will be
    * displayed.
    */
    public void onOk()
    {
       try
       {
          m_deploymentServer.getDeploymentManager().saveAppPolicySettings(
             m_policyApp);
       }
       catch(PSDeployException ex)
       {
          ErrorDialogs.showErrorMessage(this, ex.getLocalizedMessage(),
            getResourceString("errSavePolicyTitle"));
          return;
       }
       super.onOk();
    }

   /**
    * Listens to events generated by radio buttons. If disable log, trace or
    * enable app is choosen then {@link PSAppPolicySetting#setUseSetting(
    * boolean)} is set to <code>true</code> or else <code>false</code>.
    */
   private class RadioListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         String cmd = e.getActionCommand();
         if (cmd.equals(CMD_DISABLELOG))
            m_logSetting.setUseSetting(true);
         else if (cmd.equals(CMD_DO_NOT_MODIFYLOG))
            m_logSetting.setUseSetting(false);
         else if (cmd.equals(CMD_DISABLETRACE))
            m_traceSetting.setUseSetting(true);
         else if (cmd.equals(CMD_DO_NOT_MODIFYTRACE))
            m_traceSetting.setUseSetting(false);
         else if (cmd.equals(CMD_ENABLEAPP))
            m_activationSetting.setUseSetting(true);
         else if (cmd.equals(CMD_DO_NOT_MODIFYAPP))
            m_activationSetting.setUseSetting(false);
      }
   }

   /**
    * The deployment server on which to export. Never <code>null</code> or
    * modified. Initialised in the ctor and always connected.
    */
   private PSDeploymentServer m_deploymentServer;

   /**
    * Encapsulates all policy settings. Initialised in <code>initDialog()</code>
    * , never <code>null</code> after that. Modified as <code>m_logSetting
    * </code>, <code>m_traceSetting</code> and <code>m_activationSetting</code>
    * are modified when user selection changes for these settings.
    */
   private PSAppPolicySettings m_policyApp;

   /**
    * Encapsulates log policy setting.
    * Initialised in <code>initDialog()</code>, never <code>null</code>
    * after that. Modifed in {@link RadioListener#actionPerformed(ActionEvent)}
    * based on user selection of log setting.
    */
    private PSAppPolicySetting m_logSetting;

    /**
     * Encapsulates trace policy setting.
     * Initialised in <code>initDialog()</code>, never <code>null</code>
     * after that. Modifed in {@link RadioListener#actionPerformed(ActionEvent)}
     * based on user selection of trace setting.
     */
    private PSAppPolicySetting m_traceSetting;

    /**
     * Encapsulates app enable policy.
     * Initialised in <code>initDialog()</code>, never <code>null</code>
     * after that. Modifed in {@link RadioListener#actionPerformed(ActionEvent)}
     * based on user selection of activation setting.
     */
    private PSAppPolicySetting m_activationSetting;

   /**
    * Command generated on clicking 'Disble logging' radio button.
    */
   private static final String CMD_DISABLELOG = "disableLog";

   /**
    * Command generated on clicking 'Do not modify settings' radio button in
    * 'Application Logging' panel.
    */
   private static final String CMD_DO_NOT_MODIFYLOG = "defaultLog";

   /**
    * Command generated on clicking 'Disble tracing' radio button.
    */
   private static final String CMD_DISABLETRACE = "disableTrace";

   /**
    * Command generated on clicking 'Do not modify settings' radio button in
    * 'Application Tracing' panel.
    */
   private static final String CMD_DO_NOT_MODIFYTRACE = "defaultTrace";

   /**
    * Command generated on clicking 'Enable application' radio button.
    */
   private static final String CMD_ENABLEAPP = "EnableActivation";

   /**
    *  Command generated on clicking 'Do not modify settings' radio button in
    * 'Enable Activation' panel.
    */
   private static final String CMD_DO_NOT_MODIFYAPP = "DefaultActivation";
}
