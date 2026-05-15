// Runs inside maven-antrun-plugin <script language="javascript"> with Rhino (JSR-223).
// Maven/Ant properties are accessible via project.getProperty().

var dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
dbf.setNamespaceAware(false);
var db = dbf.newDocumentBuilder();

function readChildren(file) {
    if (!file.exists()) return [];
    var doc  = db.parse(new java.io.FileInputStream(file));
    var list = doc.getElementsByTagName('child');
    var out  = [];
    for (var i = 0; i < list.getLength(); i++) {
        out.push(String(list.item(i).getAttribute('location')));
    }
    return out;
}

function writeRepo(dir, fileName, piName, typeName, repoName, children) {
    var timestamp  = java.lang.System.currentTimeMillis();
    var childLines = '';
    for (var i = 0; i < children.length; i++) {
        childLines += '    <child location=\'' + children[i] + '\'/>\n';
    }
    var xml =
        '<?' + piName + ' version=\'1.0.0\'?>\n' +
        '<repository name=\'' + repoName + '\'' +
        ' type=\'' + typeName + '\'' +
        ' version=\'1.0.0\'>\n' +
        '  <properties size=\'1\'>\n' +
        '    <property name=\'p2.timestamp\' value=\'' + timestamp + '\'/>\n' +
        '  </properties>\n' +
        '  <children size=\'' + children.length + '\'>\n' +
        childLines +
        '  </children>\n' +
        '</repository>\n';
    var pw = new java.io.PrintWriter(new java.io.FileWriter(new java.io.File(dir, fileName)));
    pw.print(xml);
    pw.close();
}

function writeP2Index(dir) {
    var pw = new java.io.PrintWriter(new java.io.FileWriter(new java.io.File(dir, 'p2.index')));
    pw.println('version=1');
    pw.println('metadata.repository.factory.order=compositeContent.xml,\\!');
    pw.println('artifact.repository.factory.order=compositeArtifacts.xml,\\!');
    pw.close();
}

function addChild(dirPath, name, child) {
    var dir = new java.io.File(dirPath);
    dir.mkdirs();

    var specs = [
        ['compositeContent.xml',
         'compositeMetadataRepository',
         'org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository'],
        ['compositeArtifacts.xml',
         'compositeArtifactRepository',
         'org.eclipse.equinox.p2.artifact.repository.CompositeArtifactRepository']
    ];

    for (var s = 0; s < specs.length; s++) {
        var fileName = specs[s][0], piName = specs[s][1], typeName = specs[s][2];
        var children = readChildren(new java.io.File(dir, fileName));
        if (children.indexOf(child) < 0) {
            children.push(child);
        }
        writeRepo(dir, fileName, piName, typeName, name, children);
    }

    writeP2Index(dir);
}

var baseDir          = project.getProperty('github-local-clone');
var major            = project.getProperty('parsedVersion.majorVersion');
var minor            = project.getProperty('parsedVersion.minorVersion');
var qualifiedVersion = project.getProperty('qualifiedVersion');
var siteLabel        = project.getProperty('site.label');

var level1Path  = 'updates/' + major + '.x';
var level2Path  = major + '.' + minor + '.x';
var releasePath = '../../../releases/' + qualifiedVersion;

addChild(baseDir,
         siteLabel + ' All Versions',
         level1Path);
addChild(baseDir + '/' + level1Path,
         siteLabel + ' ' + major + '.x',
         level2Path);
addChild(baseDir + '/' + level1Path + '/' + level2Path,
         siteLabel + ' ' + major + '.' + minor + '.x',
         releasePath);

self.log('Composite p2 repository updated: added ' + qualifiedVersion);