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

package org.jcvi.ometa.action;

import com.opensymphony.xwork2.ActionSupport;
import org.jcvi.ometa.utils.Constants;
import org.jcvi.ometa.utils.EmailSender;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/27/12
 * Time: 1:53 PM
 */
public class Help extends ActionSupport {
    private String msg;
    private String name;
    private String email;

    public String process() {
        String rtnVal = INPUT;

        try {
            if(msg!=null && msg.length()>0 && name!=null && email!=null) {
                EmailSender emailSender = new EmailSender();
                StringBuffer sb = new StringBuffer(msg);
                sb.append("\n name:  " + name);
                sb.append("\n email: " + email);
                emailSender.send("help", Constants.SERVICE_NAME+"-Help", sb.toString());
                rtnVal = SUCCESS;
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            rtnVal = ERROR;
        }
        return rtnVal;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
