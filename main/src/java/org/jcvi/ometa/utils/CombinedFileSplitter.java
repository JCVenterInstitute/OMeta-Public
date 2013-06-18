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

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: 2/18/11
 * Time: 3:49 PM
 *
 * When provided with a single-screen spread sheet (of .tsv) with all tsv "files" embedded, this
 * script can break it up.
 */
public class CombinedFileSplitter {
    public static void main( String[] args ) {
        try {
            if ( args.length < 2 ) {
                throw new IllegalArgumentException("USAGE: java CombinedFileSplitter <infile> <outdir>");
            }

            String infileStr = args[ 0 ];
            String outdirStr = args[ 1 ];

            File input = new File( infileStr );
            File outdir = new File( outdirStr );

            if ( ! outdir.isDirectory() ) {
                throw new IllegalArgumentException( outdirStr + " is not a directory." );
            }

            if ( ! input.isFile() || ! input.canRead() ) {
                throw new IllegalArgumentException( infileStr + " is not a file." );
            }

            CombinedFileSplitter splitter = new CombinedFileSplitter();

            splitter.process( input, outdir );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void process( File infile, File outdir ) throws Exception {
        BufferedReader rdr = new BufferedReader( new FileReader( infile ) );
        String inbuf = null;

        boolean previousIsEmptyLine = true;
        PrintWriter writer = null;

        while ( null != (inbuf = rdr.readLine() ) ) {
            // Do the file break.
            if ( previousIsEmptyLine ) {
                String filename = inbuf.trim();
                if ( filename.length() > 0 ) {
                    previousIsEmptyLine = false;

                    if ( writer != null ) {
                        writer.close();
                    }
                    if ( ! filename.endsWith( ".tsv" ) ) {
                        filename = filename + ".tsv";
                    }
                    writer = new PrintWriter( new FileWriter( new File( outdir, filename ) ) );
                }
            }
            else {
                // Check on the empty line.
                if ( inbuf.trim().length() == 0 ) {
                    previousIsEmptyLine = true;
                }
                else {
                    // Process the current line.
                    if ( writer != null ) {
                        writer.println( inbuf );
                    }
                }

            }

        }

        rdr.close();
        if ( writer != null )
            writer.close();
    }

    /** Attempt a full removal of the directory and its contents. */
    public void removeDirectory( File directory ) {
        if ( directory != null ) {
            if ( ! directory.canWrite() ) {
                throw new IllegalArgumentException( "Cannot write " + directory );
            }
            else {
                File[] sublist = directory.listFiles();
                for ( File file: sublist ) {
                    if ( file.isFile() ) {
                        if ( ! file.delete() )
                            System.err.println( "Failed to delete file " + file );
                    }
                    else if ( file.isDirectory() ) {
                        removeDirectory( file );
                    }
                }
            }
        }
        if (! directory.delete() ) {
            System.err.println( "Failed to delete directory " + directory );
        }

    }
}
