/*******************************************************************************
 * Copyright (c) 2010 Oracle.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *     http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Hal Hildebrand - Initial JMX support 
 ******************************************************************************/

package org.eclipse.gemini.management.configurationadmin;

import static org.eclipse.gemini.management.internal.OSGiProperties.propertiesFrom;
import static org.eclipse.gemini.management.internal.OSGiProperties.tableFrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.openmbean.TabularData;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.jmx.service.cm.ConfigurationAdminMBean;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/** 
 * 
 */
public final class ConfigAdminManager implements ConfigurationAdminMBean {

	private final ConfigurationAdmin admin;
	
	private static final Logger log = Logger.getLogger(ConfigAdminManager.class.getCanonicalName());

	public ConfigAdminManager(ConfigurationAdmin admin) {
		this.admin = admin;
	}

	/**
	 * {@inheritDoc}
	 */
	public String createFactoryConfiguration(String factoryPid) throws IOException {
		if (factoryPid == null) {
			throw new IOException("Factory PID must not be null");
		}
		Configuration c = admin.createFactoryConfiguration(factoryPid);
		c.setBundleLocation(null);
		return c.getPid();
	}

	/**
	 * {@inheritDoc}
	 */
	public String createFactoryConfigurationForLocation(String factoryPid, String location) throws IOException {
		if (factoryPid == null) {
			throw new IOException("Factory PID must not be null");
		}
		Configuration c = admin.createFactoryConfiguration(factoryPid);
		c.setBundleLocation(location);
		return c.getPid();
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, null).delete();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteForLocation(String pid, String location) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, location).delete();
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteConfigurations(String filter) throws IOException {
		Configuration[] confs;
		try {
			confs = admin.listConfigurations(filter);
		} catch (InvalidSyntaxException e) {
			log.log(Level.SEVERE, "Invalid filter argument: " + filter, e);
			throw new IOException("Invalid filter: " + e);
		}
		if (confs != null) {
			for (Configuration conf : confs) {
				conf.delete();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBundleLocation(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, null).getBundleLocation();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFactoryPid(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, null).getFactoryPid();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFactoryPidForLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		return admin.getConfiguration(pid, location).getFactoryPid();
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getProperties(String pid) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		Dictionary<String, Object> properties = admin.getConfiguration(pid, null).getProperties();
		return properties == null ? null : tableFrom(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	public TabularData getPropertiesForLocation(String pid, String location)
			throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		Dictionary<String, Object> properties = admin.getConfiguration(pid, location).getProperties();
		return properties == null ? null : tableFrom(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[][] getConfigurations(String filter) throws IOException {
		ArrayList<String[]> pids = new ArrayList<String[]>();
		Configuration[] configurations;
		try {
			configurations = admin.listConfigurations(filter);
		} catch (InvalidSyntaxException e) {
			log.log(Level.SEVERE, "Invalid filter argument: " + filter, e);
			throw new IOException("Invalid filter: " + e);
		}
		if (configurations != null) {
			for (Configuration config : configurations) {
				pids.add(new String[] { config.getPid(), config.getBundleLocation() });
			}
		}
		return pids.toArray(new String[pids.size()][]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBundleLocation(String pid, String location) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid).setBundleLocation(location);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(String pid, TabularData table) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, null).update(propertiesFrom(table));
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateForLocation(String pid, String location, TabularData table) throws IOException {
		if (pid == null) {
			throw new IOException("PID must not be null");
		}
		admin.getConfiguration(pid, location).update(propertiesFrom(table));
	}

}
