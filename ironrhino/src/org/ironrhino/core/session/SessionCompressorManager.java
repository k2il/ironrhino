package org.ironrhino.core.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.ironrhino.core.session.impl.DefaultSessionCompressor;
import org.ironrhino.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@Singleton
@Named
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SessionCompressorManager {

	private Logger log = LoggerFactory.getLogger(getClass());

	private TypeReference<Map<String, String>> mapStringStringType = new TypeReference<Map<String, String>>() {
	};

	@Inject
	private ApplicationContext ctx;

	private Collection<SessionCompressor> compressors;

	private SessionCompressor defaultSessionCompressor = new DefaultSessionCompressor();

	@PostConstruct
	public void afterPropertiesSet() {
		compressors = ctx.getBeansOfType(SessionCompressor.class).values();
	}

	public String compress(WrappedHttpSession session) {
		Map<String, Object> map = session.getAttrMap();
		Map<String, String> compressedMap = new HashMap<String, String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key == null || value == null)
				continue;
			SessionCompressor compressor = null;
			for (SessionCompressor var : compressors)
				if (var.supportsKey(key)) {
					compressor = var;
					break;
				}
			if (compressor == null)
				compressor = defaultSessionCompressor;
			try {
				String s = compressor.compress(value);
				if (s != null)
					compressedMap.put(key, s);
			} catch (Exception e) {
				log.error("compress error for " + key + ",it won't be saved", e);
			}
		}
		return compressedMap.isEmpty() ? null : JsonUtils.toJson(compressedMap);

	}

	public void uncompress(WrappedHttpSession session, String str) {
		Map<String, Object> map = session.getAttrMap();
		if (StringUtils.isNotBlank(str)) {
			Map<String, String> compressedMap = null;
			try {
				compressedMap = JsonUtils.fromJson(str, mapStringStringType);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				session.invalidate();
				return;
			}
			if (compressedMap != null)
				for (Map.Entry<String, String> entry : compressedMap.entrySet()) {
					String key = entry.getKey();
					SessionCompressor compressor = null;
					for (SessionCompressor var : compressors) {
						if (var.supportsKey(key)) {
							compressor = var;
							break;
						}
					}
					if (compressor == null)
						compressor = defaultSessionCompressor;
					try {
						Object value = compressor.uncompress(entry.getValue());
						if (value != null)
							map.put(key, value);
					} catch (Exception e) {
						log.error("uncompress error for " + key
								+ ",it won't be restored", e);
					}
				}
		}
	}
}
