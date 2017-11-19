/* 
 * Copyright 2017, Emanuel Rabina (http://www.ultraq.net.nz/)
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

/**
 * A separation of the backing preferences file from the Java preferences
 * object.
 * 
 * @author Emanuel Rabina
 */
class XmlPreferencesFile {

	// Sub-directory to store preferences files
	private static final String PREFERENCES_DIRECTORY = '.preferences'

	// JAXB/Schema values
	private static final String XML_PREFERENCES_SCHEMA = 'nz/net/ultraq/preferences/xml/Preferences.xsd'
//	private static final String SCHEMA_NAMESPACE       = 'http://www.ultraq.net.nz/xml/preferences'
//	private static final String SCHEMA_URL             = 'http://schemas.ultraq.net.nz/xml/preferences.xsd'

	private final File preferencesFile

	/**
	 * Constructor, create a new backing store with the given file name.
	 * 
	 * @param filename
	 */
	XmlPreferencesFile(String filename) {

		preferencesFile = new File("${PREFERENCES_DIRECTORY}/${filename}.xml")
	}

	/**
	 * Reads and returns the data from the preferences file.  If no file exists,
	 * then an empty root node is returned.
	 * 
	 * @return JAXB object for the XML file root node.
	 */
	XmlRoot read() {

		if (preferencesFile.exists()) {
			def xmlReader = new XmlReader<XmlRoot>(XmlRoot)
			xmlReader.addValidatingSchema(this.class.classLoader.getResourceAsStream(XML_PREFERENCES_SCHEMA))
			return xmlReader.read(preferencesFile)
		}
		return new XmlRoot('')
	}

	/**
	 * Writes the preferences data to the XML file.
	 * 
	 * @param preferences
	 */
	void write(XmlRoot preferences) {

		// Ensure preferences directory exists
		def preferencesDirectory = new File(PREFERENCES_DIRECTORY)
		if (!preferencesDirectory.exists()) {
			preferencesDirectory.mkdir()
		}

		def xmlWriter = new XmlWriter<XmlRoot>(XmlRoot)
//		xmlWriter.setSchemaLocation(SCHEMA_NAMESPACE, SCHEMA_URL)
		xmlWriter.addValidatingSchema(this.class.classLoader.getResourceAsStream(XML_PREFERENCES_SCHEMA))
		xmlWriter.setFormatOutput(true)
		xmlWriter.write(preferences, preferencesFile)
	}
}
