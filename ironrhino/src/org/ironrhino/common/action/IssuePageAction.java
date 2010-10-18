package org.ironrhino.common.action;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.struts.RequestDecoratorMapper;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@AutoConfig(namespace = "/")
public class IssuePageAction extends BaseAction {

	private static final long serialVersionUID = -7189565572156313486L;

	@Inject
	protected PageManager pageManager;

	@Inject
	protected SettingControl settingControl;

	protected ResultPage<Page> resultPage;

	private String name;

	protected Page page;

	public ResultPage<Page> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Page> resultPage) {
		this.resultPage = resultPage;
	}

	public Page getPage() {
		return page;
	}

	public void setName(String name) {
		this.name = name;
		ActionContext.getContext().setName(getName());
	}

	public String getName() {
		if (name == null)
			name = ActionContext.getContext().getName();
		return name;
	}

	@Override
	public String execute() {
		return list();
	}

	@Override
	public String list() {
		if (resultPage == null)
			resultPage = new ResultPage<Page>();
		resultPage.getSorts().put("createDate", true);
		resultPage = pageManager.findResultPageByTag(resultPage, getName());
		return "issuelist";
	}

	public String p() {
		String path = getUid();
		if (StringUtils.isNotBlank(path)) {
			path = "/" + path;
			page = pageManager.getByPath(path);
		}
		return "issuepage";
	}

	@Before
	public void setDecorator() {
		RequestDecoratorMapper.setDecorator(getName());
	}
}
