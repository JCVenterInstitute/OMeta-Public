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

package org.jcvi.ometa.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 7/21/11
 * Time: 12:46 PM
 * Throws a dialog pane so username and password can be entered without displaying text.
 */
public class LoginDialog {
    private JTextField userField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JButton okButton = new JButton("OK");
    private boolean infoCollected = false;
    private JDialog dialog = new JDialog();

    private String[] loginAndPassword = new String[2];

    /**
     * Throws a dialog to be used to let the user enter the username and password.  This must
     * be called before the user and pass getters in this class!
     */
    public void promptForLoginPassword( String dialogLabel ) {
        dialog.setTitle( dialogLabel );
        dialog.setName( dialogLabel );
        JLabel userLabel = new JLabel( "Usernane:" );
        JLabel passLabel = new JLabel( "Password:" );
        dialog.setLayout( new GridLayout( 5, 1 ) );
        dialog.setSize( 300, 200 );
        dialog.setLocation( new Point( 300, 400 ) );
        dialog.add( userLabel );
        dialog.add( userField );
        dialog.add( passLabel );
        dialog.add( passwordField );
        dialog.add( okButton );

        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                loginAndPassword[ 0 ] = userField.getText().trim();
                loginAndPassword[ 1 ] = new String( passwordField.getPassword() );
                infoCollected = true;
                dialog.setVisible( false );
            }
        });

        dialog.setVisible( true );
        dialog.setModal( true );
        dialog.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent wev ) {
                infoCollected = true;
            }
        });

        // Invoke this to ensure that this method does not return until the dialog has finished collecting info.
        while ( ! infoCollected ) {
            try {
                Thread.sleep( 2000 );

            } catch ( InterruptedException ie ) {
                System.out.println( "Interrupted waiting for dialog input.  Continuing..." );
            }
        }
    }

    public String getUsername() {
        if ( ! infoCollected )
            throw new RuntimeException("Call out of order.  First prompt for login and password.");
        return loginAndPassword[ 0 ];
    }
    public String getPassword() {
        if ( ! infoCollected )
            throw new RuntimeException("Call out of order.  First prompt for login and password.");
        return loginAndPassword[ 1 ];
    }

}
