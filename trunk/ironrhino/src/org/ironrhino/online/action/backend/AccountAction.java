package org.ironrhino.online.action.backend;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ecside.common.util.RequestUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.service.AccountManager;

@AutoConfig(namespace = "/backend/online")
public class AccountAction extends BaseAction {

	private Account account;

	private ResultPage<Account> resultPage;

	private String rolesAsString;

	private String groupsAsString;

	private String password;

	private AccountManager accountManager;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGroupsAsString() {
		return groupsAsString;
	}

	public void setGroupsAsString(String groupsAsString) {
		this.groupsAsString = groupsAsString;
	}

	public String getRolesAsString() {
		return rolesAsString;
	}

	public void setRolesAsString(String rolesAsString) {
		this.rolesAsString = rolesAsString;
	}

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public ResultPage<Account> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Account> resultPage) {
		this.resultPage = resultPage;
	}

	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		DetachedCriteria dc = accountManager.detachedCriteria();

		if (account != null) {
			if (StringUtils.isNotBlank(account.getUsername()))
				dc.add(Restrictions.ilike("username", account.getUsername(),
						MatchMode.ANYWHERE));
			if (StringUtils.isNotBlank(account.getName()))
				dc.add(Restrictions.ilike("name", account.getName(),
						MatchMode.ANYWHERE));
			String value = ServletActionContext.getRequest().getParameter(
					"account.enabled");
			if ("true".equals(value))
				dc.add(Restrictions.eq("enabled", true));
			else if ("false".equals(value))
				dc.add(Restrictions.eq("enabled", false));
		}
		if (resultPage == null)
			resultPage = new ResultPage<Account>();
		resultPage.setDetachedCriteria(dc);
		resultPage.addOrder(Order.asc("username"));
		int totalRows = accountManager.countResultPage(resultPage);
		String pageSize = request.getParameter("ec_rd");
		if (StringUtils.isNumeric(pageSize))
			resultPage.setPageSize(Integer.parseInt(pageSize));
		int[] rowStartEnd = RequestUtil.getRowStartEnd(request, totalRows,
				resultPage.getPageSize());
		resultPage.setStart(rowStartEnd[0]);
		resultPage = accountManager.getResultPage(resultPage);
		request.setAttribute("recordList", resultPage.getResult());
		request.setAttribute("totalRows", resultPage.getTotalRecord());
		return "list";
	}

	public String view() {
		account = accountManager.get(getUid());
		return "view";
	}

	public String save() {
		if (account != null && account.getId() != null) {
			Account temp = account;
			account = accountManager.get(temp.getId());
			if (account != null) {
				account.setEnabled(temp.isEnabled());
				account.setLocked(temp.isLocked());
				if (StringUtils.isNotBlank(password))
					account.setLegiblePassword(password);
				if (rolesAsString != null)
					account.setRolesAsString(rolesAsString);
				if (groupsAsString != null)
					account.setGroupsAsString(groupsAsString);
				accountManager.save(account);
				addActionMessage(getText("save.success",
						"save {0} successfully", new String[] { account
								.getUsername() }));
			}
		}
		return SUCCESS;
	}

	public String delete() {
		String[] id = getId();
		if (id != null) {
			DetachedCriteria dc = accountManager.detachedCriteria();
			dc.add(Restrictions.in("id", id));
			List<Account> list = accountManager.getListByCriteria(dc);
			if (list.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("(");
				for (Account ac : list) {
					accountManager.delete(ac);
					sb.append(ac.getUsername() + ",");
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

}
