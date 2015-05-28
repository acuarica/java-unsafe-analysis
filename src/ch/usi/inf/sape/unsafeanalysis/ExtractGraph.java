package ch.usi.inf.sape.unsafeanalysis;

public class ExtractGraph {

	public static void main(String[] args) {
//		final MavenIndex index = build(Graph.class);
//
//		String localPathCsv = "db/depgraph.csv";
//		try (PrintStream out = new PrintStream(localPathCsv)) {
//
//			out.println("groupId, artifactId, depGroupId, depArtifactId, depVersion, depScope");
//
//			for (final MavenArtifact a : index) {
//				String path = a.getPomPath();
//				try {
//					SAXParserFactory spf = SAXParserFactory.newInstance();
//
//					File f = new File("db/" + path);
//					spf.newSAXParser().parse(f, new DefaultHandler() {
//						private Dependency dep;
//						private String value;
//
//						@Override
//						public void startElement(String uri, String localName,
//								String qName, Attributes attributes)
//								throws SAXException {
//							if (qName.equals("dependency")) {
//								dep = new Dependency();
//							}
//						}
//
//						@Override
//						public void characters(char[] ch, int start, int length)
//								throws SAXException {
//							value = new String(ch, start, length);
//						}
//
//						@Override
//						public void endElement(String uri, String localName,
//								String qName) throws SAXException {
//							if (qName.equals("dependency")) {
//								out.format("%s, %s, %s, %s, %s, %s\n",
//										a.groupId, a.artifactId, dep.groupId,
//										dep.artifactId, dep.version, dep.scope);
//
//								a.dependencies.add(dep);
//								dep = null;
//							} else if (dep != null && qName.equals("groupId")) {
//								dep.groupId = value;
//							} else if (dep != null
//									&& qName.equals("artifactId")) {
//								dep.artifactId = value;
//							} else if (dep != null && qName.equals("version")) {
//								dep.version = value;
//							} else if (dep != null && qName.equals("scope")) {
//								dep.scope = value;
//							}
//						}
//					});
//				} catch (FileNotFoundException e) {
//					logger.info("POM not found %s", path);
//				} catch (Exception e) {
//					logger.log("Exception (%s) in %s: %s", e.getClass(), path,
//							e.getMessage());
//				}
//			}
//		}
	}
}
