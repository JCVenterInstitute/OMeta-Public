/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jcvi.ometa.web_bean;

import org.jcvi.ometa.jndi.user_info.JndiUserInfo;
import org.jcvi.ometa.jndi.user_info.UserInfoBean;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 8/26/11
 * Time: 3:28 PM
 *
 * Self-populating User Info bean.
 */
public class UserInfoWebBean {
    private String userId;
    private String p;
    private UserInfoBean backingBean;

    public void setUserId( String userId ) {
        if ( userId != null  &&  this.backingBean == null ) {
            this.userId = userId;
            JndiUserInfo jui = new JndiUserInfo();
            backingBean = jui.getUserInfo( userId );
        }
    }

    public String getUserId() {
        return backingBean != null ? backingBean.getUserID() : null;
    }

    public String getFirstName() {
        return backingBean != null ? backingBean.getUserFirstName() : null;
    }

    public String getLastName() {
        return backingBean != null ? backingBean.getUserLastName() : null;
    }

    public String getFullname() {
        return backingBean != null ? backingBean.getRelativeDistinguishedName() : null;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getAdmin() {
        return backingBean != null ? backingBean.getAdmin() : null;
    }
}
