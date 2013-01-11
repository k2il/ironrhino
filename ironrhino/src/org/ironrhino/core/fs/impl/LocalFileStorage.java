package org.ironrhino.core.fs.impl;

import static org.ironrhino.core.metadata.Profiles.CLOUD;
import static org.ironrhino.core.metadata.Profiles.DEFAULT;
import static org.ironrhino.core.metadata.Profiles.DUAL;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.ironrhino.core.fs.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

@Singleton
@Named("fileStorage")
@Profile({ DEFAULT, DUAL, CLOUD })
public class LocalFileStorage implements FileStorage {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${fileStorage.uri:file:///tmp}")
	protected String uri;

	private File directory;

	public void setUri(String uri) {
		this.uri = uri;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(uri);
		uri = uri.replace('\\', '/');
		uri = uri.replace(" ", "%20");
		this.directory = new File(new URI(uri));
		if (this.directory.isFile())
			throw new RuntimeException(directory + " is not directory");
		if (!this.directory.exists())
			if (!this.directory.mkdirs())
				logger.error("mkdirs error:" + directory.getAbsolutePath());
	}

	public void write(InputStream is, String path) throws IOException {
		File dest = new File(directory, path);
		dest.getParentFile().mkdirs();
		FileOutputStream os = new FileOutputStream(dest);
		IOUtils.copy(is, os);
		os.close();
		is.close();
	}

	public InputStream open(String path) throws IOException {
		return new FileInputStream(new File(directory, path));
	}

	public boolean mkdir(String path) {
		return new File(directory, path).mkdirs();
	}

	public boolean delete(String path) {
		return new File(directory, path).delete();
	}

	public long getLastModified(String path) {
		return new File(directory, path).lastModified();
	}

	public boolean exists(String path) {
		return new File(directory, path).exists();
	}

	public boolean rename(String fromPath, String toPath) {
		return new File(directory, fromPath).renameTo(new File(directory,
				toPath));
	}

	public boolean isDirectory(String path) {
		return new File(directory, path).isDirectory();
	}

	public List<String> listFiles(String path) {
		final List<String> list = new ArrayList<String>();
		new File(directory, path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isFile()) {
					list.add(f.getName());
				}
				return false;
			}
		});
		return list;
	}

	public Map<String, Boolean> listFilesAndDirectory(String path) {
		final Map<String, Boolean> map = new HashMap<String, Boolean>();
		new File(directory, path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				map.put(f.getName(), f.isFile());
				return false;
			}
		});
		Map<String, Boolean> sortedMap = new TreeMap<String, Boolean>(
				new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						Boolean b1 = map.get(o1);
						Boolean b2 = map.get(o2);
						if (b2 == null)
							return -1;
						if (b1 == null)
							return 1;
						int i = b2.compareTo(b1);
						return i != 0 ? i : o1.compareTo(o2);
					}
				});
		sortedMap.putAll(map);
		return sortedMap;
	}
}
