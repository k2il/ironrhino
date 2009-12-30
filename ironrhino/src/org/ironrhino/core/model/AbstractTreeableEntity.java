package org.ironrhino.core.model;

import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;

public abstract class AbstractTreeableEntity<T extends AbstractTreeableEntity>
		extends Entity<Long> implements Treeable<T> {

	private static final long serialVersionUID = -1883496335563461601L;

	private Long id;

	private String fullId;

	protected String name;

	protected int level;

	@NotInJson
	public String getFullId() {
		return fullId;
	}

	public void setFullId(String fullId) {
		this.fullId = fullId;
	}

	@SearchableId(converter = "long")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@NotInJson
	public boolean isNew() {
		return id == null || id == 0;
	}

	@NotInCopy
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@NaturalId
	@SearchableProperty(boost = 2)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotInJson
	public String getFullname() {
		String fullname = name;
		AbstractTreeableEntity e = this;
		while ((e = (AbstractTreeableEntity) e.getParent()) != null) {
			fullname = e.getName() + fullname;
		}
		return fullname;
	}

}
