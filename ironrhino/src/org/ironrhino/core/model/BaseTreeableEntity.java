package org.ironrhino.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.search.elasticsearch.annotations.Index;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableId;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BaseTreeableEntity<T extends BaseTreeableEntity> extends
		Entity<Long> implements Treeable<T>, Ordered {

	private static final long serialVersionUID = 2462271646391940930L;

	protected Long id;

	protected String fullId;

	protected String name;

	protected int level;

	protected int displayOrder;

	protected transient T parent;

	protected transient Collection<T> children = new HashSet<T>(0);

	@NotInJson
	public String getFullId() {
		return fullId;
	}

	public void setFullId(String fullId) {
		this.fullId = fullId;
	}

	@SearchableId
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@NotInJson
	public boolean isNew() {
		return id == null || id == 0;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@NaturalId
	@SearchableProperty(boost = 3, index = Index.NOT_ANALYZED)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullnameSeperator() {
		return "";
	}

	public String getFullname() {
		String seperator = getFullnameSeperator();
		StringBuilder fullname = new StringBuilder(name);
		BaseTreeableEntity e = this;
		while ((e = e.getParent()) != null) {
			if (!(e.isRoot() && StringUtils.isBlank(e.getName())))
				fullname.insert(0, e.getName() + seperator);
		}
		return fullname.toString();
	}

	@NotInJson
	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int compareTo(Object object) {
		if (!(object instanceof Ordered))
			return 0;
		Ordered ordered = (Ordered) object;
		if (this.getDisplayOrder() != ordered.getDisplayOrder())
			return this.getDisplayOrder() - ordered.getDisplayOrder();
		return this.toString().compareTo(ordered.toString());
	}

	@NotInCopy
	@NotInJson
	public Collection<T> getChildren() {
		return children;
	}

	public void setChildren(Collection<T> children) {
		this.children = children;
	}

	public boolean isLeaf() {
		return this.children == null || this.children.size() == 0;
	}

	public boolean isHasChildren() {
		return !isLeaf();
	}

	public boolean isRoot() {
		return this.parent == null;
	}

	@NotInCopy
	@NotInJson
	public T getParent() {
		return parent;
	}

	public void setParent(T parent) {
		this.parent = parent;
	}

	public T getDescendantOrSelfById(Long id) {
		if (id == null)
			throw new IllegalArgumentException("id must not be null");
		if (id.equals(this.getId()))
			return (T) this;
		for (T t : getChildren()) {
			if (id.equals(t.getId())) {
				return t;
			} else {
				T tt = (T) t.getDescendantOrSelfById(id);
				if (tt != null)
					return tt;
			}
		}
		return null;
	}

	@NotInJson
	public List<T> getDescendants() {
		List<T> ids = new ArrayList<T>();
		if (!this.isLeaf())
			for (Object obj : this.getChildren()) {
				collect((T) obj, ids);
			}
		return ids;
	}

	@NotInJson
	public List<T> getDescendantsAndSelf() {
		List<T> ids = new ArrayList<T>();
		collect((T) this, ids);
		return ids;
	}

	private void collect(T node, Collection<T> coll) {
		coll.add(node);
		if (node.isLeaf())
			return;
		for (Object obj : node.getChildren()) {
			collect((T) obj, coll);
		}
	}

	public boolean isAncestorOrSelfOf(T t) {
		T parent = t;
		while (parent != null) {
			if (parent.getId().equals(this.getId()))
				return true;
			parent = (T) parent.getParent();
		}
		return false;
	}

	public boolean isDescendantOrSelfOf(T t) {
		return t != null && t.isAncestorOrSelfOf(this);
	}

	public String getAncestorName(int level) {
		if (level < 1 || level > this.getLevel())
			return null;
		T parent = (T) this;
		while (parent != null) {
			if (parent.getLevel() == level)
				return parent.getName();
			parent = (T) parent.getParent();
		}
		return null;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
