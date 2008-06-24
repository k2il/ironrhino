package org.ironrhino.core.ext.struts;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.model.Entity;
import org.ironrhino.core.util.ClassScaner;
import org.springframework.util.ClassUtils;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.config.entities.ResultTypeConfig;
import com.opensymphony.xwork2.config.providers.InterceptorBuilder;
import com.opensymphony.xwork2.inject.Inject;

public class AutoConfigPackageProvider implements PackageProvider {

	private static final Log log = LogFactory
			.getLog(AutoConfigPackageProvider.class);

	private String parentPackage = "ironrhino-default";

	private String baseNamespace = "";

	private String defaultActionClass = EntityAction.class.getName();

	private String packages;

	private Configuration configuration;

	private boolean initialized = false;

	private PackageLoader packageLoader;

	private ObjectFactory objectFactory;

	private String pageLocation = "/WEB-INF/view/jsp";

	private ServletContext context;

	@Inject
	public void setContext(ServletContext context) {
		this.context = context;
	}

	@Inject(value = "ironrhino.autoconfig.page.location", required = false)
	public void setPageLocation(String val) {
		this.pageLocation = val;
	}

	@Inject("ironrhino.autoconfig.packages")
	public void setPackages(String val) {
		this.packages = val;
	}

	@Inject(value = "ironrhino.autoconfig.parent.package", required = false)
	public void setParentPackage(String val) {
		this.parentPackage = val;
	}

	@Inject(value = "ironrhino.autoconfig.base.namespace", required = false)
	public void setBaseNamespace(String val) {
		this.baseNamespace = val;
	}

	@Inject
	public void setObjectFactory(ObjectFactory of) {
		this.objectFactory = of;
	}

	public void init(Configuration configuration) throws ConfigurationException {
		this.configuration = configuration;
	}

	public void loadPackages() throws ConfigurationException {
		if (StringUtils.isBlank(packages))
			return;
		Set<Class> entityClasses = ClassScaner.scan(packages.split(","),
				AutoConfig.class);
		if (entityClasses.size() == 0)
			return;
		packageLoader = new PackageLoader();
		for (Class clazz : entityClasses) {
			processEntityClass(clazz);
		}
		for (PackageConfig config : packageLoader.createPackageConfigs()) {
			PackageConfig pc = configuration.getPackageConfig(config.getName());
			if (pc == null) {
				configuration.addPackageConfig(config.getName(), config);
			} else {
				Map<String, ActionConfig> actionConfigs = new LinkedHashMap<String, ActionConfig>(
						pc.getActionConfigs());
				for (String key : config.getActionConfigs().keySet()) {
					if (actionConfigs.containsKey(key))
						continue;
					actionConfigs.put(key, config.getActionConfigs().get(key));
				}
				// this is a trick
				try {
					Field field = PackageConfig.class
							.getDeclaredField("actionConfigs");
					field.setAccessible(true);
					field.set(pc, actionConfigs);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

		}
		copyDefaultView();
		initialized = true;
	}

	private void copyDefaultView() {
		for (String result : "list,input".split(",")) {
			String location = pageLocation + baseNamespace + "/" + result
					+ ".jsp";
			try {
				InputStream is = getClass().getResourceAsStream(
						"/resources/view/" + result + ".jsp");
				if (is == null)
					continue;
				URL url = context.getResource(location);
				if (url == null) {
					FileOutputStream os = new FileOutputStream(context
							.getRealPath(location));
					byte[] buffer = new byte[1024];
					int n = 0;
					while (-1 != (n = is.read(buffer)))
						os.write(buffer, 0, n);
					is.close();
					os.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

	}

	protected void processEntityClass(Class cls) {
		AutoConfig ac = (AutoConfig) cls.getAnnotation(AutoConfig.class);
		String[] arr = getNamespaceAndActionName(cls);
		String namespace = arr[0];
		String actionName = arr[1];
		String actionClass = arr[2];
		String packageName = namespace.substring(1);
		packageName = packageName.replace('/', '_');
		PackageConfig.Builder pkgConfig = loadPackageConfig(packageName);

		ResultConfig successResult = new ResultConfig.Builder("success",
				ServletActionRedirectResult.class.getName()).addParam(
				"actionName", actionName).build();
		ResultConfig autoCofigResult = new ResultConfig.Builder("*",
				AutoConfigResult.class.getName()).build();
		ActionConfig.Builder builder = new ActionConfig.Builder(packageName,
				actionName, actionClass).addResultConfig(successResult)
				.addResultConfig(autoCofigResult);
		if (StringUtils.isNotBlank(ac.fileupload())) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("allowedTypes", ac.fileupload());
			List<InterceptorMapping> interceptors = InterceptorBuilder
					.constructInterceptorReference(pkgConfig, "fileUpload",
							params, null, objectFactory);
			interceptors
					.addAll(InterceptorBuilder.constructInterceptorReference(
							pkgConfig, "annotationDefaultStack", null, null,
							objectFactory));
			builder.interceptors(interceptors);
		}
		ActionConfig actionConfig = builder.build();
		pkgConfig.addActionConfig(actionName, actionConfig);

	}

	protected PackageConfig.Builder loadPackageConfig(String packageName) {
		String namespace = "/" + packageName.replace('_', '/');
		PackageConfig.Builder pkgConfig = packageLoader.getPackage(packageName);
		if (pkgConfig == null) {
			pkgConfig = new PackageConfig.Builder(packageName);
			pkgConfig.namespace(namespace);
			PackageConfig parent = configuration
					.getPackageConfig(parentPackage);
			if (parent != null) {
				pkgConfig.addParent(parent);
			} else {
				log.error("Unable to locate parent package: " + parentPackage);
			}
			packageLoader.registerPackage(pkgConfig);
		} else if (pkgConfig.getNamespace() == null) {
			pkgConfig.namespace(namespace);
		}
		return pkgConfig;
	}

	public boolean needsReload() {
		return !initialized;
	}

	private static class PackageLoader {

		private Map<String, PackageConfig.Builder> packageConfigBuilders = new HashMap<String, PackageConfig.Builder>();

		private Map<PackageConfig.Builder, PackageConfig.Builder> childToParent = new HashMap<PackageConfig.Builder, PackageConfig.Builder>();

		public PackageConfig.Builder getPackage(String name) {
			return packageConfigBuilders.get(name);
		}

		public void registerChildToParent(PackageConfig.Builder child,
				PackageConfig.Builder parent) {
			childToParent.put(child, parent);
		}

		public void registerPackage(PackageConfig.Builder builder) {
			packageConfigBuilders.put(builder.getName(), builder);
		}

		public Collection<PackageConfig> createPackageConfigs() {
			Map<String, PackageConfig> configs = new HashMap<String, PackageConfig>();

			Set<PackageConfig.Builder> builders;
			while ((builders = findPackagesWithNoParents()).size() > 0) {
				for (PackageConfig.Builder parent : builders) {
					PackageConfig config = parent.build();
					configs.put(config.getName(), config);
					packageConfigBuilders.remove(config.getName());

					for (Iterator<Map.Entry<PackageConfig.Builder, PackageConfig.Builder>> i = childToParent
							.entrySet().iterator(); i.hasNext();) {
						Map.Entry<PackageConfig.Builder, PackageConfig.Builder> entry = i
								.next();
						if (entry.getValue() == parent) {
							entry.getKey().addParent(config);
							i.remove();
						}
					}
				}
			}
			return configs.values();
		}

		Set<PackageConfig.Builder> findPackagesWithNoParents() {
			Set<PackageConfig.Builder> builders = new HashSet<PackageConfig.Builder>();
			for (PackageConfig.Builder child : packageConfigBuilders.values()) {
				if (!childToParent.containsKey(child)) {
					builders.add(child);
				}
			}
			return builders;
		}

		public ResultTypeConfig getDefaultResultType(
				PackageConfig.Builder pkgConfig) {
			PackageConfig.Builder parent;
			PackageConfig.Builder current = pkgConfig;

			while ((parent = childToParent.get(current)) != null) {
				current = parent;
			}
			return current.getResultType(current.getFullDefaultResultType());
		}
	}

	private Map<String, Class> entityClassURLMapping = new ConcurrentHashMap<String, Class>();

	public String[] getNamespaceAndActionName(Class cls) {
		String actionName = null;
		String namespace = null;
		String actionClass = null;
		AutoConfig ac = (AutoConfig) cls.getAnnotation(AutoConfig.class);
		if (Entity.class.isAssignableFrom(cls)) {
			actionName = StringUtils.uncapitalize(cls.getSimpleName());
			namespace = ac.namespace();
			if (StringUtils.isBlank(namespace)) {
				String p = cls.getPackage().getName();
				p = p.substring(0, p.length() - ".model".length());
				namespace = baseNamespace + "/"
						+ p.substring(p.lastIndexOf('.') + 1);
			}
			actionClass = cls.getName().replace("model", "action") + "Action";
			if (!ClassUtils.isPresent(actionClass))
				actionClass = defaultActionClass;
			entityClassURLMapping.put(namespace + "/" + actionName, cls);
		} else if (Action.class.isAssignableFrom(cls)) {
			actionName = StringUtils.uncapitalize(cls.getSimpleName());
			actionClass = cls.getName();
			namespace = ac.namespace();
			if (StringUtils.isBlank(namespace)) {
				String p = cls.getPackage().getName();
				p = p.substring(0, p.length() - ".action".length());
				namespace = baseNamespace + "/"
						+ p.substring(p.lastIndexOf('.') + 1);
			}
		}

		return new String[] { namespace, actionName, actionClass };
	}

	public Class getEntityClass(String namespace, String actionName) {
		return entityClassURLMapping.get(namespace + "/" + actionName);
	}

}
