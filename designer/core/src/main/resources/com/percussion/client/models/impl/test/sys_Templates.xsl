<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 1999-2021 Percussion Software, Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!-- $Id: sys_Templates.xsl 1.81 2002/12/07 00:05:27Z bjoginipally Exp $ -->
<!DOCTYPE xsl:stylesheet [
	<!ENTITY nbsp "&#160;">
	<!--  no-break space = non-breaking space, U+00A0 ISOnum -->
]>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" xmlns:psxi18n="urn:www.percussion.com/i18n" exclude-result-prefixes="psxi18n">
	<xsl:import href="file:sys_resources/stylesheets/sys_I18nUtils.xsl"/>
	<!-- write the xml header according to XHTML 1.0 spec -->
	<xsl:output method="xml" indent="yes" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="DTD/xhtml1-strict.dtd"/>
	<!-- 
     provide a shell to test in isolation.
     this template should be ignored when this file is imported
     
     saxon -o ContentEditor.htm ContentEditor-ph.xml ContentEditor.xsl
  -->
	<xsl:template match="/">
		<html>
			<head>
				<title>Control Library Test</title>
				<script src="../sys_resources/js/textedit.js">;</script>
			</head>
			<body>
				<xsl:for-each select="/ContentEditor/ItemContent/DisplayField">
					<p>
						<b>
							<xsl:value-of select="Label"/>
						</b>
						<xsl:apply-templates select="Control" mode="psxcontrol"/>
					</p>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
	<!-- 
     default template for read-only controls; just return the value
  -->
	<xsl:template match="Control[@isReadOnly='yes']" priority="5" mode="psxcontrol">
		<div class="datadisplay">
			<xsl:choose>
				<xsl:when test="string-length(Value)">
					<xsl:value-of select="Value"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'&nbsp;'"/>
				</xsl:otherwise>
			</xsl:choose>
		</div>
		<input type="hidden" name="{@paramName}" value="{Value}"/>
	</xsl:template>
	<!-- 
     generic templates for turning <ParamList> into attributes
  -->
	<xsl:template name="parametersToAttributes">
		<xsl:param name="controlClassName"/>
		<xsl:param name="controlNode"/>
		<xsl:param name="paramType" select="'generic'"/>
		<!-- apply any control parameter defaults defined in the metadata -->
		<xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType and psxctl:DefaultValue]" mode="internal">
			<xsl:with-param name="controlClassName" select="$controlClassName"/>
		</xsl:apply-templates>
		<!-- apply control parameters that have been defined in the metadata (will override defaults) -->
		<xsl:apply-templates select="$controlNode/ParamList/Param[@name = document('')/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@paramtype=$paramType]/@name]" mode="internal">
			<xsl:with-param name="controlname" select="$controlNode/@paramName"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="ParamList/Param[@name='alt']" mode="internal" priority="10">
		<xsl:param name="controlname"/>
		<xsl:variable name="keyval">
			<xsl:choose>
				<xsl:when test="@sourceType='sys_system'">
					<xsl:value-of select="concat('psx.ce.system.', $controlname, '.alt@', .)"/>
				</xsl:when>
				<xsl:when test="@sourceType='sys_shared'">
					<xsl:value-of select="concat('psx.ce.shared.', $controlname, '.alt@', .)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $controlname, '.alt@', .)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:attribute name="{@name}"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="$keyval"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	</xsl:template>
	<xsl:template match="ParamList/Param" mode="internal" priority="5">
		<xsl:attribute name="{@name}"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="psxctl:ParamList/psxctl:Param[@name='alt']" mode="internal" priority="10">
		<xsl:param name="controlClassName"/>
		<xsl:attribute name="{@name}"><xsl:call-template name="getLocaleString"><xsl:with-param name="key" select="concat('psx.contenteditor.sys_templates.',$controlClassName,'.alt@',psxctl:DefaultValue)"/><xsl:with-param name="lang" select="$lang"/></xsl:call-template></xsl:attribute>
	</xsl:template>
	<xsl:template match="psxctl:ParamList/psxctl:Param" mode="internal" priority="5">
		<xsl:attribute name="{@name}"><xsl:value-of select="psxctl:DefaultValue"/></xsl:attribute>
	</xsl:template>
	<!--
     generic templates for turning a <ParamList/Param> into a value
  -->
	<xsl:template name="parameterToValue">
		<xsl:param name="controlClassName"/>
		<xsl:param name="controlNode"/>
		<xsl:param name="paramName"/>
		<xsl:choose>
			<xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
				<!-- apply control parameters that have been defined in the metadata (will override defaults) -->
				<xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- apply any control parameter defaults defined in the metadata -->
				<xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="internal-value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="ParamList/Param" mode="internal-value">
		<xsl:value-of select="."/>
	</xsl:template>
	<xsl:template match="psxctl:ParamList/psxctl:Param" mode="internal-value">
		<xsl:value-of select="psxctl:DefaultValue"/>
	</xsl:template>
	<xsl:template name="getLocaleDisplayLabel">
		<xsl:param name="displayVal"/>
		<xsl:param name="sourceType"/>
		<xsl:param name="paramName"/>
		<xsl:variable name="keyval">
			<xsl:choose>
				<xsl:when test="$sourceType='sys_system'">
					<xsl:value-of select="concat('psx.ce.system.', $paramName, '@', $displayVal)"/>
				</xsl:when>
				<xsl:when test="@sourceType='sys_shared'">
					<xsl:value-of select="concat('psx.ce.shared.', $paramName, '@', $displayVal)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $paramName,             '@', $displayVal)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:call-template name="getLocaleString">
			<xsl:with-param name="key" select="$keyval"/>
			<xsl:with-param name="lang" select="$lang"/>
		</xsl:call-template>
	</xsl:template>
	<!-- core attributes common to most elements
  id       document-wide unique id
  class    space separated list of classes
  style    associated style info
  title    advisory title/amplification
-->
	<!--
control for radio button
-->
	<!-- 
     sys_RadioButtons
     
<!ATTLIST INPUT
  %attrs;                              -%coreattrs, %i18n, %events -
  checked     (checked)      #IMPLIED  -for radio buttons and check boxes -
  disabled    (disabled)     #IMPLIED  -unavailable in this context -
  readonly    (readonly)     #IMPLIED  -for text and passwd -
  size        CDATA          #IMPLIED  -specific to each type of field -
  alt         CDATA          #IMPLIED  -short description -
  tabindex    NUMBER         #IMPLIED  -position in tabbing order -
  accesskey   %Character;    #IMPLIED  -accessibility key character -
  onfocus     %Script;       #IMPLIED  -the element got the focus -
  onblur      %Script;       #IMPLIED  -the element lost the focus -
  onselect    %Script;       #IMPLIED  -some text was selected -
  onchange    %Script;       #IMPLIED  -the element value was changed -
  >
  -->
	<psxctl:ControlMeta name="sys_RadioButtons" dimension="single" choiceset="required">
		<psxctl:Description>A set of radio buttons for selecting a single value</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<!--		<psxctl:Param name="disabled" datatype="String" paramtype="generic">
				<psxctl:Description>If set, this boolean attribute disables the control for user input.</psxctl:Description>
			</psxctl:Param> -->
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_RadioButtons']" mode="psxcontrol">
		<div class="datadisplay">
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_RadioButtons'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
			<xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysradiobuttons">
				<xsl:with-param name="controlValue" select="Value"/>
				<xsl:with-param name="paramName" select="@paramName"/>
				<xsl:with-param name="accessKey">
					<xsl:call-template name="getaccesskey">
						<xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/>
						<xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/>
						<xsl:with-param name="paramName" select="@paramName"/>
						<xsl:with-param name="accessKey" select="@accessKey"/>
					</xsl:call-template>
				</xsl:with-param>
			</xsl:apply-templates>
		</div>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-sysradiobuttons">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<xsl:param name="accessKey"/>
		<!-- local/global and external can both be in the same control -->
		<!-- external is assumed to use a DTD compatible with sys_ContentEditor.dtd (items in <DisplayEntry>s) -->
		<xsl:apply-templates select="DisplayEntry" mode="psxcontrol-sysradiobuttons">
			<xsl:with-param name="controlValue" select="$controlValue"/>
			<xsl:with-param name="paramName" select="$paramName"/>
			<xsl:with-param name="accessKey" select="$accessKey"/>
		</xsl:apply-templates>
		<xsl:if test="string(@href)">
			<xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-sysradiobuttons">
				<xsl:with-param name="controlValue" select="$controlValue"/>
				<xsl:with-param name="paramName" select="$paramName"/>
				<xsl:with-param name="accessKey" select="$accessKey"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-sysradiobuttons">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<xsl:param name="accessKey"/>
		<input type="radio" name="{$paramName}" value="{Value}">
			<xsl:if test="$accessKey!=''">
				<xsl:attribute name="accesskey"><xsl:value-of select="$accessKey"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="Value = $controlValue">
				<xsl:attribute name="checked"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="checked"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@sourceType">
					<xsl:call-template name="getLocaleDisplayLabel">
						<xsl:with-param name="sourceType" select="@sourceType"/>
						<xsl:with-param name="paramName" select="$paramName"/>
						<xsl:with-param name="displayVal" select="DisplayLabel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="DisplayLabel"/>
				</xsl:otherwise>
			</xsl:choose>
			<br/>
		</input>
	</xsl:template>
	<!-- read only template for single Radio Button -->
	<xsl:template match="Control[@name='sys_RadioButtons' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<div class="datadisplay">
			<xsl:variable name="Val" select="Value"/>
			<xsl:variable name="paramName" select="@paramName"/>
			<xsl:for-each select="DisplayChoices/DisplayEntry[Value=$Val]">
				<xsl:choose>
					<xsl:when test="@sourceType">
						<xsl:call-template name="getLocaleDisplayLabel">
							<xsl:with-param name="sourceType" select="@sourceType"/>
							<xsl:with-param name="paramName" select="$paramName"/>
							<xsl:with-param name="displayVal" select="DisplayLabel"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="DisplayLabel"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</div>
		<input type="hidden" name="{@paramName}" value="{Value}"/>
	</xsl:template>
	<!-- 
     sys_EditBox
     
<!ATTLIST input
%attrs;
type        %InputType;    "text"
name        CDATA          #IMPLIED
value       CDATA          #IMPLIED
checked     (checked)      #IMPLIED
disabled    (disabled)     #IMPLIED
readonly    (readonly)     #IMPLIED
size        CDATA          #IMPLIED
maxlength   %Number;       #IMPLIED
src         %URI;          #IMPLIED
alt         CDATA          #IMPLIED
usemap      %URI;          #IMPLIED
tabindex    %Number;       #IMPLIED
accesskey   %Character;    #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onselect    %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
accept      %ContentTypes; #IMPLIED
>
 -->
	<psxctl:ControlMeta name="sys_EditBox" dimension="single" choiceset="none">
		<psxctl:Description>The standard control for editing text:  an &lt;input type="text"> tag.</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in number of characters.  The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>50</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="maxlength" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the maximum number of characters the user may enter. This number may exceed the specified size, in which case the user agent should offer a scrolling mechanism. The default value for This parameter is an unlimited number.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_EditBox']" mode="psxcontrol">
		<input type="text" name="{@paramName}" value="{Value}">
			<xsl:if test="@accessKey!=''">
				<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
			</xsl:if>
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_EditBox'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
		</input>
	</xsl:template>
	<!--
     sys_File
 -->
	<psxctl:ControlMeta name="sys_File" dimension="single" choiceset="none">
		<psxctl:Description>The standard control for uploading files:  an &lt;input type="file"> tag.</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>50</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="cleartext" datatype="String" paramtype="custom">
				<psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear'.</psxctl:Description>
				<psxctl:DefaultValue>Clear</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:Dependencies>
			<psxctl:Dependency status="readyToGo" occurrence="single">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_FileInfo</name>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_File']" mode="psxcontrol">
		<input type="file" name="{@paramName}">
			<xsl:if test="@accessKey!=''">
				<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
			</xsl:if>
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_File'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
		</input>
		<xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
      &nbsp;&nbsp;&nbsp;
      <xsl:call-template name="sys_filereadonly"/>
			<xsl:if test="boolean(@clearBinaryParam)">
         &nbsp;&nbsp;
         <xsl:call-template name="sys_fileclear"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- when the file control is used in read-only mode, provide a binary preview -->
	<xsl:template name="sys_filereadonly" match="Control[@name='sys_File' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<xsl:variable name="childkey">
			<xsl:choose>
				<xsl:when test="boolean(../../@childkey)">
					<xsl:value-of select="../../@childkey"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="/*/ItemContent/@childkey"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="url">
			<xsl:choose>
				<xsl:when test="contains(/ContentEditor/@submitHref, '.html')">
					<!-- exclude the extension, as it causes IE to ignore the content-type header -->
					<xsl:value-of select="substring-before(/ContentEditor/@submitHref, '.html')"/>
					<xsl:value-of select="substring-after(/ContentEditor/@submitHref,'.html')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="/ContentEditor/@submitHref"/>
				</xsl:otherwise>
			</xsl:choose>
			<!-- ? or & depending on the Href already having CGI vars -->
			<xsl:choose>
				<xsl:when test="contains(/ContentEditor/@submitHref, '?')">&amp;</xsl:when>
				<xsl:otherwise>?</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="concat('sys_command=binary&amp;sys_contentid=',/*/Workflow/@contentId,
         '&amp;sys_revision=',/*/Workflow/ContentStatus/@thisRevision,
         '&amp;sys_submitname=',@paramName,'&amp;sys_childrowid=',$childkey)"/>
			<!-- childid if it exists -->
		</xsl:variable>
		<a href="{$url}" target="_blank">
			<xsl:call-template name="getLocaleString">
				<xsl:with-param name="key" select="'psx.contenteditor.sys_templates@Preview File'"/>
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template>
		</a>
	</xsl:template>
	<!-- when the file control is used in edit mode, provide a clear checkbox -->
	<xsl:template name="sys_fileclear" match="Control[@name='sys_File' and @isReadOnly='no']" priority="10" mode="psxcontrol-sys_fileclear">
		<span class="datadisplay">
			<input name="{@clearBinaryParam}" type="checkbox" value="yes">
				<xsl:if test="@accessKey!=''">
					<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
				</xsl:if>
			</input>
			<xsl:call-template name="parameterToValuefileclear">
				<xsl:with-param name="controlClassName" select="'sys_File'"/>
				<xsl:with-param name="controlNode" select="."/>
				<xsl:with-param name="paramName" select="'cleartext'"/>
			</xsl:call-template>
			<br/>
		</span>
	</xsl:template>
	<xsl:template name="parameterToValuefileclear">
		<xsl:param name="controlClassName"/>
		<xsl:param name="controlNode"/>
		<xsl:param name="paramName"/>
		<xsl:choose>
			<xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
				<!-- apply control parameters that have been defined in the metadata (will override defaults) -->
				<xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- apply any control parameter defaults defined in the metadata -->
				<xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="fileclear-internal-value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:ParamList/psxctl:Param" mode="fileclear-internal-value">
		<xsl:call-template name="getLocaleString">
			<xsl:with-param name="key" select="concat('psx.ce.',../../@name, '@', psxctl:DefaultValue)"/>
			<xsl:with-param name="lang" select="$lang"/>
		</xsl:call-template>
	</xsl:template>
	<!--
	sys_webImageFX
   -->
	<psxctl:ControlMeta name="sys_webImageFX" dimension="single" choiceset="none">
		<psxctl:Description>Custom Ektron control for editing and uploading image files:  an &lt;input type="file"> tag.</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="width" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>800</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="height" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>400</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="config_src_url" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the location of the config.xml that will the control will use for configuration. The default value is "../rx_resources/webimagefx/ImageEditConfig.xml".</psxctl:Description>
				<psxctl:DefaultValue>../rx_resources/webimagefx/ImageEditConfig.xml</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cleartext" datatype="String" paramtype="custom">
				<psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear'.</psxctl:Description>
				<psxctl:DefaultValue>Clear</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="webimagefx.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/webimagefx/webimagefx.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
		<psxctl:Dependencies>
			<psxctl:Dependency status="readyToGo" occurrence="single">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_FileInfo</name>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_webImageFX' and @isReadOnly='no']" priority="10" mode="psxcontrol">
		<xsl:choose>
			<!-- Check for IE on Windows-->
			<xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE')">
				<!-- set up the variables that will be used in the javascript -->
				<xsl:variable name="name">
					<xsl:value-of select="@paramName"/>
				</xsl:variable>
				<xsl:variable name="width">
					<xsl:choose>
						<xsl:when test="ParamList/Param[@name='width']">
							<xsl:value-of select="ParamList/Param[@name='width']"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='width']/psxctl:DefaultValue"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="height">
					<xsl:choose>
						<xsl:when test="ParamList/Param[@name='height']">
							<xsl:value-of select="ParamList/Param[@name='height']"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='height']/psxctl:DefaultValue"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="config_src_url">
					<xsl:choose>
						<xsl:when test="ParamList/Param[@name='config_src_url']">
							<xsl:value-of select="ParamList/Param[@name='config_src_url']"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_webImageFX']/psxctl:ParamList/psxctl:Param[@name='config_src_url']/psxctl:DefaultValue"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<input type="hidden" name="uploadfilephoto" value="{Value}"/>
				<xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
					<xsl:if test="boolean(@clearBinaryParam)">
						<br/>
						<xsl:call-template name="sys_imageclear"/>
					</xsl:if>
				</xsl:if>
				<script language="JavaScript"><![CDATA[
	   <!-- 	      
	      
	      if(WifxLicenseKeys != "")
	      {
	         WebImageFX.Config = "]]><xsl:value-of select="$config_src_url"/>";	         
	         WebImageFX.create("uploadfilephoto", <xsl:value-of select="$width"/>, <xsl:value-of select="$height"/>);	      
	         <![CDATA[
	          ps_hasWifx = true;
	      }
	      else
	      {
	         var doc = window.document;
	         doc.open();
	         doc.writeln("<table bgcolor='#ffffff' width='60%' height='50' border='1' cellpadding='0' cellspacing='0'>");
	         doc.writeln("<tr><td align='center' valign='middle'>");
	         doc.writeln("No license for WebImageFX control.<br>Please contact Percussion technical support.");
	         doc.writeln("</td></tr></table>");
	         doc.close();
	         
	      }
	   //-->
	   ]]></script>
			</xsl:when>
			<xsl:otherwise>
				<input type="file" name="uploadfilephoto">
					<xsl:call-template name="parametersToAttributes">
						<xsl:with-param name="controlClassName" select="'sys_File'"/>
						<xsl:with-param name="controlNode" select="."/>
					</xsl:call-template>
				</input>
				<xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
					<xsl:if test="boolean(@clearBinaryParam)">
						<br/>
						<xsl:call-template name="sys_filereadonly"/>
					</xsl:if>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- when the file control is used in edit mode, provide a clear checkbox -->
	<xsl:template name="sys_imageclear" match="Control[@name='sys_webImageFX' and @isReadOnly='no']" priority="10" mode="psxcontrol-sys_webimagefxclear">
		<span class="datadisplay">
			<input name="{@clearBinaryParam}" type="checkbox" value="yes"/>
			<xsl:call-template name="parameterToValueimageclear">
				<xsl:with-param name="controlClassName" select="'sys_webImageFX'"/>
				<xsl:with-param name="controlNode" select="."/>
				<xsl:with-param name="paramName" select="'cleartext'"/>
			</xsl:call-template>
			<br/>
		</span>
	</xsl:template>
	<xsl:template name="parameterToValueimageclear">
		<xsl:param name="controlClassName"/>
		<xsl:param name="controlNode"/>
		<xsl:param name="paramName"/>
		<xsl:choose>
			<xsl:when test="$controlNode/ParamList/Param[@name = $paramName]">
				<!-- apply control parameters that have been defined in the metadata (will override defaults) -->
				<xsl:apply-templates select="$controlNode/ParamList/Param[@name = $paramName]" mode="internal-value"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- apply any control parameter defaults defined in the metadata -->
				<xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name=$controlClassName]/psxctl:ParamList/psxctl:Param[@name=$paramName and psxctl:DefaultValue]" mode="imageclear-internal-value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="psxctl:ParamList/psxctl:Param" mode="imageclear-internal-value">
		<xsl:call-template name="getLocaleString">
			<xsl:with-param name="key" select="concat('psx.ce.',../../@name, '@', psxctl:DefaultValue)"/>
			<xsl:with-param name="lang" select="$lang"/>
		</xsl:call-template>
	</xsl:template>
	<!--
	      End of sys_webImageFX
       -->
	<!--
     sys_HiddenInput: needs a higher priority than the default read-only template
  -->
	<psxctl:ControlMeta name="sys_HiddenInput" dimension="single" choiceset="none">
		<psxctl:Description>a hidden input field</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_HiddenInput']" priority="10" mode="psxcontrol">
		<input type="hidden" name="{@paramName}" value="{Value}">
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
		</input>
	</xsl:template>
	<!--
     when the mode is "psxcontrol-hidden", visibility rules have suppressed
     the control; render all controls as if they were sys_HiddenInput.
-->
	<xsl:template match="Control" mode="psxcontrol-hidden">
		<xsl:variable name="paramname" select="@paramName"/>
		<xsl:variable name="controlnode" select="."/>
		<xsl:choose>
			<!--In case of DisplayChoices like check boxes, we need hidden input elements wherever @selected=yes -->
			<xsl:when test="DisplayChoices[not(boolean(preceding-sibling::Value))]">
				<xsl:choose>
					<xsl:when test="@name='sys_DropDownSingle'">
						<xsl:choose>
							<xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']">
								<input type="hidden" name="{$paramname}" value="{DisplayChoices/DisplayEntry[@selected='yes']/Value}">
									<xsl:call-template name="parametersToAttributes">
										<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
										<xsl:with-param name="controlNode" select="$controlnode"/>
									</xsl:call-template>
								</input>
							</xsl:when>
							<xsl:when test="string(DisplayChoices/@href) and document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']">
								<input type="hidden" name="{$paramname}" value="{document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']/Value}">
									<xsl:call-template name="parametersToAttributes">
										<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
										<xsl:with-param name="controlNode" select="$controlnode"/>
									</xsl:call-template>
								</input>
							</xsl:when>
							<xsl:otherwise>
								<input type="hidden" name="{$paramname}" value="{DisplayChoices/DisplayEntry/Value}">
									<xsl:call-template name="parametersToAttributes">
										<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
										<xsl:with-param name="controlNode" select="$controlnode"/>
									</xsl:call-template>
								</input>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!-- Loop through all display entries with @selected=yes -->
						<xsl:for-each select="DisplayChoices/DisplayEntry[@selected='yes']">
							<input type="hidden" name="{$paramname}" value="{Value}">
								<xsl:call-template name="parametersToAttributes">
									<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
									<xsl:with-param name="controlNode" select="$controlnode"/>
								</xsl:call-template>
							</input>
						</xsl:for-each>
						<!-- Loop through all display entries with @selected=yes if entries come from a external document -->
						<xsl:if test="string(DisplayChoices/@href)">
							<xsl:for-each select="document(DisplayChoices/@href)/*/DisplayEntry[@selected='yes']">
								<input type="hidden" name="{$paramname}" value="{Value}">
									<xsl:call-template name="parametersToAttributes">
										<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
										<xsl:with-param name="controlNode" select="$controlnode"/>
									</xsl:call-template>
								</input>
							</xsl:for-each>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<input type="hidden" name="{$paramname}" value="{Value}">
					<xsl:call-template name="parametersToAttributes">
						<xsl:with-param name="controlClassName" select="'sys_HiddenInput'"/>
						<xsl:with-param name="controlNode" select="$controlnode"/>
					</xsl:call-template>
				</input>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- 
     sys_DropDownSingle
     
<!ATTLIST select
%attrs;
name        CDATA          #IMPLIED
size        %Number;       #IMPLIED
multiple    (multiple)     #IMPLIED
disabled    (disabled)     #IMPLIED
tabindex    %Number;       #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
>
  -->
	<psxctl:ControlMeta name="sys_DropDownSingle" dimension="single" choiceset="required">
		<psxctl:Description>a drop down combo box for selecting a single value</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>If the element is presented as a scrolled list box, This parameter specifies the number of rows in the list that should be visible at the same time.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="multiple" datatype="String" paramtype="generic">
				<psxctl:Description>If set, this boolean attribute allows multiple selections. If not set, the element only permits single selections.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="disabled" datatype="String" paramtype="generic">
				<psxctl:Description>If set, this boolean attribute disables the control for user input.</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_DropDownSingle']" mode="psxcontrol">
		<div>
			<select name="{@paramName}">
				<xsl:call-template name="parametersToAttributes">
					<xsl:with-param name="controlClassName" select="'sys_DropDownSingle'"/>
					<xsl:with-param name="controlNode" select="."/>
				</xsl:call-template>
				<xsl:apply-templates select="DisplayChoices" mode="psxcontrol-sysdropdownsingle">
					<xsl:with-param name="controlValue" select="Value"/>
					<xsl:with-param name="paramName" select="@paramName"/>
				</xsl:apply-templates>
			</select>
		</div>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-sysdropdownsingle">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<!-- local/global and external can both be in the same control -->
		<!-- external is assumed to use a DTD compatible with sys_ContentEditor.dtd (items in <DisplayEntry>s) -->
		<xsl:apply-templates select="DisplayEntry" mode="psxcontrol-sysdropdownsingle">
			<xsl:with-param name="controlValue" select="$controlValue"/>
			<xsl:with-param name="paramName" select="$paramName"/>
		</xsl:apply-templates>
		<xsl:if test="string(@href)">
			<xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-sysdropdownsingle">
				<xsl:with-param name="controlValue" select="$controlValue"/>
				<xsl:with-param name="paramName" select="$paramName"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-sysdropdownsingle">
		<xsl:param name="controlValue"/>
		<xsl:param name="paramName"/>
		<option value="{Value}">
			<xsl:if test="Value = $controlValue">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="@sourceType">
					<xsl:call-template name="getLocaleDisplayLabel">
						<xsl:with-param name="sourceType" select="@sourceType"/>
						<xsl:with-param name="paramName" select="$paramName"/>
						<xsl:with-param name="displayVal" select="DisplayLabel"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="DisplayLabel"/>
				</xsl:otherwise>
			</xsl:choose>
		</option>
	</xsl:template>
	<!-- read only template for dropdown single -->
	<xsl:template match="Control[@name='sys_DropDownSingle' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<div class="datadisplay">
			<xsl:variable name="Val" select="Value"/>
			<xsl:variable name="paramName" select="@paramName"/>
			<xsl:choose>
				<xsl:when test="not($Val)">
					<xsl:variable name="displayValue">
						<xsl:choose>
							<xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']">
								<xsl:value-of select="DisplayChoices/DisplayEntry[@selected='yes']/DisplayLabel"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="DisplayChoices/DisplayEntry/DisplayLabel"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:choose>
						<xsl:when test="@sourceType">
							<xsl:call-template name="getLocaleDisplayLabel">
								<xsl:with-param name="sourceType" select="@sourceType"/>
								<xsl:with-param name="paramName" select="$paramName"/>
								<xsl:with-param name="displayVal" select="$displayValue"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$displayValue"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="DisplayChoices/DisplayEntry[Value=$Val]">
						<xsl:choose>
							<xsl:when test="@sourceType">
								<xsl:call-template name="getLocaleDisplayLabel">
									<xsl:with-param name="sourceType" select="@sourceType"/>
									<xsl:with-param name="paramName" select="$paramName"/>
									<xsl:with-param name="displayVal" select="DisplayLabel"/>
								</xsl:call-template>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="DisplayLabel"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</div>
		<input type="hidden" name="{@paramName}">
			<xsl:attribute name="value"><xsl:choose><xsl:when test="Value!=''"><xsl:value-of select="Value"/></xsl:when><xsl:when test="DisplayChoices/DisplayEntry[@selected='yes']"><xsl:value-of select="DisplayChoices/DisplayEntry[@selected='yes']/Value"/></xsl:when><xsl:otherwise><xsl:value-of select="DisplayChoices/DisplayEntry/Value"/></xsl:otherwise></xsl:choose></xsl:attribute>
		</input>
	</xsl:template>
	<!-- 
     sys_CheckBoxGroup
  -->
	<psxctl:ControlMeta name="sys_CheckBoxGroup" dimension="array" choiceset="required">
		<psxctl:Description>a group of check boxes with the same HTML param name</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="columncount" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the number of column(s) displayed.</psxctl:Description>
				<psxctl:DefaultValue>1</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="columnwidth" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the width of the column in pixels or percentage.</psxctl:Description>
				<psxctl:DefaultValue>100%</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="selectall.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/selectall.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_CheckBoxGroup']" priority="10" mode="psxcontrol">
		<!-- both a local/global <DisplayChoices> and an external <DisplayChoices> can be in the same control
        (in case you want to hardcode a few, and have the rest dynamic) -->
		<xsl:variable name="columncount">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='columncount']">
					<xsl:value-of select="ParamList/Param[@name='columncount']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxGroup']/psxctl:ParamList/psxctl:Param[@name='columncount']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="columnwidth">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='columnwidth']">
					<xsl:value-of select="ParamList/Param[@name='columnwidth']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CheckBoxGroup']/psxctl:ParamList/psxctl:Param[@name='columnwidth']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$columncount > 1">
				<xsl:apply-templates select="DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
					<xsl:with-param name="Control" select="."/>
					<xsl:with-param name="columncount" select="$columncount"/>
					<xsl:with-param name="columnwidth" select="$columnwidth"/>
				</xsl:apply-templates>
				<xsl:for-each select="DisplayChoices">
					<xsl:if test="string(@href)">
						<xsl:apply-templates select="document(@href)/*/DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
							<xsl:with-param name="Control" select="."/>
							<xsl:with-param name="columncount" select="$columncount"/>
							<xsl:with-param name="columnwidth" select="$columnwidth"/>
						</xsl:apply-templates>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="DisplayChoices" mode="psxcontrol-syscheckboxgroup-onecolumn">
					<xsl:with-param name="Control" select="."/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:if test="@isReadOnly !='yes'">
			<!-- syntax checkAll(document.EditForm.$fieldnanme) -->
			<a href="#">
				<xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOcheckAll(document.EditForm.]]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[)]]><xsl:text/></xsl:attribute>Check All</a>
			<xsl:text>&#160; &#160;</xsl:text>
			<a href="#">
				<xsl:attribute name="href"><xsl:text disable-output-escaping="yes"><![CDATA[javascript:PSOuncheckAll(document.EditForm.]]></xsl:text><xsl:value-of select="@paramName"/><![CDATA[)]]><xsl:text/></xsl:attribute>Uncheck All</a>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-syscheckboxgroup-onecolumn">
		<xsl:param name="Control"/>
		<xsl:apply-templates select="DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
			<xsl:with-param name="Control" select="$Control"/>
		</xsl:apply-templates>
		<xsl:if test="string(@href)">
			<!-- external xml is assumed to use a DTD compatible with rx_ContentEditor.dtd
           (namely that items are in <DisplayEntry>s) -->
			<xsl:apply-templates select="document(@href)/*/DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
				<xsl:with-param name="Control" select="$Control"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-onecolumn">
		<!-- Control is a reference to the parent node
        (supplied in case we are processing nodes from external source) -->
		<xsl:param name="Control"/>
		<div class="datadisplay">
			<xsl:choose>
				<xsl:when test="$Control/@isReadOnly = 'yes'">
					<xsl:choose>
						<xsl:when test="@selected = 'yes'">
							<img src="../sys_resources/images/checked.gif" height="16" width="16"/>
							<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>
						</xsl:when>
						<xsl:otherwise>
							<img src="../sys_resources/images/unchecked.gif" height="16" width="16"/>
						</xsl:otherwise>
					</xsl:choose>
         &nbsp;<xsl:value-of select="DisplayLabel"/>
				</xsl:when>
				<xsl:otherwise>
					<input name="{$Control/@paramName}" type="checkbox" value="{Value}">
						<xsl:if test="@accessKey!=''">
							<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
						</xsl:if>
						<xsl:call-template name="parametersToAttributes">
							<xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
							<xsl:with-param name="controlNode" select="$Control"/>
						</xsl:call-template>
						<xsl:if test="@selected = 'yes'">
							<xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
						</xsl:if>
						<xsl:value-of select="DisplayLabel"/>
					</input>
					<br/>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>
	<xsl:template match="DisplayChoices" mode="psxcontrol-syscheckboxgroup-ncolumn">
		<xsl:param name="Control"/>
		<xsl:param name="columncount"/>
		<xsl:param name="columnwidth"/>
		<xsl:variable name="DisplayEntrycount" select="number(count(DisplayEntry))"/>
		<xsl:variable name="DisplayEntrycountreadonly" select="number(count(DisplayEntry[@selected = 'yes']))"/>
		<xsl:choose>
			<!-- READ ONLY MODE -->
			<xsl:when test="$Control/@isReadOnly = 'yes'">
				<table width="100%" cellpadding="0" cellspacing="0" border="0">
					<tr class="headercell2">
						<td valign="top">
							<table border="0">
								<xsl:apply-templates select="DisplayEntry[position() mod $columncount = 1]" mode="psxcontrol-syscheckboxgroup-newrow">
									<xsl:with-param name="Control" select="$Control"/>
									<xsl:with-param name="columncount" select="$columncount"/>
									<xsl:with-param name="readonly" select="'yes'"/>
									<xsl:with-param name="columnwidth" select="$columnwidth"/>
								</xsl:apply-templates>
							</table>
						</td>
					</tr>
				</table>
			</xsl:when>
			<!-- EDIT ONLY MODE -->
			<xsl:otherwise>
				<table width="100%" cellpadding="0" cellspacing="0" border="0">
					<tr class="headercell2">
						<td valign="top">
							<table border="0">
								<xsl:apply-templates select="DisplayEntry[position() mod $columncount = 1]" mode="psxcontrol-syscheckboxgroup-newrow">
									<xsl:with-param name="Control" select="$Control"/>
									<xsl:with-param name="columncount" select="$columncount"/>
									<xsl:with-param name="columnwidth" select="$columnwidth"/>
								</xsl:apply-templates>
							</table>
						</td>
					</tr>
				</table>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-newrow">
		<xsl:param name="Control"/>
		<xsl:param name="columncount"/>
		<xsl:param name="readonly"/>
		<xsl:param name="columnwidth"/>
		<tr>
			<td class="datadisplay" valign="top" align="left" width="{$columnwidth}">
				<xsl:choose>
					<xsl:when test="$readonly='yes' and @selected = 'yes'">
						<img src="../sys_resources/images/checked.gif" height="16" width="16"/>
						<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
					</xsl:when>
					<xsl:when test="$readonly='yes' and @selected != 'yes'">
						<img src="../sys_resources/images/unchecked.gif" height="16" width="16"/>
						<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
					</xsl:when>
					<xsl:otherwise>
						<input name="{$Control/@paramName}" type="checkbox" value="{Value}">
							<xsl:if test="@accessKey!=''">
								<xsl:attribute name="accesskey"><xsl:call-template name="getaccesskey"><xsl:with-param name="label" select="preceding-sibling::DisplayLabel"/><xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType"/><xsl:with-param name="paramName" select="@paramName"/><xsl:with-param name="accessKey" select="@accessKey"/></xsl:call-template></xsl:attribute>
							</xsl:if>
							<xsl:call-template name="parametersToAttributes">
								<xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
								<xsl:with-param name="controlNode" select="$Control"/>
							</xsl:call-template>
							<xsl:if test="@selected = 'yes'">
								<xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="DisplayLabel"/>
						</input>
					</xsl:otherwise>
				</xsl:choose>
			</td>
			<xsl:apply-templates select="following-sibling::DisplayEntry" mode="psxcontrol-syscheckboxgroup-ncolumns">
				<xsl:with-param name="Control" select="$Control"/>
				<xsl:with-param name="columncount" select="$columncount"/>
				<xsl:with-param name="readonly" select="$readonly"/>
				<xsl:with-param name="columnwidth" select="$columnwidth"/>
			</xsl:apply-templates>
		</tr>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-syscheckboxgroup-ncolumns">
		<xsl:param name="Control"/>
		<xsl:param name="columncount"/>
		<xsl:param name="readonly"/>
		<xsl:param name="columnwidth"/>
		<xsl:if test="position() &lt; $columncount">
			<td class="datadisplay" valign="top" align="left" width="{$columnwidth}">
				<xsl:choose>
					<xsl:when test="$readonly='yes' and @selected = 'yes'">
						<img src="../sys_resources/images/checked.gif" height="16" width="16"/>
						<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
					</xsl:when>
					<xsl:when test="$readonly='yes' and @selected != 'yes'">
						<img src="../sys_resources/images/unchecked.gif" height="16" width="16"/>
						<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>&nbsp;<xsl:value-of select="DisplayLabel"/>
					</xsl:when>
					<xsl:otherwise>
						<input name="{$Control/@paramName}" type="checkbox" value="{Value}">
							<xsl:if test="@accessKey!=''">
								<xsl:attribute name="accesskey"><xsl:value-of select="@accessKey"/></xsl:attribute>
							</xsl:if>
							<xsl:call-template name="parametersToAttributes">
								<xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
								<xsl:with-param name="controlNode" select="$Control"/>
							</xsl:call-template>
							<xsl:if test="@selected = 'yes'">
								<xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
							</xsl:if>
							<xsl:value-of select="DisplayLabel"/>
						</input>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</xsl:if>
	</xsl:template>
	<!-- 
     sys_TextArea

<!ATTLIST textarea
  %attrs;
  name        CDATA          #IMPLIED
  rows        %Number;       #REQUIRED
  cols        %Number;       #REQUIRED
  disabled    (disabled)     #IMPLIED
  readonly    (readonly)     #IMPLIED
  tabindex    %Number;       #IMPLIED
  accesskey   %Character;    #IMPLIED
  onfocus     %Script;       #IMPLIED
  onblur      %Script;       #IMPLIED
  onselect    %Script;       #IMPLIED
  onchange    %Script;       #IMPLIED
  >
  -->
	<psxctl:ControlMeta name="sys_TextArea" dimension="single" choiceset="none">
		<psxctl:Description>A simple text area</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="rows" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the number of visible text lines. The default value is 4.</psxctl:Description>
				<psxctl:DefaultValue>4</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cols" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the visible width in average character widths. The default value is 80.</psxctl:Description>
				<psxctl:DefaultValue>80</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_TextArea']" mode="psxcontrol">
		<textarea name="{@paramName}" wrap="soft">
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_TextArea'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
			<xsl:value-of select="Value"/>
		</textarea>
	</xsl:template>
	<!-- 
     sys_CalendarSimple
  -->
	<psxctl:ControlMeta name="sys_CalendarSimple" dimension="single" choiceset="none">
		<psxctl:Description>A input box with icon to pop-up calendar picker</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="alt" datatype="String" paramtype="img">
				<psxctl:Description>This parameter specifies alternate text the calendar picker icon, for user agents that cannot display images. The default value is "Calendar Pop-up"</psxctl:Description>
				<psxctl:DefaultValue>Calendar Pop-up</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="src" datatype="String" paramtype="img">
				<psxctl:Description>This parameter specifies the location of the image resource used for the calendar picker icon. The default value is "../sys_resources/images/cal.gif"</psxctl:Description>
				<psxctl:DefaultValue>../sys_resources/images/cal.gif</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="height" datatype="String" paramtype="img">
				<psxctl:Description>This parameter specifies the height of the calendar picker icon. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 20.</psxctl:Description>
				<psxctl:DefaultValue>20</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="width" datatype="String" paramtype="img">
				<psxctl:Description>This parameter specifies the width of the calendar picker icon. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is 20.</psxctl:Description>
				<psxctl:DefaultValue>20</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="formname" datatype="String" paramtype="jscript">
				<psxctl:Description>This parameter specifies the name of the form that contains this control. It is used by the calendar's JavaScript. The default value is "EditForm"</psxctl:Description>
				<psxctl:DefaultValue>EditForm</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="time" datatype="String" paramtype="jscript">
				<psxctl:Description>This parameter specifies whether time is to be displayed or not.If 0 then no time.</psxctl:Description>
				<psxctl:DefaultValue>0</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="calPopup.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/calPopup.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_CalendarSimple']" mode="psxcontrol">
		<input type="text" name="{@paramName}" value="{Value}">
			<xsl:if test="@accessKey!=''">
				<xsl:attribute name="accesskey"><xsl:value-of select="@accessKey"/></xsl:attribute>
			</xsl:if>
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_CalendarSimple'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
		</input>&#160;
      <xsl:variable name="formname">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='formname']">
					<xsl:value-of select="ParamList/Param[@name='formname']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CalendarSimple']/psxctl:ParamList/psxctl:Param[@name='formname']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="time">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='time']">
					<xsl:value-of select="ParamList/Param[@name='time']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_CalendarSimple']/psxctl:ParamList/psxctl:Param[@name='time']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="timetemp">
			<xsl:choose>
				<xsl:when test="$time='yes'">
					<xsl:value-of select="'1'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'0'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<a href="javascript:doNothing()">
			<xsl:attribute name="onclick"><xsl:value-of select="concat('showCalendar(document.',$formname,'.',@paramName,',',$timetemp,');')"/></xsl:attribute>
			<img border="0">
				<xsl:call-template name="parametersToAttributes">
					<xsl:with-param name="controlClassName" select="'sys_CalendarSimple'"/>
					<xsl:with-param name="controlNode" select="."/>
					<xsl:with-param name="paramType" select="'img'"/>
				</xsl:call-template>
			</img>
		</a>
	</xsl:template>
	<!-- 
     sys_HtmlEditor
  -->
	<psxctl:ControlMeta name="sys_HtmlEditor" dimension="single" choiceset="none" deprecate="yes" replacewith="sys_eWebEditPro">
		<psxctl:Description>WYSIWYG HTML Editor</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="NAME" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to the inline frame. The default value is "dynamsg".</psxctl:Description>
				<psxctl:DefaultValue>dynamsg</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="width" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the width of the inline frame. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
				<psxctl:DefaultValue>100%</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="height" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the height of the inline frame. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 250.</psxctl:Description>
				<psxctl:DefaultValue>250</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="SCROLLING" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies scrolling information for the inline frame.  The default value is "auto".</psxctl:Description>
				<psxctl:DefaultValue>auto</psxctl:DefaultValue>
				<psxctl:ChoiceList>
					<psxctl:Entry>auto</psxctl:Entry>
					<psxctl:Entry>yes</psxctl:Entry>
					<psxctl:Entry>no</psxctl:Entry>
				</psxctl:ChoiceList>
			</psxctl:Param>
			<psxctl:Param name="SRC" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the location of the HTML that will populate the inline frame. The default value is "../sys_resources/texteditor/deditor.html".</psxctl:Description>
				<psxctl:DefaultValue>../sys_resources/texteditor/deditor.html</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="formname" datatype="String" paramtype="jscript">
				<psxctl:Description>This parameter specifies the name of the form that contains this control. It is used by the editor's JavaScript. The default value is "EditForm"</psxctl:Description>
				<psxctl:DefaultValue>EditForm</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
				<psxctl:DefaultValue>103</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
				<psxctl:DefaultValue>104</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineVariantSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inlinevariant slot. The inline search dialog box shows the content types that have at least one variant added to the inline variant slot. The default value is system inline variant slotid 105.</psxctl:Description>
				<psxctl:DefaultValue>105</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="textedit.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/textedit.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
		<psxctl:Dependencies>
			<psxctl:Dependency status="setupOptional" occurrence="multiple">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/xmldom/sys_xdTextCleanup</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>$(fieldName)</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>rxW2Ktidy.properties</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>rxW2KserverPageTags.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>yes</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
	</psxctl:ControlMeta>
	<!-- form-onsubmit functions must return true or the submit will be cancelled -->
	<xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol-form-onsubmit">
		<!-- Check for IE first -->
		<xsl:choose>
			<xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE') and ../@displayType!='sys_hidden'">
				<xsl:value-of select="concat(' &amp;&amp; set',@paramName,'()')"/>
			</xsl:when>
			<!-- If not IE -->
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol-body-onload">
		<!-- Check for IE first -->
		<xsl:choose>
			<xsl:when test="not(contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')) and contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE') and ../@displayType!='sys_hidden'">
				<xsl:value-of select="concat('get',@paramName,'();')"/>
			</xsl:when>
			<!-- If not IE -->
			<xsl:otherwise/>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="Control[@name='sys_HtmlEditor']" mode="psxcontrol">
		<!-- Check for IE first -->
		<!-- set up the variables that will be used in the javascript -->
		<xsl:variable name="name">
			<xsl:value-of select="@paramName"/>
		</xsl:variable>
		<xsl:variable name="formname">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='formname']">
					<xsl:value-of select="ParamList/Param[@name='formname']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_HtmlEditor']/psxctl:ParamList/psxctl:Param[@name='formname']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="id">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='NAME']">
					<xsl:value-of select="ParamList/Param[@name='NAME']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_HtmlEditor']/psxctl:ParamList/psxctl:Param[@name='NAME']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineLinkSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineImageSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineImageSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineVariantSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineVariantSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineVariantSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineVariantSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<script >
   function set<xsl:value-of select="$name"/>() {
      if(<xsl:value-of select="$id"/>.document.all.switchMode.checked){
         <xsl:value-of select="$id"/>.document.all.switchMode.checked = false;
         <xsl:value-of select="$id"/>.setMode(false);
      }
      <xsl:value-of select="$formname"/>.<xsl:value-of select="$name"/>.value = <xsl:value-of select="$id"/>.window.Composition.document.body.innerHTML;
      return true;
   }
   function get<xsl:value-of select="$name"/>() {
      <xsl:value-of select="$id"/>.window.Composition.document.InlineLinkSlot = "<xsl:value-of select="$InlineLinkSlot"/>";
	   <xsl:value-of select="$id"/>.window.Composition.document.InlineImageSlot = "<xsl:value-of select="$InlineImageSlot"/>";
	   <xsl:value-of select="$id"/>.window.Composition.document.InlineVariantSlot = "<xsl:value-of select="$InlineVariantSlot"/>";
      <xsl:value-of select="$id"/>.window.Composition.document.editorName = "<xsl:value-of select="@paramName"/>";
      <xsl:value-of select="$id"/>.window.Composition.document.body.innerHTML = <xsl:value-of select="$formname"/>.<xsl:value-of select="$name"/>.value;
   }
   </script>
		<xsl:choose>
			<xsl:when test="contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'Mac')">
				<!-- This is only a temporary arrangment. Permanent fix should use sys_Textarea control -->
				<textarea cols="50" rows="15" wrap="soft">
					<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
					<xsl:value-of select="Value"/>
				</textarea>
			</xsl:when>
			<xsl:when test="contains(/ContentEditor/UserStatus/RequestProperties/UserAgent, 'MSIE')">
				<!-- If not a Mac or NN -->
				<IFrame>
					<xsl:call-template name="parametersToAttributes">
						<xsl:with-param name="controlClassName" select="'sys_HtmlEditor'"/>
						<xsl:with-param name="controlNode" select="."/>
					</xsl:call-template>
				</IFrame>
				<input type="hidden" name="{$name}" value="{Value}"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- This is only a temporary arrangment. Permanent fix should use sys_Textarea control -->
				<textarea cols="50" rows="15" wrap="soft">
					<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
					<xsl:value-of select="Value"/>
				</textarea>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- 
     sys_eWebEditPro
  -->
	<psxctl:ControlMeta name="sys_eWebEditPro" dimension="single" choiceset="none">
		<psxctl:Description>WYSIWYG HTML Editor</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="width" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the width of the inline frame. This parameter may be either a pixel or a percentage of the available horizontal space. The default value is "700".</psxctl:Description>
				<psxctl:DefaultValue>760</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="height" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the height of the inline frame. This parameter may be either a pixel or a percentage of the available vertical space. The default value is 250.</psxctl:Description>
				<psxctl:DefaultValue>250</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="config_src_url" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the location of the config.xml that will the control will use for configuration. The default value is "../rx_resources/ewebeditpro/config.xml".</psxctl:Description>
				<psxctl:DefaultValue>../rx_resources/ewebeditpro/config.xml</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
				<psxctl:DefaultValue>103</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
				<psxctl:DefaultValue>104</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineVariantSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline variant slot. The inline search dialog box shows the content types that have at least one variant added to the inline variant slot. The default value is system inline variant slotid 105.</psxctl:Description>
				<psxctl:DefaultValue>105</psxctl:DefaultValue>
			</psxctl:Param>
			<!-- will be enabled when Ektron ships the java version -->
			<!-- psxctl:Param name="JavaAutoDetect" datatype="String" paramtype="generic">
      <psxctl:Description>This parameter indicates whether to used the ActiveX control or Java control.  If yes, this will sniff and only if ActiveX is not available, it will use the Java control.  If no it will always used the Java control.  If no value is entered, ActiveX will always be used.</psxctl:Description>
      <psxctl:DefaultValue/> 
      <psxctl:ChoiceList>
         <psxctl:Entry>yes</psxctl:Entry>
         <psxctl:Entry>no</psxctl:Entry>        
      </psxctl:ChoiceList>
   </psxctl:Param -->
		</psxctl:ParamList>
		<psxctl:AssociatedFileList>
			<psxctl:FileDescriptor name="ewebeditpro.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../rx_resources/ewebeditpro/ewebeditpro.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
			<psxctl:FileDescriptor name="rx_wep.js" type="script" mimetype="text/javascript">
				<psxctl:FileLocation>../sys_resources/js/ewebeditpro/rx_wep.js</psxctl:FileLocation>
				<psxctl:Timestamp/>
			</psxctl:FileDescriptor>
		</psxctl:AssociatedFileList>
		<psxctl:Dependencies>
			<psxctl:Dependency status="setupOptional" occurrence="multiple">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/xmldom/sys_xdTextCleanup</name>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>$(fieldName)</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>rxW2Ktidy.properties</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>rxW2KserverPageTags.xml</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text/>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
						<PSXExtensionParamValue id="0">
							<value>
								<PSXTextLiteral id="0">
									<text>yes</text>
								</PSXTextLiteral>
							</value>
						</PSXExtensionParamValue>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_eWebEditPro']" priority="10" mode="psxcontrol">
		<!-- Check for IE first -->
		<!-- set up the variables that will be used in the javascript -->
		<xsl:variable name="name">
			<xsl:value-of select="@paramName"/>
		</xsl:variable>
		<xsl:variable name="width">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='width']">
					<xsl:value-of select="ParamList/Param[@name='width']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='width']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="height">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='height']">
					<xsl:value-of select="ParamList/Param[@name='height']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='height']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="config_src_url">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='config_src_url']">
					<xsl:value-of select="ParamList/Param[@name='config_src_url']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='config_src_url']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="JavaAutoDetect">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='JavaAutoDetect']">
					<xsl:value-of select="ParamList/Param[@name='JavaAutoDetect']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='JavaAutoDetect']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineLinkSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineImageSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineImageSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineVariantSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineVariantSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineVariantSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('')/*/psxctl:ControlMeta[@name='sys_eWebEditPro']/psxctl:ParamList/psxctl:Param[@name='InlineVariantSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<input type="hidden" name="{@paramName}" value="{Value}"/>
		<script language="JavaScript">
			<xsl:if test="@isReadOnly='yes'">
	eWebEditPro.parameters.readOnly = true;
      		</xsl:if><![CDATA[eWebEditPro.parameters.config = "]]><xsl:value-of select="$config_src_url"/><![CDATA[";
	var ]]><xsl:value-of select="@paramName"/><![CDATA[_InlineLinkSlot = ]]><xsl:value-of select="$InlineLinkSlot"/><![CDATA[;
	var ]]><xsl:value-of select="@paramName"/><![CDATA[_InlineImageSlot = ]]><xsl:value-of select="$InlineImageSlot"/><![CDATA[;
	var ]]><xsl:value-of select="@paramName"/><![CDATA[_InlineVariantSlot = ]]><xsl:value-of select="$InlineVariantSlot"/><![CDATA[;
	var ]]><xsl:value-of select="@paramName"/><![CDATA[_ReadOnly = ']]><xsl:value-of select="@isReadOnly"/><![CDATA[';
      // When Java becomes available use this command:
      // eWebEditPro.isActiveXSupported="]]><xsl:value-of select="$JavaAutoDetect"/><![CDATA[";
      eWebEditPro.parameters.locale = "../rx_resources/ewebeditpro/]]><xsl:call-template name="getEktronLangFile">
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template><![CDATA[";

      eWebEditPro.create("]]><xsl:value-of select="$name"/><![CDATA[", "]]><xsl:value-of select="$width"/><![CDATA[", ]]><xsl:value-of select="$height"/><![CDATA[);

      eWebEditPro.load();
      eWebEditPro.onready="ps_hasEktron=true; window.focus()";
   ]]></script>
	</xsl:template>
	<!-- END sys_eWebEditPro -->
	<!-- 
     sys_Table: needs a higher priority than the default read-only template

<!ATTLIST table
  %attrs;
  summary     %Text;         #IMPLIED
  width       %Length;       #IMPLIED
  border      %Pixels;       #IMPLIED
  frame       %TFrame;       #IMPLIED
  rules       %TRules;       #IMPLIED
  cellspacing %Length;       #IMPLIED
  cellpadding %Length;       #IMPLIED
  >
  -->
	<psxctl:ControlMeta name="sys_Table" dimension="table" choiceset="none">
		<psxctl:Description>A simple table</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="summary" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter provides a summary of the table's purpose and structure for user agents rendering to non-visual media such as speech and Braille.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="width" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the desired width of the entire table.  This parameter may be either a number of pixels or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
				<psxctl:DefaultValue>100%</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cellspacing" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies how much space the user agent should leave between the left side of the table and the left-hand side of the leftmost column, the top of the table and the top side of the topmost row, and so on for the right and bottom of the table. The attribute also specifies the amount of space to leave between cells.  The default value is 0.</psxctl:Description>
				<psxctl:DefaultValue>0</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cellpadding" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the amount of space between the border of the cell and its contents. If the value of this attribute is a pixel length, all four margins should be this distance from the contents. If the value of the attribute is a percentage length, the top and bottom margins should be equally separated from the content based on a percentage of the available vertical space, and the left and right margins should be equally separated from the content based on a percentage of the available horizontal space. The default value is 5.</psxctl:Description>
				<psxctl:DefaultValue>5</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="border" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the width (in pixels only) of the frame around the table. The default value is 1.</psxctl:Description>
				<psxctl:DefaultValue>1</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_Table']" priority="10" mode="psxcontrol">
		<table border="0" cellpadding="0" cellspacing="0">
			<xsl:call-template name="parametersToAttributes">
				<xsl:with-param name="controlClassName" select="'sys_Table'"/>
				<xsl:with-param name="controlNode" select="."/>
			</xsl:call-template>
			<thead>
				<xsl:apply-templates select="Table/Header" mode="inside"/>
			</thead>
			<tbody>
				<xsl:apply-templates select="Table/RowData/Row" mode="inside"/>
			</tbody>
		</table>
		<table width="100%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td align="center">
					<xsl:apply-templates select="Table/ActionLinkList" mode="actionlist"/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="Header" mode="inside">
		<tr>
			<xsl:for-each select="HeaderColumn">
				<th class="headercell">
               <span class="datadisplay"><xsl:value-of select="."/></span>
				</th>
			</xsl:for-each>
			<xsl:if test="../RowData/Row/ActionLinkList">
            <th class="headercell"><span class="datadisplay">Action</span></th>
			</xsl:if>
		</tr>
	</xsl:template>
	<xsl:template match="Row" mode="inside">
		<tr>
			<xsl:for-each select="Column">
            <td>
					<xsl:apply-templates select="Control" mode="psxcontrol"/>
				</td>
			</xsl:for-each>
			<xsl:if test="ActionLinkList">
            <td>
					<xsl:apply-templates select="ActionLinkList" mode="actionlist"/>
				</td>
			</xsl:if>
		</tr>
	</xsl:template>
	<!-- OLD CODE
   apply any control parameter defaults defined in the metadata 
   <xsl:apply-templates select="document('')/*/psxctl:ControlMeta[@name='sys_EditBox']/psxctl:ParamList/psxctl:Param[psxctl:DefaultValue]" mode="internal"/>
   apply control parameters that have been defined in the metadata (will override defaults) 
   <xsl:apply-templates select="ParamList/Param[@name = document('')/*/psxctl:ControlMeta[@name='sys_EditBox']/psxctl:ParamList/psxctl:Param/@name]" mode="internal"/>
   -->
	<!-- suppress text nodes in these modes, but keep walking element nodes -->
	<xsl:template match="*" mode="psxcontrol-body-onload">
		<xsl:apply-templates select="*" mode="psxcontrol-body-onload"/>
	</xsl:template>
	<xsl:template match="*" mode="psxcontrol-form-onsubmit">
		<xsl:apply-templates select="*" mode="psxcontrol-form-onsubmit"/>
	</xsl:template>
	<!-- 
     sys_RelatedContentTable

-->
	<psxctl:ControlMeta name="sys_RelatedContentTable" dimension="table" choiceset="none">
		<psxctl:Description>A related content table</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="summary" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter provides a summary of the table's purpose and structure for user agents rendering to non-visual media such as speech and Braille.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="width" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the desired width of the entire table.  This parameter may be either a number of pixels or a percentage of the available horizontal space. The default value is "100%".</psxctl:Description>
				<psxctl:DefaultValue>100%</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cellspacing" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies how much space the user agent should leave between the left side of the table and the left-hand side of the leftmost column, the top of the table and the top side of the topmost row, and so on for the right and bottom of the table. The attribute also specifies the amount of space to leave between cells.  The default value is 0.</psxctl:Description>
				<psxctl:DefaultValue>0</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="cellpadding" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the amount of space between the border of the cell and its contents. If the value of this attribute is a pixel length, all four margins should be this distance from the contents. If the value of the attribute is a percentage length, the top and bottom margins should be equally separated from the content based on a percentage of the available vertical space, and the left and right margins should be equally separated from the content based on a percentage of the available horizontal space. The default value is 5.</psxctl:Description>
				<psxctl:DefaultValue>5</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="border" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the width (in pixels only) of the frame around the table. The default value is 1.</psxctl:Description>
				<psxctl:DefaultValue>1</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_RelatedContentTable']" priority="10" mode="psxcontrol">
		<!-- if the following condition is true old related item table is put -->
		<xsl:if test="not(contains(/ContentEditor/SectionLinkList/SectionLink[@name='RelatedLookupURL'], 'sys_rcSupport'))">
			<table width="100%" cellpadding="4" cellspacing="0" border="0">
				<tr>
					<td class="headercell2">
						<table width="100%" cellpadding="0" cellspacing="0" border="0">
							<tr>
								<td class="backgroundcolor">
									<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
										<tr class="headercell">
											<td class="headercell2font" align="center">Title</td>
											<td class="headercell2font" align="center">Type</td>
											<td class="headercell2font" align="center">Variant</td>
											<td class="headercell2font" align="center">Slot</td>
											<td class="headercell2font" align="center">&#160;</td>
											<xsl:if test="Table/RowData/Row/ActionLinkList">
												<td class="headercellfont" align="center">Action</td>
											</xsl:if>
										</tr>
										<xsl:apply-templates select="Table/RowData/Row" mode="Row"/>
									</table>
									<table width="100%" border="0" cellpadding="0" cellspacing="0">
										<tr class="headercell2">
											<td align="center">
												<xsl:apply-templates select="Table/ActionLinkList" mode="actionlist"/>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</xsl:if>
	</xsl:template>
	<xsl:template match="Row" mode="Row">
		<xsl:variable name="RelatedURL">
			<!--<xsl:value-of select="concat('http://38.164.160.65:9992/Rhythmyx/rx_ceRelatedContentSearch/relatedcontentlookup.xml','?sysid=',Column/Control[@paramName='sysid']/Value)" />-->
			<xsl:value-of select="concat(/ContentEditor/SectionLinkList/SectionLink[@name='RelatedLookupURL'],'&amp;sysid=',Column/Control[@paramName='sysid']/Value)"/>
		</xsl:variable>
		<tr class="datacell1">
			<xsl:apply-templates select="document($RelatedURL)" mode="RowData"/>
			<xsl:if test="ActionLinkList">
				<td class="datacell2">
					<xsl:apply-templates select="ActionLinkList" mode="actionlist">
						<xsl:with-param name="separator" select="'&nbsp;'"/>
					</xsl:apply-templates>
				</td>
			</xsl:if>
		</tr>
	</xsl:template>
	<xsl:template match="/RelatedContentPrevew/item" mode="RowData">
		<td class="datacell1font" valign="top" align="left">
			<xsl:value-of select="concat(titles,'(',titles/@id,')')"/>&#160;</td>
		<td class="datacell1font" valign="top" align="left">
			<xsl:value-of select="type"/>&#160;</td>
		<td class="datacell1font" valign="top" align="left">
			<xsl:value-of select="variant"/>&#160;</td>
		<td class="datacell1font" valign="top" align="left">
			<xsl:value-of select="slot"/>&#160;</td>
		<td class="datacell1font" valign="top" align="center">
			<a href="{previewurl}" target="_blank">
				<img src="../sys_resources/images/eye.gif" alt="Preview" align="top" width="16" height="16" border="0"/>
			</a>
		</td>
	</xsl:template>
	<!-- 
     sys_VariantDropDown control

-->
	<!--

<!ATTLIST select
%attrs;
name        CDATA          #IMPLIED
size        %Number;       #IMPLIED
multiple    (multiple)     #IMPLIED
disabled    (disabled)     #IMPLIED
tabindex    %Number;       #IMPLIED
onfocus     %Script;       #IMPLIED
onblur      %Script;       #IMPLIED
onchange    %Script;       #IMPLIED
>
  -->
	<psxctl:ControlMeta name="sys_VariantDropDown" dimension="single" choiceset="optional" deprecated="yes">
		<psxctl:Description>a drop down combo box for selecting a single variant</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to the inline frame element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to the inline frame element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the inline frame element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>If the element is presented as a scrolled list box, This parameter specifies the number of rows in the list that should be visible at the same time.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="multiple" datatype="String" paramtype="generic">
				<psxctl:Description>If set, this boolean attribute allows multiple selections. If not set, the element only permits single selections.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="disabled" datatype="String" paramtype="generic">
				<psxctl:Description>If set, this boolean attribute disables the control for user input.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="OutputFormat" datatype="String" paramtype="custom">
				<psxctl:Description>This parameter selects the output format (snippet or page).</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_VariantDropDown']" mode="psxcontrol">
		<xsl:variable name="OutputFormat">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='OutputFormat']">
					<xsl:value-of select="ParamList/Param[@name='OutputFormat']"/>
				</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="LookupURL">
			<xsl:value-of select="concat(DisplayChoices/@href,'&amp;outputformat=',$OutputFormat,'&amp;contenttypeid=',/ContentEditor/@contentTypeId)"/>
		</xsl:variable>
		<div>
			<select name="{@paramName}">
				<xsl:call-template name="parametersToAttributes">
					<xsl:with-param name="controlClassName" select="'sys_VariantDropDown'"/>
					<xsl:with-param name="controlNode" select="."/>
				</xsl:call-template>
				<xsl:apply-templates select="document($LookupURL)/*/item" mode="psxcontrol-variantdropdownsingle">
					<xsl:with-param name="controlValue" select="Value"/>
				</xsl:apply-templates>
			</select>
		</div>
	</xsl:template>
	<xsl:template match="item" mode="psxcontrol-variantdropdownsingle">
		<xsl:param name="controlValue"/>
		<option value="{value}">
			<xsl:if test="value = $controlValue">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@selected='yes'">
				<xsl:attribute name="selected"><xsl:value-of select="'selected'"/></xsl:attribute>
			</xsl:if>
			<xsl:value-of select="display"/>
		</option>
	</xsl:template>
	<xsl:template match="Control[@name='sys_VariantDropDown' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<xsl:variable name="OutputFormat">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='OutputFormat']">
					<xsl:value-of select="ParamList/Param[@name='OutputFormat']"/>
				</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="LookupURL">
			<xsl:value-of select="concat(DisplayChoices/@href,'&amp;outputformat=',$OutputFormat,'&amp;contenttypeid=',/ContentEditor/@contentTypeId)"/>
		</xsl:variable>
		<xsl:variable name="Val" select="Value"/>
		<div class="datadisplay">
			<xsl:value-of select="document($LookupURL)/*/item[value=$Val]/display"/>
		</div>
	</xsl:template>
	<!--
     sys_GenericPageError

     Displays the generic page validation error message on top of the form.
 -->
	<xsl:template name="sys_GenericPageError">
		<xsl:for-each select="DisplayError">
			<tr>
				<td class="headercell2">
					<table width="100%" cellpadding="4" cellspacing="0" border="0">
						<td>
							<table width="100%" cellpadding="0" cellspacing="1" border="0" class="backgroundcolor">
								<tr class="headercell">
									<td class="headercell2errorfont">
										<xsl:value-of select="GenericMessage"/>
									</td>
								</tr>
								<xsl:for-each select="Details/FieldError">
									<tr>
										<td>
											<xsl:if test="@submitName!=''">
												<xsl:variable name="subName" select="@submitName"/>
												<xsl:variable name="displayName" select="@displayName"/>
												<xsl:variable name="dfield" select="//ItemContent/DisplayField[Control/@paramName=$subName]"/>
												<table width="100%" cellpadding="0" cellspacing="0" border="0" class="backgroundcolor">
													<tr>
														<td width="20%" class="headererrorcell">
															<xsl:variable name="keyval">
																<xsl:choose>
																	<xsl:when test="$dfield/DisplayLabel/@sourceType='sys_system'">
																		<xsl:value-of select="concat('psx.ce.system.', $dfield/Control/@paramName, '@', $displayName)"/>
																	</xsl:when>
																	<xsl:when test="$dfield/DisplayLabel/@sourceType='sys_shared'">
																		<xsl:value-of select="concat('psx.ce.shared.', $dfield/Control/@paramName, '@', $displayName)"/>
																	</xsl:when>
																	<xsl:otherwise>
																		<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $dfield/Control/@paramName, '@', $displayName)"/>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:variable>
															<xsl:call-template name="getLocaleString">
																<xsl:with-param name="key" select="$keyval"/>
																<xsl:with-param name="lang" select="$lang"/>
															</xsl:call-template>
                                             &#160;
                                          </td>
														<td class="headererrorcell">
															<xsl:if test=".!=''">
																<xsl:call-template name="getLocaleString">
																	<xsl:with-param name="key" select="concat('psx.ce.error@',.)"/>
																	<xsl:with-param name="lang" select="$lang"/>
																</xsl:call-template>
															</xsl:if>
														</td>
													</tr>
												</table>
											</xsl:if>
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</td>
					</table>
				</td>
			</tr>
		</xsl:for-each>
	</xsl:template>
	<!--
     sys_FileWord
 -->
	<psxctl:ControlMeta name="sys_FileWord" dimension="single" choiceset="none">
		<psxctl:Description>a file upload input control with MS Word launcher</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters.  The default value is "datadisplay".</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="size" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter tells the user agent the initial width of the control. The width is given in pixels. The default value is 50.</psxctl:Description>
				<psxctl:DefaultValue>50</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
				<psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="cleartext" datatype="String" paramtype="custom">
				<psxctl:Description>This parameter determines the text that will be displayed along with a checkbox when the field supports being cleared.  The default value is 'Clear Word'.</psxctl:Description>
				<psxctl:DefaultValue>Clear Word</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="RxContentEditorURL" datatype="String" paramtype="msword">
				<psxctl:Description>This parameter specifies the absolute URL to the content editor of the current content item.  The value is passed to the OCX control, which uses it to obtain the metadata fields of the current content item.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="WordTemplateURL" datatype="String" paramtype="msword">
				<psxctl:Description>This parameter specifies the absolute URL to retrieve the Microsoft Word template document which provides the macros used to edit content items within Word.  The value is passed to the OCX control.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="ContentBodyURL" datatype="String" paramtype="msword">
				<psxctl:Description>This parameter specifies the absolute URL to retrieve the Microsoft Word document associated with the current content item.  The value is passed to the OCX control.</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="InlineLinkSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline link slot. The inline search dialog box shows the content types that have at least one variant added to the inline link slot. The default value is system inline link slotid 103.</psxctl:Description>
				<psxctl:DefaultValue>103</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="InlineImageSlot" datatype="String" paramtype="generic">
				<psxctl:Description>This parameter specifies the id of inline image slot. The inline search dialog box shows the content types that have at least one variant added to the inline image slot. The default value is system inline image slotid 104.</psxctl:Description>
				<psxctl:DefaultValue>104</psxctl:DefaultValue>
			</psxctl:Param>
		</psxctl:ParamList>
		<psxctl:Dependencies>
			<psxctl:Dependency status="readyToGo" occurrence="single">
				<psxctl:Default>
					<PSXExtensionCall id="0">
						<name>Java/global/percussion/generic/sys_FileInfo</name>
					</PSXExtensionCall>
				</psxctl:Default>
			</psxctl:Dependency>
		</psxctl:Dependencies>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_FileWord']" mode="psxcontrol">
		<xsl:call-template name="rx_filereadonly"/>
		<xsl:if test="contains(/*/ItemContent/@newDocument, 'no')">
			<xsl:if test="boolean(@clearBinaryParam)">
         &nbsp;&nbsp;
         <xsl:call-template name="sys_fileclear"/>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<!-- when the file control is used in read-only mode, provide a binary preview -->
	<xsl:template name="rx_filereadonly" match="Control[@name='sys_FileWord' and @isReadOnly='yes']" priority="10" mode="psxcontrol">
		<xsl:variable name="childkey">
			<xsl:choose>
				<xsl:when test="boolean(../../@childkey)">
					<xsl:value-of select="../../@childkey"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="/*/ItemContent/@childkey"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="contenteditorurl">
			<xsl:value-of select="ParamList/Param[@name='RxContentEditorURL']"/>
		</xsl:variable>
		<xsl:variable name="wordtemplateurl">
			<xsl:value-of select="ParamList/Param[@name='WordTemplateURL']"/>
		</xsl:variable>
		<xsl:variable name="contentbodyurl">
			<xsl:value-of select="ParamList/Param[@name='ContentBodyURL']"/>
		</xsl:variable>
		<xsl:variable name="bodysourcename">
			<xsl:value-of select="@paramName"/>
		</xsl:variable>
		<xsl:variable name="firsttimeuse">
			<xsl:value-of select="/*/ItemContent/@newDocument"/>
		</xsl:variable>
		<xsl:variable name="encodingfieldname">
			<xsl:value-of select="concat(@paramName,'_encoding')"/>
		</xsl:variable>
		<xsl:variable name="encoding">
			<xsl:value-of select="/ContentEditor/ItemContent/DisplayField/Control[@paramName=$encodingfieldname]/Value"/>
		</xsl:variable>
		<xsl:variable name="InlineLinkSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineLinkSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineLinkSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('../rx_resources/stylesheets/rx_Templates.xsl')/*/psxctl:ControlMeta[@name='sys_FileWord']/psxctl:ParamList/psxctl:Param[@name='InlineLinkSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="InlineImageSlot">
			<xsl:choose>
				<xsl:when test="ParamList/Param[@name='InlineImageSlot']">
					<xsl:value-of select="ParamList/Param[@name='InlineImageSlot']"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="document('../rx_resources/stylesheets/rx_Templates.xsl')/*/psxctl:ControlMeta[@name='sys_FileWord']/psxctl:ParamList/psxctl:Param[@name='InlineImageSlot']/psxctl:DefaultValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<script language="javaScript"><![CDATA[
         function launchWordandPreview(contentid,revision) {
               var paramstr = "";
               var edform = document.EditForm;
               var flen = edform.length;
               var selfirst = true;             
               var parvalsep = "|_|_";
               var valparsep = "#|#|";
       		var folderid = parseParam("sys_folderid", document.location.href); 
	       	if(contentid!=""){
                   paramstr += "sys_contentid" + parvalsep + contentid + valparsep;
               }
               if(revision !=""){
                   paramstr += "sys_revision" + parvalsep + revision + valparsep;
               }
               if(folderid !=""){
                   paramstr += "sys_folderid" + parvalsep + folderid + valparsep;
               }
               for(i=0;i<flen;i++){
                  var parValue = edform[i].value;
                  if (edform[i].type == "checkbox")
                  {
                     if (edform[i].checked) {
                           paramstr += edform[i].name + parvalsep + parValue;
                     if(i<flen-1) 
                        paramstr += valparsep;                    
                     }
                  }
                  else if(edform[i].type == "select-multiple")
                  {
                     selfirst=true;
                     for(j=0;j<edform[i].options.length;j++)
                     {
                        if(edform[i].options[j].selected)
                        {
                           if(selfirst)
                              selfirst = false;
                           else
                              paramstr += valparsep;                    
                           paramstr += edform[i].name + parvalsep + edform[i].options[j].value;
                        }
                     }
                     if(i<flen-1) 
                        paramstr += valparsep;
                  }
                  else {
                     paramstr += edform[i].name + parvalsep + parValue;
                     if(i<flen-1) 
                        paramstr += valparsep;
                  }
                  

               }
            if(document.URL.indexOf("debugrxword=yes")!=-1){
               document.all.word.setAttribute("DebugMode","yes");
            }
            else{
               document.all.word.setAttribute("DebugMode","no");
            }
            
            document.all.word.setAttribute("ParamString",paramstr);
            
            document.all.word.Fire();
            
            if (document.all.word.Success == true && document.all.sys_currentview.value == "sys_All") {
		self.close();

	    }
	    else {
	       if (contentid != "" && revision != "") {	 
		       docurl = document.location.href;
		       docurl  = docurl.split("?")[0] + "?sys_command=preview&sys_contentid=" + contentid + "&sys_revision=" + revision
		       document.location.href = docurl;
	       }
	    }
         }
    ]]></script>
		<a>
			<xsl:attribute name="href">javascript:launchWordandPreview("<xsl:value-of select="/*/Workflow/@contentId"/>","<xsl:value-of select="/*/Workflow/ContentStatus/@thisRevision"/>");</xsl:attribute>
			<xsl:call-template name="getLocaleString">
				<xsl:with-param name="key" select="'psx.contenteditor.sys_templates@Launch Word'"/>
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template>
		</a>
		<object id="MSXML4" classid="clsid:88d969c0-f192-11d4-a65f-0040963251e5" codebase="../sys_resources/word/msxml4.cab#version=4,00,9004,0" type="application/x-oleobject" style="display: none"/>
		<object id="word" classid="clsid:DA87CB4F-8EDF-4087-8F04-87EC3C938202" codebase="../rx_resources/word/rxwordocx.cab#version=5,5,0,5" type="application/x-oleobject" style="display: none">
			<param name="ContentEditorURL" value="{$contenteditorurl}"/>
			<param name="WordTemplateURL" value="{$wordtemplateurl}"/>
			<param name="EncodingParam" value="{$encoding}"/>
			<param name="ContentBodyURL" value="{$contentbodyurl}"/>
			<param name="BodySourceName" value="{$bodysourcename}"/>
			<param name="FirstTimeUse" value="{$firsttimeuse}"/>
			<param name="InlineSlots" value="{concat('InlineLinkSlot#',$InlineLinkSlot,'##InlineImageSlot#',$InlineImageSlot)}"/>
		</object>
	</xsl:template>
	<!--    SingleCheckBox
     (eventually this control should support rows/cols for layout)
-->
	<psxctl:ControlMeta name="sys_SingleCheckBox" dimension="single" choiceset="required">
		<psxctl:Description>a group of check boxes with the same HTML param name</psxctl:Description>
		<psxctl:ParamList>
			<psxctl:Param name="id" datatype="String" paramtype="generic">
				<psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
			</psxctl:Param>
			<psxctl:Param name="class" datatype="String" paramtype="generic">
				<psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
				<psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
			</psxctl:Param>
			<psxctl:Param name="style" datatype="String" paramtype="generic">
				<psxctl:Description>XHTML 1.0 attribute</psxctl:Description>
			</psxctl:Param>
		</psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_SingleCheckBox']" priority="10" mode="psxcontrol">
		<xsl:variable name="ISselected">
			<xsl:value-of select="Value"/>
		</xsl:variable>
		<xsl:apply-templates select="DisplayChoices/DisplayEntry[position()=1]" mode="psxcontrol-syssinglecheckbox">
			<xsl:with-param name="Control" select="."/>
			<xsl:with-param name="ISselected" select="$ISselected"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="DisplayEntry" mode="psxcontrol-syssinglecheckbox">
		<xsl:param name="Control"/>
		<xsl:param name="ISselected"/>
		<table width="100%" cellpadding="0" cellspacing="0" border="0">
			<tr class="headercell2">
				<td>
					<xsl:choose>
						<xsl:when test="$Control/@isReadOnly = 'yes'">
							<xsl:choose>
								<xsl:when test="Value = $ISselected">
									<table border="0">
										<tr>
											<td class="datadisplay" valign="top" align="left">
												<img src="../sys_resources/images/checked.gif" height="12" width="12"/>
												<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>
											</td>
										</tr>
									</table>
								</xsl:when>
								<xsl:otherwise>
									<table border="0">
										<tr>
											<td class="datadisplay" valign="top" align="left">
												<img src="../sys_resources/images/unchecked.gif" height="12" width="12"/>
												<input type="hidden" name="{$Control/@paramName}" value="{Value}"/>
											</td>
										</tr>
									</table>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<table border="0">
								<tr>
									<td class="datadisplay" valign="top" align="left">
										<input name="{$Control/@paramName}" type="checkbox" value="{Value}">
											<xsl:if test="@accessKey!=''">
												<xsl:attribute name="accesskey"><xsl:value-of select="@accessKey"/></xsl:attribute>
											</xsl:if>
											<xsl:call-template name="parametersToAttributes">
												<xsl:with-param name="controlClassName" select="'sys_CheckBoxGroup'"/>
												<xsl:with-param name="controlNode" select="$Control"/>
											</xsl:call-template>
											<xsl:if test="Value = $ISselected">
												<xsl:attribute name="checked"><xsl:value-of select="'checked'"/></xsl:attribute>
											</xsl:if>
										</input>
										<xsl:choose>
											<xsl:when test="@sourceType">
												<xsl:call-template name="getLocaleDisplayLabel">
													<xsl:with-param name="sourceType" select="@sourceType"/>
													<xsl:with-param name="paramName" select="$Control/@paramName"/>
													<xsl:with-param name="displayVal" select="DisplayLabel"/>
												</xsl:call-template>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="DisplayLabel"/>
											</xsl:otherwise>
										</xsl:choose>
										<br/>
									</td>
								</tr>
							</table>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template name="getaccesskey">
		<xsl:param name="label"/>
		<xsl:param name="sourceType"/>
		<xsl:param name="paramName"/>
		<xsl:param name="accessKey"/>
		<xsl:if test="$label!='' and $accessKey != '' and $paramName != ''">
			<xsl:variable name="keyval">
				<xsl:choose>
					<xsl:when test="$sourceType='sys_system'">
						<xsl:value-of select="concat('psx.ce.system.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
					</xsl:when>
					<xsl:when test="DisplayLabel/@sourceType='sys_shared'">
						<xsl:value-of select="concat('psx.ce.shared.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat('psx.ce.local.', /ContentEditor/@contentTypeId, '.', $paramName, '.mnemonic.', $label,'@',$accessKey)"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:call-template name="getLocaleString">
				<xsl:with-param name="key" select="$keyval"/>
				<xsl:with-param name="lang" select="$lang"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!--  END Single Check Box -->
	<psxi18n:lookupkeys>
		<key name="psx.contenteditor.sys_templates@Launch Word">Launch word link label</key>
		<key name="psx.contenteditor.sys_templates.sys_CalendarSimple.alt@Calendar Pop-up">Alt text for calendar image.</key>
		<key name="psx.contenteditor.sys_templates@Preview File">Preview file link label</key>
		<key name="psx.ce.sys_File@Clear">File clear check box label.</key>
	</psxi18n:lookupkeys>
</xsl:stylesheet>
