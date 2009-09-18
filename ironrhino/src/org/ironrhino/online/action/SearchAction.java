package org.ironrhino.online.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.compass.core.support.search.CompassSearchResults;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;

@AutoConfig(namespace = "/")
public class SearchAction extends BaseAction {

	private static final long serialVersionUID = -605092283512211959L;

	public static final String QUERY_PARAMETER_NAME = "q";

	private String q;

	private int pn;

	private int ps = 10;

	private transient CompassSearchService compassSearchService;

	public List<AggregateResult> getSuggestions() {
		if (StringUtils.isNotBlank(q)) {
			// TODO how
			return null;
		}
		return null;
	}

	public CompassSearchResults getSearchResults() {
		// put logic here for <@cache> in page
		if (StringUtils.isBlank(q))
			return null;
		String query = q.trim();
		CompassCriteria cc = new CompassCriteria();
		cc.setQuery(query);
		cc.setAliases(new String[] { "product" });
		if (pn > 0)
			cc.setPageNo(pn);
		if (ps > 0)
			cc.setPageSize(ps);
		if (ps > 100)
			cc.setPageSize(100);
		CompassSearchResults searchResults = compassSearchService.search(cc);
		return searchResults;

	}

	public void setCompassSearchService(
			CompassSearchService compassSearchService) {
		this.compassSearchService = compassSearchService;
	}

	public int getPn() {
		return pn;
	}

	public void setPn(int pn) {
		this.pn = pn;
	}

	public int getPs() {
		return ps;
	}

	public void setPs(int ps) {
		this.ps = ps;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	@Override
	@SkipValidation
	public String execute() {
		String queryString = ServletActionContext.getRequest().getQueryString();
		if (StringUtils.isNotBlank(queryString)) {
			String[] array = queryString.split("&");
			for (String s : array) {
				String[] arr = s.split("=", 2);
				if (arr.length == 2 && arr[0].equals(QUERY_PARAMETER_NAME)) {
					q = arr[1];
					break;
				}
			}
			if (q != null)
				q = org.ironrhino.common.util.StringUtils.decodeUrl(q);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String suggest() {
		ServletActionContext.getResponse().setHeader("Cache-Control",
				"max-age=86400");
		return "suggest";
	}

	public String formatScore(float score) {
		if (score > 0.999)
			return "100%";
		else
			return NumberUtils.formatPercent(score, 1) + "%";
	}
}
