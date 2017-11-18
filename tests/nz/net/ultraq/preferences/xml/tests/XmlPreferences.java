/* 
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.preferences.xml.tests;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Tests for the XML preferences library.
 * 
 * @author Emanuel Rabina
 */
public class XmlPreferences {

	/**
	 * Test setup, clear everything out.
	 */
	@BeforeClass
	public static void setup() {

		assumeFalse(Boolean.parseBoolean(System.getenv("CI")));

		File preferencesDir = new File(".preferences");
		for (File file: preferencesDir.listFiles()) {
			file.delete();
		}
		preferencesDir.delete();
	}

	/**
	 * Creates a system-wide preferences file.
	 */
	@Test
	public void createSystemPreferences() throws BackingStoreException {

		Preferences preferences = Preferences.systemRoot();
		preferences.flush();

		File preferencesFile = new File(".preferences/application-preferences.xml");
		assertTrue(preferencesFile.exists());
	}

	/**
	 * Creates a user-specific preferences file.
	 */
	@Test
	public void createUserPreferences() throws BackingStoreException {

		String username = System.getProperty("user.name").replace(" ", "").toLowerCase();
		Preferences preferences = Preferences.userRoot();
		preferences.flush();

		File preferencesFile = new File(".preferences/user-preferences-" + username + ".xml");
		assertTrue(preferencesFile.exists());
	}
}
