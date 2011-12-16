package org.ironrhino.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

public class ResultPage<T> implements Serializable {

	private static final long serialVersionUID = -3653886488085413894L;

	public static final int MAX_RECORDS_PER_PAGE = 1000;

	public static final int DEFAULT_PAGE_SIZE = 10;

	public static final String PAGENO_PARAM_NAME = "pn";

	public static final String PAGESIZE_PARAM_NAME = "ps";

	private int pageNo = 1;

	private int pageSize = DEFAULT_PAGE_SIZE;

	private long totalRecord = -1;

	private Collection<T> result = new ArrayList<T>();

	private Object criteria;

	private boolean reverse;

	private boolean counting = true;

	private int start = -1;

	private Map<String, Boolean> sorts = new LinkedHashMap<String, Boolean>(2);

	public Map<String, Boolean> getSorts() {
		return sorts;
	}

	public int getStart() {
		return (this.pageNo - 1) * this.pageSize;
	}

	public void setStart(int start) {
		this.pageNo = start / pageSize + 1;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public boolean isCounting() {
		return counting;
	}

	public void setCounting(boolean counting) {
		this.counting = counting;
	}

	public int getPageNo() {
		if (start >= 0)
			return start / pageSize + 1;
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Collection<T> getResult() {
		return result;
	}

	public void setResult(Collection<T> result) {
		this.result = result;
	}

	public int getTotalPage() {
		return (int) (totalRecord % pageSize == 0 ? totalRecord / pageSize
				: totalRecord / pageSize + 1);
	}

	public long getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(long totalRecord) {
		this.totalRecord = totalRecord;
	}

	public Object getCriteria() {
		return criteria;
	}

	public void setCriteria(Object criteria) {
		this.criteria = criteria;
	}

	public boolean isFirst() {
		return this.pageNo <= 1;
	}

	public boolean isLast() {
		return this.pageNo >= getTotalPage();
	}

	public int getPreviousPage() {
		return this.pageNo > 1 ? this.pageNo - 1 : 1;
	}

	public int getNextPage() {
		return this.pageNo < getTotalPage() ? this.pageNo + 1 : getTotalPage();
	}

	public boolean isDefaultPageSize() {
		return this.pageSize == DEFAULT_PAGE_SIZE;
	}

	public String renderUrl(int pn) {
		HttpServletRequest request = ServletActionContext.getRequest();
		String requestURI = (String) request.getAttribute("struts.request_uri");
		if (requestURI == null)
			requestURI = (String) request
					.getAttribute("javax.servlet.forward.request_uri");
		if (requestURI == null)
			requestURI = request.getRequestURI();
		StringBuilder sb = new StringBuilder(requestURI);
		String parameterString = _getParameterString();
		if (StringUtils.isNotBlank(parameterString))
			sb.append("?").append(parameterString);
		if (isDefaultPageSize()) {
			if (pn <= 1)
				return sb.toString();
			else
				return sb
						.append(StringUtils.isNotBlank(parameterString) ? "&"
								: "?").append(PAGENO_PARAM_NAME).append("=")
						.append(pn).toString();
		} else {
			if (pn <= 1)
				return sb
						.append(StringUtils.isNotBlank(parameterString) ? "&"
								: "?").append(PAGESIZE_PARAM_NAME).append("=")
						.append(pageSize).toString();
			else
				return sb
						.append(StringUtils.isNotBlank(parameterString) ? "&"
								: "?").append(PAGENO_PARAM_NAME).append("=")
						.append(pn).append("&").append(PAGESIZE_PARAM_NAME)
						.append("=").append(pageSize).toString();
		}
	}

	private String _parameterString;

	private String _getParameterString() {
		if (_parameterString == null) {
			StringBuilder sb = new StringBuilder();
			Map<String, String[]> map = ServletActionContext.getRequest()
					.getParameterMap();
			for (Map.Entry<String, String[]> entry : map.entrySet()) {
				String name = entry.getKey();
				String[] values = entry.getValue();
				if (values.length == 1
						&& values[0].equals("")
						|| name.equals("_")
						|| name.equals(PAGENO_PARAM_NAME)
						|| name.equals(PAGESIZE_PARAM_NAME)
						|| name.startsWith(StringUtils
								.uncapitalize(ResultPage.class.getSimpleName()) + '.'))
					continue;
				for (String value : values)
					sb.append(name)
							.append('=')
							.append(value.length() > 256 ? value.substring(0,
									256) : value).append('&');
			}
			if (sb.length() > 0)
				sb.deleteCharAt(sb.length() - 1);
			_parameterString = sb.toString();
		}
		return _parameterString;
	}

}
