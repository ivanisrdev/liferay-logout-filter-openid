package com.sample.logout.idp.filter.config;

import aQute.bnd.annotation.ProviderType;
import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.sample.logout.idp.filter.constants.LogoutIdpFilterKeys;

/**
 * @author Ivan Sánchez
 */
@ProviderType
@ExtendedObjectClassDefinition(
        category = LogoutIdpFilterKeys.CONFIGURATION_CATEGORY,
        scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
        id = LogoutIdpFilterKeys.WIDGET_CONFIGURATION_NAME,
        localization = LogoutIdpFilterKeys.CONFIGURATION_LOCALIZATION
)
public interface LogoutIdpFilterConfiguration {

    @Meta.AD(
            deflt = "http://localhost:8088/auth/realms/master/protocol/openid-connect/logout",
            required = false
    )
    String logoutUrl();

}