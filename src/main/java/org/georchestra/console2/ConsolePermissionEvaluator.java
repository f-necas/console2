package org.georchestra.console2;

import org.georchestra.console2.dao.AdvancedDelegationDao;
import org.georchestra.console2.dao.DelegationDao;
import org.georchestra.console2.dto.SimpleAccount;
import org.georchestra.console2.model.DelegationEntry;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.roles.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public class ConsolePermissionEvaluator implements PermissionEvaluator {

    private static final GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

    @Autowired
    private DelegationDao delegationDao;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (isSuperAdministrator(authentication)) {
            return true;
        } else {
            String username = authentication.getName();
            DelegationEntry delegation = delegationDao.findByUid(username);
            if (delegation == null) {
                return false;
            }

            // Filter based on object type
            if (targetDomainObject instanceof Role r) {
                // Filter users in role and role itself
                List<String> userList = r.getUserList();
                // Remove users not under delegation
                userList.retainAll(this.advancedDelegationDao.findUsersUnderDelegation(username));
                r.setFavorite(true);
                // Remove role not under delegation
                return Arrays.asList(delegation.getRoles()).contains(r.getName());
            } else if (targetDomainObject instanceof Org org) {
                // Filter org
                return Arrays.asList(delegation.getOrgs()).contains(org.getId());
            } else if (targetDomainObject instanceof SimpleAccount account) {
                // filter account
                return Arrays.asList(delegation.getOrgs()).contains(account.getOrgId());
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        return isSuperAdministrator(authentication);
    }

    private boolean isSuperAdministrator(Authentication authentication) {
        return authentication.getAuthorities().contains(ROLE_SUPERUSER);
    }

}
