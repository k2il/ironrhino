package org.ironrhino.core.struts;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.hibernate.CriterionUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.Ordered;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.SearchService.Mapper;
import org.ironrhino.core.search.compass.CompassSearchCriteria;
import org.ironrhino.core.search.compass.CompassSearchService;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.util.AnnotationUtils;
import org.ironrhino.core.util.ApplicationContextUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.reflection.ReflectionContextState;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EntityAction extends BaseAction {

	private static final long serialVersionUID = -8442983706126047413L;

	protected static Logger log = LoggerFactory.getLogger(EntityAction.class);

	@javax.inject.Inject
	private transient EntityManager<Persistable<?>> _entityManager;

	private ResultPage resultPage;

	private Persistable entity;

	private Map<String, UiConfigImpl> _uiConfigs;

	private Map<String, List> lists;

	private boolean readonly;

	private boolean searchable;

	@Autowired(required = false)
	private transient CompassSearchService<Persistable<?>> compassSearchService;

	public Map<String, List> getLists() {
		return lists;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public Persistable getEntity() {
		return entity;
	}

	public ResultPage getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage resultPage) {
		this.resultPage = resultPage;
	}

	private boolean readonly() {
		AutoConfig ac = getAutoConfig();
		return (ac != null) && ac.readonly();
	}

	private AutoConfig getAutoConfig() {
		return getEntityClass().getAnnotation(AutoConfig.class);
	}

	private BaseManager<Persistable<?>> getEntityManager(
			Class<Persistable<?>> entityClass) {
		String entityManagerName = StringUtils.uncapitalize(entityClass
				.getSimpleName()) + "Manager";
		try {
			Object bean = ApplicationContextUtils.getBean(entityManagerName);
			if (bean != null)
				return (BaseManager<Persistable<?>>) bean;
			else
				_entityManager.setEntityClass(entityClass);
		} catch (NoSuchBeanDefinitionException e) {
			_entityManager.setEntityClass(entityClass);
		}
		return _entityManager;
	}

	private void tryFindEntity() {
		BaseManager entityManager = getEntityManager(getEntityClass());
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			Set<String> naturalIds = getNaturalIds().keySet();
			if (StringUtils.isNotBlank(getUid())) {
				bw.setPropertyValue("id", getUid());
				Serializable id = (Serializable) bw.getPropertyValue("id");
				entity = entityManager.get(id);
				if (entity == null && naturalIds.size() == 1)
					entity = entityManager.findByNaturalId(id);
			}
			if (entity == null && naturalIds.size() > 0) {
				Serializable[] paramters = new Serializable[naturalIds.size() * 2];
				int i = 0;
				boolean satisfied = true;
				for (String naturalId : naturalIds) {
					paramters[i] = naturalId;
					i++;
					bw.setPropertyValue(naturalId, ServletActionContext
							.getRequest().getParameter(naturalId));
					Serializable value = (Serializable) bw
							.getPropertyValue(naturalId);
					if (value != null)
						paramters[i] = value;
					else {
						satisfied = false;
						break;
					}
					i++;
				}
				if (satisfied)
					entity = entityManager.findByNaturalId(paramters);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public String list() {
		final BaseManager entityManager = getEntityManager(getEntityClass());
		Set<String> searchablePropertyNames = new HashSet<String>();
		for (Map.Entry<String, UiConfigImpl> entry : getUiConfigs().entrySet()) {
			if (entry.getValue().isSearchable())
				searchablePropertyNames.add(entry.getKey());
		}
		AutoConfig ac = getAutoConfig();
		searchable = (ac != null) && ac.searchable()
				|| searchablePropertyNames.size() > 0;
		if (!searchable || StringUtils.isBlank(keyword)
				|| (searchable && compassSearchService == null)) {
			DetachedCriteria dc = entityManager.detachedCriteria();
			Criterion filtering = CriterionUtils.filter(constructEntity(),
					searchablePropertyNames.toArray(new String[0]));
			if (filtering != null)
				dc.add(filtering);
			if (searchable && StringUtils.isNotBlank(keyword)
					&& searchablePropertyNames.size() > 0)
				dc.add(CriterionUtils.like(keyword, MatchMode.ANYWHERE,
						searchablePropertyNames.toArray(new String[0])));
			if (resultPage == null)
				resultPage = new ResultPage();
			resultPage.setCriteria(dc);
			if (ac != null && StringUtils.isNotBlank(ac.order())) {
				String[] arr = ac.order().split("\\s");
				if (arr[arr.length - 1].equalsIgnoreCase("asc"))
					dc.addOrder(Order.asc(arr[arr.length - 2]));
				else if (arr[arr.length - 1].equalsIgnoreCase("desc"))
					dc.addOrder(Order.desc(arr[arr.length - 2]));
				else
					dc.addOrder(Order.asc(arr[arr.length - 1]));
			} else if (Ordered.class.isAssignableFrom(getEntityClass()))
				dc.addOrder(Order.asc("displayOrder"));
			resultPage = entityManager.findByResultPage(resultPage);
		} else {
			String query = keyword.trim();
			CompassSearchCriteria criteria = new CompassSearchCriteria();
			criteria.setQuery(query);
			criteria.setAliases(new String[] { getEntityName() });
			if (Ordered.class.isAssignableFrom(getEntityClass()))
				criteria.addSort("displayOrder", false);
			if (resultPage == null)
				resultPage = new ResultPage();
			resultPage.setCriteria(criteria);
			resultPage = compassSearchService.search(resultPage,
					new Mapper<Persistable<?>>() {
						public Persistable map(Persistable source) {
							return entityManager.get(source.getId());
						}
					});
		}
		readonly = readonly();
		return LIST;
	}

	@Override
	public String input() {
		if (readonly())
			return ACCESSDENIED;
		tryFindEntity();
		if (entity == null)
			try {
				entity = (Persistable) getEntityClass().newInstance();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		if (entity != null && entity.isNew()) {
			Set<String> naturalIds = getNaturalIds().keySet();
			if (getUid() != null && naturalIds.size() == 1) {
				bw.setPropertyValue(naturalIds.iterator().next(), getUid());
			}
		}
		Set<String> editablePropertyNames = getUiConfigs().keySet();
		for (String parameterName : ServletActionContext.getRequest()
				.getParameterMap().keySet()) {
			String propertyName = parameterName;
			if (propertyName.startsWith(getEntityName() + "."))
				propertyName = propertyName
						.substring(propertyName.indexOf('.') + 1);
			if (editablePropertyNames.contains(propertyName))
				bw.setPropertyValue(propertyName, ServletActionContext
						.getRequest().getParameter(parameterName));
		}
		setEntity(entity);
		return INPUT;
	}

	@Override
	public String save() {
		if (readonly())
			return ACCESSDENIED;
		if (!makeEntityValid())
			return INPUT;
		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		entityManager.save(entity);
		addActionMessage(getText("save.success"));
		return SUCCESS;
	}

	public String checkavailable() {
		return makeEntityValid() ? NONE : INPUT;
	}

	private boolean makeEntityValid() {
		Map<String, UiConfigImpl> uiConfigs = getUiConfigs();
		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		entity = constructEntity();
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		Persistable persisted = null;
		Map<String, Annotation> naturalIds = getNaturalIds();
		boolean naturalIdMutable = isNaturalIdMutable();
		boolean caseInsensitive = naturalIds.size() > 0
				&& ((NaturalId) naturalIds.values().iterator().next())
						.caseInsensitive();
		if (entity.isNew()) {
			if (naturalIds.size() > 0) {
				Object[] args = new Object[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = entityManager
						.findByNaturalId(caseInsensitive, args);
				if (persisted != null) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("validation.already.exists"));
					}
					return false;
				}
				for (Map.Entry<String, UiConfigImpl> entry : uiConfigs
						.entrySet())
					if (entry.getValue().isUnique()
							&& StringUtils.isNotBlank(ServletActionContext
									.getRequest().getParameter(
											getEntityName() + '.'
													+ entry.getKey()))) {
						persisted = entityManager.findByNaturalId(entry
								.getKey(), (Serializable) bw
								.getPropertyValue(entry.getKey()));
						if (persisted != null) {
							addFieldError(
									getEntityName() + "." + entry.getKey(),
									getText("validation.already.exists"));
							return false;
						}
					}
			}
			try {
				Persistable temp = entity;
				entity = (Persistable) getEntityClass().newInstance();
				BeanUtils.copyProperties(temp, entity);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			if (naturalIdMutable && naturalIds.size() > 0) {
				Object[] args = new Object[naturalIds.size() * 2];
				Iterator<String> it = naturalIds.keySet().iterator();
				int i = 0;
				try {
					while (it.hasNext()) {
						String name = it.next();
						args[i] = name;
						i++;
						args[i] = bw.getPropertyValue(name);
						i++;
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				persisted = entityManager
						.findByNaturalId(caseInsensitive, args);
				entityManager.evict(persisted);
				if (persisted != null
						&& !persisted.getId().equals(entity.getId())) {
					it = naturalIds.keySet().iterator();
					while (it.hasNext()) {
						addFieldError(getEntityName() + "." + it.next(),
								getText("validation.already.exists"));
					}
					return false;
				}

				for (Map.Entry<String, UiConfigImpl> entry : uiConfigs
						.entrySet())
					if (entry.getValue().isUnique()
							&& StringUtils.isNotBlank(ServletActionContext
									.getRequest().getParameter(
											getEntityName() + '.'
													+ entry.getKey()))) {
						persisted = entityManager.findByNaturalId(entry
								.getKey(), (Serializable) bw
								.getPropertyValue(entry.getKey()));
						entityManager.evict(persisted);
						if (persisted != null
								&& !persisted.getId().equals(entity.getId())) {
							addFieldError(
									getEntityName() + "." + entry.getKey(),
									getText("validation.already.exists"));
							return false;
						}
					}

				if (persisted != null
						&& !persisted.getId().equals(entity.getId())) {
					persisted = null;
				}
			}
			try {
				if (persisted == null)
					persisted = entityManager.get((Serializable) bw
							.getPropertyValue("id"));
				BeanWrapperImpl bwp = new BeanWrapperImpl(persisted);
				Set<String> editedPropertyNames = new HashSet<String>();
				String propertyName = null;
				for (String parameterName : ServletActionContext.getRequest()
						.getParameterMap().keySet()) {
					if (parameterName.startsWith(getEntityName() + '.')
							|| parameterName.startsWith("__checkbox_"
									+ getEntityName() + '.')) {
						propertyName = parameterName.substring(parameterName
								.indexOf('.') + 1);
						if (propertyName.indexOf('.') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('.'));
						if (propertyName.indexOf('[') > 0)
							propertyName = propertyName.substring(0,
									propertyName.indexOf('['));
					}
					UiConfigImpl uiConfig = uiConfigs.get(propertyName);
					if (uiConfig == null)
						continue;
					if (uiConfig.isReadonly())
						continue;
					if (!naturalIdMutable
							&& naturalIds.keySet().contains(propertyName))
						continue;
					if (Persistable.class.isAssignableFrom(bwp
							.getPropertyDescriptor(propertyName)
							.getPropertyType()))
						continue;
					editedPropertyNames.add(propertyName);
				}

				for (String name : editedPropertyNames)
					bwp.setPropertyValue(name, bw.getPropertyValue(name));

				bw = bwp;
				entity = persisted;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		try {
			Set<String> editablePropertyNames = getUiConfigs().keySet();
			PropertyDescriptor[] pds = org.springframework.beans.BeanUtils
					.getPropertyDescriptors(entity.getClass());
			for (PropertyDescriptor pd : pds) {
				if (!editablePropertyNames.contains(pd.getName()))
					continue;
				Class<Persistable<?>> returnType = (Class<Persistable<?>>) pd
						.getPropertyType();
				if (!Persistable.class.isAssignableFrom(returnType))
					continue;
				String parameterValue = ServletActionContext.getRequest()
						.getParameter(getEntityName() + "." + pd.getName());
				if (parameterValue == null)
					parameterValue = ServletActionContext.getRequest()
							.getParameter(
									getEntityName() + "." + pd.getName()
											+ ".id");
				if (parameterValue == null)
					parameterValue = ServletActionContext.getRequest()
							.getParameter(pd.getName() + "Id");
				if (parameterValue == null) {
					continue;
				} else if (StringUtils.isBlank(parameterValue)) {
					pd.getWriteMethod().invoke(entity, new Object[] { null });
				} else {
					UiConfig uiConfig = pd.getReadMethod().getAnnotation(
							UiConfig.class);
					String listKey = uiConfig != null ? uiConfig.listKey()
							: UiConfig.DEFAULT_LIST_KEY;
					BeanWrapperImpl temp = new BeanWrapperImpl(
							returnType.newInstance());
					temp.setPropertyValue(listKey, parameterValue);
					BaseManager em = getEntityManager(returnType);
					Object obj;
					if (listKey.equals(UiConfig.DEFAULT_LIST_KEY))
						obj = em.get((Serializable) temp
								.getPropertyValue(listKey));
					else
						obj = em.findByNaturalId(listKey,
								(Serializable) temp.getPropertyValue(listKey));
					pd.getWriteMethod().invoke(entity, new Object[] { obj });
					em = getEntityManager(getEntityClass());
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	@Override
	public String view() {
		tryFindEntity();
		setEntity(entity);
		return VIEW;
	}

	@Override
	public String delete() {
		if (readonly())
			return ACCESSDENIED;
		BaseManager<Persistable<?>> entityManager = getEntityManager(getEntityClass());
		String[] arr = getId();
		Serializable[] id = (arr != null) ? new Serializable[arr.length]
				: new Serializable[0];
		try {
			BeanWrapperImpl bw = new BeanWrapperImpl(getEntityClass()
					.newInstance());
			for (int i = 0; i < id.length; i++) {
				bw.setPropertyValue("id", arr[i]);
				id[i] = (Serializable) bw.getPropertyValue("id");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (id.length > 0) {
			List<Persistable<?>> list;
			if (id.length == 1) {
				list = new ArrayList<Persistable<?>>(1);
				list.add(entityManager.get(id[0]));
			} else {
				DetachedCriteria dc = entityManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = entityManager.findListByCriteria(dc);
			}

			if (list.size() > 0) {
				boolean deletable = true;
				for (Object obj : list) {
					Persistable entity = (Persistable) obj;
					if (!entityManager.canDelete(entity)) {
						deletable = false;
						addActionError(getText("delete.forbidden",
								new String[] { entity.toString() }));
						break;
					}
				}
				if (deletable) {
					for (Object obj : list)
						entityManager.delete((Persistable) obj);
					addActionMessage(getText("delete.success"));
				}
			}

		}
		return SUCCESS;
	}

	@Override
	protected Authorize findAuthorize() {
		Class<?> c = getEntityClass();
		return c.getAnnotation(Authorize.class);
	}

	public boolean isNew() {
		return entity == null || entity.isNew();
	}

	private Map<String, Annotation> _naturalIds;

	// need call once before view
	public String getEntityName() {
		if (entityName == null)
			entityName = ActionContext.getContext().getActionInvocation()
					.getProxy().getActionName();
		return entityName;
	}

	private String entityName;

	public Map<String, Annotation> getNaturalIds() {
		if (_naturalIds != null)
			return _naturalIds;
		_naturalIds = AnnotationUtils.getAnnotatedPropertyNameAndAnnotations(
				getEntityClass(), NaturalId.class);
		return _naturalIds;
	}

	public boolean isNaturalIdMutable() {
		return getNaturalIds().size() > 0
				&& ((NaturalId) getNaturalIds().values().iterator().next())
						.mutable();
	}

	public Map<String, UiConfigImpl> getUiConfigs() {
		if (_uiConfigs == null) {
			Class clazz = getEntityClass();
			Set<String> hides = new HashSet<String>();
			hides.addAll(AnnotationUtils.getAnnotatedPropertyNames(clazz,
					NotInCopy.class));
			final Map<String, UiConfigImpl> map = new HashMap<String, UiConfigImpl>();
			PropertyDescriptor[] pds = org.springframework.beans.BeanUtils
					.getPropertyDescriptors(clazz);
			for (PropertyDescriptor pd : pds) {
				String propertyName = pd.getName();
				if (pd.getWriteMethod() == null)
					continue;
				SearchableProperty searchableProperty = pd.getReadMethod()
						.getAnnotation(SearchableProperty.class);
				if (searchableProperty == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							searchableProperty = f
									.getAnnotation(SearchableProperty.class);
					} catch (Exception e) {
					}
				SearchableId searchableId = pd.getReadMethod().getAnnotation(
						SearchableId.class);
				if (searchableId == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							searchableId = f.getAnnotation(SearchableId.class);
					} catch (Exception e) {
					}
				UiConfig uiConfig = pd.getReadMethod().getAnnotation(
						UiConfig.class);
				if (uiConfig == null)
					try {
						Field f = clazz.getDeclaredField(propertyName);
						if (f != null)
							uiConfig = f.getAnnotation(UiConfig.class);
					} catch (Exception e) {
					}
				if (uiConfig != null && uiConfig.hidden())
					continue;
				if ("new".equals(propertyName) || "id".equals(propertyName)
						|| "class".equals(propertyName)
						|| pd.getReadMethod() == null
						|| hides.contains(propertyName))
					continue;
				Class<?> returnType = pd.getPropertyType();
				if (returnType.isEnum()) {
					UiConfigImpl uci = new UiConfigImpl(uiConfig);
					uci.setType("select");
					uci.setListKey("name");
					uci.setListValue("displayName");
					try {
						if (lists == null)
							lists = new HashMap<String, List>();
						Method method = pd.getReadMethod().getReturnType()
								.getMethod("values", new Class[0]);
						lists.put(propertyName,
								Arrays.asList((Enum[]) method.invoke(null)));
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
					map.put(propertyName, uci);
					continue;
				} else if (Persistable.class.isAssignableFrom(returnType)) {
					UiConfigImpl uci = new UiConfigImpl(uiConfig);
					uci.setType("listpick");
					uci.setExcludeIfNotEdited(true);
					if (StringUtils.isBlank(uci.getPickUrl())) {
						String url = ((AutoConfigPackageProvider) packageProvider)
								.getEntityUrl(returnType);
						if (url != null) {
							StringBuilder sb = new StringBuilder(url);
							sb.append("/pick");
							List<String> columns = new ArrayList<String>();
							for (String column : "fullname,name,description,code"
									.split(","))
								try {
									if (returnType.getMethod("get"
											+ StringUtils.capitalize(column),
											new Class[0]) != null) {
										if (!columns.contains("fullname")
												&& column.equals("name")
												|| !column.equals("name"))
											columns.add(column);
									}
								} catch (NoSuchMethodException e) {
								}
							if (!columns.isEmpty()) {
								sb.append("?columns="
										+ StringUtils.join(columns, ','));
							}
							uci.setPickUrl(sb.toString());
						}
					}
					map.put(propertyName, uci);
					continue;
				}
				UiConfigImpl uci = new UiConfigImpl(uiConfig);
				if (returnType == Integer.TYPE || returnType == Integer.class
						|| returnType == Short.TYPE
						|| returnType == Short.class || returnType == Long.TYPE
						|| returnType == Long.class) {
					uci.addCssClass("integer");
				} else if (returnType == Double.TYPE
						|| returnType == Double.class
						|| returnType == Float.TYPE
						|| returnType == Float.class
						|| returnType == BigDecimal.class) {
					uci.addCssClass("double");
				} else if (Date.class.isAssignableFrom(returnType)) {
					uci.addCssClass("date");
					if (StringUtils.isBlank(uci.getCellEdit()))
						uci.setCellEdit("click,date");
				} else if (String.class == returnType
						&& pd.getName().toLowerCase().contains("email")) {
					uci.addCssClass("email");
				} else if (returnType == Boolean.TYPE
						|| returnType == Boolean.class) {
					uci.setType("checkbox");
				}

				if (String.class == returnType
						&& (searchableProperty != null || searchableId != null))
					uci.setSearchable(true);

				if (getNaturalIds().containsKey(pd.getName())) {
					uci.setRequired(true);
					if (getNaturalIds().size() == 1)
						uci.addCssClass("checkavailable");
				}
				map.put(propertyName, uci);
			}
			Map<String, UiConfigImpl> sortedMap = new TreeMap<String, UiConfigImpl>(
					new Comparator<String>() {
						public int compare(String o1, String o2) {
							UiConfigImpl uci1 = map.get(o1);
							UiConfigImpl uci2 = map.get(o2);
							if (uci1 == null)
								return -1;
							if (uci2 == null)
								return 1;
							int i = Integer.valueOf(uci1.getDisplayOrder())
									.compareTo(uci2.getDisplayOrder());
							return i != 0 ? i : o1.compareTo(o2);
						}
					});
			sortedMap.putAll(map);
			_uiConfigs = sortedMap;
		}
		return _uiConfigs;
	}

	public static class UiConfigImpl implements Serializable {
		private static final long serialVersionUID = -5963246979386241924L;
		private String type = UiConfig.DEFAULT_TYPE;
		private boolean required;
		private boolean unique;
		private int size;
		private int maxlength;
		private String cssClass = "";
		private boolean readonly;
		private int displayOrder = Integer.MAX_VALUE;
		private String displayName;
		private boolean hiddenInList;
		private String template;
		private String width;
		private Map<String, String> dynamicAttributes = Collections.EMPTY_MAP;
		private boolean excludeIfNotEdited;
		private String listKey = UiConfig.DEFAULT_LIST_KEY;
		private String listValue = UiConfig.DEFAULT_LIST_VALUE;
		private String cellEdit = "";
		private boolean searchable;
		private String pickUrl = "";
		private String templateName = "";

		public UiConfigImpl() {
		}

		public UiConfigImpl(UiConfig config) {
			if (config == null)
				return;
			this.type = config.type();
			this.listKey = config.listKey();
			this.listValue = config.listValue();
			this.required = config.required();
			this.unique = config.unique();
			this.size = config.size();
			this.maxlength = config.maxlength();
			this.readonly = config.readonly();
			this.displayOrder = config.displayOrder();
			if (StringUtils.isNotBlank(config.displayName()))
				this.displayName = config.displayName();
			this.hiddenInList = config.hiddenInList();
			this.template = config.template();
			this.width = config.width();
			if (StringUtils.isNotBlank(config.dynamicAttributes()))
				try {
					this.dynamicAttributes = JsonUtils.fromJson(
							config.dynamicAttributes(),
							new TypeReference<Map<String, String>>() {
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			this.cellEdit = config.cellEdit();
			this.excludeIfNotEdited = config.excludeIfNotEdited();
			this.cssClass = config.cssClass();
			if (this.excludeIfNotEdited)
				cssClass += " excludeIfNotEdited";
			this.searchable = config.searchable();
			this.pickUrl = config.pickUrl();
			this.templateName = config.templateName();
		}

		public boolean isHiddenInList() {
			return hiddenInList;
		}

		public void setHiddenInList(boolean hiddenInList) {
			this.hiddenInList = hiddenInList;
		}

		public String getPickUrl() {
			return pickUrl;
		}

		public void setPickUrl(String pickUrl) {
			this.pickUrl = pickUrl;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}

		public Map<String, String> getDynamicAttributes() {
			return dynamicAttributes;
		}

		public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
			this.dynamicAttributes = dynamicAttributes;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public boolean isUnique() {
			return unique;
		}

		public void setUnique(boolean unique) {
			this.unique = unique;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public int getDisplayOrder() {
			return displayOrder;
		}

		public void setDisplayOrder(int displayOrder) {
			this.displayOrder = displayOrder;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public int getMaxlength() {
			return maxlength;
		}

		public void setMaxlength(int maxlength) {
			this.maxlength = maxlength;
		}

		public String getListKey() {
			return listKey;
		}

		public void setListKey(String listKey) {
			this.listKey = listKey;
		}

		public String getListValue() {
			return listValue;
		}

		public void setListValue(String listValue) {
			this.listValue = listValue;
		}

		public String getCssClass() {
			if (required)
				this.cssClass += (this.cssClass.length() > 0 ? " " : "")
						+ "required";
			if (unique)
				this.cssClass += (this.cssClass.length() > 0 ? " " : "")
						+ "checkavailable";
			return this.cssClass;
		}

		public void addCssClass(String cssClass) {
			this.cssClass += (this.cssClass.length() > 0 ? " " : "") + cssClass;
		}

		public boolean isReadonly() {
			return readonly;
		}

		public void setReadonly(boolean readonly) {
			this.readonly = readonly;
		}

		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		public String getWidth() {
			return width;
		}

		public void setWidth(String width) {
			this.width = width;
		}

		public boolean isExcludeIfNotEdited() {
			return excludeIfNotEdited;
		}

		public void setExcludeIfNotEdited(boolean excludeIfNotEdited) {
			this.excludeIfNotEdited = excludeIfNotEdited;
			if (this.excludeIfNotEdited
					&& !this.cssClass.contains("excludeIfNotEdited"))
				cssClass += "excludeIfNotEdited";
			if (!this.excludeIfNotEdited
					&& this.cssClass.contains("excludeIfNotEdited"))
				cssClass = cssClass.replace("excludeIfNotEdited", "");
		}

		public String getCellEdit() {
			return cellEdit;
		}

		public void setCellEdit(String cellEdit) {
			this.cellEdit = cellEdit;
		}

		public boolean isSearchable() {
			return searchable;
		}

		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}

	}

	// need call once before view
	private Class<Persistable<?>> getEntityClass() {
		if (entityClass == null) {
			ActionProxy proxy = ActionContext.getContext()
					.getActionInvocation().getProxy();
			String actionName = getEntityName();
			String namespace = proxy.getNamespace();
			entityClass = (Class<Persistable<?>>)((AutoConfigPackageProvider) packageProvider)
					.getEntityClass(namespace, actionName);
		}
		return entityClass;
	}

	private Class<Persistable<?>> entityClass;

	private void setEntity(Persistable entity) {
		ValueStack vs = ActionContext.getContext().getValueStack();
		vs.set(getEntityName(), entity);
	}

	private Persistable constructEntity() {
		Persistable entity = null;
		try {
			entity = (Persistable) getEntityClass().newInstance();
			ValueStack temp = valueStackFactory.createValueStack();
			temp.set(getEntityName(), entity);
			Map<String, Object> context = temp.getContext();
			Map<String, Object> parameters = ActionContext.getContext()
					.getParameters();
			try {
				ReflectionContextState.setCreatingNullObjects(context, true);
				ReflectionContextState.setDenyMethodExecution(context, true);
				for (Map.Entry<String, Object> entry : parameters.entrySet()) {
					String name = entry.getKey();
					if (name.startsWith(getEntityName() + "."))
						temp.setParameter(name, entry.getValue());
				}
			} finally {
				ReflectionContextState.setCreatingNullObjects(context, false);
				ReflectionContextState.setDenyMethodExecution(context, false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return entity;
	}

	@Inject("ironrhino-autoconfig")
	private transient PackageProvider packageProvider;

	@Inject
	private transient ValueStackFactory valueStackFactory;

}