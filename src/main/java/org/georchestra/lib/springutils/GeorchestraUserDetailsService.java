package org.georchestra.lib.springutils;

import org.georchestra.commons.security.SecurityHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static org.georchestra.commons.security.SecurityHeaders.SEC_ROLES;

public class GeorchestraUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws AuthenticationException {
        Assert.notNull(token.getDetails(), "No details provided with authentication token");
        return createUserDetails(token, null);
    }

    protected UserDetails createUserDetails(Authentication token, Collection<? extends GrantedAuthority> authorities) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String secRoles = SecurityHeaders.decode(request.getHeader(SEC_ROLES));
        List<GrantedAuthority> auth = StringUtils.isEmpty(secRoles) ? AuthorityUtils.NO_AUTHORITIES
                : semicolonSeparatedStringToAuthorityList(secRoles);

        return new User(token.getName(), "N/A", true, true, true, true, auth);
    }

    public static List<GrantedAuthority> semicolonSeparatedStringToAuthorityList(String authorityString) {
        return AuthorityUtils.createAuthorityList(StringUtils.tokenizeToStringArray(authorityString, ";"));
    }
}
