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

package org.eclipse.gemini.management;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.eclipse.gemini.management.configurationadmin.ConfigAdminManager;
import org.eclipse.gemini.management.framework.BundleState;
import org.eclipse.gemini.management.framework.BundleWiringState;
import org.eclipse.gemini.management.framework.CustomServiceStateMBean;
import org.eclipse.gemini.management.framework.Framework;
import org.eclipse.gemini.management.framework.PackageState;
import org.eclipse.gemini.management.framework.ServiceState;
import org.eclipse.gemini.management.permissionadmin.PermissionManager;
import org.eclipse.gemini.management.provisioning.Provisioning;
import org.eclipse.gemini.management.useradmin.UserManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.jmx.framework.PackageStateMBean;
import org.osgi.jmx.framework.wiring.BundleWiringStateMBean;
import org.osgi.jmx.service.cm.ConfigurationAdminMBean;
import org.osgi.jmx.service.permissionadmin.PermissionAdminMBean;
import org.osgi.jmx.service.provisioning.ProvisioningServiceMBean;
import org.osgi.jmx.service.useradmin.UserAdminMBean;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;
import org.osgi.service.permissionadmin.PermissionAdmin;
import org.osgi.service.provisioning.ProvisioningService;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The bundle activator which starts and stops the system, as well as providing the service tracker which listens for
 * the MBeanServer. When the MBeanServer is found, the MBeans representing the OSGi services will be installed.
 * 
 */
@SuppressWarnings("deprecation")
public class Activator implements BundleActivator {
	
	private final List<MBeanServer> mbeanServers = new CopyOnWriteArrayList<MBeanServer>();
	
	private final AtomicBoolean servicesRegistered = new AtomicBoolean(false);
	
    private ObjectNameTranslator objectNameTranslator;
	
	private ObjectName frameworkName;
	
	private ObjectName bundleStateName;
	
	private ObjectName bundleWiringStateName;
	
	private ObjectName packageStateName;
	
	private ObjectName serviceStateName;
	
	private ObjectName configAdminName;
	
	private ObjectName permissionAdminName;
	
	private ObjectName provisioningServiceName;
	
	private ObjectName userAdminName;
	
	private ServiceTracker<MBeanServer, ?> mbeanServiceTracker;
	
	private BundleContext bundleContext = null;
	
	private StandardMBean framework;
	
	private StandardMBean bundleState;
	
	private StandardMBean bundleWiringState;
	
	private StandardMBean packageState;
	
	private StandardMBean serviceState;
	
	private ServiceTracker<ConfigurationAdmin, ?> configAdminTracker;
	
	private ServiceTracker<PermissionAdmin, ?> permissionAdminTracker;
	
	private ServiceTracker<ProvisioningService, ?> provisioningServiceTracker;
	
	private ServiceTracker<UserAdmin, ?> userAdminTracker;
	
	private ServiceTracker<LogService, ?> logServiceTracker;
	
	private LogService logger;
	
	private void log (int level, String message) {
		if (logger != null) {
			logger.log(level, message);
		}
	}
	
	private void log (int level, String message, Throwable t) {
		if (logger != null) {
			logger.log(level, message, t);
		}
	}

	private void createObjectNames() {
		try {
			frameworkName = translateObjectName(FrameworkMBean.OBJECTNAME);
			bundleStateName = translateObjectName(BundleStateMBean.OBJECTNAME);
			bundleWiringStateName = translateObjectName(BundleWiringStateMBean.OBJECTNAME);
			serviceStateName = translateObjectName(CustomServiceStateMBean.OBJECTNAME);
			packageStateName = translateObjectName(PackageStateMBean.OBJECTNAME);
			configAdminName = translateObjectName(ConfigurationAdminMBean.OBJECTNAME);
			permissionAdminName = translateObjectName(PermissionAdminMBean.OBJECTNAME);
			provisioningServiceName = translateObjectName(ProvisioningServiceMBean.OBJECTNAME);
			userAdminName = translateObjectName(UserAdminMBean.OBJECTNAME);		
		} catch (Exception e) {
			throw new IllegalStateException("Unable to start Gemini Management, Object name creation failed.", e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void start(BundleContext bundleContext) throws Exception {
		logServiceTracker = new ServiceTracker<LogService, Object>(bundleContext, LogService.class, new LogServiceTracker());
		logServiceTracker.open();
        objectNameTranslator = DefaultObjectNameTranslator.initialiseObjectNameTranslator(bundleContext, logger);
        createObjectNames();
		this.bundleContext = bundleContext;
		registerDefaultMBeanServer();
		this.mbeanServiceTracker = new ServiceTracker<MBeanServer, Object>(this.bundleContext, MBeanServer.class, new MBeanServiceTracker());
		log(LogService.LOG_INFO, "Awaiting initial MBeanServer service registration");
		this.mbeanServiceTracker.open();
	}
	
    private ObjectName translateObjectName(String objectName) throws MalformedObjectNameException {
        return this.objectNameTranslator.translate(new ObjectName(objectName));
    }
    
    private void registerDefaultMBeanServer () {
    	if ("false".equals(bundleContext.getProperty("register.default.mbeanserver"))) {
    		return;
    	}
    	
    	ServiceReference<MBeanServer> ref = bundleContext.getServiceReference(MBeanServer.class);
		if (ref == null) {
			bundleContext.registerService(MBeanServer.class.getCanonicalName(), ManagementFactory.getPlatformMBeanServer(), null);
		}
    }


	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext arg0) throws Exception {
		mbeanServiceTracker.close();
		for (MBeanServer mbeanServer : mbeanServers) {
			deregisterServices(mbeanServer);
		}
		mbeanServers.clear();
		logServiceTracker.close();
	}

	/**
	 * Unregister all MBeans from a MBean server
	 * 
	 * @param mbeanServer MBean Server to unregister the MBeans from
     */
	private synchronized void deregisterServices(MBeanServer mbeanServer) {
		if (!servicesRegistered.get()) {
			return;
		}
		log(LogService.LOG_INFO, "Deregistering framework with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(frameworkName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_INFO, "FrameworkMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "FrameworkMBean deregistration problem", e);
		}
		framework = null;

		log(LogService.LOG_INFO, "Deregistering bundle state with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(bundleStateName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_INFO, "OSGi BundleStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi BundleStateMBean deregistration problem", e);
		}
		bundleState = null;

		log(LogService.LOG_INFO, "Deregistering bundle wiring state with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(bundleWiringStateName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi BundleWiringStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi BundleWiringStateMBean deregistration problem", e);
		}
		bundleWiringState = null;
		
		log(LogService.LOG_INFO, "Deregistering services monitor with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(serviceStateName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi ServiceStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi ServiceStateMBean deregistration problem", e);
		}
		serviceState = null;

		log(LogService.LOG_INFO, "Deregistering packages monitor with MBeanServer: " + mbeanServer);
		try {
			mbeanServer.unregisterMBean(packageStateName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi PackageStateMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi PackageStateMBean deregistration problem", e);
		}
		packageState = null;

		log(LogService.LOG_INFO, "Deregistering config admin with MBeanServer: " + mbeanServer);
		configAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(configAdminName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi ConfigAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi ConfigAdminMBean deregistration problem", e);
		}
		configAdminTracker = null;

		log(LogService.LOG_INFO, "Deregistering permission admin with MBeanServer: " + mbeanServer);
		permissionAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(permissionAdminName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi PermissionAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi PermissionAdminMBean deregistration problem", e);
		}
		permissionAdminTracker = null;

		log(LogService.LOG_INFO, "Deregistering provisioning service admin with MBeanServer: " + mbeanServer);
		provisioningServiceTracker.close();
		try {
			mbeanServer.unregisterMBean(provisioningServiceName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi ProvisioningServiceMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi ProvisioningServiceMBean deregistration problem", e);
		}
		provisioningServiceTracker = null;

		log(LogService.LOG_INFO, "Deregistering user admin with MBeanServer: " + mbeanServer);
		userAdminTracker.close();
		try {
			mbeanServer.unregisterMBean(userAdminName);
		} catch (InstanceNotFoundException e) {
			log(LogService.LOG_DEBUG, "OSGi UserAdminMBean not found on deregistration", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_INFO, "OSGi UserAdminMBean deregistration problem", e);
		}
		userAdminTracker = null;
		servicesRegistered.set(false);
	}

	/**
	 * Register all MBeans in a MBean server
	 * 
	 * @param mbeanServer MBean Server to register the MBeans in
     */
	private synchronized void registerServices(MBeanServer mbeanServer) {
		try {
			framework = new StandardMBean(new Framework(bundleContext), FrameworkMBean.class);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Unable to create StandardMBean for Framework", e);
			return;
		}
		try {
			bundleState = new StandardMBean(new BundleState(bundleContext), BundleStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Unable to create StandardMBean for BundleState", e);
			return;
		}
		try {
			bundleWiringState = new StandardMBean(new BundleWiringState(bundleContext), BundleWiringStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Unable to create StandardMBean for BundleWiringState", e);
			return;
		}
		try {
			serviceState = new StandardMBean(new ServiceState(bundleContext), CustomServiceStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Unable to create StandardMBean for ServiceState", e);
			return;
		}
		try {
			packageState = new StandardMBean(new PackageState(bundleContext), PackageStateMBean.class);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Unable to create StandardMBean for PackageState", e);
			return;
		}

		log(LogService.LOG_INFO, "Registering Framework with MBeanServer: " + mbeanServer + " with name: " + frameworkName);
		try {
			mbeanServer.registerMBean(framework, frameworkName);
		} catch (InstanceAlreadyExistsException e) {
			log(LogService.LOG_INFO, "Cannot register OSGi framework MBean", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi framework MBean", e);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi framework MBean", e);
		}

		log(LogService.LOG_INFO, "Registering bundle state with MBeanServer: " + mbeanServer + " with name: " + bundleStateName);
		try {
			mbeanServer.registerMBean(bundleState, bundleStateName);
		} catch (InstanceAlreadyExistsException e) {
			log(LogService.LOG_INFO, "Cannot register OSGi BundleStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi BundleStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi BundleStateMBean", e);
		}

		log(LogService.LOG_INFO, "Registering bundle wiring state with MBeanServer: " + mbeanServer + " with name: " + bundleStateName);
		try {
			mbeanServer.registerMBean(bundleWiringState, bundleWiringStateName);
		} catch (InstanceAlreadyExistsException e) {
			log(LogService.LOG_INFO, "Cannot register OSGi BundleWiringStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi BundleWiringStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi BundleWiringStateMBean", e);
		}

		log(LogService.LOG_INFO, "Registering services monitor with MBeanServer: " + mbeanServer + " with name: " + serviceStateName);
		try {
			mbeanServer.registerMBean(serviceState, serviceStateName);
		} catch (InstanceAlreadyExistsException e) {
			log(LogService.LOG_INFO, "Cannot register OSGi ServiceStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi ServiceStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi ServiceStateMBean", e);
		}

		log(LogService.LOG_INFO, "Registering packages monitor with MBeanServer: " + mbeanServer + " with name: " + packageStateName);
		try {
			mbeanServer.registerMBean(packageState, packageStateName);
		} catch (InstanceAlreadyExistsException e) {
			log(LogService.LOG_INFO, "Cannot register OSGi PackageStateMBean", e);
		} catch (MBeanRegistrationException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi PackageStateMBean", e);
		} catch (NotCompliantMBeanException e) {
			log(LogService.LOG_ERROR, "Cannot register OSGi PackageStateMBean", e);
		}

        configAdminTracker = new ServiceTracker<ConfigurationAdmin, Object>(bundleContext, "org.osgi.service.cm.ConfigurationAdmin",
            new ConfigAdminTracker());
        permissionAdminTracker = new ServiceTracker<PermissionAdmin, Object>(bundleContext, "org.osgi.service.permissionadmin.PermissionAdmin",
            new PermissionAdminTracker());
        provisioningServiceTracker = new ServiceTracker<ProvisioningService, Object>(bundleContext,
            "org.osgi.service.provisioning.ProvisioningService", new ProvisioningServiceTracker());
        userAdminTracker = new ServiceTracker<UserAdmin, Object>(bundleContext, "org.osgi.service.useradmin.UserAdmin", new UserAdminTracker());
        configAdminTracker.open();
        permissionAdminTracker.open();
        provisioningServiceTracker.open();
        userAdminTracker.open();
        servicesRegistered.set(true);
    }

	private class MBeanServiceTracker implements ServiceTrackerCustomizer<MBeanServer, Object> {

		/**
		 * Register all MBeans in a newly registered MBean server
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public MBeanServer addingService(ServiceReference<MBeanServer> servicereference) {
			try {
				log(LogService.LOG_INFO, "Adding MBeanServer: " + servicereference);
				final MBeanServer mbeanServer = bundleContext.getService(servicereference);
				mbeanServers.add(mbeanServer);
				Runnable registration = new Runnable() {
					public void run() {
						registerServices(mbeanServer);
					}
				};
				
				Thread registrationThread = new Thread(registration, "JMX Core MBean Registration");
				registrationThread.setDaemon(true);
				registrationThread.start();

				return mbeanServer;
			} catch (RuntimeException e) {
				log(LogService.LOG_ERROR, "uncaught exception in addingService", e);
				throw e;
			}
		}

		public void modifiedService(ServiceReference<MBeanServer> servicereference, Object obj) {
			// no op
		}

		/**
		 * Unregister all MBeans from a MBean server when it gets unregistered
		 *  
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference<MBeanServer> servicereference, Object obj) {
			try {
				log(LogService.LOG_INFO, "Removing MBeanServer: " + servicereference);
				final MBeanServer mbeanServer = bundleContext.getService(servicereference);
				mbeanServers.remove(mbeanServer);
				Runnable deregister = new Runnable() {
					public void run() {
						deregisterServices(mbeanServer);
					}
				};

				Thread deregisterThread = new Thread(deregister, "JMX Core MBean Deregistration");
				deregisterThread.setDaemon(true);
				deregisterThread.start();

			} catch (Throwable e) {
				log(LogService.LOG_INFO, "uncaught exception in removedService", e);
			}
		}
	}

	private class ConfigAdminTracker implements ServiceTrackerCustomizer<ConfigurationAdmin, Object> {
		
		private StandardMBean manager;

		/**
		 * Register a MBean for the ConfigurationAdmin service in all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public Object addingService(ServiceReference<ConfigurationAdmin> reference) {
			ConfigurationAdmin admin;
			try {
				admin = bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log(LogService.LOG_ERROR, "Incompatible class version for the Configuration Admin Manager", e);
				return bundleContext.getService(reference);
			}

			try {
				manager = new StandardMBean(new ConfigAdminManager(admin, logger), ConfigurationAdminMBean.class);
			} catch (NotCompliantMBeanException e) {
				log(LogService.LOG_ERROR, "Unable to create Configuration Admin Manager", e);
				return admin;
			}
			
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "Registering configuration admin with MBeanServer: " + mbeanServer + " with name: " + configAdminName);
				try {
					mbeanServer.registerMBean(manager, configAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log(LogService.LOG_INFO, "Cannot register Configuration Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot register Configuration Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log(LogService.LOG_ERROR, "Cannot register Configuration Manager MBean", e);
				}
			}
			
			return admin;
		}

		public void modifiedService(ServiceReference<ConfigurationAdmin> reference, Object service) {
			// no op
		}

		/**
		 * Unregister the MBean for the ConfigurationAdmin service from all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference<ConfigurationAdmin> reference, Object service) {

			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "deregistering configuration admin from: " + mbeanServer + " with name: " + configAdminName);
				try {
					mbeanServer.unregisterMBean(configAdminName);
				} catch (InstanceNotFoundException e) {
					log(LogService.LOG_INFO, "Configuration Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_INFO, "Cannot deregister Configuration Manager MBean", e);
				}
			}
		}
	}

	private class PermissionAdminTracker implements ServiceTrackerCustomizer<PermissionAdmin, Object> {
		
		private StandardMBean manager;

		/**
		 * Register a MBean for the PermissionAdmin service in all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference<PermissionAdmin> reference) {
			PermissionAdmin admin;
			try {
				admin = bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log(LogService.LOG_ERROR, "Incompatible class version for the Permission Admin Manager", e);
				return bundleContext.getService(reference);
			}
			try {
				manager = new StandardMBean(new PermissionManager(admin), PermissionAdminMBean.class);
			} catch (NotCompliantMBeanException e) {
				log(LogService.LOG_ERROR, "Unable to create Permission Admin Manager", e);
				return admin;
			}
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "Registering permission admin with MBeanServer: " + mbeanServer + " with name: " + permissionAdminName);
				try {
					mbeanServer.registerMBean(manager, permissionAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log(LogService.LOG_INFO, "Cannot register Permission Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot register Permission Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log(LogService.LOG_ERROR, "Cannot register Permission Manager MBean", e);
				}
			}
			return admin;
		}

		public void modifiedService(ServiceReference<PermissionAdmin> reference, Object service) {
			// no op
		}

		/**
		 * Unregister the MBean for the PermissionAdmin service from all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference<PermissionAdmin> reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "deregistering permission admin with MBeanServer: " + mbeanServer + " with name: " + permissionAdminName);
				try {
					mbeanServer.unregisterMBean(permissionAdminName);
				} catch (InstanceNotFoundException e) {
					log(LogService.LOG_INFO, "Permission Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot deregister Permission Manager MBean", e);
				}
			}
		}
	}

	private class ProvisioningServiceTracker implements ServiceTrackerCustomizer<ProvisioningService, Object> {
		
		private StandardMBean provisioning;

		/**
		 * Register a MBean for the Provisioning service in all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference<ProvisioningService> reference) {
			ProvisioningService service;
			
			try {
				service = bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log(LogService.LOG_ERROR, "Incompatible class version for the Provisioning service", e);
				return bundleContext.getService(reference);
			}
			
			try {
				provisioning = new StandardMBean(new Provisioning(service), ProvisioningServiceMBean.class);
			} catch (NotCompliantMBeanException e) {
				log(LogService.LOG_ERROR, "Unable to create Provisioning Service Manager", e);
				return service;
			}
			
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "Registering provisioning service with MBeanServer: " + mbeanServer + " with name: " + provisioningServiceName);
				try {
					mbeanServer.registerMBean(provisioning, provisioningServiceName);
				} catch (InstanceAlreadyExistsException e) {
					log(LogService.LOG_INFO, "Cannot register Provisioning Service MBean", e);
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot register Provisioning Service MBean", e);
				} catch (NotCompliantMBeanException e) {
					log(LogService.LOG_ERROR, "Cannot register Provisioning Service MBean", e);
				}
			}
			return service;
		}

		public void modifiedService(ServiceReference<ProvisioningService> reference, Object service) {
			// no op
		}

		/**
		 * Unregister the MBean for the Provisioning service from all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference<ProvisioningService> reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "deregistering provisioning service with MBeanServer: " + mbeanServer + " with name: " + provisioningServiceName);
				try {
					mbeanServer.unregisterMBean(provisioningServiceName);
				} catch (InstanceNotFoundException e) {
					log(LogService.LOG_INFO, "Provisioning Service MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot deregister Provisioning Service MBean", e);
				}
			}
		}
	}

	private class UserAdminTracker implements ServiceTrackerCustomizer<UserAdmin, Object> {
		
		private StandardMBean manager;

		/**
		 * Register a MBean for the UserAdmin service in all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference<UserAdmin> reference) {
			UserAdmin admin;
			try {
				admin = bundleContext.getService(reference);
			} catch (ClassCastException e) {
				log(LogService.LOG_ERROR, "Incompatible class version for the User Admin manager", e);
				return bundleContext.getService(reference);
			}
			
			try {
				manager = new StandardMBean(new UserManager(admin), UserAdminMBean.class);
			} catch (NotCompliantMBeanException e1) {
				log(LogService.LOG_ERROR, "Unable to create User Admin Manager");
				return admin;
			}
			
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "Registering user admin with MBeanServer: " + mbeanServer + " with name: " + userAdminName);
				try {
					mbeanServer.registerMBean(manager, userAdminName);
				} catch (InstanceAlreadyExistsException e) {
					log(LogService.LOG_INFO, "Cannot register User Manager MBean", e);
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot register User Manager MBean", e);
				} catch (NotCompliantMBeanException e) {
					log(LogService.LOG_ERROR, "Cannot register User Manager MBean", e);
				}
			}
			return admin;
		}

		public void modifiedService(ServiceReference<UserAdmin> reference, Object service) {
			// no op
		}

		/**
		 * Unregister the MBean for the UserAdmin service from all MBean servers
		 * 
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference<UserAdmin> reference, Object service) {
			for (MBeanServer mbeanServer : mbeanServers) {
				log(LogService.LOG_INFO, "Deregistering user admin with MBeanServer: " + mbeanServer + " with name: " + userAdminName);
				try {
					mbeanServer.unregisterMBean(userAdminName);
				} catch (InstanceNotFoundException e) {
					log(LogService.LOG_INFO, "User Manager MBean was never registered");
				} catch (MBeanRegistrationException e) {
					log(LogService.LOG_ERROR, "Cannot deregister User Manager MBean", e);
				}
			}
		}
	}
	
	private class LogServiceTracker implements ServiceTrackerCustomizer<LogService, Object> {

		@Override
		public Object addingService(ServiceReference<LogService> reference) {
			logger = bundleContext.getService(reference);
			return logger;
		}

		@Override
		public void modifiedService(ServiceReference<LogService> reference,
				Object service) {
			// no op
		}

		@Override
		public void removedService(ServiceReference<LogService> reference,
				Object service) {
			logger = null;
		}
		
	}
	
}
