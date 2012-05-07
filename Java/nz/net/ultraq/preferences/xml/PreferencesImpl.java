
package nz.net.ultraq.preferences.xml;

import nz.net.ultraq.xml.preferences.xml.XMLPreference;
import nz.net.ultraq.xml.preferences.xml.XMLPreferences;
import nz.net.ultraq.xml.preferences.xml.XMLPreferencesData;
import nz.net.ultraq.xml.utilities.XMLException;
import nz.net.ultraq.xml.utilities.XMLReader;
import nz.net.ultraq.xml.utilities.XMLWriter;

import java.io.File;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Implementation of the <tt>Preferences</tt> class of the Preferences API,
 * stores user and system preferences to XML files within the application
 * directory tree, instead of using user home directories or the Windows
 * registry.
 * 
 * @author Emanuel Rabina
 */
class PreferencesImpl extends AbstractPreferences {

	// Sub-directory to store preferences files
	public static final String PREFERENCES_DIR = "Preferences";

	// JAXB/Schema values
	private static final String XML_PACKAGE = "nz.net.ultraq.xml.preferences.xml";
	private static final String SCHEMA_FILENAME  = "Preferences.xsd";
//	private static final String SCHEMA_NAMESPACE = "http://www.ultraq.net.nz/xml/preferences";
//	private static final String SCHEMA_URL       = "http://schemas.ultraq.net.nz/xml/preferences.xsd";

	// JAXB representation of the preferences
	private final XMLPreferencesData root;
	private XMLPreferences preferences;
	private File preferencesfile;

	/**
	 * Constructor, creates a new top-level preference node.
	 * 
	 * @param appname Name of the application for which these preferences are
	 * 				  for.
	 * @param username Name of the current user if this object is for user
	 * 				   preferences, <tt>null</tt> for system preferences.
	 */
	PreferencesImpl(String appname, String username) {

		super(null, "");

		// Ensure preferences directory exists
		File preferencesdir = new File(PREFERENCES_DIR);
		if (!preferencesdir.exists()) {
			preferencesdir.mkdir();
		}

		// Check if a preferences file already exists, create one otherwise
		preferencesfile = new File(PREFERENCES_DIR + "/" + appname + "_" + (username != null ? username : "") + ".xml");

		// Read from the file, or create a new preferences object
		root = preferencesfile.exists() ? readFromXML() : new XMLPreferencesData();
	}

	/**
	 * Constructor, creates a new child preference node.
	 * 
	 * @param parent	  Parent node of this child.
	 * @param name		  Name of this child node.
	 * @param preferences Node in the XML that maps to this child.
	 */
	private PreferencesImpl(PreferencesImpl parent, String name, XMLPreferences preferences) {

		super(parent, name);
		this.root        = null;
		this.preferences = preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPreferences childSpi(String name) {

		List<XMLPreferences> children = root != null ? root.getPreferences() : preferences.getPreferences();

		// Check to see if the named child node already exists
		for (XMLPreferences childprefs: children) {
			if (name.equals(childprefs.getPackage())) {
				return new PreferencesImpl(this, name, childprefs);
			}
		}

		// Create a new child node
		XMLPreferences newpreferences = new XMLPreferences();
		newpreferences.setPackage(name);
		children.add(newpreferences);
		return new PreferencesImpl(this, name, newpreferences);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] childrenNamesSpi() {

		List<XMLPreferences> children = preferences.getPreferences();
		String[] childrennames = new String[children.size()];
		for (int i = 0; i < childrennames.length; i++) {
			childrennames[i] = children.get(i).getPackage();
		}
		return childrennames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws BackingStoreException {

		// Flush cannot be called on a child node
		if (root == null) {
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
	protected void flushSpi() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSpi(String key) {

		for (XMLPreference preference: preferences.getPreference()) {
			if (key.equals(preference.getKey())) {
				return preference.getValue();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] keysSpi() {

		List<XMLPreference> preferencelist = preferences.getPreference();
		String[] keys = new String[preferencelist.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = preferencelist.get(i).getKey();
		}
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Preferences node(String path) {

		return super.node(path.replace(".", "/"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void putSpi(String key, String value) {

		List<XMLPreference> preferencelist = preferences.getPreference();

		// Check that the key doesn't already exist, replace it's value if it does
		for (int i = 0; i < preferencelist.size(); i++) {
			XMLPreference preference = preferencelist.get(i);
			if (key.equals(preference.getKey())) {
				preference.setValue(value);
				return;
			}
		}

		// Otherwise, add a new pereference
		XMLPreference newpreference = new XMLPreference();
		newpreference.setKey(key);
		newpreference.setValue(value);
		preferencelist.add(newpreference);
	}

	/**
	 * Reads and returns the data from the preferences file.
	 * 
	 * @return JAXB object for the XML file root node.
	 * @throws XMLException
	 */
	private synchronized XMLPreferencesData readFromXML() throws XMLException {

		XMLReader<XMLPreferencesData> xmlreader = new XMLReader<XMLPreferencesData>(XML_PACKAGE);
		xmlreader.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(SCHEMA_FILENAME));
		return xmlreader.readXMLData(preferencesfile);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeNodeSpi() {

		((PreferencesImpl)parent()).preferences.getPreferences().remove(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeSpi(String key) {

		List<XMLPreference> preferencelist = preferences.getPreference();
		for (int i = 0; i < preferencelist.size(); i++) {
			XMLPreference preference = preferencelist.get(i);
			if (key.equals(preference.getKey())) {
				preferencelist.remove(i);
				return;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sync() throws BackingStoreException {

		// Sync cannot be called on a child node
		if (root == null) {
			throw new UnsupportedOperationException("sync() cannot be called on a child node.");
		}

		try {
			// Update from the XML file
			XMLPreferencesData fileroot = readFromXML();
			for (XMLPreferences filepreferences: fileroot.getPreferences()) {
				String filechildname = filepreferences.getPackage();
				PreferencesImpl child = (PreferencesImpl)getChild(filechildname);
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
	private void sync(XMLPreferences updatepreferences) throws BackingStoreException {

		// Update this node's preferences
		for (XMLPreference newpreference: updatepreferences.getPreference()) {
			put(newpreference.getKey(), newpreference.getValue());
		}

		// Update children
		List<XMLPreferences> newpreferenceslist = updatepreferences.getPreferences();
		for (XMLPreferences newpreferences: newpreferenceslist) {
			String newchildname = newpreferences.getPackage();
			PreferencesImpl child = (PreferencesImpl)getChild(newchildname);
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

		XMLWriter<XMLPreferencesData> xmlwriter = new XMLWriter<XMLPreferencesData>(XML_PACKAGE);
//		xmlwriter.setSchemaLocation(SCHEMA_NAMESPACE, SCHEMA_URL);
		xmlwriter.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(SCHEMA_FILENAME));
		xmlwriter.writeXMLData(root, preferencesfile);
	}
}
