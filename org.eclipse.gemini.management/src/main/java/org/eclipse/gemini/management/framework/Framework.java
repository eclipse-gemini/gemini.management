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
 *     Christopher Frost - 5.0 spec changes
 ******************************************************************************/

package org.eclipse.gemini.management.framework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.management.openmbean.CompositeData;

import org.eclipse.gemini.management.framework.internal.BundleBatchActionResult;
import org.eclipse.gemini.management.framework.internal.BundleBatchInstallResult;
import org.eclipse.gemini.management.framework.internal.BundleBatchResolveResult;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * {@inheritDoc}
 */
public final class Framework implements FrameworkMBean {

	private BundleContext bundleContext;
	private FrameworkStartLevel frameworkStartLevel;
	private FrameworkWiring frameworkWiring;
	
	public Framework(BundleContext bc) {
		this.bundleContext = bc;
		this.frameworkStartLevel = bc.getBundle(0).adapt(FrameworkStartLevel.class);
		this.frameworkWiring = bc.getBundle(0).adapt(FrameworkWiring.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFrameworkStartLevel() throws IOException {
		return frameworkStartLevel.getStartLevel();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialBundleStartLevel() throws IOException {
		return frameworkStartLevel.getInitialBundleStartLevel();
	}

	/**
	 * {@inheritDoc}
	 */
	public long installBundle(String location) throws IOException {
		try {
			return bundleContext.installBundle(location).getBundleId();
		} catch (Throwable e) {
			throw new IOException("Unable to install bundle: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long installBundleFromURL(String location, String url) throws IOException {
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			return bundleContext.installBundle(location, is).getBundleId();
		} catch (Throwable e) {
			throw new IOException("Unable to install bundle: " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData installBundles(String[] locations) throws IOException {
		if (locations == null) {
			throw new IOException("locations must not be null");
		}
		Long ids[] = new Long[locations.length];
		for (int i = 0; i < locations.length; i++) {
			try {
				ids[i] = bundleContext.installBundle(locations[i]).getBundleId();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(ids, 0, completed, 0, completed.length);
				String[] remaining = new String[locations.length - i - 1];
				System.arraycopy(locations, i + 1, remaining, 0, remaining.length);
				return new BundleBatchInstallResult(e.toString(), completed, locations[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchInstallResult(ids).asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData installBundlesFromURL(String[] locations, String[] urls) throws IOException {
		if (locations == null) {
			throw new IOException("locations must not be null");
		}
		if (urls == null) {
			throw new IOException("urls must not be null");
		}
		Long ids[] = new Long[locations.length];
		for (int i = 0; i < locations.length; i++) {
			InputStream is = null;
			try {
				is = new URL(urls[i]).openStream();
				ids[i] = bundleContext.installBundle(locations[i], is).getBundleId();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(ids, 0, completed, 0, completed.length);
				String[] remaining = new String[locations.length - i - 1];
				System.arraycopy(locations, i + 1, remaining, 0, remaining.length);
				return new BundleBatchInstallResult(e.toString(), completed, locations[i], remaining).asCompositeData();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return new BundleBatchInstallResult(ids).asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshBundle(long bundleIdentifier) throws IOException {
		Collection<Bundle> bundles = Arrays.asList(bundle(bundleIdentifier));
		this.frameworkWiring.refreshBundles(bundles);
	}

	/**
	 * {@inheritDoc}
	 */
	public void refreshBundles(long[] bundleIdentifiers) throws IOException {
		List<Bundle> bundles = new ArrayList<Bundle>();
		if (bundleIdentifiers != null) {
			for (int i = 0; i < bundleIdentifiers.length; i++) {
				try {
					bundles.add(bundle(bundleIdentifiers[i]));
				} catch (Throwable e) {
					IOException iox = new IOException("Unable to refresh packages");
					iox.initCause(e);
					throw iox;
				}
			}
		}
		try {
			this.frameworkWiring.refreshBundles(bundles);
		} catch (Throwable e) {
			IOException iox = new IOException("Unable to refresh packages");
			iox.initCause(e);
			throw iox;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean resolveBundle(long bundleIdentifier) throws IOException {
		Collection<Bundle> bundles = Arrays.asList(bundle(bundleIdentifier));
		return this.frameworkWiring.resolveBundles(bundles);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean resolveBundles(long[] bundleIdentifiers) throws IOException {
		List<Bundle> bundles = new ArrayList<Bundle>();
		if (bundleIdentifiers != null) {
			for (int i = 0; i < bundleIdentifiers.length; i++) {
				bundles.add(bundle(bundleIdentifiers[i]));
			}
		}
		return this.frameworkWiring.resolveBundles(bundles);
	}

	/**
	 * {@inheritDoc}
	 */
	public void restartFramework() throws IOException {
		try {
			bundle(0).update();
		} catch (BundleException e) {
			throw new IOException("Unable to restart framework: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBundleStartLevel(long bundleIdentifier, int newlevel) throws IOException {
		try {
			bundle(bundleIdentifier).adapt(BundleStartLevel.class).setStartLevel(newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData setBundleStartLevels(long[] bundleIdentifiers, int[] newlevels) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		if (newlevels == null) {
			throw new IOException("new start levels must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).adapt(BundleStartLevel.class).setStartLevel(newlevels[i]);
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFrameworkStartLevel(int newlevel) throws IOException {
		try {
			this.frameworkStartLevel.setStartLevel(newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInitialBundleStartLevel(int newlevel) throws IOException {
		try {
			this.frameworkStartLevel.setInitialBundleStartLevel(newlevel);
		} catch (Throwable e) {
			IOException iox = new IOException("Cannot set start level: " + e);
			iox.initCause(e);
			throw iox;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdownFramework() throws IOException {
		try {
			bundle(0).stop();
		} catch (Throwable be) {
			throw new IOException("Shutting down not implemented in this framework: " + be);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void startBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).start();
		} catch (Throwable e) {
			throw new IOException("Unable to start bundle: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData startBundles(long[] bundleIdentifiers) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).start();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void stopBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).stop();
		} catch (Throwable e) {
			throw new IOException("Unable to stop bundle: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData stopBundles(long[] bundleIdentifiers) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).stop();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void uninstallBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).uninstall();
		} catch (BundleException e) {
			throw new IOException("Unable to uninstall bundle: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData uninstallBundles(long[] bundleIdentifiers) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).uninstall();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateBundle(long bundleIdentifier) throws IOException {
		try {
			bundle(bundleIdentifier).update();
		} catch (Throwable e) {
			throw new IOException("Unable to update bundle: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateBundleFromURL(long bundleIdentifier, String url) throws IOException {
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			bundle(bundleIdentifier).update(is);
		} catch (Throwable e) {
			throw new IOException("Unable to update bundle: " + e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData updateBundles(long[] bundleIdentifiers) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			try {
				bundle(bundleIdentifiers[i]).update();
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData updateBundlesFromURL(long[] bundleIdentifiers, String[] urls) throws IOException {
		if (bundleIdentifiers == null) {
			throw new IOException("Bundle identifiers must not be null");
		}
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			InputStream is = null;
			try {
				is = new URL(urls[i]).openStream();
				bundle(bundleIdentifiers[i]).update(is);
			} catch (Throwable e) {
				Long[] completed = new Long[i];
				System.arraycopy(bundleIdentifiers, 0, completed, 0, completed.length);
				Long[] remaining = new Long[bundleIdentifiers.length - i - 1];
				System.arraycopy(bundleIdentifiers, i + 1, remaining, 0, remaining.length);
				return new BundleBatchActionResult(e.toString(), completed, bundleIdentifiers[i], remaining).asCompositeData();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return new BundleBatchActionResult().asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateFramework() throws IOException {
		try {
			bundle(0).update();
		} catch (BundleException be) {
			throw new IOException("Update of the framework is not implemented: " + be);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getDependencyClosure(long[] bundleIdentifiers) throws IOException {
		Collection<Bundle> bundles = this.frameworkWiring.getDependencyClosure(this.getBundles(bundleIdentifiers));
		long[] result = new long[bundles.size()];
		int i = 0;
		for (Bundle bundle : bundles) {
			result[i] = bundle.getBundleId();
			i++;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProperty(String key) throws IOException {
		return this.bundleContext.getProperty(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public long[] getRemovalPendingBundles() throws IOException {
		Collection<Bundle> removalPendingBundles = this.frameworkWiring.getRemovalPendingBundles();
		long[] result = new long[removalPendingBundles.size()];
		int i = 0;
		for (Bundle bundle : removalPendingBundles) {
			result[i] = bundle.getBundleId();
			i++;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean refreshBundleAndWait(long bundleIdentifier) throws IOException {
		Collection<Bundle> bundles = new HashSet<Bundle>();
		bundles.add(this.bundle(bundleIdentifier));
		StandardFrameworkListener standardFrameworkListener = new StandardFrameworkListener();
		this.frameworkWiring.refreshBundles(bundles, standardFrameworkListener);
		return standardFrameworkListener.getResult();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData refreshBundlesAndWait(long[] bundleIdentifiers) throws IOException {
		Collection<Bundle> bundles = bundleIdentifiers == null ? null : this.getBundles(bundleIdentifiers);
		StandardFrameworkListener standardFrameworkListener = new StandardFrameworkListener();
		this.frameworkWiring.refreshBundles(bundles, standardFrameworkListener);
		Long[] completedBundles = new Long[bundleIdentifiers.length];
		System.arraycopy(bundleIdentifiers, 0, completedBundles, 0, bundleIdentifiers.length);
		return new BundleBatchResolveResult(completedBundles, standardFrameworkListener.getResult()).asCompositeData();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData resolve(long[] bundleIdentifiers) throws IOException {
		boolean result;
		Long[] completedBundles;
		if(bundleIdentifiers == null){
			Collection<Long> unresolvedBundles = new HashSet<Long>();
			Bundle[] allBundles = this.bundleContext.getBundles();
			for (Bundle bundle : allBundles) {
				if(bundle.getState() == Bundle.INSTALLED){
					unresolvedBundles.add(bundle.getBundleId());
				}
			}
			completedBundles = new Long[unresolvedBundles.size()];
			unresolvedBundles.toArray(completedBundles);
			result = this.frameworkWiring.resolveBundles(null);	
		}else{
			Collection<Bundle> bundles = this.getBundles(bundleIdentifiers);
			result = this.frameworkWiring.resolveBundles(bundles);
			completedBundles = new Long[bundleIdentifiers.length];
			System.arraycopy(bundleIdentifiers, 0, completedBundles, 0, bundleIdentifiers.length);
		}
		return new BundleBatchResolveResult(completedBundles, result).asCompositeData();
	}

	private Collection<Bundle> getBundles(long[] bundleIdentifiers) throws IOException{
		Collection<Bundle> bundles = new HashSet<Bundle>();
		for (int i = 0; i < bundleIdentifiers.length; i++) {
			bundles.add(this.bundle(bundleIdentifiers[i]));
		}
		return bundles;	
	}
	
	private Bundle bundle(long bundleIdentifier) throws IOException {
		Bundle b = bundleContext.getBundle(bundleIdentifier);
		if (b == null) {
			throw new IOException("Bundle <" + bundleIdentifier + "> does not exist");
		}
		return b;
	}
	
	/**
	 * 
	 * @author Christopher Frost
	 *
	 * This class is thread safe and will only block until a framework event is received. 
	 *
	 */
	private static class StandardFrameworkListener implements FrameworkListener {

		private final int waitForEvent = FrameworkEvent.PACKAGES_REFRESHED;
				
		private final Object monitor = new Object();
						
		private volatile boolean sucsess = false;

		private volatile boolean completed = false;
		
		@Override
		public void frameworkEvent(FrameworkEvent event) {
			if(this.waitForEvent == event.getType()){
				this.sucsess = true;
			} else if(FrameworkEvent.ERROR == event.getType()){
				this.sucsess = false;
			}
			this.completed = true;
			this.monitor.notifyAll();
		}
		
		public boolean getResult(){
			synchronized (monitor) {
				while(!this.completed){
					try {
						this.monitor.wait(5000); //Just to make sure as it is possible that we could get to wait after notify has been called
					} catch (InterruptedException e) {
						// no-op
					}
				}
				return this.sucsess;
			}
		}
		
	}
	
}
