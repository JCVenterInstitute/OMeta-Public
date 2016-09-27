package org.jcvi.ometa.action;

import org.jcvi.ometa.helper.LDAPHelper;
import org.junit.Test;

/**
 * User: movence
 * Date: 12/8/14
 * Time: 1:11 PM
 * org.jcvi.ometa.action
 */
public class LDAPTest {
    LDAPHelper ldap = new LDAPHelper();

    @Test
    public void main() throws Exception {
        //ldap.updatePassword("taccount", "test123", "123123");
        ldap.resetPassword("taccount", "Hyunsoo Kim", "hkim@jcvi.org");
    }
}
