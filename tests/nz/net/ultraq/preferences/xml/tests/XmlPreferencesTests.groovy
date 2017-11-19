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

package nz.net.ultraq.preferences.xml.tests

import nz.net.ultraq.preferences.xml.XmlNode
import nz.net.ultraq.preferences.xml.XmlPreferences
import nz.net.ultraq.preferences.xml.XmlPreferencesFile
import nz.net.ultraq.preferences.xml.XmlRoot

import org.junit.Before
import org.junit.Test
import org.junit.rules.ExpectedException
import static org.mockito.Mockito.*

/**
 * Tests for the XML preferences class using a mock backing store.
 * 
 * @author Emanuel Rabina
 */
class XmlPreferencesTests {

	private XmlPreferencesFile mockXmlPreferencesFile
	private XmlRoot mockXmlRoot

	@Before
	void setup() {

		mockXmlPreferencesFile = mock(XmlPreferencesFile)
		mockXmlRoot = new XmlRoot('')
		when(mockXmlPreferencesFile.read()).thenReturn(mockXmlRoot)
	}

	/**
	 * Test for new preferences to return an empty list of child preference names.
	 */
	@Test
	void childrenNamesEmpty() {

		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		def childrenNames = xmlPreferences.childrenNamesSpi()
		assert childrenNames == []
	}

	/**
	 * Existing preferences return existing child preference names.
	 */
	@Test
	void childrenNamesExisting() {

		def childNodes = [
			new XmlNode('Child1'),
			new XmlNode('Child2'),
			new XmlNode('Child3')
		]
		when(mockXmlPreferencesFile.read()).thenReturn(
			new XmlRoot(
				name: '',
				nodes: childNodes
			)
		)
		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		def childrenNames = xmlPreferences.childrenNamesSpi()
		assert childrenNames == childNodes*.name
	}

	/**
	 * Creates non-existent child nodes to satisfy node navigation.
	 */
	@Test
	void childNodeCreateIfNotExist() {

		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		assert !xmlPreferences.nodeExists('child')
		def childPreferences = xmlPreferences.childSpi('child')
		assert childPreferences.name() == 'child'
		assert childPreferences.parent() == xmlPreferences
	}

	/**
	 * Returns existing child nodes when requested.
	 */
	@Test
	void childNodeReturnExisting() {

		def childNode = new XmlNode('child')
		when(mockXmlPreferencesFile.read()).thenReturn(
			new XmlRoot(
				name: '',
				nodes: [
					childNode
				]
			)
		)
		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		assert xmlPreferences.nodeExists('child')
		def childPreferences = xmlPreferences.childSpi('child')
		assert childPreferences.name() == 'child'
		assert childPreferences.parent() == xmlPreferences
	}

	/**
	 * Flush attempts to write to the backing file.
	 */
	@Test
	void flushToFile() {

		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		xmlPreferences.flush()
		verify(mockXmlPreferencesFile).write(mockXmlRoot)
	}

	/**
	 * Cannot flush child nodes.
	 */
	@Test
	void flushThrowExceptionOnChildNodes() {

		def xmlPreferences = new XmlPreferences(mockXmlPreferencesFile)

		def childPreferences = xmlPreferences.childSpi('child')
		try {
			childPreferences.flush()
		}
		catch (UnsupportedOperationException ex) {
		}
		verify(mockXmlPreferencesFile, never()).write(mockXmlRoot)
	}
}
