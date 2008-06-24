package org.ironrhino.pms.action;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.util.HtmlUtils;
import org.ironrhino.core.annotation.JsonConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.pms.model.Category;
import org.ironrhino.pms.service.CategoryManager;
import org.ironrhino.pms.support.CategoryTreeControl;

public class CategoryAction extends BaseAction {

	private Category category;

	private Integer parentId;

	private String rolesAsString;

	private CategoryManager categoryManager;

	private Collection<Category> children;

	private CategoryTreeControl categoryTreeControl;

	private boolean async;

	private int root;

	public int getRoot() {
		return root;
	}

	public void setRoot(int root) {
		this.root = root;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public Collection<Category> getChildren() {
		return children;
	}

	public void setCategoryTreeControl(CategoryTreeControl categoryTreeControl) {
		this.categoryTreeControl = categoryTreeControl;
	}

	public String getRolesAsString() {
		return rolesAsString;
	}

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public void setCategoryManager(CategoryManager categoryManager) {
		this.categoryManager = categoryManager;
	}

	public String execute() {
		if (parentId != null && parentId > 0) {
			category = categoryManager.get(parentId);
		} else {
			category = new Category();
			DetachedCriteria dc = categoryManager.detachedCriteria();
			dc.add(Restrictions.isNull("parent"));
			dc.addOrder(Order.asc("displayOrder"));
			dc.addOrder(Order.asc("name"));
			category.setChildren(categoryManager.getListByCriteria(dc));
		}
		HttpServletRequest request = ServletActionContext.getRequest();
		request.setAttribute("recordList", category.getChildren());
		request.setAttribute("totalRows", category.getChildren().size());
		return "list";
	}

	public String input() {
		if (getUid() != null)
			category = categoryManager.get(new Integer(getUid()));
		if (category == null)
			category = new Category();
		return INPUT;
	}

	public String save() {
		if (category.isNew()) {
			if (categoryManager.getByNaturalId("code", category.getCode()) != null) {
				addFieldError("category.code", getText("category.code.exists"));
				return INPUT;
			}
			if (parentId != null) {
				Category parent = categoryManager.get(parentId);
				category.setParent(parent);
			}
		} else {
			Category temp = category;
			category = categoryManager.get(temp.getId());
			category.setName(temp.getName());
			category.setDescription(temp.getDescription());
			category.setDisplayOrder(temp.getDisplayOrder());
			if (rolesAsString != null)
				category.setRolesAsString(rolesAsString);
		}
		categoryManager.save(category);
		addActionMessage(getText("save.success", "save {0} successfully",
				new String[] { category.getName() }));
		return SUCCESS;
	}

	public String move() {
		String id = getUid();
		if (StringUtils.isNotBlank(id)) {
			category = categoryManager.get(new Integer(getUid()));
			if (parentId != null && parentId > 0
					&& !id.equals(parentId.toString()))
				category.setParent(categoryManager.get(parentId));
			else
				category.setParent(null);
			categoryManager.save(category);
		}
		return "tree";
	}

	public String delete() {
		String[] arr = getId();
		Integer[] id = new Integer[arr.length];
		for (int i = 0; i < id.length; i++)
			id[i] = new Integer(arr[i]);
		if (id != null) {
			DetachedCriteria dc = categoryManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Category> list = categoryManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Category category : list) {
					categoryManager.delete(category);
					sb.append(category.getCode() + ",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(")");
				addActionMessage(getText("delete.success",
						"delete {0} successfully",
						new String[] { sb.toString() }));
			}
		}
		return SUCCESS;
	}

	public String tree() {
		if (!async) {
			if (root < 1)
				category = categoryTreeControl.getCategoryTree();
			else
				category = categoryTreeControl.getCategoryTree()
						.getDescendantOrSelfById(root);
			children = category.getChildren();
		}
		return "tree";
	}

	@JsonConfig(top = "children")
	public String children() {
		Category category;
		if (root < 1)
			category = categoryTreeControl.getCategoryTree();
		else
			category = categoryTreeControl.getCategoryTree()
					.getDescendantOrSelfById(root);
		children = category.getChildren();
		return JSON;
	}

	public String getTreeViewHtml() {
		return HtmlUtils.getTreeViewHtml(children, async);
	}
}
