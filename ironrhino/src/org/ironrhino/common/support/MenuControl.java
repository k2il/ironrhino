package org.ironrhino.common.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.model.tuples.Pair;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.Menu;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.security.dynauth.DynamicAuthorizer;
import org.ironrhino.core.security.dynauth.DynamicAuthorizerManager;
import org.ironrhino.core.struts.AutoConfigPackageProvider;
import org.ironrhino.core.struts.EntityAction;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.inject.Container;

@Singleton
@Named("menuControl")
public class MenuControl {

	private MenuNode tree;

	@com.opensymphony.xwork2.inject.Inject(value = "ironrhino-autoconfig", required = true)
	private PackageProvider packageProvider;

	@Autowired(required = false)
	protected transient DynamicAuthorizerManager dynamicAuthorizerManager;

	public Map<String, Pair<Menu, Authorize>> getMapping() {
		ActionContext ctx = ActionContext.getContext();
		if (ctx == null)
			return null;
		Container container = ctx.getContainer();
		container.inject(this);
		Map<String, Pair<Menu, Authorize>> mapping = new HashMap<String, Pair<Menu, Authorize>>();
		Collection<PackageConfig> pcs = ((AutoConfigPackageProvider) packageProvider)
				.getAllPackageConfigs();
		for (PackageConfig pc : pcs) {
			Collection<ActionConfig> acs = pc.getActionConfigs().values();
			for (ActionConfig ac : acs) {
				try {
					Class c = Class.forName(ac.getClassName());
					if (EntityAction.class.equals(c)) {
						Class entityClass = ((AutoConfigPackageProvider) packageProvider)
								.getEntityClass(pc.getNamespace(), ac.getName());
						Menu menu = (Menu) entityClass
								.getAnnotation(Menu.class);
						if (menu != null) {
							StringBuilder sb = new StringBuilder();
							sb.append(pc.getNamespace())
									.append(pc.getNamespace().endsWith("/") ? ""
											: "/").append(ac.getName());
							mapping.put(sb.toString(), new Pair(menu,
									entityClass.getAnnotation(Authorize.class)));
						}
						continue;
					}
					Menu menuOnClass = (Menu) c.getAnnotation(Menu.class);
					Authorize authorizeOnClass = (Authorize) c
							.getAnnotation(Authorize.class);
					if (menuOnClass != null) {
						StringBuilder sb = new StringBuilder();
						sb.append(pc.getNamespace())
								.append(pc.getNamespace().endsWith("/") ? ""
										: "/").append(ac.getName());
						mapping.put(sb.toString(), new Pair(menuOnClass,
								authorizeOnClass));
					}
					Set<Method> methods = AnnotationUtils.getAnnotatedMethods(
							c, Menu.class);
					for (Method m : methods) {
						Menu menu = m.getAnnotation(Menu.class);
						Authorize authorize = m.getAnnotation(Authorize.class);
						StringBuilder sb = new StringBuilder();
						sb.append(pc.getNamespace())
								.append(pc.getNamespace().endsWith("/") ? ""
										: "/").append(ac.getName());
						if (!m.getName().equals("execute"))
							sb.append("/").append(m.getName());
						mapping.put(sb.toString(), new Pair(menu,
								authorize == null ? authorizeOnClass
										: authorize));
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return mapping;
	}

	public MenuNode getTree() {
		if (tree == null) {
			Map<String, Pair<Menu, Authorize>> mapping = getMapping();
			if (mapping != null)
				tree = assemble(mapping);
		}
		MenuNode newTree = BeanUtils.deepClone(tree);
		filterUnauthorized(newTree);
		return newTree;
	}

	private void filterUnauthorized(MenuNode node) {
		List<MenuNode> list = new ArrayList<MenuNode>();
		List<MenuNode> children = (List) node.getChildren();
		for (int i = 0; i < children.size(); i++) {
			MenuNode child = children.get(i);
			if (hasPermission(child)) {
				list.add(child);
				filterUnauthorized(child);
			}else{
				child.getParent().getChildren().remove(child);
			}
		}
		if (list.size() == 0 && node.getUrl() == null
				&& node.getParent() != null)
			node.getParent().getChildren().remove(node);
		else
			node.setChildren(list);
	}

	private boolean hasPermission(MenuNode node) {
		Authorize authorize = node.getAuthorize();
		if (authorize == null || node.getUrl() == null)
			return true;
		boolean authorized = AuthzUtils.authorize(authorize.ifAllGranted(),
				authorize.ifAnyGranted(), authorize.ifNotGranted());
		if (!authorized && dynamicAuthorizerManager != null
				&& !authorize.authorizer().equals(DynamicAuthorizer.class)) {
			String resource = node.getUrl();
			UserDetails user = AuthzUtils.getUserDetails();
			authorized = dynamicAuthorizerManager.authorize(
					authorize.authorizer(), user, resource);
		}
		return authorized;
	}

	public static class MenuNode extends BaseTreeableEntity<MenuNode> {

		private static final long serialVersionUID = 5262995463588604661L;

		private String url;

		private Authorize authorize;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Authorize getAuthorize() {
			return authorize;
		}

		public void setAuthorize(Authorize authorize) {
			this.authorize = authorize;
		}

		public Long getId() {
			return (long) hashCode();
		}

		public int getLevel() {
			int level = 0;
			MenuNode parent = this;
			while ((parent = parent.getParent()) != null)
				level++;
			return level;
		}

		public int hashCode() {
			return url == null ? (name == null ? 0 : name.hashCode()) : url
					.hashCode();
		}

		public boolean equals(Object obj) {
			return obj instanceof MenuNode && this.hashCode() == obj.hashCode();
		}

		public MenuNode getDescendantOrSelfByUrl(String url) {
			if (url == null)
				throw new IllegalArgumentException("url must not be null");
			if (url.equals(this.getUrl()))
				return this;
			for (MenuNode t : getChildren()) {
				if (url.equals(t.getUrl())) {
					return t;
				} else {
					MenuNode tt = t.getDescendantOrSelfByUrl(url);
					if (tt != null)
						return tt;
				}
			}
			return null;
		}

		public MenuNode getChildByName(String name) {
			if (name == null)
				throw new IllegalArgumentException("name must not be null");
			for (MenuNode t : getChildren()) {
				if (name.equals(t.getName()))
					return t;
			}
			return null;
		}
	}

	private MenuNode assemble(Map<String, Pair<Menu, Authorize>> mapping) {
		List<Map.Entry<String, Pair<Menu, Authorize>>> list = new ArrayList<Map.Entry<String, Pair<Menu, Authorize>>>();
		list.addAll(mapping.entrySet());
		MenuNode root = new MenuNode();
		Collections.sort(list,
				new Comparator<Map.Entry<String, Pair<Menu, Authorize>>>() {
					public int compare(
							Map.Entry<String, Pair<Menu, Authorize>> o1,
							Map.Entry<String, Pair<Menu, Authorize>> o2) {
						return o1.getValue().getA().parents().length
								- o2.getValue().getA().parents().length;
					}
				});
		for (Map.Entry<String, Pair<Menu, Authorize>> entry : list) {
			String url = entry.getKey();
			Menu menu = entry.getValue().getA();
			MenuNode node = new MenuNode();
			node.setUrl(url);
			node.setName(menu.name());
			node.setDisplayOrder(menu.displayOrder());
			node.setAuthorize(entry.getValue().getB());
			MenuNode parent = root;
			if (menu.parents().length != 0) {
				String[] parents = menu.parents();
				for (String str : parents) {
					MenuNode mn = parent.getChildByName(str);
					if (mn == null) {
						mn = new MenuNode();
						mn.setName(str);
						mn.setParent(parent);
						if (!(parent.getChildren() instanceof List)) {
							List children = new ArrayList<MenuNode>();
							children.addAll(parent.getChildren());
							parent.setChildren(children);
						}
						Collections.sort((List) parent.getChildren());
						parent.getChildren().add(mn);
					}
					parent = mn;
				}
			}
			node.setParent(parent);
			if (!(parent.getChildren() instanceof List)) {
				List children = new ArrayList<MenuNode>();
				children.addAll(parent.getChildren());
				parent.setChildren(children);
			}
			parent.getChildren().add(node);
			Collections.sort((List) parent.getChildren());
		}
		return root;
	}

}
