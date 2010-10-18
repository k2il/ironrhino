package org.apache.struts2.sitemesh;

import org.apache.commons.lang.xwork.ObjectUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.factory.DefaultFactory;


public class StrutsSiteMeshFactory extends DefaultFactory {

    public StrutsSiteMeshFactory(Config config) {
        super(config);
    }

    /**
     * Determine whether a Page of given content-type should be parsed or not, avoiding inner action parsing.
     */
    @Override
    public boolean shouldParsePage(String contentType) {
        return !isInsideActionTag() && super.shouldParsePage(contentType);
    }

    private boolean isInsideActionTag() {
        return (Boolean) ObjectUtils.defaultIfNull(ServletActionContext.getRequest().getAttribute(StrutsStatics.STRUTS_ACTION_TAG_INVOCATION), Boolean.FALSE);
    }
}
