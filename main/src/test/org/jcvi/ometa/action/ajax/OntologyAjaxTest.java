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

package org.jcvi.ometa.action.ajax;

import org.apache.struts2.json.JSONUtil;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 4/18/13
 * Time: 1:55 PM
 */
public class OntologyAjaxTest {
    OntologyAjax ajax = new OntologyAjax();

    @Ignore
    public void getOntology() {
        ajax.setT("ot");
        System.out.println("Getting all ontologies");
        this.check();
    }

    @Ignore
    public void search() {
        ajax.setT("srch");
        ajax.setSw("att");
        ajax.setOt("GO");
        System.out.printf("Searching '%s' under '%s'%n", ajax.getSw(), ajax.getOt());
        this.check();

    }

    @Test
    public void searchAll() throws Exception {
        ajax.setT("sall");
        ajax.setSw("gene");
        System.out.printf("Searching every term matching '%s'%n", ajax.getSw());
        this.check();
        JSONUtil util = new JSONUtil();
        //util.serialize(ajax.getResult());
    }

    private void check() {
        assertEquals("success", ajax.runAjax());
        int i = 0;
        for(Object o : ajax.getResult()) {
            System.out.println(o);
            i++;
        }
        System.out.println("Totol record count = " + i);
    }
}
