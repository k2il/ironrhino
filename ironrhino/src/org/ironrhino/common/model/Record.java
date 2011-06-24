package org.ironrhino.common.model;

import java.util.Date;

import org.ironrhino.core.dataroute.DataRoute;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;

@DataRoute("miscGroup")
@AutoConfig(readonly = true,order = "recordDate desc")
public class Record extends BaseEntity {

	private static final long serialVersionUID = -8287907984213799302L;

	@UiConfig(width = "200px", displayOrder = 1)
	private String entityClass;

	@UiConfig(width = "200px", displayOrder = 2)
	private String entityId;

	@UiConfig(width = "100px", displayOrder = 3)
	private String action;

	@UiConfig(width = "200px", displayOrder = 4)
	private String operatorId;

	@UiConfig(width = "140px", displayOrder = 5)
	private Date recordDate;

	@UiConfig(hide = true)
	private String operatorClass;

	@UiConfig(hide = true)
	private String entityToString;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityToString() {
		return entityToString;
	}

	public void setEntityToString(String entityToString) {
		this.entityToString = entityToString;
	}

	public Date getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorClass() {
		return operatorClass;
	}

	public void setOperatorClass(String operatorClass) {
		this.operatorClass = operatorClass;
	}

}
