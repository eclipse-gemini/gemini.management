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

package org.eclipse.gemini.mgmt.useradmin;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.osgi.framework.InvalidSyntaxException;
import org.eclipse.gemini.mgmt.Monitor;
import org.eclipse.gemini.mgmt.codec.OSGiProperties;
import org.eclipse.gemini.mgmt.useradmin.codec.OSGiAuthorization;
import org.eclipse.gemini.mgmt.useradmin.codec.OSGiGroup;
import org.eclipse.gemini.mgmt.useradmin.codec.OSGiRole;
import org.eclipse.gemini.mgmt.useradmin.codec.OSGiUser;
import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/** 
 * 
 */
public class UserManager extends Monitor implements UserAdminMBean {

	protected UserAdmin admin;

	public UserManager(UserAdmin admin) {
		this.admin = admin;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addCredential(String key, byte[] value, String username) throws IOException {
		if (username == null) {
			throw new IOException("User name must not be null");
		}
		if (key == null) {
			throw new IOException("Credential key must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(username);
		} catch (ClassCastException e) {
			throw new IOException("Not a User: " + username);
		}
		if (user == null) {
			throw new IOException("Not a User: " + username);
		}
		user.getCredentials().put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addCredentialString(String key, String value, String username) throws IOException {
		if (username == null) {
			throw new IOException("User name must not be null");
		}
		if (key == null) {
			throw new IOException("Credential key must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(username);
		} catch (ClassCastException e) {
			throw new IOException("Not a User: " + username);
		}
		if (user == null) {
			throw new IOException("Not a User: " + username);
		}
		user.getCredentials().put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addMember(String groupname, String rolename) throws IOException {
		if (groupname == null) {
			throw new IOException("Group name must not be null");
		}
		if (rolename == null) {
			throw new IOException("Role name must not be null");
		}
		Role group = admin.getRole(groupname);
		if (group == null) {
			throw new IOException("Group does not exist: " + groupname);
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			throw new IOException("Role does not exist: " + rolename);
		}
		return group.getType() == Role.GROUP && ((Group) group).addMember(role);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addProperty(String key, byte[] value, String rolename) throws IOException {
		if (rolename == null) {
			throw new IOException("Role name must not be null");
		}
		if (key == null) {
			throw new IOException("Credential key must not be null");
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			throw new IOException("Role does not exist: " + rolename);
		}
		role.getProperties().put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void addPropertyString(String key, String value, String rolename) throws IOException {
		if (rolename == null) {
			throw new IOException("Role name must not be null");
		}
		if (key == null) {
			throw new IOException("Credential key must not be null");
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			throw new IOException("Role does not exist: " + rolename);
		}
		role.getProperties().put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean addRequiredMember(String groupname, String rolename) throws IOException {
		if (groupname == null) {
			throw new IOException("Group name must not be null");
		}
		Role group = admin.getRole(groupname);
		if (group == null) {
			throw new IOException("Group does not exist: " + rolename);
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			throw new IOException("Role does not exist: " + rolename);
		}
		return group.getType() == Role.GROUP && ((Group) group).addRequiredMember(role);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createGroup(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		admin.createRole(name, Role.GROUP);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createUser(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		admin.createRole(name, Role.USER);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createRole(String name) throws IOException {
		throw new UnsupportedOperationException("This method is deprecated and does not work");
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getAuthorization(String u) throws IOException {
		if (u == null) {
			throw new IOException("User name must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(u);
		} catch (ClassCastException e) {
			throw new IOException("Not a user: " + u);
		}
		try {
			return new OSGiAuthorization(admin.getAuthorization(user)).asCompositeData();
		} catch (OpenDataException e) {
			throw new IOException("Unable to create open data type: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public TabularData getCredentials(String username) throws IOException {
		if (username == null) {
			throw new IOException("User name must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(username);
		} catch (ClassCastException e) {
			throw new IOException("Not a user: " + username);
		} 
		if (user == null) {
			throw new IOException("Not a user: " + username);
		}
		return OSGiProperties.tableFrom(user.getCredentials());
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getGroup(String groupname) throws IOException {
		if (groupname == null) {
			throw new IOException("Group name must not be null");
		}
		Group group;
		try {
			group = (Group) admin.getRole(groupname);
		} catch (ClassCastException e) {
			throw new IOException("Not a group: " + groupname);
		}
		try {
			return new OSGiGroup(group).asCompositeData();
		} catch (OpenDataException e) {
			throw new IOException("Cannot encode open data for group: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] listGroups() throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(null);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException("Cannot use null filter, apparently: " + e);
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Role role : roles) {
			if (role.getType() == Role.GROUP) {
				groups.add(role.getName());
			}
		}
		return groups.toArray(new String[groups.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getGroups(String filter) throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(filter);
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter: " + e);
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Role role : roles) {
			if (role.getType() == Role.GROUP) {
				groups.add(role.getName());
			}
		}
		return groups.toArray(new String[groups.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getImpliedRoles(String username) throws IOException {
		if (username == null) {
			throw new IOException("Name must not be null");
		}
		Role role = admin.getRole(username);
		if (role.getType() == Role.USER && role instanceof User) {
			return admin.getAuthorization((User) role).getRoles();
		} else {
			return new String[0];
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getMembers(String groupname) throws IOException {
		if (groupname == null) {
			throw new IOException("Name must not be null");
		}
		Group group;
		try {
			group = (Group) admin.getRole(groupname);
		} catch (ClassCastException e) {
			throw new IOException("Not a group: " + groupname);
		}
		Role[] members = group.getMembers();
		if (members == null) {
			return new String[0];
		}
		String[] names = new String[members.length];
		for (int i = 0; i < members.length; i++) {
			names[i] = members[i].getName();
		}
		return names;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public TabularData getProperties(String rolename) throws IOException {
		if (rolename == null) {
			throw new IOException("Name must not be null");
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			return null;
		}
		return OSGiProperties.tableFrom(role.getProperties());
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getRequiredMembers(String groupname) throws IOException {
		if (groupname == null) {
			throw new IOException("Name must not be null");
		}
		Group group;
		try {
			group = (Group) admin.getRole(groupname);
		} catch (ClassCastException e) {
			throw new IOException("Not a group: " + groupname);
		}
		Role[] members = group.getRequiredMembers();
		if (members == null) {
			return new String[0];
		}
		String[] names = new String[members.length];
		for (int i = 0; i < members.length; i++) {
			names[i] = members[i].getName();
		}
		return names;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getRole(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		Role role = admin.getRole(name);
		try {
			return role == null ? null : new OSGiRole(role).asCompositeData();
		} catch (OpenDataException e) {
			throw new IOException("Unable to create open data: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] listRoles() throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(null);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException("Cannot use null filter, apparently: " + e);
		}
		String[] result = new String[roles.length];
		for (int i = 0; i < roles.length; i++) {
			result[i] = roles[i].getName();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getRoles(String filter) throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(filter);
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter: " + e);
		}
		String[] result = new String[roles.length];
		for (int i = 0; i < roles.length; i++) {
			result[i] = roles[i].getName();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getUser(String username) throws IOException {
		if (username == null) {
			throw new IOException("Name must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(username);
		} catch (ClassCastException e) {
			throw new IOException("Not a user: " + username);
		}
		try {
			return user == null ? null : new OSGiUser(user).asCompositeData();
		} catch (OpenDataException e) {
			throw new IOException("Unable to create open data: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserWithProperty(String key, String value) throws IOException {
		if (key == null) {
			throw new IOException("Name must not be null");
		}
		User user = admin.getUser(key, value);
		return user == null ? null : user.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] listUsers() throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(null);
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException("Cannot use null filter, apparently: " + e);
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Role role : roles) {
			if (role.getType() == Role.USER) {
				groups.add(role.getName());
			}
		}
		return groups.toArray(new String[groups.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getUsers(String filter) throws IOException {
		Role[] roles;
		try {
			roles = admin.getRoles(filter);
		} catch (InvalidSyntaxException e) {
			throw new IOException("Invalid filter: " + e);
		}
		ArrayList<String> groups = new ArrayList<String>();
		for (Role role : roles) {
			if (role.getType() == Role.USER) {
				groups.add(role.getName());
			}
		}
		return groups.toArray(new String[groups.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeCredential(String key, String username) throws IOException {
		if (username == null || username.length() == 0) {
			throw new IOException("Name must not be null or empty");
		}
		if (key == null) {
			throw new IOException("Credential key must not be null");
		}
		User user;
		try {
			user = (User) admin.getRole(username);
		} catch (ClassCastException e) {
			throw new IOException("Not a user: " + username);
		}
		if (user == null) {
			return;
		}
		user.getCredentials().remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeMember(String groupname, String rolename) throws IOException {
		if (groupname == null) {
			throw new IOException("Group name must not be null");
		}
		if (rolename == null) {
			throw new IOException("Role name must not be null");
		}
		Group group;
		try {
			group = (Group) admin.getRole(groupname);
		} catch (ClassCastException e) {
			throw new IOException("Not a group: " + groupname);
		}
		if (group == null) {
			return false;
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			return false;
		}
		return group.removeMember(role);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(String key, String rolename) throws IOException {
		if (rolename == null) {
			throw new IOException("Name must not be null");
		}
		Role role = admin.getRole(rolename);
		if (role == null) {
			return;
		}
		role.getProperties().remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeRole(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		return admin.removeRole(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeUser(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		return admin.removeRole(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeGroup(String name) throws IOException {
		if (name == null) {
			throw new IOException("Name must not be null");
		}
		return admin.removeRole(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addListener() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void removeListener() {
		
	}

}
