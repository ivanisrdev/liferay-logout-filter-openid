package com.sample.logout.idp.filter;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.*;
import com.sample.logout.idp.filter.config.LogoutIdpFilterConfiguration;
import com.sample.logout.idp.filter.constants.LogoutIdpFilterKeys;
import org.osgi.service.component.annotations.*;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component(
        immediate = true,
        property = "key=logout.events.post",
        service = LifecycleAction.class
)
public class LogoutIdpFilter implements LifecycleAction {

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    private volatile Portal _portal;

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    private volatile PrefsProps _prefsProps;

    private static final Log _log = LogFactoryUtil.getLog(LogoutIdpFilter.class);
    private volatile LogoutIdpFilterConfiguration _configuration;

    @Override
    public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {

        try {
            HttpServletRequest request = lifecycleEvent.getRequest();
            HttpServletResponse response = lifecycleEvent.getResponse();
            String redirectUri = getRedirectUrl(request);
            String logoutUrl = _configuration.logoutUrl() + LogoutIdpFilterKeys.REDIRECT_URI + redirectUri;

            response.sendRedirect(logoutUrl);

        } catch (Exception exception) {
            _log.error("Error in LogoutIdpFilter: " + exception.getMessage(), exception);
        }
    }

    private String getRedirectUrl(HttpServletRequest request) {
        String portalURL = _portal.getPortalURL(request);
        long companyId = _portal.getCompanyId(request);
        PortletPreferences preferences = _prefsProps.getPreferences(companyId);
        String logoutPath =  _prefsProps.getString(preferences, PropsKeys.DEFAULT_LOGOUT_PAGE_PATH);
        return portalURL + logoutPath;
    }

    @Activate
    @Modified
    protected void active(Map<String, Object> properties) {
        _configuration = ConfigurableUtil.createConfigurable(LogoutIdpFilterConfiguration.class, properties);
    }

}