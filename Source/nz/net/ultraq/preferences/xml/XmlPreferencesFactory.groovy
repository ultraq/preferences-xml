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

import java.util.prefs.Preferences
import java.util.prefs.PreferencesFactory

/**
 * Implementation of the {@code PreferencesFactory} interface of the Preferences
 * API, creates and returns an XML file-based preferences implementation for use
 * with the Preferences API.
 * 
 * @author Emanuel Rabina
 */
class XmlPreferencesFactory implements PreferencesFactory {

	private static final String username = System.getProperty('user.name').replace(' ', '').toLowerCase()

	private static Preferences systemRoot
	private static Preferences userRoot

	/**
	 * {@inheritDoc}
	 */
	@Override
	synchronized Preferences systemRoot() {

		if (!systemRoot) {
			systemRoot = new XmlPreferences(null)
		}
		return systemRoot
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	synchronized Preferences userRoot() {

		if (!userRoot) {
			userRoot = new XmlPreferences(username)
		}
		return userRoot
	}
}
