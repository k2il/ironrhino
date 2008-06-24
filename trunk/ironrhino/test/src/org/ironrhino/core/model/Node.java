package org.ironrhino.core.model;

import org.ironrhino.core.model.BaseTreeableEntity;

public class Node extends BaseTreeableEntity<Node> {

	private String description;

	public Node() {

	}

	public Node(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
