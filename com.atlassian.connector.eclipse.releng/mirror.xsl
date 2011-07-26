<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">

	<xsl:param name="siteUrl"/>
	
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:template match="/">
		<site pack200="true" associateSitesURL="{$siteUrl}/associates-e3.7.xml">
			<description url="{$siteUrl}">Atlassian Connector for Eclipse</description>
		    <xsl:apply-templates select="site/feature"/>
		    <xsl:apply-templates select="site/category-def[@name='Connector']"/>
		    <xsl:apply-templates select="site/category-def[@name='Integrations']"/>
		    <category-def name="Dependencies" label="Dependencies (optional)">
   			</category-def>
		</site>
	</xsl:template>

	<xsl:template match="site/feature/category[not(@name='Connector' or @name='Integrations')]">
		<category name="Dependencies" />
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>