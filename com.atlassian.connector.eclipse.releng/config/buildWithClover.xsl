<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:param name="initstring" expression="no_initstring_supplied"/>
	<xsl:param name="enabled" expression="false"/>
	<xsl:template match="node()|@*">
	   <xsl:copy>
	   <xsl:apply-templates select="@*"/>
	   <xsl:apply-templates/>
	   </xsl:copy>
	 </xsl:template>
	 <xsl:template match="javac">
		<taskdef resource="cloverlib.xml" classpath="${{builder}}/../lib/clover.jar"/>
		<clover-setup initstring="{$initstring}" enabled="{$enabled}"/>
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	 </xsl:template>
</xsl:stylesheet>