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

import org.jcvi.ometa.PSTTestAbstract;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/26/12
 * Time: 12:55 PM
 */
public class ProductionStatusAjaxTest extends PSTTestAbstract {
    @Test
    public void statusPagination() {
        ProductionStatusAjax ajax = new ProductionStatusAjax(this.getReadBean("dev"));
        ajax.setAttributes("ALL");
        ajax.setProjectNames("AIR");
        ajax.setIDisplayStart(0);
        ajax.setIDisplayLength(10);
        //ajax.setSSearch("2011");
        ajax.setISortCol_0("Aliquot Merged ID");
        ajax.setSSortDir_0("desc");
        ajax.runAjax();
        assertTrue(1==1);
    }
}
