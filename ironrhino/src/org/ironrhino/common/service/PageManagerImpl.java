package org.ironrhino.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchResults;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Page;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.search.CompassCriteria;
import org.ironrhino.core.search.CompassSearchService;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named("pageManager")
public class PageManagerImpl extends BaseManagerImpl<Page> implements
		PageManager {

	@Autowired(required = false)
	private transient CompassSearchService compassSearchService;

	@Override
	@Transactional
	@FlushCache(key = "${args[0].path}", namespace = "page", renew = "${args[0]}")
	public void save(Page page) {
		page.setDraft(null);
		page.setDraftDate(null);
		super.save(page);
	}

	@Override
	@Transactional
	@FlushCache(key = "${args[0].path}", namespace = "page")
	public void delete(Page page) {
		super.delete(page);
	}

	@Transactional(readOnly = true)
	@CheckCache(key = "${args[0]}", namespace = "page", eternal = true)
	public Page getByPath(String path) {
		Page page = findByNaturalId(path);
		if (page != null)
			page.setDraft(null);
		return page;
	}

	@Transactional
	public Page saveDraft(Page page) {
		Page p = get(page.getId());
		boolean isnew = false;
		if (p == null) {
			isnew = true;
			p = page;
		}
		p.setDraftDate(new Date());
		Map<String, String> draft = new HashMap<String, String>();
		draft.put("path", page.getPath());
		draft.put("title", page.getTitle());
		draft.put("content", page.getContent());
		p.setDraft(JsonUtils.toJson(draft));
		if (isnew) {
			p.setTitle("");
			p.setContent("");
		}
		super.save(p);
		return p;
	}

	@Transactional(readOnly = true)
	public Page getDraftByPath(String path) {
		Page page = findByNaturalId(path);
		if (page == null || StringUtils.isBlank(page.getDraft()))
			return null;
		pullDraft(page);
		return page;
	}

	@Transactional
	public Page dropDraft(String id) {
		Page page = get(id);
		page.setDraft(null);
		page.setDraftDate(null);
		super.save(page);
		return page;
	}

	public void pullDraft(Page page) {
		try {
			Map<String, String> map = JsonUtils.fromJson(page.getDraft(),
					new TypeReference<Map<String, String>>() {
					});
			page.setPath(map.get("path"));
			page.setTitle(map.get("title"));
			page.setContent(map.get("content"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Page> findListByTag(String tag) {
		return findListByTag(new String[] { tag });
	}

	public List<Page> findListByTag(String... tag) {
		if (tag.length == 0 || StringUtils.isBlank(tag[0]))
			return Collections.EMPTY_LIST;
		List<Page> list;
		if (compassSearchService != null) {
			String query = null;
			if (tag.length == 1) {
				query = "tags:" + tag[0];
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("tags:").append(tag[0]);
				for (int i = 1; i < tag.length; i++)
					sb.append(" AND ").append("tags:").append(tag[i]);
				query = sb.toString();
			}
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setPageSize(Integer.MAX_VALUE);
			cc.setAliases(new String[] { "page" });
			CompassSearchResults searchResults = compassSearchService
					.search(cc);
			CompassHit[] hits = searchResults.getHits();
			if (hits == null)
				return Collections.EMPTY_LIST;
			list = new ArrayList(hits.length);
			for (CompassHit ch : searchResults.getHits()) {
				list.add((Page) ch.getData());
			}
		} else {
			DetachedCriteria dc = detachedCriteria();
			for (int i = 0; i < tag.length; i++) {
				dc.add(Restrictions.or(Restrictions.eq("tagsAsString", tag[i]),
						Restrictions.or(Restrictions.like("tagsAsString",
								tag[i] + ",", MatchMode.START), Restrictions
								.or(Restrictions.like("tagsAsString", ","
										+ tag[i], MatchMode.END), Restrictions
										.like("tagsAsString", "," + tag[i]
												+ ",", MatchMode.ANYWHERE)))));
			}
			list = findListByCriteria(dc);
		}
		Collections.sort(list);
		return list;
	}

	public ResultPage<Page> findResultPageByTag(ResultPage<Page> resultPage,
			String tag) {
		return findResultPageByTag(resultPage, new String[] { tag });
	}

	public ResultPage<Page> findResultPageByTag(ResultPage<Page> resultPage,
			String... tag) {
		if (tag.length == 0 || StringUtils.isBlank(tag[0])) {
			resultPage.setResult(Collections.EMPTY_LIST);
			return resultPage;
		}

		if (compassSearchService != null) {
			String query = null;
			if (tag.length == 1) {
				query = "tags:" + tag[0];
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("tags:").append(tag[0]);
				for (int i = 1; i < tag.length; i++)
					sb.append(" AND ").append("tags:").append(tag[i]);
				query = sb.toString();
			}
			CompassCriteria cc = new CompassCriteria();
			cc.setQuery(query);
			cc.setPageNo(resultPage.getPageNo());
			cc.setPageSize(resultPage.getPageSize());
			cc.setAliases(new String[] { "page" });
			cc.addSort("displayOrder", null, false);
			CompassSearchResults searchResults = compassSearchService
					.search(cc);
			CompassHit[] hits = searchResults.getHits();
			if (hits == null) {
				resultPage.setResult(Collections.EMPTY_LIST);
				return resultPage;
			}
			List<Page> list = new ArrayList(hits.length);
			for (CompassHit ch : searchResults.getHits()) {
				list.add((Page) ch.getData());
			}
			resultPage.setTotalRecord(searchResults.getTotalHits());
			resultPage.setResult(list);
		} else {
			DetachedCriteria dc = detachedCriteria();
			for (int i = 0; i < tag.length; i++) {
				dc.add(Restrictions.or(Restrictions.eq("tagsAsString", tag[i]),
						Restrictions.or(Restrictions.like("tagsAsString",
								tag[i] + ",", MatchMode.START), Restrictions
								.or(Restrictions.like("tagsAsString", ","
										+ tag[i], MatchMode.END), Restrictions
										.like("tagsAsString", "," + tag[i]
												+ ",", MatchMode.ANYWHERE)))));
			}
			dc.addOrder(Order.asc("displayOrder"));
			resultPage.setDetachedCriteria(dc);
			resultPage = findByResultPage(resultPage);
		}
		return resultPage;
	}

}
