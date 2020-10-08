package com.percussion.CodeGenerator;

import com.percussion.utils.licensemanagement.metrics.PSLicenseMetrics;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OfflineLicenseGenerator extends JPanel
{
   private JTextArea txtCurrentUsage;

   private JTextField txtCompany;

   private JComboBox cmbStatus;

   private JComboBox cmbLicenseStatus;

   private JComboBox cmbActivationStatus;

   private JTextField txtMaxSites;

   private JTextField txtMaxPages;

   private JTextField txtLicenseId;

   private JTextField txtLastRefresh;

   private JTextField txtUsageExceeded;

   private JTextField txtDaysActive;

   private JLabel lblDaysActive;

   private JTextField txtServerUUID;
   
   private JTextField txtServerID;

   private JTextField txtLicenseType;

   private JLabel lblLicenseType;

   private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("MM/dd/yyyy");

   private static final String FIXED_SALT = "EY3T.eXd.8G^$m7r,B3e`5L3]t=f.fU+|J$oFy1`a-w!Dp7)T1&5*E0_<tT~{F<";

   private static final String INVALID_USAGE = "INVALID USAGE - USAGE HAS BEEN TAMPERED WITH";
   
   
  

   public static void main(String[] args)
   {
         JFrame f = new JFrame("Percussion Offline Licensing Tool");
         f.setLocation(100, 100);
         f.setSize(800, 600);
         f.setResizable(false);
         Container content = new OfflineLicenseGenerator();
         f.setContentPane(content);
         f.setVisible(true);
   }

   /**
    * Create the panel.
    */
   public OfflineLicenseGenerator()
   {
      setToolTipText("");
      setLayout(null);

      JButton btnFileOpen = new JButton("Open Usage File");
      btnFileOpen.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            openLicenseInfoMetric();
         }
      });
      btnFileOpen.setBounds(174, 466, 187, 23);
      add(btnFileOpen);

      JLabel lblPrimary = new JLabel("Percussion Offline Licensing Tool");
      lblPrimary.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 16));
      lblPrimary.setBounds(371, 11, 339, 23);
      add(lblPrimary);

      txtCurrentUsage = new JTextArea();
      txtCurrentUsage.setFont(new Font("Tahoma", Font.PLAIN, 11));
      txtCurrentUsage.setForeground(Color.BLACK);
      txtCurrentUsage.setBackground(Color.WHITE);
      txtCurrentUsage.setBounds(10, 61, 352, 394);
      add(txtCurrentUsage);

      JLabel lblCurrentUsage = new JLabel("Current Usage");
      lblCurrentUsage.setBounds(10, 41, 94, 14);
      add(lblCurrentUsage);

      txtCompany = new JTextField();
      txtCompany.setToolTipText("The company to which the product is licenced.");
      txtCompany.setBounds(371, 61, 244, 20);
      add(txtCompany);
      txtCompany.setColumns(10);

      JLabel lblCompany = new JLabel("Company");
      lblCompany.setBounds(625, 64, 85, 14);
      add(lblCompany);

      String[] activationStrings =
      {"", "SUCCESS", "Active", "Active, Overlimit", "Inactive", "Inactive, Registered", "Suspended", "Suspended, Refresh Required", "SUSPENDED_REFRESH", "CUSTOM_STATUS_ACTIVE_OVERLIMIT"};
      cmbStatus = new JComboBox(activationStrings);
      cmbStatus.setToolTipText("A status, generally Status and License Status should be set the same values");
      cmbStatus.setBounds(371, 123, 244, 20);
      add(cmbStatus);
      

      cmbLicenseStatus = new JComboBox(activationStrings);
      cmbLicenseStatus.setToolTipText("A status, generally Status and License Status should be set the same values");
      cmbLicenseStatus.setBounds(371, 154, 244, 20);
      add(cmbLicenseStatus);
      

      String[] activationStatusStrings = { "", "true", "false"};
      cmbActivationStatus = new JComboBox(activationStatusStrings);
      cmbActivationStatus.setToolTipText("True or False, is the license active");
      cmbActivationStatus.setBounds(371, 185, 244, 20);
      add(cmbActivationStatus);
     

      JLabel lblStatus = new JLabel("Status");
      lblStatus.setBounds(625, 129, 85, 14);
      add(lblStatus);

      JLabel lblActivationStatus = new JLabel("Activation Status");
      lblActivationStatus.setBounds(625, 191, 103, 14);
      add(lblActivationStatus);

      txtMaxSites = new JTextField();
      txtMaxSites.setToolTipText("A number representing the maximum number of sites the license allows.");
      txtMaxSites.setColumns(10);
      txtMaxSites.setBounds(371, 216, 244, 20);
      add(txtMaxSites);

      JLabel lblLicenseStatus = new JLabel("License Status");
      lblLicenseStatus.setBounds(625, 157, 103, 14);
      add(lblLicenseStatus);

      JLabel lblMaxSites = new JLabel("Max Sites");
      lblMaxSites.setBounds(625, 222, 85, 14);
      add(lblMaxSites);

      txtMaxPages = new JTextField();
      txtMaxPages.setToolTipText("A number representing the maximum number of pages the license allows.");
      txtMaxPages.setColumns(10);
      txtMaxPages.setBounds(371, 247, 244, 20);
      add(txtMaxPages);

      JLabel lblMaxPages = new JLabel("Max Pages");
      lblMaxPages.setBounds(625, 253, 103, 14);
      add(lblMaxPages);

      txtLicenseId = new JTextField();
      txtLicenseId.setToolTipText("The license ID");
      txtLicenseId.setColumns(10);
      txtLicenseId.setBounds(371, 278, 244, 20);
      add(txtLicenseId);

      JLabel lblLicenseId = new JLabel("License ID");
      lblLicenseId.setBounds(625, 284, 103, 14);
      add(lblLicenseId);

      txtLastRefresh = new JTextField();
      txtLastRefresh.setToolTipText("A date in the format mm/dd/yyy");
      txtLastRefresh.setColumns(10);
      txtLastRefresh.setBounds(371, 309, 244, 20);
      add(txtLastRefresh);

      JLabel lblLastRefresh = new JLabel("Last Refresh");
      lblLastRefresh.setBounds(625, 315, 103, 14);
      add(lblLastRefresh);

      txtUsageExceeded = new JTextField();
      txtUsageExceeded.setToolTipText("A date in the format mm/dd/yyy");
      txtUsageExceeded.setColumns(10);
      txtUsageExceeded.setBounds(371, 340, 244, 20);
      add(txtUsageExceeded);

      JLabel lblUsageExceeded = new JLabel("Usage Exceeded");
      lblUsageExceeded.setBounds(625, 346, 103, 14);
      add(lblUsageExceeded);

      txtDaysActive = new JTextField();
      txtDaysActive.setToolTipText("The number of days that the license is active.  Overrides 60 day default.");
      txtDaysActive.setColumns(10);
      txtDaysActive.setBounds(371, 371, 244, 20);
      add(txtDaysActive);

      lblDaysActive = new JLabel("Days Active");
      lblDaysActive.setBounds(625, 377, 103, 14);
      add(lblDaysActive);

      txtServerUUID = new JTextField();
      txtServerUUID.setToolTipText("The unique identifier of the server to which the license applies");
      txtServerUUID.setBounds(371, 405, 244, 20);
      add(txtServerUUID);
      txtServerUUID.setColumns(10);

      JLabel lblServerUuid = new JLabel("Server UUID");
      lblServerUuid.setBounds(625, 411, 103, 14);
      add(lblServerUuid);

      JButton btnGenerateLicenseFile = new JButton("Generate License File");
      btnGenerateLicenseFile.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            writeEntitlementFile();
         }
      });
      btnGenerateLicenseFile.setBounds(446, 466, 169, 23);
      add(btnGenerateLicenseFile);

      txtLicenseType = new JTextField();
      txtLicenseType.setToolTipText("The license type");
      txtLicenseType.setColumns(10);
      txtLicenseType.setBounds(371, 92, 244, 20);
      add(txtLicenseType);

      lblLicenseType = new JLabel("License Type");
      lblLicenseType.setBounds(626, 98, 85, 14);
      add(lblLicenseType);
      
      txtServerID = new JTextField();
      txtServerID.setToolTipText("The Server's Mac Address");
      txtServerID.setColumns(10);
      txtServerID.setBounds(371, 437, 244, 20);
      add(txtServerID);
      
      JLabel lblServerId = new JLabel("Server ID");
      lblServerId.setBounds(625, 443, 103, 14);
      add(lblServerId);
   }
   
   protected void setDialogsEmpty()
   {
      txtCurrentUsage.setText("");
      txtCompany.setText("");
      cmbStatus.setSelectedIndex(-1);
      cmbLicenseStatus.setSelectedIndex(-1);
      cmbActivationStatus.setSelectedIndex(-1);
      txtMaxSites.setText("");
      txtMaxPages.setText("");
      txtLicenseId.setText("");
      txtLastRefresh.setText("");
      txtUsageExceeded.setText("");
      txtDaysActive.setText("365");
      txtServerUUID.setText("");
      txtServerID.setText("");
      txtLicenseType.setText("");
   }
   
   protected void writeEntitlementFile()
   {
      JFileChooser fc = new JFileChooser();
      fc.addChoosableFileFilter(new FileFilter()
      {

         public String getDescription()
         {
            return "Percussion Entitlement File (entitlement.percussion)";
         }

         public boolean accept(File f)
         {
            if (f.isDirectory())
            {
               return true;
            }
            else
            {
               return f.getName().toLowerCase().equals(new String("entitlement.percussion"));
            }
         }
      });
      fc.setSelectedFile(new File("license.percussion"));
      int returnVal = fc.showSaveDialog(null);
      File file = fc.getSelectedFile();

      PSLicenseMetrics licenseStatus = new PSLicenseMetrics();
      setMetricForField(licenseStatus, "company", String.class, txtCompany);
      setMetricForField(licenseStatus, "licenseType", String.class, txtLicenseType);
      setMetricForField(licenseStatus, "status", String.class, cmbStatus);
      setMetricForField(licenseStatus, "licenseStatus", String.class, cmbLicenseStatus);
      setMetricForField(licenseStatus, "activationStatus", Boolean.class, cmbActivationStatus);
      setMetricForField(licenseStatus, "maxSites", Integer.class, txtMaxSites);
      setMetricForField(licenseStatus, "maxPages", Integer.class, txtMaxPages);
      setMetricForField(licenseStatus, "licenseId", String.class, txtLicenseId);
      setMetricForField(licenseStatus, "lastRefresh", Date.class, txtLastRefresh);
      setMetricForField(licenseStatus, "usageExceeded", Date.class, txtUsageExceeded);
      setMetricForField(licenseStatus, "serverUUID", String.class, txtServerUUID);
      setMetricForField(licenseStatus, "serverId", String.class, txtServerID);
      setMetricForField(licenseStatus, "daysActive", Integer.class, txtDaysActive);
      licenseStatus.putMetric("dateGenerated", new Date());
      licenseStatus.signMetrics(FIXED_SALT);
      FileOutputStream fileOut;

      try
      {
         fileOut = new FileOutputStream(file);
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(licenseStatus);
         out.close();
         fileOut.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   private void setMetricForField(final PSLicenseMetrics metrics, final String metricName, Class metricClass,
         JTextField textField)
   {
      String stringForSet = textField.getText();
      setMetricForField(metrics, metricName, metricClass, stringForSet);
   }

   private void setMetricForField(final PSLicenseMetrics metrics, final String metricName, Class metricClass,
         JComboBox cmbBox)
   {
      String stringForSet = (String) cmbBox.getSelectedItem();
      setMetricForField(metrics, metricName, metricClass, stringForSet);
   }

   private void setMetricForField(final PSLicenseMetrics metrics, final String metricName, Class metricClass,
         String text)
   {
      Object objectMetric = null;
      try
      {

         if (!(text == null) && !text.isEmpty())
         {
            if (metricClass.isAssignableFrom(Date.class))
            {
               objectMetric = DATEFORMAT.parse(text);
            }

            else if (metricClass.isAssignableFrom(Integer.class))
            {
               objectMetric = Integer.parseInt(text);
            }

            else if (metricClass.isAssignableFrom(Boolean.class))
            {
               objectMetric = Boolean.parseBoolean(text);
            }

            else
            {
               objectMetric = text;
            }
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
         objectMetric = null;
      }
      finally
      {
         metrics.putMetric(metricName, (Serializable) objectMetric);
      }
   }

   private void openLicenseInfoMetric()
   {
      JFileChooser fc = new JFileChooser();
      fc.addChoosableFileFilter(new FileFilter()
      {

         public String getDescription()
         {
            return "Percussion Usage Files (usage.percussion)";
         }

         public boolean accept(File f)
         {
            if (f.isDirectory())
            {
               return true;
            }
            else
            {
               return f.getName().toLowerCase().equals(new String("usage.percussion"));
            }
         }
      });
      setDialogsEmpty();
      int returnVal = fc.showOpenDialog(null);
      File file = fc.getSelectedFile();

      try
      {
         InputStream inputStream = new FileInputStream(file);
         InputStream buffer = new BufferedInputStream(inputStream);
         ObjectInput input = new ObjectInputStream(buffer);
         try
         {
            PSLicenseMetrics entitlement = (PSLicenseMetrics) input.readObject();
            String currentUsage = " CURRENT USAGE\n\n";
            if (entitlement.areMetricsValid(FIXED_SALT))
            {

               if (entitlement.getMetric("company") != null)
               {
                  String company = ((String) entitlement.getMetric("company"));
                  this.txtCompany.setText(company);
                  currentUsage = currentUsage.concat(" Company: ");
                  currentUsage = currentUsage.concat(company);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("licenseType") != null)
               {
                  String licenseType = ((String) entitlement.getMetric("licenseType"));
                  this.txtLicenseType.setText(licenseType);
                  currentUsage = currentUsage.concat(" License Type: ");
                  currentUsage = currentUsage.concat(licenseType);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("licenseStatus") != null)
               {
                  String licenseStatus = ((String) entitlement.getMetric("licenseStatus"));
                  
                  this.cmbStatus.setSelectedItem(licenseStatus);
                  currentUsage = currentUsage.concat(" Status: ");
                  currentUsage = currentUsage.concat(licenseStatus);
                  currentUsage = currentUsage.concat("\n");
                  
                  this.cmbLicenseStatus.setSelectedItem(licenseStatus);
                  currentUsage = currentUsage.concat(" License Status: ");
                  currentUsage = currentUsage.concat(licenseStatus);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("activationStatus") != null)
               {
                  String activationStatus = ((Boolean) entitlement.getMetric("activationStatus")).toString();
                  this.cmbActivationStatus.setSelectedItem(activationStatus);
                  currentUsage = currentUsage.concat(" Activation Status: ");
                  currentUsage = currentUsage.concat(activationStatus);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("maxSites") != null)
               {
                  String maxSites = ((Integer) entitlement.getMetric("maxSites")).toString();
                  this.txtMaxSites.setText(maxSites);
                  currentUsage = currentUsage.concat(" Max Sites: ");
                  currentUsage = currentUsage.concat(maxSites);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("maxPages") != null)
               {
                  String maxPages = ((Integer) entitlement.getMetric("maxPages")).toString();
                  this.txtMaxPages.setText(maxPages);
                  currentUsage = currentUsage.concat(" Max Pages: ");
                  currentUsage = currentUsage.concat(maxPages);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("licenseId") != null)
               {
                  String licenseId = ((String) entitlement.getMetric("licenseId"));
                  this.txtLicenseId.setText(licenseId);
                  currentUsage = currentUsage.concat(" License ID: ");
                  currentUsage = currentUsage.concat(licenseId);
                  currentUsage = currentUsage.concat("\n");
               }

               String lastRefresh = (this.DATEFORMAT.format(new Date()));
               this.txtLastRefresh.setText(lastRefresh);
               if (entitlement.getMetric("lastRefresh") != null)
               {
                  lastRefresh = (this.DATEFORMAT.format((Date) entitlement.getMetric("lastRefresh")));
                  currentUsage = currentUsage.concat(" Last Refresh: ");
                  currentUsage = currentUsage.concat(lastRefresh);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("usageExceeded") != null)
               {
                  String usageExceeded = (this.DATEFORMAT.format((Date) entitlement.getMetric("usageExceeded")));
                  this.txtUsageExceeded.setText(usageExceeded);
                  currentUsage = currentUsage.concat(" Usage Exceeded: ");
                  currentUsage = currentUsage.concat(usageExceeded);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("serverUUID") != null)
               {
                  String serverUUID = ((String) entitlement.getMetric("serverUUID"));
                  this.txtServerUUID.setText(serverUUID);
                  currentUsage = currentUsage.concat(" Server UUID: ");
                  currentUsage = currentUsage.concat(serverUUID);
                  currentUsage = currentUsage.concat("\n");
               }
               
               if (entitlement.getMetric("serverId") != null) 
               {
                  String serverID = ((String) entitlement.getMetric("serverId"));
                  this.txtServerID.setText(serverID);
                  currentUsage = currentUsage.concat(" Server ID: ");
                  currentUsage = currentUsage.concat(serverID);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("currentPages") != null)
               {
                  String currentPages = ((Integer) entitlement.getMetric("currentPages")).toString();
                  currentUsage = currentUsage.concat(" Current Pages: ");
                  currentUsage = currentUsage.concat(currentPages);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("currentSites") != null)
               {
                  String currentSites = ((Integer) entitlement.getMetric("currentSites")).toString();
                  currentUsage = currentUsage.concat(" Current Sites: ");
                  currentUsage = currentUsage.concat(currentSites);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("firstUsageDate") != null)
               {
                  String firstUsageDate = (this.DATEFORMAT.format((Date) entitlement.getMetric("firstUsageDate")));
                  currentUsage = currentUsage.concat(" First Usage Date: ");
                  currentUsage = currentUsage.concat(firstUsageDate);
                  currentUsage = currentUsage.concat("\n");
               }

               if (entitlement.getMetric("signature") != null)
               {
                  String signature = ((String) entitlement.getMetric("signature"));
                  currentUsage = currentUsage.concat(" Signature: ");
                  currentUsage = currentUsage.concat(signature);
                  currentUsage = currentUsage.concat("\n");
               }
               
               if (entitlement.getMetric("daysActive")!=null)
               {
                  String daysactive = ((Integer) entitlement.getMetric("daysActive")).toString();
                  currentUsage = currentUsage.concat(" Days Active: ");
                  currentUsage = currentUsage.concat(daysactive);
                  currentUsage = currentUsage.concat("\n");
               }
               
               if (entitlement.getMetric("usageExceeded")!=null)
               {
                  String usageexceeded = ((Integer) entitlement.getMetric("usageExceeded")).toString();
                  currentUsage = currentUsage.concat(" Days Exceeded: ");
                  currentUsage = currentUsage.concat(usageexceeded);
                  currentUsage = currentUsage.concat("\n");
               }
            }
            else
            {

               currentUsage = INVALID_USAGE;
            }

            this.txtCurrentUsage.setText(currentUsage);
         }
         finally
         {
            input.close();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
