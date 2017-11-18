/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.preferences.xml;

import nz.net.ultraq.jaxb.XMLException;
import nz.net.ultraq.jaxb.XMLReader;
import nz.net.ultraq.jaxb.XMLWriter;

import java.io.File;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * Implementation of the {@code Preferences} class of the Preferences API,
 * stores user and system preferences to XML files within the application
 * directory tree, instead of using user home directories or the Windows
 * registry.
 * 
 * @author Emanuel Rabina
 */
class XmlPreferences extends AbstractPreferences {

	// Sub-directory to store preferences files
	public static final String PREFERENCES_DIR = ".preferences";

	// JAXB/Schema values
	private static final String XML_PREFERENCES_SCHEMA = "nz/net/ultraq/preferences/xml/Preferences.xsd";
//	private static final String SCHEMA_NAMESPACE = "http://www.ultraq.net.nz/xml/preferences";
//	private static final String SCHEMA_URL       = "http://schemas.ultraq.net.nz/xml/preferences.xsd";

	// JAXB representation of the preferences
	private final XmlNode preferences;
	private final boolean root;
	private File preferencesFile;

	/**
	 * Constructor, creates a new top-level preference node.
	 * 
	 * @param username Name of the current user if this object is for user
	 * 				   preferences, <tt>null</tt> for system preferences.
	 */
	XmlPreferences(String username) {

		super(null, "");

		// Ensure preferences directory exists
		File preferencesDir = new File(PREFERENCES_DIR);
		if (!preferencesDir.exists()) {
			preferencesDir.mkdir();
		}

		// Check if a preferences file already exists (reading from that one if
		// it does), create one otherwise
		preferencesFile = new File(PREFERENCES_DIR + "/" +
				(username == null ? "application-preferences" : "user-preferences-" + username) + ".xml");
		preferences = preferencesFile.exists() ? readFromXml() : new XmlRoot("");
		root = true;
	}

	/**
	 * Constructor, creates a new child preference node.
	 * 
	 * @param parent	  Parent node.
	 * @param preferences Preferences of this node.
	 * @param name		  Name of this node.
	 */
	private XmlPreferences(XmlPreferences parent, XmlNode preferences, String name) {

		super(parent, name);
		if (preferences != null) {
			this.preferences = preferences;
		}
		else {
			this.preferences = new XmlNode(name);
			parent.preferences.getNodes().add(this.preferences);
		}
		root = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] childrenNamesSpi() {

		List<XmlNode> nodes = preferences.getNodes();
		String[] childrenNames = new String[nodes.size()];
		for (int i = 0; i < childrenNames.length; i++) {
			childrenNames[i] = nodes.get(i).getName();
		}
		return childrenNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XmlPreferences childSpi(String name) {

		for (XmlNode node: preferences.getNodes()) {
			if (node.getName().equals(name)) {
				return new XmlPreferences(this, node, name);
			}
		}
		return new XmlPreferences(this, null, name);
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
			writeToXml();
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

		for (XmlEntry entry: preferences.getEntries()) {
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

		List<XmlEntry> entries = preferences.getEntries();
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

		for (XmlEntry entry: preferences.getEntries()) {
			if (key.equals(entry.getKey())) {
				entry.setValue(value);
				return;
			}
		}
		preferences.getEntries().add(new XmlEntry(key, value));
	}

	/**
	 * Reads and returns the data from the preferences file.
	 * 
	 * @return JAXB object for the XML file root node.
	 * @throws XMLException
	 */
	private synchronized XmlRoot readFromXml() throws XMLException {

		XMLReader<XmlRoot> xmlReader = new XMLReader<>(XmlRoot.class);
		xmlReader.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(XML_PREFERENCES_SCHEMA));
		return xmlReader.readXMLData(preferencesFile);
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

		for (XmlEntry entry: preferences.getEntries()) {
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
			XmlRoot fileRoot = readFromXml();
			for (XmlNode filepreferences: fileRoot.getNodes()) {
				String fileChildName = filepreferences.getName();
				XmlPreferences child = (XmlPreferences)getChild(fileChildName);
				child.sync(filepreferences);
			}

			// Write to XML file
			writeToXml();
		}
		catch (XMLException ex) {
			throw new BackingStoreException(ex);
		}
	}

	/**
	 * Recursively update this node, and it's children, with the given
	 * preferences.
	 * 
	 * @param updatePreferences The preferences to update existing ones with.
	 * @throws BackingStoreException
	 */
	private void sync(XmlNode updatePreferences) throws BackingStoreException {

		// Update this node's preferences
		for (XmlEntry updatePreference: updatePreferences.getEntries()) {
			put(updatePreference.getKey(), updatePreference.getValue());
		}

		// Update children
		List<XmlNode> newPreferencesList = updatePreferences.getNodes();
		for (XmlNode newpreferences: newPreferencesList) {
			String newChildName = newpreferences.getName();
			XmlPreferences child = (XmlPreferences)getChild(newChildName);
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
	private synchronized void writeToXml() throws XMLException {

		XMLWriter<XmlRoot> xmlWriter = new XMLWriter<>(XmlRoot.class);
//		xmlwriter.setSchemaLocation(SCHEMA_NAMESPACE, SCHEMA_URL);
		xmlWriter.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(XML_PREFERENCES_SCHEMA));
		xmlWriter.setFormatOutput(true);
		xmlWriter.writeXMLData((XmlRoot)preferences, preferencesFile);
	}
}
