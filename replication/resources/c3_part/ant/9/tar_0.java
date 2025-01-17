/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.aut.nativelib;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import org.apache.avalon.excalibur.util.StringUtil;
import org.apache.myrmidon.api.TaskException;
import org.apache.tools.ant.taskdefs.exec.Execute;

/**
 * This is the class that can be used to retrieve the environment
 * variables of the native process.
 *
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:thomas.haas@softwired-inc.com">Thomas Haas</a>
 * @version $Revision$ $Date$
 */
public final class Environment
{
    private final static String[] COMMAND_COM = new String[]{"command.com", "/c", "set"};
    private final static String[] CMD_EXE = new String[]{"cmd", "/c", "set"};

    // Alternatively one could use: /bin/sh -c env
    private final static String[] ENV_CMD = new String[]{"/usr/bin/env"};
    private final static String[] ENV_RAW = new String[]{"env"};

    private static Properties c_procEnvironment;

    /**
     * Private constructor to block instantiation.
     */
    private Environment()
    {
    }

    public static void addNativeEnvironment( final Properties environment )
        throws ExecException, IOException
    {
        final Properties nativeEnvironment = getEnvironmentVariables();
        final Iterator nativeKeys = nativeEnvironment.keySet().iterator();
        while( nativeKeys.hasNext() )
        {
            final String key = (String)nativeKeys.next();
            if( environment.contains( key ) )
            {
                //Skip environment variables that are overidden
                continue;
            }

            final String value = nativeEnvironment.getProperty( key );
            environment.setProperty( key, value );
        }
    }

    /**
     * Retrieve a Properties object that contains the list of all
     * native EnvironmentData Variables for the current process.
     */
    public static Properties getNativeEnvironment()
        throws IOException, ExecException
    {
        final Properties properties = new Properties();
        properties.putAll( getEnvironmentVariables() );
        return properties;
    }

    /**
     * Get the Property object with all environment variables and
     * attempt to load it if it has not already been loaded.
     */
    private static synchronized Properties getEnvironmentVariables()
        throws IOException, ExecException
    {
        if( null == c_procEnvironment )
        {
            c_procEnvironment = retrieveEnvironmentVariables();
        }

        return c_procEnvironment;
    }

    /**
     * Retrieve a last of environment variables from the native OS.
     */
    private static synchronized Properties retrieveEnvironmentVariables()
        throws IOException, ExecException
    {
        final String data = getEnvironmentText();

        final Properties properties = new Properties();
        final BufferedReader in = new BufferedReader( new StringReader( data ) );
        final StringBuffer var = new StringBuffer();
        String line;
        while( null != ( line = in.readLine() ) )
        {
            if( -1 == line.indexOf( '=' ) )
            {
                // Chunk part of previous env var (UNIX env vars can
                // contain embedded new lines).
                var.append( StringUtil.LINE_SEPARATOR );
            }
            else
            {
                // New env var...append the previous one if we have it.
                if( 0 != var.length() )
                {
                    addProperty( properties, var.toString() );
                    var.setLength( 0 );
                }
            }
            var.append( line );
        }

        // Since we "look ahead" before adding, there's one last env var.
        if( 0 != var.length() )
        {
            addProperty( properties, var.toString() );
        }
        return properties;
    }

    /**
     * Parse the specified data into a key=value pair. If there is no
     * '=' character then generate an exception. After parsed data place
     * the key-value pair into the specified Properties object.
     */
    private static void addProperty( final Properties properties,
                                     final String data )
        throws ExecException
    {
        final int index = data.indexOf( '=' );
        if( -1 == index )
        {
            //Our env variable does not have any = in it.
            final String message = "EnvironmentData variable '" + data +
                "' does not have a '=' character in it";
            throw new ExecException( message );
        }
        else
        {
            final String key = data.substring( 0, index );
            final String value = data.substring( index + 1 );
            properties.setProperty( key, value );
        }
    }

    /**
     * Retrieve the text of data that is the result of
     * running the environment command.
     */
    private static String getEnvironmentText()
        throws IOException, ExecException
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Execute exe = new Execute();
        exe.setOutput( output );
        exe.setError( output );

        exe.setCommandline( getEnvCommand() );

        // Make sure we do not recurse forever
        exe.setNewenvironment( true );

        try
        {
            final int retval = exe.execute();
            if( retval != 0 )
            {
                // Just try to use what we got
            }
        }
        catch( final TaskException te )
        {
            throw new ExecException( te.getMessage(), te );
        }

        return output.toString();
    }

    /**
     * Retrieve the command that will display a list of environment
     * variables.
     */
    private static String[] getEnvCommand()
        throws ExecException
    {
        if( Os.isFamily( "os/2" ) )
        {
            // OS/2 - use same mechanism as Windows 2000
            return CMD_EXE;
        }
        else if( Os.isFamily( "windows" ) )
        {
            final String osname =
                System.getProperty( "os.name" ).toLowerCase( Locale.US );
            // Determine if we're running under 2000/NT or 98/95
            if( osname.indexOf( "nt" ) >= 0 || osname.indexOf( "2000" ) >= 0 )
            {
                // Windows 2000/NT
                return CMD_EXE;
            }
            else
            {
                // Windows 98/95 - need to use an auxiliary script
                return COMMAND_COM;
            }
        }
        else if( Os.isFamily( "unix" ) )
        {
            // Generic UNIX
            return ENV_CMD;
        }
        else if( Os.isFamily( "netware" ) )
        {
            return ENV_RAW;
        }
        else
        {
            final String message =
                "Unable to determine native environment variables";
            throw new ExecException( message );
        }
    }
}
