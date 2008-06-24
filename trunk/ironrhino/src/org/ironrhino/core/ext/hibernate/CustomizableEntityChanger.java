package org.ironrhino.core.ext.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.hibernate.SessionFactory;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.Type;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.model.Customizable;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.core.util.XMLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CustomizableEntityChanger {

	protected Log log = LogFactory.getLog(getClass());

	@Autowired
	private ApplicationContext ctx;

	private LocalSessionFactoryBean lsfb;

	private SessionFactory sf;

	private Map<String, List<PropertyChange>> changes = new HashMap<String, List<PropertyChange>>();

	private static Map<String, Map<String, PropertyType>> customizableEntities;

	public Map<String, Map<String, PropertyType>> getCustomizableEntities() {
		return customizableEntities;
	}

	public Map<String, PropertyType> getCustomizedProperties(
			String entityClassName) {
		Map<String, PropertyType> map = customizableEntities
				.get(entityClassName);
		return map;
	}

	public static void convertCustomPropertiesType(Customizable entity) {
		if (customizableEntities != null) {
			Map<String, PropertyType> map = customizableEntities.get(entity
					.getClass().getName());
			Map<String, Serializable> customProperties = entity
					.getCustomProperties();
			for (String name : customProperties.keySet()) {
				Object o = customProperties.get(name);
				if (o == null || !(o instanceof String[]))
					continue;
				String array[] = (String[]) o;
				if (array.length == 0 || StringUtils.isBlank(array[0]))
					continue;
				String value = array[0];
				try {
					if (PropertyType.DATE == map.get(name))
						customProperties
								.put(name, DateUtils.parseDate10(value));
					else if (PropertyType.DOUBLE == map.get(name))
						customProperties.put(name, new Double(value));
					else if (PropertyType.LONG == map.get(name))
						customProperties.put(name, new Long(value));
					else if (PropertyType.INTEGER == map.get(name))
						customProperties.put(name, new Integer(value));
					else
						customProperties.put(name, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		lsfb = (LocalSessionFactoryBean) ctx.getBean("&sessionFactory");
		sf = (SessionFactory) lsfb.getObject();
		customizableEntities = new HashMap<String, Map<String, PropertyType>>();
		Iterator it = lsfb.getConfiguration().getClassMappings();
		while (it.hasNext()) {
			PersistentClass pc = (PersistentClass) it.next();
			try {
				Class clazz = Class.forName(pc.getClassName());
				if (Customizable.class.isAssignableFrom(clazz)) {
					Map<String, PropertyType> map = new HashMap<String, PropertyType>();
					Iterator propertyIterator = getDynamicComponent(clazz)
							.getPropertyIterator();
					while (propertyIterator.hasNext()) {
						Property p = (Property) propertyIterator.next();
						Class t = p.getType().getReturnedClass();
						if (Date.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.DATE);
						else if (Double.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.DOUBLE);
						else if (Long.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.LONG);
						else if (Integer.class.isAssignableFrom(t))
							map.put(p.getName(), PropertyType.INTEGER);
						else
							map.put(p.getName(), PropertyType.STRING);
					}
					customizableEntities.put(clazz.getName(), map);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<String, List<PropertyChange>> getChanges() {
		return changes;
	}

	public void prepareChange(String entityClass, PropertyChange change)
			throws ClassNotFoundException {
		Class clazz = Class.forName(entityClass);
		if (!Customizable.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException("Class '" + entityClass
					+ "' must implements interface '"
					+ Customizable.class.getName() + "'");

		List<PropertyChange> list = changes.get(entityClass);
		if (list == null) {
			list = new ArrayList<PropertyChange>();
			changes.put(entityClass, list);
		}

		Component customProperties = getDynamicComponent(clazz);
		if (!change.isRemove()) {
			PropertyChange pc = null;
			for (PropertyChange temp : list)
				if (temp.getName().equals(change.getName())) {
					pc = temp;
					break;
				}
			if (pc != null) {
				if (pc.isRemove())
					list.remove(pc);
				return;
			}
			boolean exists = false;
			if (BeanUtils.hasProperty(clazz, change.getName()))
				exists = true;
			if (!exists) {
				Iterator propertyIterator = customProperties
						.getPropertyIterator();
				while (propertyIterator.hasNext()) {
					Property property = (Property) propertyIterator.next();
					if (property.getName().equals(change.getName())) {
						exists = true;
						break;
					}
				}
			}
			if (exists)
				throw new IllegalArgumentException("Property '"
						+ change.getName() + "' already exists");
			list.add(change);
		} else {
			PropertyChange pc = null;
			for (PropertyChange temp : list)
				if (temp.getName().equals(change.getName())) {
					pc = temp;
					break;
				}
			if (pc != null) {
				if (!pc.isRemove()) {
					list.remove(pc);
					if (list.size() == 0)
						changes.remove(entityClass);
				}
				return;
			}

			boolean exists = false;
			Iterator propertyIterator = customProperties.getPropertyIterator();
			while (propertyIterator.hasNext()) {
				Property property = (Property) propertyIterator.next();
				if (property.getName().equals(change.getName())) {
					exists = true;
					break;
				}
			}
			if (!exists)
				throw new IllegalArgumentException("Customized property '"
						+ change.getName() + "' doesn't exists");
			list.add(change);
		}

	}

	public void discardChanges() {
		changes.clear();
	}

	public synchronized void applyChanges() throws Exception {
		if (changes.isEmpty())
			return;
		try {
			updatePersistentClass();
			updateMappingFile();
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		changes.clear();
		rebuildSessionFactory();

	}

	private Component getDynamicComponent(Class entityClass) {
		Property property = getPersistentClass(entityClass).getProperty(
				Customizable.CUSTOM_COMPONENT_NAME);
		return (Component) property.getValue();
	}

	private PersistentClass getPersistentClass(Class entityClass) {
		return lsfb.getConfiguration().getClassMapping(entityClass.getName());
	}

	private void rebuildSessionFactory() throws Exception {
		sf.close();
		HibernateGpsDevice hibernateGpsDevice = (HibernateGpsDevice) ctx
				.getBean("hibernateGpsDevice");
		if (hibernateGpsDevice != null)
			hibernateGpsDevice.stop();
		lsfb.afterPropertiesSet();
		sf = (SessionFactory) lsfb.getObject();
		resetSessionFactory(sf);
		hibernateGpsDevice.setSessionFactory(sf);
		if (hibernateGpsDevice != null)
			hibernateGpsDevice.start();
	}

	private void resetSessionFactory(SessionFactory sf) {
		for (String id : ctx.getBeanDefinitionNames()) {
			if (!id.endsWith("Manager"))
				continue;
			Object bean = ctx.getBean(id);
			try {
				org.apache.commons.beanutils.BeanUtils.setProperty(bean,
						"sessionFactory", sf);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void updatePersistentClass() throws ClassNotFoundException {
		for (String entityClass : changes.keySet()) {
			Class clazz = Class.forName(entityClass);
			for (PropertyChange change : changes.get(entityClass)) {
				if (!change.isRemove()) {
					SimpleValue simpleValue = new SimpleValue();
					simpleValue.addColumn(new Column(change.getName()));
					if (change.getType() == PropertyType.STRING)
						simpleValue.setTypeName(String.class.getName());
					else if (change.getType() == PropertyType.INTEGER)
						simpleValue.setTypeName(Integer.class.getName());
					if (change.getType() == PropertyType.LONG)
						simpleValue.setTypeName(Long.class.getName());
					if (change.getType() == PropertyType.DOUBLE)
						simpleValue.setTypeName(Double.class.getName());
					if (change.getType() == PropertyType.DATE)
						simpleValue.setTypeName(Date.class.getName());

					PersistentClass persistentClass = getPersistentClass(clazz);
					simpleValue.setTable(persistentClass.getTable());
					Property property = new Property();
					property.setName(change.getName());
					property.setValue(simpleValue);
					getDynamicComponent(clazz).addProperty(property);
				} else {
					Iterator propertyIterator = getDynamicComponent(clazz)
							.getPropertyIterator();

					while (propertyIterator.hasNext()) {
						Property property = (Property) propertyIterator.next();
						if (property.getName().equals(change.getName()))
							propertyIterator.remove();
					}
				}
			}
		}

	}

	private void updateMappingFile() throws ClassNotFoundException {
		for (String entityClass : changes.keySet()) {
			Class clazz = Class.forName(entityClass);
			try {
				String file = ctx.getResource(getMappingResource(clazz))
						.getFile().getAbsolutePath();

				Document document = XMLUtils.loadDocument(file);
				NodeList componentTags = document
						.getElementsByTagName("dynamic-component");
				Node node = componentTags.item(0);
				XMLUtils.removeChildren(node);
				Iterator propertyIterator = getDynamicComponent(clazz)
						.getPropertyIterator();
				while (propertyIterator.hasNext()) {
					Property property = (Property) propertyIterator.next();
					Element element = createPropertyElement(document, property);
					node.appendChild(element);
				}
				XMLUtils.saveDocument(document, file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Element createPropertyElement(Document document, Property property) {
		Element element = document.createElement("property");
		Type type = property.getType();
		element.setAttribute("name", property.getName());
		element.setAttribute("column", ((Column) property.getColumnIterator()
				.next()).getName());
		element.setAttribute("type", type.getReturnedClass().getName());
		element.setAttribute("not-null", "false");
		return element;
	}

	protected String getMappingResource(Class entityClass) {
		String moduleName = entityClass.getName();
		moduleName = moduleName.substring(0, moduleName.indexOf(".model"));
		moduleName = moduleName.substring(moduleName.lastIndexOf('.') + 1);
		return "classpath:/resources/hibernate/" + moduleName + "/"
				+ entityClass.getSimpleName() + ".hbm.xml";
	}

}
