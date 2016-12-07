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

package nz.net.ultraq.preferences.xml

import nz.net.ultraq.jaxb.XmlReader
import nz.net.ultraq.jaxb.XmlWriter

import java.util.prefs.AbstractPreferences

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
	static final String PREFERENCES_DIRECTORY = '.preferences'

	// JAXB/Schema values
	private static final String XML_PREFERENCES_SCHEMA = 'nz/net/ultraq/preferences/xml/Preferences.xsd'
	private static final String SCHEMA_NAMESPACE       = 'http://www.ultraq.net.nz/xml/preferences'
	private static final String SCHEMA_URL             = 'http://schemas.ultraq.net.nz/xml/preferences.xsd'

	// JAXB representation of the preferences
	private final XmlNode preferences
	private final boolean root
	private File preferencesFile

	/**
	 * Constructor, creates a new top-level preference node.
	 * 
	 * @param username Name of the current user if this object is for user
	 *                 preferences, {@code null} for system preferences.
	 */
	XmlPreferences(String username) {

		super(null, '')

		// Ensure preferences directory exists
		def preferencesDirectory = new File(PREFERENCES_DIRECTORY)
		if (!preferencesDirectory.exists()) {
			preferencesDirectory.mkdir()
		}

		// Check if a preferences file already exists (reading from that one if it
		// does), create one otherwise
		preferencesFile = new File(
			"${PREFERENCES_DIRECTORY}/${(username ? "user-preferences-${username}" : 'application-preferences')}.xml"
		)
		preferences = preferencesFile.exists() ? readXml() : new XmlRoot("")
		root = true
	}

	/**
	 * Constructor, creates a new child preference node.
	 * 
	 * @param parent      Parent node.
	 * @param preferences Preferences of this node.
	 * @param name        Name of this node.
	 */
	private XmlPreferences(XmlPreferences parent, XmlNode preferences, String name) {

		super(parent, name)
		if (preferences) {
			this.preferences = preferences
		}
		else {
			this.preferences = new XmlNode(name)
			parent.preferences.nodes.add(this.preferences)
		}
		root = false
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] childrenNamesSpi() {

		return preferences.nodes.map { node -> node.name }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XmlPreferences childSpi(String name) {

		def childNode = preferences.nodes.find { node -> node.name == name }
		return childNode ?: new XmlPreferences(this, null, name)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void flush() {

		// Flush cannot be called on a child node
		if (!root) {
			throw new UnsupportedOperationException('flush() cannot be called on a child node')
		}

		writeXml()
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

		def entry = preferences.entries.find { entry -> entry.key == key }
		return entry?.value
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String[] keysSpi() {

		return preferences.entries.map { entry -> entry.key }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void putSpi(String key, String value) {

		def entry = preferences.entries.find { entry -> entry.key == key }
		if (entry) {
			entry.value = value
		}
		else {
			preferences.entries.add(new XmlEntry(key, value))
		}
	}

	/**
	 * Reads and returns the data from the preferences file.
	 * 
	 * @return JAXB object for the XML file root node.
	 */
	private synchronized XmlRoot readXml() {

		def xmlReader = new XmlReader<XmlRoot>(XMLRoot)
		xmlReader.addValidatingSchema(this.class.classLoader.getResourceAsStream(XML_PREFERENCES_SCHEMA))
		return xmlReader.read(preferencesFile)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeNodeSpi() {

		preferences.entries.clear()
		preferences.nodes.clear()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeSpi(String key) {

		preferences.entries.removeAll { entry -> entry.key == key }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void sync() {

		// Sync cannot be called on a child node
		if (!root) {
			throw new UnsupportedOperationException('sync() cannot be called on a child node')
		}

		// Update from the XML file, then write it back out
		syncFromNode(readXml())
		writeXml()
	}

	/**
	 * Recursively update this node, and it's children, with the given
	 * preferences.
	 * 
	 * @param updateNode The preferences to update existing ones with.
	 */
	private void syncFromNode(XmlNode updateNode) {

		// Update this node's preferences
		updateNode.entries.each { node -> put(node.key, node.value) }

		// Update children
		updateNode.nodes.each { node ->
			def childNode = getChild(node.name)
			childNode.syncFromNode(node)
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
	 */
	private synchronized void writeXml() {

		def xmlWriter = new XmlWriter<XmlRoot>(XMLRoot)
		xmlWriter.setSchemaLocation(SCHEMA_NAMESPACE, SCHEMA_URL)
		xmlWriter.addValidatingSchema(getClass().getClassLoader().getResourceAsStream(XML_PREFERENCES_SCHEMA))
		xmlWriter.setFormatOutput(true);
		xmlWriter.write(preferences, preferencesFile)
	}
}
