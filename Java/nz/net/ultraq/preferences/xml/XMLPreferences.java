
package nz.net.ultraq.preferences.xml;

import nz.net.ultraq.jaxb.XMLException;
import nz.net.ultraq.jaxb.XMLReader;
import nz.net.ultraq.jaxb.XMLWriter;

import java.io.File;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Implementation of the <tt>Preferences</tt> class of the Preferences API,
 * stores user and system preferences to XML files within the application
 * directory tree, instead of using user home directories or the Windows
 * registry.
 * 
 * @author Emanuel Rabina
 */
class XMLPreferences extends AbstractPreferences {

	// Sub-directory to store preferences files
	public static final String PREFERENCES_DIR = ".preferences";

	// JAXB/Schema values
	private static final String XML_PREFERENCES_SCHEMA  = "nz/net/ultraq/preferences/xml/Preferences.xsd";
//	private static final String SCHEMA_NAMESPACE = "http://www.ultraq.net.nz/xml/preferences";
//	private static final String SCHEMA_URL       = "http://schemas.ultraq.net.nz/xml/preferences.xsd";

	// JAXB representation of the preferences
	private final XMLNode preferences;
	private final boolean root;
	private File preferencesfile;

	/**
	 * Constructor, creates a new top-level preference node.
	 * 
	 * @param username Name of the current user if this object is for user
	 * 				   preferences, <tt>null</tt> for system preferences.
	 */
	XMLPreferences(String username) {

		super(null, "");

		// Ensure preferences directory exists
		File preferencesdir = new File(PREFERENCES_DIR);
		if (!preferencesdir.exists()) {
			preferencesdir.mkdir();
		}

		// Check if a preferences file already exists (reading from that one if
		// it does), create one otherwise
		preferencesfile = new File(PREFERENCES_DIR + "/" +
				(username == null ? "application-preferences" : "user-preferences-" + username) + ".xml");
		preferences = preferencesfile.exists() ? readFromXML() : new XMLRoot("");
		root = true;
	}

	/**
	 * Constructor, creates a new child preference node.
	 * 
	 * @param parent	  Parent node.
	 * @param preferences Preferences of this node.
	 * @param name		  Name of this node.
	 */
	private XMLPreferences(XMLPreferences parent, XMLNode preferences, String name) {

		super(parent, name);
		if (preferences != null) {
			this.preferences = preferences;
		}
		else {
			this.preferences = new XMLNode(name);
			parent.preferences.getNodes().add(this.preferences);
		}
		root = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] childrenNamesSpi() {

		List<XMLNode> nodes = preferences.getNodes();
		String[] childrennames = new String[nodes.size()];
		for (int i = 0; i < childrennames.length; i++) {
			childrennames[i] = nodes.get(i).getName();
		}
		return childrennames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XMLPreferences childSpi(String name) {

		for (XMLNode node: preferences.getNodes()) {
			if (node.getName().equals(name)) {
				return new XMLPreferences(this, node, name);
			}
		}
		return new XMLPreferences(this, null, name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws BackingStoreException {

		// Flush cannot be called on a child node
		if (!root) {
			throw new UnsupportedOperationException("flush() cannot be called on a child node.");
		}

		try {
			// Write the entire preferences tree to an XML file
			writeToXML();
		}
		catch (XMLException ex) {
			throw new BackingStoreException(ex);
		}
	}

	/**
	 * Does nothing as {@link #flush()} is overidden instead.
	 */
	@Override
	protected void flushSpi() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSpi(String key) {

		for (XMLEntry entry: preferences.getEntries()) {
			if (key.equals(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] keysSpi() {

		List<XMLEntry> entries = preferences.getEntries();
		String[] keys = new String[entries.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = entries.get(i).getKey();
		}
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void putSpi(String key, String value) {

		for (XMLEntry entry: preferences.getEntries()) {
			if (key.equals(entry.getKey())) {
				entry.setValue(value);
				return;
			}
		}
		preferences.getEntries().add(new XMLEntry(key, value));
	}

	/**
	 * Reads and returns the data from the preferences file.
	 * 
	 * @return JAXB object for the XML file root node.
	 * @throws XMLException
	 */
	private synchronized XMLRoot readFromXML() throws XMLException {

		XMLReader<XMLRoot> xmlreader = new XMLReader<XMLRoot>(XMLRoot.class);
		xmlreader.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(XML_PREFERENCES_SCHEMA));
		return xmlreader.readXMLData(preferencesfile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeNodeSpi() {

		preferences.getEntries().clear();
		preferences.getNodes().clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeSpi(String key) {

		for (XMLEntry entry: preferences.getEntries()) {
			if (key.equals(entry.getKey())) {
				entry.setValue(null);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sync() throws BackingStoreException {

		// Sync cannot be called on a child node
		if (!root) {
			throw new UnsupportedOperationException("sync() cannot be called on a child node.");
		}

		try {
			// Update from the XML file
			XMLRoot fileroot = readFromXML();
			for (XMLNode filepreferences: fileroot.getNodes()) {
				String filechildname = filepreferences.getName();
				XMLPreferences child = (XMLPreferences)getChild(filechildname);
				child.sync(filepreferences);
			}

			// Write to XML file
			writeToXML();
		}
		catch (XMLException ex) {
			throw new BackingStoreException(ex);
		}
	}

	/**
	 * Recursively update this node, and it's children, with the given
	 * preferences.
	 * 
	 * @param updatepreferences The preferences to update existing ones with.
	 * @throws BackingStoreException
	 */
	private void sync(XMLNode updatepreferences) throws BackingStoreException {

		// Update this node's preferences
		for (XMLEntry updatepreference: updatepreferences.getEntries()) {
			put(updatepreference.getKey(), updatepreference.getValue());
		}

		// Update children
		List<XMLNode> newpreferenceslist = updatepreferences.getNodes();
		for (XMLNode newpreferences: newpreferenceslist) {
			String newchildname = newpreferences.getName();
			XMLPreferences child = (XMLPreferences)getChild(newchildname);
			child.sync(newpreferences);
		}
	}

	/**
	 * Does nothing as {@link #sync()} is overidden instead.
	 */
	@Override
	protected void syncSpi() {
	}

	/**
	 * Writes the current preferences data to the XML file.
	 * 
	 * @throws XMLException
	 */
	private synchronized void writeToXML() throws XMLException {

		XMLWriter<XMLRoot> xmlwriter = new XMLWriter<XMLRoot>(XMLRoot.class);
//		xmlwriter.setSchemaLocation(SCHEMA_NAMESPACE, SCHEMA_URL);
		xmlwriter.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(XML_PREFERENCES_SCHEMA));
		xmlwriter.setFormatOutput(true);
		xmlwriter.writeXMLData((XMLRoot)preferences, preferencesfile);
	}
}
