/*
 * Copyright (c) OSGi Alliance (2010, 2011). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.gemini.mgmt.framework;

import java.io.IOException;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

import org.osgi.jmx.Item;
import org.osgi.jmx.JmxConstants;

/**
 * This MBean represents the bundle wiring state.
 *
 * Note that not all information from the BundleWiring Java API is provided.
 *
 * Particularly, the limitations are:
 *  - Cannot retrieve references to resources (e.g. class) of a particular bundle wiring.
 */
public interface CustomBundleWiringStateMBean {

    String OBJECTNAME = JmxConstants.OSGI_CORE + ":type=wiringState,version=1.0";

    /**
     * To be specified on any operation that takes a 'namespace' argument when results from all namespaces are wanted.
     */
    String ALL_NAMESPACE = "osgi.wiring.all";
    
    /**
     * Types and Items for the capabilities and requirements of a bundle
     */
    
    String KEY = "Key";
    Item KEY_ITEM = new Item(KEY, "The property key", SimpleType.STRING);

    String VALUE = "Value";
    Item VALUE_ITEM = new Item(VALUE, "The property value", SimpleType.STRING);

    CompositeType PROPERTY_TYPE = Item.compositeType("PROPERTY", "Describes a single directive or attribute of a capability or requirement", KEY_ITEM, VALUE_ITEM);

    TabularType DIRECTIVES_TYPE = Item.tabularType("DIRECTIVES", "Describes the directives of a capability or requirement", PROPERTY_TYPE, KEY);

    TabularType ATTRIBUTES_TYPE = Item.tabularType("ATTRIBUTES", "Describes attributes of a capability or requirement", JmxConstants.PROPERTY_TYPE, JmxConstants.KEY);
    
    String DIRECTIVES = "Directives";
    Item DIRECTIVES_ITEM = new Item(DIRECTIVES, "The directives of a capability or requirement", DIRECTIVES_TYPE);

    String ATTRIBUTES = "Attributes";
    Item ATTRIBUTES_ITEM = new Item(ATTRIBUTES, "The attributes of a capability or requirement", ATTRIBUTES_TYPE);

    String NAMESPACE = "Namespace";
    Item NAMESPACE_ITEM = new Item(NAMESPACE, "The namespace of a capability or requirement", SimpleType.STRING);
    
    /**
     * Types for Bundle requirements and capabilities
     */
    CompositeType BUNDLE_REQUIREMENT_TYPE = Item.compositeType("BUNDLE_REQUIREMENT", "Describes the live wired requirements of a bundle", 
    			ATTRIBUTES_ITEM, 
				DIRECTIVES_ITEM, 
				NAMESPACE_ITEM);

	@SuppressWarnings("unchecked")
	ArrayType<CompositeData> REQUIREMENT_ARRAY_TYPE = Item.arrayType(1, BUNDLE_REQUIREMENT_TYPE);
	
	CompositeType BUNDLE_CAPABILITY_TYPE = Item.compositeType("BUNDLE_CAPABILITY", "Describes the live wired capabilities of a bundle", 
				ATTRIBUTES_ITEM, 
				DIRECTIVES_ITEM, 
				NAMESPACE_ITEM);
	
	@SuppressWarnings("unchecked")
	ArrayType<CompositeData> CAPABILITY_ARRAY_TYPE = Item.arrayType(1, BUNDLE_CAPABILITY_TYPE);
	
	/**
	 * Common items
	 */

    String BUNDLE_REVISION_ID = "BundleRevisionId";
    Item BUNDLE_REVISION_ID_ITEM = new Item(BUNDLE_REVISION_ID, "The local identifier of the bundle revision", SimpleType.INTEGER);
    
	/**
	 * For a single bundle, the requirements of each revision
	 */

    String REQUIREMENTS = "Requirements";
    Item REQUIREMENTS_ITEM = new Item(REQUIREMENTS, "The bundle requirements of a bundle revision wiring", REQUIREMENT_ARRAY_TYPE);

    CompositeType BUNDLE_REVISION_REQUIREMENTS_TYPE = Item.compositeType("BUNDLE_REVISION_REQUIREMENTS", "Describes the requirements for a bundle revision", 
    			BUNDLE_REVISION_ID_ITEM, 
    			REQUIREMENTS_ITEM);

    TabularType BUNDLE_REVISIONS_REQUIREMENTS_TYPE =  Item.tabularType("REVISIONS_REQUIREMENTS", "The bundle requirements for all bundle revisions", BUNDLE_REVISION_REQUIREMENTS_TYPE, BUNDLE_REVISION_ID);

	/**
	 * For a single bundle, the capabilities of each revision
	 */

    String CAPABILITIES = "Capabilities";
    Item CAPABILITIES_ITEM = new Item(CAPABILITIES, "The bundle capabilities of a bundle revision wiring", CAPABILITY_ARRAY_TYPE);
    
    CompositeType BUNDLE_REVISION_CAPABILITIES_TYPE = Item.compositeType("BUNDLE_REVISION_CAPABILITIES", "Describes the capabilities for a bundle revision",
    			BUNDLE_REVISION_ID_ITEM, 
    			CAPABILITIES_ITEM);

    TabularType BUNDLE_REVISIONS_CAPABILITIES_TYPE = Item.tabularType("REVISIONS_CAPABILITIES", "The bundle capabilities for all bundle revisions", BUNDLE_REVISION_CAPABILITIES_TYPE, BUNDLE_REVISION_ID);
 
    /**
     * Items for WIRE_TYPE
     */
    String BUNDLE_REQUIREMENT = "BundleRequirement";
    Item BUNDLE_REQUIREMENT_ITEM = new Item(BUNDLE_REQUIREMENT, "The wired requirements of a bundle", BUNDLE_REQUIREMENT_TYPE);

    String BUNDLE_CAPABILITY = "BundleCapability";
    Item BUNDLE_CAPABILITY_ITEM = new Item(BUNDLE_CAPABILITY, "The wired capabilities of a bundle", BUNDLE_CAPABILITY_TYPE);

    String PROVIDER_BUNDLE_ID = "ProviderBundleId";
    Item PROVIDER_BUNDLE_ID_ITEM = new Item(PROVIDER_BUNDLE_ID, "The identifier of the bundle that is the provider of the capability", SimpleType.LONG);
    
    String PROVIDER_BUNDLE_REVISION_ID = "ProviderBundleRevisionId";
    Item PROVIDER_BUNDLE_REVISION_ID_ITEM = new Item(PROVIDER_BUNDLE_REVISION_ID, "A local id for the bundle revision that is the provider of the capability", SimpleType.INTEGER);

    String REQUIRER_BUNDLE_ID = "RequirerBundleId";
    Item REQUIRER_BUNDLE_ID_ITEM = new Item(REQUIRER_BUNDLE_ID, "The identifier of the bundle that is the requirer of the requirement", SimpleType.LONG);
    
    String REQUIRER_BUNDLE_REVISION_ID = "RequirerBundleRevisionId";
    Item REQUIRER_BUNDLE_REVISION_ID_ITEM =  new Item(REQUIRER_BUNDLE_REVISION_ID, "A local id for the bundle revision that is the requirer of the requirement", SimpleType.INTEGER);
    
    /**
     * Describes a single bundle wire between a provider of a capability and a requirer of the corresponding requirement.
     */
    CompositeType BUNDLE_WIRE_TYPE = Item.compositeType("BUNDLE_WIRE", "Describes the live association between a provider and a requirer",
                BUNDLE_REQUIREMENT_ITEM,
                BUNDLE_CAPABILITY_ITEM,
                PROVIDER_BUNDLE_ID_ITEM,
                REQUIRER_BUNDLE_ID_ITEM);

	@SuppressWarnings("unchecked")
	ArrayType<CompositeData> BUNDLE_WIRE_ARRAY_TYPE = Item.arrayType(1, BUNDLE_WIRE_TYPE);

    /**
     * Describes a single bundle wire between a provider of a capability and a requirer of the corresponding requirement where the providers revision is versioned and the requirerers revision is versioned.
     */
    CompositeType BUNDLE_REVISION_WIRE_TYPE = Item.compositeType("BUNDLE_REVISION_WIRE", "Describes the live association between a provider and a requirer",
                BUNDLE_REQUIREMENT_ITEM,
                BUNDLE_CAPABILITY_ITEM,
                PROVIDER_BUNDLE_ID_ITEM,
                PROVIDER_BUNDLE_REVISION_ID_ITEM,
                REQUIRER_BUNDLE_ID_ITEM,
                REQUIRER_BUNDLE_REVISION_ID_ITEM);

	@SuppressWarnings("unchecked")
	ArrayType<CompositeData> BUNDLE_REVISION_WIRE_ARRAY_TYPE = Item.arrayType(1, BUNDLE_REVISION_WIRE_TYPE);
    
	/**
	 * For bundle wirings
	 */
    
    String PROVIDED_WIRES = "ProvidedWires";
    Item PROVIDED_WIRES_ITEM = new Item(PROVIDED_WIRES, "The bundle wires to the capabilities provided by this bundle wiring.", BUNDLE_WIRE_ARRAY_TYPE);

    String REQUIRED_WIRES = "RequiredWires";
    Item REQUIRED_WIRES_ITEM = new Item(REQUIRED_WIRES, "The bundle wires to requirements in use by this bundle wiring", BUNDLE_WIRE_ARRAY_TYPE);
    
    String REVISION_PROVIDED_WIRES = "RevisionProvidedWires";
    Item REVISION_PROVIDED_WIRES_ITEM = new Item(REVISION_PROVIDED_WIRES, "The bundle wires to the capabilities provided by this bundle revision wiring.", BUNDLE_REVISION_WIRE_ARRAY_TYPE);

    String REVISION_REQUIRED_WIRES = "RevisionRequiredWires";
    Item REVISION_REQUIRED_WIRES_ITEM = new Item(REVISION_REQUIRED_WIRES, "The bundle wires to requirements in use by this bundle revision wiring", BUNDLE_REVISION_WIRE_ARRAY_TYPE);
    
    CompositeType BUNDLE_WIRING_TYPE = Item.compositeType("BUNDLE_WIRING", "Describes the runtime association between a provider and a requirer",
            REQUIREMENTS_ITEM,            /* REQUIREMENT_TYPE [] */
            CAPABILITIES_ITEM,            /* CAPABILITIES_TYPE [] */
            REQUIRED_WIRES_ITEM,          /* BUNDLE_WIRE_TYPE [] */
            PROVIDED_WIRES_ITEM);         /* BUNDLE_WIRE_TYPE [] */
    
    CompositeType BUNDLE_REVISION_WIRING_TYPE = Item.compositeType("BUNDLE_REVISION_WIRING", "Describes the runtime association between a provider and a requirer",
            BUNDLE_REVISION_ID_ITEM,      /* Integer (local scope) */
            REQUIREMENTS_ITEM,            /* REQUIREMENT_TYPE [] */
            CAPABILITIES_ITEM,            /* CAPABILITIES_TYPE [] */
            REVISION_REQUIRED_WIRES_ITEM, /* BUNDLE_REVISION_WIRES_TYPE [] */
            REVISION_PROVIDED_WIRES_ITEM);/* BUNDLE_REVISION_WIRES_TYPE [] */
    TabularType BUNDLE_REVISIONS_WIRINGS_TYPE = Item.tabularType("BUNDLE_REVISIONS_WIRINGS", "A bundle wiring for each revision of a bundle", BUNDLE_REVISION_WIRING_TYPE, BUNDLE_REVISION_ID);

    String BUNDLE_ID = "BundleId";
    Item BUNDLE_ID_ITEM = new Item(BUNDLE_ID, "The bundle identifier of the bundle revision", SimpleType.LONG);
    
    CompositeType BUNDLE_REVISION_WIRING_CLOSURE_TYPE = Item.compositeType("BUNDLE_REVISION_WIRING_CLOSURE", "Describes the runtime association between a provider and a requirer",
            BUNDLE_ID_ITEM,               /* Long */
            BUNDLE_REVISION_ID_ITEM,      /* Integer (local scope) */
            REQUIREMENTS_ITEM,            /* REQUIREMENT_TYPE [] */
            CAPABILITIES_ITEM,            /* CAPABILITIES_TYPE [] */
            REVISION_REQUIRED_WIRES_ITEM, /* BUNDLE_REVISION_WIRES_TYPE [] */
            REVISION_PROVIDED_WIRES_ITEM);/* BUNDLE_REVISION_WIRES_TYPE [] */
    TabularType BUNDLE_REVISIONS_WIRINGS_CLOSURES_TYPE = Item.tabularType("BUNDLE_REVISIONS_WIRING_CLOSURES_TYPE", "A transitivly complete set of bundle wirings for each revision of a bundle", BUNDLE_REVISION_WIRING_CLOSURE_TYPE, BUNDLE_ID, BUNDLE_REVISION_ID);
    
    // ****** START METHODS ******
    
    /**
     * Returns the requirements for the current bundle revision.
     * The Array is typed by the {@link #REQUIREMENT_ARRAY_TYPE}.
     *
     * @param bundleId
     * @param namespace the namespace of the requirements involved in this revision.
     * @return the declared requirements for the current revision of <code>bundleId</code> and <code>namespace</code>
     */
    CompositeData[] getCurrentRevisionDeclaredRequirements(long bundleId, String namespace) throws IOException;

    /**
     * Returns the capabilities for the current bundle revision.
     * The Array is typed by the {@link #CAPABILITY_ARRAY_TYPE}
     *
     * @param bundleId
     * @param namespace the namespace of the capabilities involved in this revision.
     * @return the declared capabilities for the current revision of <code>bundleId</code> and <code>namespace</code>
     */
    CompositeData[] getCurrentRevisionDeclaredCapabilities(long bundleId, String namespace) throws IOException;

    /**
     * Returns the bundle wiring for the current bundle revision.
     * The CompositeData is typed by the {@link #BUNDLE_WIRING_TYPE}
     *
     * @param bundleId
     * @param namespace the namespace of the capabilities and requirements involved in this wiring.
     * @return the wires for the current revision of <code>bundleId</code> and <code>namespace</code>
     */
    CompositeData getCurrentWiring(long bundleId, String namespace) throws IOException;

    /**
     * Returns the bundle wiring closure for the current revision of the specified bundle.
     * The TabularData is typed by the {@link #BUNDLE_REVISIONS_WIRINGS_CLOSURES_TYPE}.
     *
     * @param rootBundleId the root bundle of the closure.
     * @param namespace the namespace of the capabilities and requirements involved in this wiring closure.
     * @return a tabular representation of all the wiring in the closure. The bundle revision ids
     * only have meaning in the context of the current result. The revision of the rootBundle is set
     * to 0.
     */
    TabularData getCurrentWiringClosure(long rootBundleId, String namespace) throws IOException;

    /**
     * Returns the requirements for all revisions of the bundle.
     * The TabularData is typed by the {@link #BUNDLE_REVISIONS_REQUIREMENTS_TYPE}.
     * The requirements are in no particular order, and may change in
     * subsequent calls to this operation.
     *
     * @param bundleId
     * @param namespace the namespace of the requirements involved in these revisions.
     * @return the declared requirements for all revisions of <code>bundleId</code>
     *
     */
    TabularData getRevisionsDeclaredRequirements(long bundleId, String namespace) throws IOException;

    /**
     * Returns the capabilities for all revisions of the bundle.
     * The TabularData is typed by the {@link #BUNDLE_REVISIONS_CAPABILITIES_TYPE}
     * The capabilities are in no particular order, and may change in
     * subsequent calls to this operation.
     *
     * @param bundleId
     * @param namespace the namespace of the capabilities involved in these revisions.
     * @return the declared capabilities for all revisions of <code>bundleId</code>
     */
    TabularData getRevisionsDeclaredCapabilities(long bundleId, String namespace) throws IOException;

    /**
     * Returns the bundle wirings for all revisions of the bundle.
     * The TabularData is typed by the {@link #BUNDLE_REVISIONS_WIRINGS_TYPE}
     * The bundle wirings are in no particular order, and may change in 
     * subsequent calls to this operations.
     *
     * @param bundleId
     * @param namespace the namespace of the capabilities and requirements involved in these revisions wirings.
     * @return the wires for all revisions of <code>bundleId</code>
     */
    TabularData getRevisionsWiring(long bundleId, String namespace) throws IOException;

    /**
     * Returns a closure of all bundle wirings for all revisions of the 
     * bundle linked by their bundle wires, starting at <code>rootBundleId</code>.
     * 
     * The TabularData is typed by the {@link #BUNDLE_REVISIONS_WIRING_CLOSURES_TYPE}
     * The bundle wirings are in no particular order, and may change in subsequent 
     * calls to this operation. Furthermore, the bundle revision IDs are local and 
     * cannot be reused across invocations. The revision of the rootBundle is set
     * to 0.
     *
     * @param rootBundleId the root bundle of the closure.
     * @param namespace the namespace of the capabilities and requirements involved in these revisions wirings closures.
     * @return a closure of bundle wirings linked together by wires.
     */
    TabularData getRevisionsWiringClosure(long rootBundleId, String namespace) throws IOException;

}