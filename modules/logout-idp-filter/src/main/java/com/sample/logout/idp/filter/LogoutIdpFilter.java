package com.sample.logout.idp.filter;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.*;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProvider;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectProviderRegistry;
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

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    private volatile OpenIdConnectProviderRegistry _openIdConnectProviderRegistry;

    private static final Log _log = LogFactoryUtil.getLog(LogoutIdpFilter.class);
    private volatile LogoutIdpFilterConfiguration _configuration;

    @Override
    public void processLifecycleEvent(LifecycleEvent lifecycleEvent) throws ActionException {

        try {
            HttpServletRequest request = lifecycleEvent.getRequest();
            HttpServletResponse response = lifecycleEvent.getResponse();

            //Get OpenId provider specified in the OSGI system configuration
            String openIdConnectProviderName = String.valueOf(_openIdConnectProviderRegistry.getOpenIdConnectProviderNames()
                    .stream()
                    .filter(openIdConnectProviderName1 -> _openIdConnectProviderRegistry.getOpenIdConnectProviderNames().contains(_configuration.idpName()))
                    .findAny()
                    .orElse(null));

            if (openIdConnectProviderName == null || openIdConnectProviderName.isEmpty()) {
                _log.warn("No OpenID Connect Providers found.");
                return;
            }

            OpenIdConnectProvider openIdConnectProvider = _openIdConnectProviderRegistry.getOpenIdConnectProvider(openIdConnectProviderName);

            Object providerMetadata = openIdConnectProvider.getOIDCProviderMetadata();
            JSONObject jsonObject = JSONFactoryUtil.createJSONObject(providerMetadata.toString());
            Object authEndpoint = jsonObject.get(LogoutIdpFilterKeys.PARAM_AUTH_ENDPOINT);
            String logoutEndpoint = StringUtil.replaceLast(authEndpoint.toString(), LogoutIdpFilterKeys.PARAM_AUTH, LogoutIdpFilterKeys.PARAM_LOGOUT);
            String redirectUri = getRedirectUrl(request);
            String logoutUrl = logoutEndpoint + LogoutIdpFilterKeys.REDIRECT_URI + redirectUri;
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