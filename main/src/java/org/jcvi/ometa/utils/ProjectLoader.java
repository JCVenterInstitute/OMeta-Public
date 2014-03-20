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

import org.jcvi.ometa.engine.LoadingEngine;
import org.jcvi.ometa.engine.LoadingEngineUsage;

import java.io.Console;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 3/4/11
 * Time: 11:20 AM
 *
 * This utility app will load an entire project worth of files, using the loading engine,
 * and leveraging file naming conventions.
 */
public class ProjectLoader {
    public static void main(String[] args) {
        if ( args.length < 2 ) {
            throw new IllegalArgumentException(
                    "USAGE: java " + ProjectLoader.class.getName() + " <project base directory path> <server URL> [username] [password]" +
                            "\n    server URL should be an application server URL like localhost:1399, etc." +
                            "\n    Username and password may be omitted, but if so, there shall be an interactive prompt." +
                            "\n    This application will load an entire project into the database."
            );
        }

        ProjectLoader loader = new ProjectLoader();
        loader.run(args);
    }

    public void run(String[] args) {
        String projectPath = args[ 0 ];
        String serverUrl = args[ 1 ];
        String username = args.length > 2 ? args[ 2 ] : null;
        String password = args.length > 3 ? args[ 3 ] : null;

        String currentFileName = null;

        // NOTE: Do not wish to force users to login over and over....
        if ( username == null ) {
            Console console = System.console();
            username = console.readLine("Please enter username.  Same username will be used for whole project: ");
        }
        if ( password == null ) {
            Console console = System.console();
            password = new String( console.readPassword("Please enter password.  Same password will be used for whole project: ") );
        }
        try {
            // Check inputs.
            File path = new File( projectPath );
            if ( path.isDirectory()  &&  path.canRead() ) {
                // This is the basic parameter object for the loading engine.
                File[] contents = path.listFiles();
                boolean foundProjectFile = false;

                // Ensure that the project file is processed first.
                for ( File file: contents ) {
                    if ( file.isFile()  &&   file.getName().toLowerCase().endsWith("_project.tsv")) {
                        foundProjectFile = true;
                        currentFileName = file.getName();
                        loadMultipartFile(serverUrl, username, password, file);
                    }
                }

                if (! foundProjectFile ) {
                    // Samples depend on project.
                    System.out.println( "WARNING: Directory " + projectPath + " has no project file.  Processing may fail unless project already loaded." );
                }

                // Process all of the sample files.
                for ( File file: contents ) {
                    if ( file.isFile()  &&  file.getName().toLowerCase().endsWith("_sample.tsv")) {
                        currentFileName = file.getName();
                        loadMultipartFile(serverUrl, username, password, file);
                    }
                }

                System.out.println("Loading process done!");
            }
            else {
                throw new IllegalArgumentException("File " + projectPath + " is not a directory or cannot be read.");
            }

        } catch (Exception ex) {
            System.out.printf("*** Failed to load the project from path '%s%s' - %s", projectPath, currentFileName, ex.getMessage());
            System.exit( 0 );
        }
    }

    /** All functionality needed for getting the loading engine param read, and loading the file. */
    private static void loadMultipartFile(
            String serverUrl,
            String username,
            String password,
            File file
    ) throws Exception {
        LoadingEngineUsage usage = new LoadingEngineUsage();
        usage.setServerUrl(serverUrl);
        usage.setUsername(username);
        usage.setPassword(password);
        usage.setMultipartInputfileName(file.getAbsolutePath());
        usage.validate();
        LoadingEngine engine = new LoadingEngine( usage );
        engine.digestMultipart();
    }
}
