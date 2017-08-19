package com.example;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

@SuppressWarnings("unchecked")
public class LdapTest {
    private static final String DOMAIN_DSN = "dc=example,dc=com";

    @Rule
    public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
            .newInstance()
            .usingDomainDsn(DOMAIN_DSN)
            .withSchema("custom-schema.ldif")
            .importingLdifs("test.ldif")
            .build();

    private List<Map<String, String>> serachLdap(final String baseDn, final String filter) throws Exception {
        final DirContext dirContext = embeddedLdapRule.dirContext();

        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        final NamingEnumeration<SearchResult> results = dirContext
                .search(baseDn,
                        filter,
                        searchControls);

        final List<Map<String, String>> objectList = new ArrayList<>();
        while (results.hasMore()) {
            final SearchResult sr = results.next();
            final Attributes attrs = sr.getAttributes();
            final NamingEnumeration<Attribute> allAttr = (NamingEnumeration<Attribute>) attrs.getAll();

            final Map<String, String> attrMap = new HashMap<>();
            while (allAttr.hasMore()) {
                final Attribute attr = allAttr.next();

                final NamingEnumeration<?> values = attr.getAll();
                while (values.hasMore()) {
                    final String value = (String) values.next();
                    attrMap.put(attr.getID(), value);
                }
            }
            objectList.add(attrMap);
        }
        return objectList;
    }

    private void showResults(final List<Map<String, String>> objects) {
        final String parentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        System.out.println("-------------- " + parentMethodName + " --------------");

        for (final Map<String, String> attrs : objects) {
            System.out.println(attrs.get("givenName;katakana"));
        }
    }

    @Test
    public void ラビットハウス() throws Exception {
        showResults(serachLdap(
                "ou=people," + DOMAIN_DSN,
                "(cafe=ラビットハウス)"));
    }

    @Test
    public void ラビットハウス以外() throws Exception {
        showResults(serachLdap(
                "ou=people," + DOMAIN_DSN,
                "(&(objectClass=gochiusaPerson)(!(cafe=ラビットハウス)))"));
    }

    @Test
    public void 保登家() throws Exception {
        showResults(serachLdap(
                "ou=people," + DOMAIN_DSN,
                "sn;local=保登"));
    }
}
