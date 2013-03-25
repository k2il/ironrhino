package org.ironrhino.common.action;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.common.model.Page;
import org.ironrhino.common.service.PageManager;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.elasticsearch.ElasticSearchCriteria;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.struts.RequestDecoratorMapper;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.interceptor.annotations.Before;

@AutoConfig(namespace = IssuePageAction.NAMESPACE, actionName = IssuePageAction.ACTION_NAME)
public class IssuePageAction extends BaseAction {

	private static final long serialVersionUID = -7189565572156313486L;

	public static final String NAMESPACE = "/";
	public static final String ACTION_NAME = "_issue_page_";

	@Inject
	protected PageManager pageManager;

	protected ResultPage<Page> resultPage;

	private String name;

	protected Page page;

	protected Page previousPage;

	protected Page nextPage;

	public ResultPage<Page> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Page> resultPage) {
		this.resultPage = resultPage;
	}

	public Page getPage() {
		return page;
	}

	public Page getPreviousPage() {
		return previousPage;
	}

	public Page getNextPage() {
		return nextPage;
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
		ElasticSearchCriteria criteria = new ElasticSearchCriteria();
		criteria.addSort("createDate", true);
		resultPage.setCriteria(criteria);
		resultPage = pageManager.findResultPageByTag(resultPage, getName());
		return "issuelist";
	}

	public String p() {
		String path = getUid();
		if (StringUtils.isNotBlank(path)) {
			path = "/" + path;
			page = pageManager.getByPath(path);
		}
		if (page == null)
			return NOTFOUND;
		List<Page> pages = pageManager.findListByTag(getName());
		if (StringUtils.isNotBlank(path)) {
			for (int i = 0; i < pages.size(); i++) {
				Page p = pages.get(i);
				if (p.getPagepath().equals(path)) {
					page = p;
					if (i > 0)
						previousPage = pages.get(i - 1);
					if (i < pages.size() - 1)
						nextPage = pages.get(i + 1);
					break;
				}
			}
		}
		return "issuepage";
	}

	@Before
	public void setDecorator() {
		RequestDecoratorMapper.setDecorator(getName());
	}
}
