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

	private final XmlPreferencesFile preferencesFile
	private final XmlNode preferences

	/**
	 * Constructor, creates a new top-level preference node.
	 * 
	 * @param preferencesFile Backing file to use for these preferences.
	 */
	XmlPreferences(XmlPreferencesFile preferencesFile) {

		super(null, '')
		this.preferencesFile = preferencesFile
		preferences = preferencesFile.read()
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
		preferencesFile = null
		if (preferences) {
			this.preferences = preferences
		}
		else {
			this.preferences = new XmlNode(name)
			parent.preferences.nodes.add(this.preferences)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings('UnnecessaryCollectCall')
	protected String[] childrenNamesSpi() {

		return preferences.nodes.collect { node -> node.name }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected XmlPreferences childSpi(String name) {

		def childNode = preferences.nodes.find { node -> node.name == name }
		return childNode ? new XmlPreferences(this, childNode, name) : new XmlPreferences(this, null, name)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void flush() {

		if (!preferencesFile) {
			throw new UnsupportedOperationException('flush() cannot be called on a child node')
		}
		preferencesFile.write(preferences)
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
	@SuppressWarnings('UnnecessaryCollectCall')
	protected String[] keysSpi() {

		return preferences.entries.collect { entry -> entry.key }
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

		if (!preferencesFile) {
			throw new UnsupportedOperationException('sync() cannot be called on a child node')
		}

		// Update from the XML file, then write it back out
		syncFromNode(preferencesFile.read())
		preferencesFile.write(preferences)
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
}
