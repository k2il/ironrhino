package org.ironrhino.common.support;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.analysis.TreeNode;

public interface MonitorControl {

	public void archive() throws FileNotFoundException;

	public void archive(Date date) throws FileNotFoundException;

	public Map<String, List<TreeNode>> getResult(Date date)
			throws FileNotFoundException;

	public Map<String, List<TreeNode>> getResult(Date from, Date to)
			throws FileNotFoundException;
}
