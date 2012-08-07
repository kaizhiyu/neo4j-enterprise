/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.consistency.report;

import org.neo4j.consistency.RecordType;
import org.neo4j.kernel.impl.nioneo.store.AbstractBaseRecord;
import org.neo4j.kernel.impl.util.StringLogger;

public class MessageConsistencyLogger implements ConsistencyLogger
{
    private final StringLogger logger;
    private static final String ERROR = "ERROR:", WARNING = "WARNING:";

    public MessageConsistencyLogger( StringLogger logger )
    {
        this.logger = logger;
    }

    @Override
    public void error( RecordType recordType, AbstractBaseRecord record, String message, Object... args )
    {
        log( record( entry( ERROR, message ), record ), args );
    }

    @Override
    public void error( RecordType recordType, AbstractBaseRecord oldRecord, AbstractBaseRecord newRecord,
                       String message, Object... args )
    {
        log( diff( entry( ERROR, message ), oldRecord, newRecord ), args );
    }

    @Override
    public void warning( RecordType recordType, AbstractBaseRecord record, String message, Object... args )
    {
        log( record( entry( WARNING, message ), record ), args );
    }

    @Override
    public void warning( RecordType recordType, AbstractBaseRecord oldRecord, AbstractBaseRecord newRecord,
                         String message, Object... args )
    {
        log( diff( entry( WARNING, message ), oldRecord, newRecord ), args );
    }

    private static StringBuilder entry( String type, String message )
    {
        StringBuilder log = new StringBuilder( type );
        for ( String line : message.split( "\n" ) )
        {
            log.append( ' ' ).append( line.trim() );
        }
        return log;
    }

    private static StringBuilder record( StringBuilder log, AbstractBaseRecord record )
    {
        return log.append( "\n\t" ).append( record );
    }

    private static StringBuilder diff( StringBuilder log, AbstractBaseRecord oldRecord, AbstractBaseRecord newRecord )
    {
        return log.append( "\n\t- " ).append( oldRecord ).append( "\n\t+ " ).append( newRecord );
    }

    private void log( StringBuilder log, Object[] args )
    {
        if ( args != null && args.length > 0 )
        {
            log.append( "\n\tInconsistent with:" );
            for ( Object arg : args )
            {
                log.append( ' ' ).append( arg );
            }
        }
        logger.logMessage( log.toString() );
    }
}