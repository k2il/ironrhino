package org.ironrhino.core.remoting.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.zookeeper.WatchedEventListener;

@Singleton
@Named("serviceRegistry")
public class DefaultServiceRegistry extends AbstractServiceRegistry implements
		WatchedEventListener {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/remoting";

	private ExecutorService executorService;

	private ZooKeeper zooKeeper;

	private String zooKeeperPath = DEFAULT_ZOOKEEPER_PATH;

	private int maxRetryTimes = 5;

	private Map<String, String> discoveredServices = new HashMap<String, String>();

	private boolean ready;

	public void setZooKeeperPath(String zooKeeperPath) {
		this.zooKeeperPath = zooKeeperPath;
	}

	public void setMaxRetryTimes(int maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	public void setZooKeeper(ZooKeeper zooKeeper) {
		this.zooKeeper = zooKeeper;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public void prepare() {
		try {
			if (zooKeeper != null) {
				Stat stat = zooKeeper.exists(zooKeeperPath, false);
				if (stat == null)
					zooKeeper.create(zooKeeperPath, null,
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@Override
	protected void onDiscover(String serviceName, String host) {
		super.onDiscover(serviceName, host);
		discoveredServices.put(serviceName, host);
		if (ready) {
			writeDiscoveredServices();
		}
	}

	@Override
	public void onReady() {
		writeDiscoveredServices();
		ready = true;
	}

	public void register(String serviceName) {
		if (zooKeeper != null)
			doRegister(serviceName, AppInfo.getHostAddress(), maxRetryTimes);
	}

	@Override
	protected void lookup(String serviceName) {
		if (zooKeeper != null) {
			doLookup(serviceName, 3);
		}
	}

	protected void writeDiscoveredServices() {
		if (discoveredServices.size() == 0)
			return;
		final String host = AppInfo.getHostAddress();
		final String services = JsonUtils.toJson(discoveredServices);
		Runnable runnable = new Runnable() {
			public void run() {
				doWriteDiscoveredServices(host, services, maxRetryTimes);
			}
		};
		if (executorService != null) {
			executorService.execute(runnable);
		} else {
			runnable.run();
		}
	}

	private void doLookup(String serviceName, int retryTimes) {
		retryTimes--;
		try {
			List<String> children = zooKeeper.getChildren(new StringBuilder(
					zooKeeperPath).append("/").append(serviceName).toString(),
					true);
			if (children != null && children.size() > 0)
				importServices.put(serviceName, children);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			if (retryTimes < 0)
				return;
			doLookup(serviceName, retryTimes);
		}
	}

	private void doWriteDiscoveredServices(String host, String services,
			int retryTimes) {
		retryTimes--;
		String node = new StringBuilder().append(zooKeeperPath).append("/")
				.append(host).toString();
		byte[] data = services.getBytes();
		try {
			Stat stat = zooKeeper.exists(node, false);
			if (stat == null) {
				zooKeeper.create(node, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL);
			} else {
				zooKeeper.setData(node, data, 0);
			}
		} catch (Exception e1) {
			if (retryTimes < 0) {
				log.error("error writeDiscoveredServices for " + node, e1);
				return;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doWriteDiscoveredServices(host, services, retryTimes);
		}
	}

	private void doRegister(String serviceName, String host, int retryTimes) {
		retryTimes--;
		if (retryTimes < -1) {
			log.error("error register " + serviceName + "@" + host);
			return;
		}
		String node = new StringBuilder().append(zooKeeperPath).append("/")
				.append(serviceName).toString();
		try {
			Stat stat = zooKeeper.exists(node, false);
			if (stat == null)
				zooKeeper.create(node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			node = new StringBuilder(node).append('/').append(host).toString();
			zooKeeper.create(node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL);
			onRegister(serviceName, host);
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doRegister(serviceName, host, retryTimes);
		}
	}

	@Override
	public boolean supports(String path) {
		if (path != null && path.startsWith(zooKeeperPath)) {
			String serviceName = path.substring(zooKeeperPath.length() + 1);
			return importServices.containsKey(serviceName);
		}
		return false;
	}

	@Override
	public void onNodeChildrenChanged(String path, List<String> children) {
		String serviceName = path.substring(zooKeeperPath.length() + 1);
		importServices.put(serviceName, children);
	}

	@Override
	public void onNodeCreated(String path) {

	}

	@Override
	public void onNodeDeleted(String path) {

	}

	@Override
	public void onNodeDataChanged(String path, byte[] data) {

	}

}
