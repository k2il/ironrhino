package org.ironrhino.core.coordination.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ironrhino.core.coordination.Membership;
import org.ironrhino.core.metadata.ClusterProfile;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.zookeeper.WatchedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("membership")
@ClusterProfile
public class ZooKeeperMembership implements Membership, WatchedEventListener {

	public static final String DEFAULT_ZOOKEEPER_PATH = "/membership";

	protected Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, List<String>> groups = new ConcurrentHashMap<String, List<String>>();

	private ExecutorService executorService;

	private ZooKeeper zooKeeper;

	private String zooKeeperPath = DEFAULT_ZOOKEEPER_PATH;

	private int maxRetryTimes = 5;

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

	@PostConstruct
	public void init() {
		if (zooKeeper == null)
			return;
		try {
			Stat stat = zooKeeper.exists(zooKeeperPath, false);
			if (stat == null)
				zooKeeper.create(zooKeeperPath, null,
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	public void join(final String group) {
		if (zooKeeper == null)
			return;
		if (executorService != null) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					doJoin(group, AppInfo.getInstanceId(), maxRetryTimes);
				}
			});
		} else
			doJoin(group, AppInfo.getInstanceId(), maxRetryTimes);
	}

	public void leave(final String group) {
		if (zooKeeper == null)
			return;
		if (executorService != null) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					doLeave(group, AppInfo.getInstanceId(), maxRetryTimes);
				}
			});
		} else
			doLeave(group, AppInfo.getInstanceId(), maxRetryTimes);
	}

	public boolean isLeader(String group) {
		return AppInfo.getInstanceId().equals(getLeader(group));
	}

	public String getLeader(String group) {
		List<String> members = getMembers(group);
		if (members == null || members.isEmpty())
			return null;
		else
			return members.get(0);
	}

	public List<String> getMembers(String group) {
		if (zooKeeper == null)
			return Arrays.asList(AppInfo.getInstanceId());
		return groups.get(group);
	}

	private void doJoin(String group, String instanceId, int retryTimes) {
		retryTimes--;
		if (retryTimes < -1) {
			log.error("error join " + instanceId + " to " + group);
			return;
		}
		String groupNode = new StringBuilder().append(zooKeeperPath)
				.append("/").append(group).toString();
		try {
			Stat stat = zooKeeper.exists(groupNode, true);
			if (stat == null)
				zooKeeper.create(groupNode, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			String memberNode = new StringBuilder(groupNode).append('/')
					.append(instanceId).toString();
			zooKeeper.create(memberNode, AppInfo.getHostAddress().getBytes(),
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			List<String> children = zooKeeper.getChildren(groupNode, true);
			groups.put(group, children);
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doJoin(group, instanceId, retryTimes);
		}
	}

	private void doLeave(String group, String instanceId, int retryTimes) {
		retryTimes--;
		if (retryTimes < -1) {
			log.error("error leave " + instanceId + " to " + group);
			return;
		}
		String memberNode = new StringBuilder().append(zooKeeperPath)
				.append("/").append(group).append('/').append(instanceId)
				.toString();
		try {
			Stat stat = zooKeeper.exists(memberNode, true);
			if (stat != null)
				zooKeeper.delete(memberNode, stat.getVersion());
		} catch (Exception e) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
			}
			doLeave(group, instanceId, retryTimes);
		}
	}

	public boolean supports(String path) {
		if (path != null && path.startsWith(zooKeeperPath)) {
			String group = path.substring(zooKeeperPath.length() + 1);
			return groups.containsKey(group);
		}
		return false;
	}

	public void onNodeChildrenChanged(String path, List<String> children) {
		String group = path.substring(zooKeeperPath.length() + 1);
		groups.put(group, children);
	}

	public void onNodeCreated(String path) {
		path = path.substring(zooKeeperPath.length() + 1);
		String[] array = path.split("/");
		if (array.length > 1) {
			String group = array[0];
			String instanceId = array[1];
			List<String> members = groups.get(group);
			if (members == null) {
				members = new ArrayList<String>();
				groups.put(group, members);
			}
			if (!members.contains(instanceId))
				members.add(instanceId);
		}
	}

	public void onNodeDeleted(String path) {
		path = path.substring(zooKeeperPath.length() + 1);
		String[] array = path.split("/");
		if (array.length > 1) {
			String group = array[0];
			String instanceId = array[1];
			List<String> members = groups.get(group);
			if (members != null)
				members.remove(instanceId);
		}
	}

	public void onNodeDataChanged(String path, byte[] data) {

	}

}
