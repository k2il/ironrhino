package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.model.Attributable;
import org.ironrhino.core.model.Attribute;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.util.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

@PublishAware
@AutoConfig
@Entity
@Table(name = "common_treenode")
public class TreeNode extends BaseTreeableEntity<TreeNode> implements
		Attributable {

	private static final long serialVersionUID = 8878337541387688086L;

	private String description;

	@NotInCopy
	@Transient
	private List<Attribute> attributes = new ArrayList<Attribute>();

	public TreeNode() {

	}

	public TreeNode(String name) {
		this.name = name;
	}

	public TreeNode(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public List<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	@NotInCopy
	@Column(name = "attributes")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Access(AccessType.PROPERTY)
	public String getAttributesAsString() {
		if (attributes == null || attributes.isEmpty()
				|| attributes.size() == 1 && attributes.get(0).isBlank())
			return null;
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (Attribute attr : attributes)
			map.put(attr.getName(), attr.getValue());
		return JsonUtils.toJson(map);
	}

	@Override
	public void setAttributesAsString(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				Map<String, String> map = JsonUtils.fromJson(str,
						new TypeReference<Map<String, String>>() {
						});
				attributes = new ArrayList<Attribute>(map.size());
				for (Map.Entry<String, String> entry : map.entrySet())
					attributes.add(new Attribute(entry.getKey(), entry
							.getValue()));
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
