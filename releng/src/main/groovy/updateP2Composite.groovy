import groovy.xml.XmlParser

def addChild(File dir, String name, String child) {
    dir.mkdirs()
    long timestamp = System.currentTimeMillis()

    ['compositeContent.xml', 'compositeArtifacts.xml'].each { fileName ->
        boolean isContent = fileName == 'compositeContent.xml'
        def pi   = isContent ? 'compositeMetadataRepository' : 'compositeArtifactRepository'
        def type = isContent
            ? 'org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository'
            : 'org.eclipse.equinox.p2.artifact.repository.CompositeArtifactRepository'

        def repoFile = new File(dir, fileName)
        def children = []

        if (repoFile.exists()) {
            def xml = new XmlParser().parse(repoFile)
            xml.children.child.each { children << it.@location }
        }

        if (!children.contains(child)) {
            children << child
        }

        repoFile.text = """\
<?${pi} version='1.0.0'?>
<repository name='${name}' type='${type}' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='${timestamp}'/>
  </properties>
  <children size='${children.size()}'>
${children.collect { "    <child location='${it}'/>" }.join('\n')}
  </children>
</repository>
"""
    }

    new File(dir, 'p2.index').text = '''\
version=1
metadata.repository.factory.order=compositeContent.xml,\\!
artifact.repository.factory.order=compositeArtifacts.xml,\\!
'''
}

def baseDir          = new File(project.properties['github-local-clone'])
def major            = project.properties['parsedVersion.majorVersion']
def minor            = project.properties['parsedVersion.minorVersion']
def qualifiedVersion = project.properties['qualifiedVersion']
def siteLabel        = project.properties['site.label']

def level1Path   = "updates/${major}.x"
def level2Path   = "${major}.${minor}.x"
def releasePath  = "../../../releases/${qualifiedVersion}"

addChild(baseDir,
         "${siteLabel} All Versions",
         level1Path)
addChild(new File(baseDir, level1Path),
         "${siteLabel} ${major}.x",
         level2Path)
addChild(new File(baseDir, "${level1Path}/${level2Path}"),
         "${siteLabel} ${major}.${minor}.x",
         releasePath)

log.info("Composite p2 repository updated: added ${qualifiedVersion}")
