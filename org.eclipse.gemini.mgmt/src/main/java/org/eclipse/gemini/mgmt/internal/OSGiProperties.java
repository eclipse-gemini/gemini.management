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
 *     Christopher Frost - Refactoring for Spec updates
 ******************************************************************************/

package org.eclipse.gemini.mgmt.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.eclipse.gemini.mgmt.framework.CustomBundleWiringStateMBean;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.jmx.JmxConstants;

/**
 * <p>
 * This class serves as both the documentation of the type structure and as the
 * codification of the mechanism to convert to/from the TabularData.
 * <p>
 * This class represents the CODEC for property dictionaries. As JMX is a rather
 * primitive system and is not intended to be a generic RMI type system, the set
 * of types that can be transfered between the management agent and the managed
 * OSGi container is limited to simple types, arrays of simple types and vectors
 * of simple types. This enforcement is strict and no attempt is made to create
 * a yet another generic serialization mechanism for transferring property
 * values outside of these types.
 * <p>
 * The syntax for the type indicator
 * 
 * <pre>
 * type   ::=    scalar | vector | array 
 * scalar ::=    String | Integer | Long | Float | 
 *               Double | Byte | Short | Character |
 *               Boolean | BigDecimal | BigInteger
 * primitive ::= int | long | float | double | byte | short | 
 *               char | boolean 
 * array ::=     &lt;Array of primitive&gt; | &lt;Array of scalar&gt;
 * vector ::=    Vector of scalar
 * </pre>
 * 
 * The values for Arrays and Vectors are separated by ",".
 * <p>
 * The structure of the composite data for a row in the table is:
 * <table border="1">
 * <tr>
 * <td>Key</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Value</td>
 * <td>String</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>String</td>
 * </tr>
 * </table>
 * <p>
 * The
 */
public final class OSGiProperties {
	
	private static final String VERSION = "Version";
	
	/**
	 * The scalar type
	 */
	private static final List<String> SCALAR_TYPES = Collections.unmodifiableList(Arrays.asList(
		JmxConstants.STRING,
		JmxConstants.INTEGER,
		JmxConstants.LONG,
		JmxConstants.FLOAT,
		JmxConstants.DOUBLE,
		JmxConstants.BYTE,
		JmxConstants.SHORT,
		JmxConstants.CHARACTER,
		JmxConstants.BOOLEAN,
		JmxConstants.BIGDECIMAL,
		JmxConstants.BIGINTEGER));
	
	/**
	 * The primitive types
	 */
	private static final List<String> PRIMITIVE_TYPES = Collections.unmodifiableList(Arrays.asList(
		JmxConstants.P_BYTE,
		JmxConstants.P_CHAR,
		JmxConstants.P_SHORT,
		JmxConstants.P_INT,
		JmxConstants.P_LONG,
		JmxConstants.P_DOUBLE,
		JmxConstants.P_FLOAT));
	
	/**
	 * Answer the tabular data representation of the properties dictionary
	 * 
	 * @param properties
	 * @return the tabular data representation of the properties
	 */
	public static TabularData tableFrom(Dictionary<String, Object> properties) {
		TabularDataSupport table = new TabularDataSupport(JmxConstants.PROPERTIES_TYPE);
		if (properties != null) {
			for (Enumeration<?> keys = properties.keys(); keys.hasMoreElements();) {
				String key = (String) keys.nextElement();
				table.put(encode(key, properties.get(key)));
			}
		}
		return table;
	}

	/**
	 * Answer the tabular data representation of the service references
	 * properties
	 * 
	 * @param ref
	 * @return the tabular data representing the service reference properties
	 */
	public static TabularData tableFrom(ServiceReference<?> ref) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		for (String key : ref.getPropertyKeys()) {
			props.put(key, ref.getProperty(key));
		}
		return tableFrom(props);
	}

	/**
	 * Encode the key and value as composite data
	 * 
	 * @param key
	 * @param value
	 * @return the encoded composite data of the key and value
	 */
	public static CompositeData encode(String key, Object value) {
		Class<?> clazz = value.getClass();
		if (clazz.isArray()) {
			return encodeArray(key, value, clazz.getComponentType());
		} else if (clazz.equals(Vector.class)) {
			return encodeVector(key, (Vector<?>) value);
		}
		return propertyData(key, value.toString(), typeOf(clazz));
	}

	/**
	 * Answer the hashtable converted from the supplied tabular data
	 * 
	 * @param table
	 * @return the hashtable represented by the tabular data
	 */
	@SuppressWarnings("unchecked")
	public static Dictionary<String, Object> propertiesFrom(TabularData table) {
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		if (table == null) {
			return props;
		}
		for (CompositeData data : (Collection<CompositeData>) table.values()) {
			props.put((String) data.get(JmxConstants.KEY), parse((String) data.get(JmxConstants.VALUE), (String) data.get(JmxConstants.TYPE)));
		}
		return props;
	}
	
	/**
	 * Convert a key-value directive in to the required format for representation over JMX
	 * 
	 * @param key
	 * @param value
	 * @return a map of key to the key and value to the value
	 */
	public static Map<String, ?> getDirectiveKeyValueItem(String key, Object value){
		Map<String, Object> items = new HashMap<String, Object>();
		items.put(CustomBundleWiringStateMBean.KEY, key);
		items.put(CustomBundleWiringStateMBean.VALUE, value);
		return items;
	}
	
	/**
	 * Encode the array as composite data
	 * 
	 * @param key
	 * @param value
	 * @param componentClazz
	 * @return the composite data representation
	 */
	private static CompositeData encodeArray(String key, Object value, Class<?> componentClazz) {
		StringBuffer buf = new StringBuffer();
		if (Integer.TYPE.equals(componentClazz)) {
			int[] array = (int[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Long.TYPE.equals(componentClazz)) {
			long[] array = (long[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Double.TYPE.equals(componentClazz)) {
			double[] array = (double[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Float.TYPE.equals(componentClazz)) {
			float[] array = (float[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Byte.TYPE.equals(componentClazz)) {
			byte[] array = (byte[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Short.TYPE.equals(componentClazz)) {
			short[] array = (short[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Character.TYPE.equals(componentClazz)) {
			char[] array = (char[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else if (Boolean.TYPE.equals(componentClazz)) {
			boolean[] array = (boolean[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		} else {
			Object[] array = (Object[]) value;
			for (int i = 0; i < array.length; i++) {
				buf.append(array[i]);
				if (i < array.length - 1) {
					buf.append(',');
				}
			}
		}
		String type = typeOf(componentClazz);
		return propertyData(key, buf.toString(), "Array of " + type);
	}

	/**
	 * Encode the vector as composite data
	 * 
	 * @param key
	 * @param value
	 * @return the composite data representation
	 */
	private static CompositeData encodeVector(String key, Vector<?> value) {
		String type = JmxConstants.STRING;
		if (value.size() > 0) {
			type = typeOf(value.get(0).getClass());
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < value.size(); i++) {
			buf.append(value.get(i));
			if (i < value.size() - 1) {
				buf.append(',');
			}
		}
		return propertyData(key, buf.toString(), "Vector of " + type);
	}
	
	/**
	 * Answer the string type of the class
	 * 
	 * @param clazz
	 * @return the string type of the class
	 */
	private static String typeOf(Class<?> clazz) {
		if (clazz.equals(String.class)) {
			return JmxConstants.STRING;
		}
		if (clazz.equals(Version.class)) {
			return VERSION;
		}
		if (clazz.equals(Integer.class)) {
			return JmxConstants.INTEGER;
		}
		if (clazz.equals(Long.class)) {
			return JmxConstants.LONG;
		}
		if (clazz.equals(Double.class)) {
			return JmxConstants.DOUBLE;
		}
		if (clazz.equals(Double.class)) {
			return JmxConstants.FLOAT;
		}
		if (clazz.equals(Byte.class)) {
			return JmxConstants.BYTE;
		}
		if (clazz.equals(Short.class)) {
			return JmxConstants.SHORT;
		}
		if (clazz.equals(Character.class)) {
			return JmxConstants.CHARACTER;
		}
		if (clazz.equals(Boolean.class)) {
			return JmxConstants.BOOLEAN;
		}
		if (clazz.equals(BigDecimal.class)) {
			return JmxConstants.BIGDECIMAL;
		}
		if (clazz.equals(BigInteger.class)) {
			return JmxConstants.BIGINTEGER;
		}
		if (clazz.equals(Integer.TYPE)) {
			return JmxConstants.P_INT;
		}
		if (clazz.equals(Long.TYPE)) {
			return JmxConstants.P_LONG;
		}
		if (clazz.equals(Double.TYPE)) {
			return JmxConstants.P_DOUBLE;
		}
		if (clazz.equals(Double.TYPE)) {
			return JmxConstants.P_FLOAT;
		}
		if (clazz.equals(Byte.TYPE)) {
			return JmxConstants.P_BYTE;
		}
		if (clazz.equals(Short.TYPE)) {
			return JmxConstants.P_SHORT;
		}
		if (clazz.equals(Character.TYPE)) {
			return JmxConstants.P_CHAR;
		}
		if (clazz.equals(Boolean.TYPE)) {
			return JmxConstants.P_BOOLEAN;
		}
		throw new IllegalArgumentException("Illegal type: " + clazz);
	}

	/**
	 * Answer the composite data representation of the key/value pair
	 * 
	 * @param key
	 * @param value
	 * @param type
	 * @return the composite data representation of the key/value pair
	 */
	private static CompositeData propertyData(String key, String value, String type) {
		Map<String, Object> items = new HashMap<String, Object>(); 
		items.put(JmxConstants.KEY, key);
		items.put(JmxConstants.VALUE, value);
		items.put(JmxConstants.TYPE, type);
		try {
			return new CompositeDataSupport(JmxConstants.PROPERTY_TYPE, items);
		} catch (OpenDataException e) {
			throw new IllegalStateException("Cannot form property open data", e);
		}
	}

	/**
	 * Parse the string value into an Object
	 * 
	 * @param value
	 * @param type
	 * @return the object represented by the String
	 */
	private static Object parse(String value, String type) {
		StringTokenizer tokens = new StringTokenizer(type);
		if (!tokens.hasMoreElements()) {
			throw new IllegalArgumentException("Type is empty");
		}
		String token = tokens.nextToken();
		if ("Array".equals(token)) {
			return parseArray(value, tokens);
		}
		if ("Vector".equals(token)) {
			return parseVector(value, tokens);
		}
		if (SCALAR_TYPES.contains(token) || PRIMITIVE_TYPES.contains(token)) {
			return parseValue(value, token);
		}
		throw new IllegalArgumentException("Unknown type: " + type);
	}

	/**
	 * Parse the array represented by the string value
	 * 
	 * @param value
	 * @param tokens
	 * @return the array represented by the string value
	 */
	private static Object parseArray(String value, StringTokenizer tokens) {
		if (!tokens.hasMoreTokens()) {
			throw new IllegalArgumentException("Expecting <of> token in Array type");
		}
		if (!"of".equals(tokens.nextToken())) {
			throw new IllegalArgumentException("Expecting <of> token in Array type");
		}
		if (!tokens.hasMoreTokens()) {
			throw new IllegalArgumentException("Expecting <primitive>|<scalar> token in Array type");
		}
		String type = tokens.nextToken();
		if (SCALAR_TYPES.contains(type)) {
			return parseScalarArray(value, type);
		} else if (PRIMITIVE_TYPES.contains(type)) {
			return parsePrimitiveArray(value, type);
		} else {
			throw new IllegalArgumentException("Expecting <scalar>|<primitive> type token in Array type: " + type);
		}
	}

	/**
	 * Parse the vector represented by the supplied string value
	 * 
	 * @param value
	 * @param tokens
	 * @return the vector represented by the supplied string value
	 */
	private static Object parseVector(String value, StringTokenizer tokens) {
		if (!tokens.hasMoreTokens()) {
			throw new IllegalArgumentException("Expecting <of> token in Vector type");
		}
		if (!tokens.nextElement().equals("of")) {
			throw new IllegalArgumentException("Expecting <of> token in Vector type");
		}
		if (!tokens.hasMoreTokens()) {
			throw new IllegalArgumentException("Expecting <scalar> token in Vector type");
		}
		String type = tokens.nextToken();
		StringTokenizer values = new StringTokenizer(value, ",");
		Vector<Object> vector = new Vector<Object>();
		if (!SCALAR_TYPES.contains(type)) {
			throw new IllegalArgumentException("Expecting <scalar> type token in Vector type: " + type);
		}
		while (values.hasMoreTokens()) {
			vector.add(parseScalar(values.nextToken().trim(), type));
		}
		return vector;
	}

	/**
	 * Parse the array represented by the string value
	 * 
	 * @param value
	 * @param type
	 * @return the array represented by the string value
	 */
	private static Object[] parseScalarArray(String value, String type) {
		ArrayList<Object> array = new ArrayList<Object>();
		StringTokenizer values = new StringTokenizer(value, ",");
		while (values.hasMoreTokens()) {
			array.add(parseScalar(values.nextToken().trim(), type));
		}
		return array.toArray(createScalarArray(type, array.size()));
	}

	/**
	 * Parse the array from the supplied values
	 * 
	 * @param value
	 * @param type
	 * @return the array from the supplied values
	 */
	private static Object parsePrimitiveArray(String value, String type) {
		StringTokenizer values = new StringTokenizer(value, ",");
		if (JmxConstants.P_INT.equals(type)) {
			int[] array = new int[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Integer.parseInt(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_LONG.equals(type)) {
			long[] array = new long[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Long.parseLong(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_DOUBLE.equals(type)) {
			double[] array = new double[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Double.parseDouble(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_FLOAT.equals(type)) {
			float[] array = new float[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Float.parseFloat(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_BYTE.equals(type)) {
			byte[] array = new byte[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Byte.parseByte(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_SHORT.equals(type)) {
			short[] array = new short[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Short.parseShort(values.nextToken().trim());
			}
			return array;
		}
		if (JmxConstants.P_CHAR.equals(type)) {
			char[] array = new char[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = values.nextToken().trim().charAt(0);
			}
			return array;
		}
		if (JmxConstants.P_BOOLEAN.equals(type)) {
			boolean[] array = new boolean[values.countTokens()];
			int i = 0;
			while (values.hasMoreTokens()) {
				array[i++] = Boolean.parseBoolean(values.nextToken().trim());
			}
			return array;
		}
		throw new IllegalArgumentException("Unknown primitive type: " + type);
	}
	
	/**
	 * Create the scalar array from the supplied type
	 * 
	 * @param type
	 * @param size
	 * @return the scalar array from the supplied type
	 */
	private static Object[] createScalarArray(String type, int size) {
		if (JmxConstants.STRING.equals(type)) {
			return new String[size];
		}
		if (VERSION.equals(type)) {
			return new Version[size];
		}
		if (JmxConstants.INTEGER.equals(type)) {
			return new Integer[size];
		}
		if (JmxConstants.LONG.equals(type)) {
			return new Long[size];
		}
		if (JmxConstants.DOUBLE.equals(type)) {
			return new Double[size];
		}
		if (JmxConstants.FLOAT.equals(type)) {
			return new Float[size];
		}
		if (JmxConstants.BYTE.equals(type)) {
			return new Byte[size];
		}
		if (JmxConstants.SHORT.equals(type)) {
			return new Short[size];
		}
		if (JmxConstants.CHARACTER.equals(type)) {
			return new Character[size];
		}
		if (JmxConstants.BOOLEAN.equals(type)) {
			return new Boolean[size];
		}
		if (JmxConstants.BIGDECIMAL.equals(type)) {
			return new BigDecimal[size];
		}
		if (JmxConstants.BIGINTEGER.equals(type)) {
			return new BigInteger[size];
		}
		throw new IllegalArgumentException("Unknown scalar type: " + type);
	}

	/**
	 * Construct the scalar value represented by the string
	 * 
	 * @param value
	 * @param type
	 * @return the scalar value represented by the string
	 */
	private static Object parseScalar(String value, String type) {
		if (JmxConstants.STRING.equals(type)) {
			return value;
		}
		if (VERSION.equals(type)) {
			return Version.parseVersion(value);
		}
		if (JmxConstants.INTEGER.equals(type)) {
			return Integer.parseInt(value);
		}
		if (JmxConstants.LONG.equals(type)) {
			return Long.parseLong(value);
		}
		if (JmxConstants.DOUBLE.equals(type)) {
			return Double.parseDouble(value);
		}
		if (JmxConstants.FLOAT.equals(type)) {
			return Float.parseFloat(value);
		}
		if (JmxConstants.BYTE.equals(type)) {
			return Byte.parseByte(value);
		}
		if (JmxConstants.SHORT.equals(type)) {
			return Short.parseShort(value);
		}
		if (JmxConstants.CHARACTER.equals(type)) {
			return value.charAt(0);
		}
		if (JmxConstants.BOOLEAN.equals(type)) {
			return Boolean.parseBoolean(value);
		}
		if (JmxConstants.BIGDECIMAL.equals(type)) {
			return new BigDecimal(value);
		}
		if (JmxConstants.BIGINTEGER.equals(type)) {
			return new BigInteger(value);
		}
		throw new IllegalArgumentException("Unknown scalar type: " + type);
	}

	/**
	 * Construct the scalar value represented by the string
	 * 
	 * @param value
	 * @param type
	 * @return the scalar value represented by the string
	 */
	private static Object parseValue(String value, String type) {
		try{
			return parseScalar(value, type);
		}catch (IllegalArgumentException e) {
			if (JmxConstants.P_INT.equals(type)) {
				return Integer.parseInt(value);
			}
			if (JmxConstants.P_LONG.equals(type)) {
				return Long.parseLong(value);
			}
			if (JmxConstants.P_DOUBLE.equals(type)) {
				return Double.parseDouble(value);
			}
			if (JmxConstants.P_FLOAT.equals(type)) {
				return Float.parseFloat(value);
			}
			if (JmxConstants.P_BYTE.equals(type)) {
				return Byte.parseByte(value);
			}
			if (JmxConstants.P_SHORT.equals(type)) {
				return Short.parseShort(value);
			}
			if (JmxConstants.P_CHAR.equals(type)) {
				return value.charAt(0);
			}
			if (JmxConstants.P_BOOLEAN.equals(type)) {
				return Boolean.parseBoolean(value);
			}
			throw new IllegalArgumentException("Unknown scalar type: " + type);
		}
	}
	
}
