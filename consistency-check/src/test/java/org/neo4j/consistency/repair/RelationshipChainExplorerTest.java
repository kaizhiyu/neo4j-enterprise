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
package org.neo4j.consistency.repair;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.nioneo.store.RecordStore;
import org.neo4j.kernel.impl.nioneo.store.RelationshipRecord;
import org.neo4j.kernel.impl.nioneo.store.StoreAccess;
import org.neo4j.test.TargetDirectory;

import static org.junit.Assert.assertEquals;

public class RelationshipChainExplorerTest
{
    private static final TargetDirectory target = TargetDirectory.forTest( RelationshipChainExplorerTest.class );

    @Rule
    public TargetDirectory.TestDirectory storeLocation = target.cleanTestDirectory();

    @Test
    public void shouldLoadAllConnectedRelationshipRecordsAndTheirFullChainsOfRelationshipRecords() throws Exception
    {
        // given
        int nDegreeTwoNodes = 10;
        StoreAccess store = createStoreWithOneHighDegreeNodeAndSeveralDegreeTwoNodes( nDegreeTwoNodes );
        RecordStore<RelationshipRecord> relationshipStore = store.getRelationshipStore();

        // when
        int relationshipIdInMiddleOfChain = 10;
        RecordSet<RelationshipRecord> records = new RelationshipChainExplorer( relationshipStore )
                .exploreRelationshipRecordChainsToDepthTwo(
                        relationshipStore.getRecord( relationshipIdInMiddleOfChain ) );

        // then
        assertEquals( nDegreeTwoNodes * 2, records.size() );
    }

    @Test
    public void shouldCopeWithAChainThatReferencesNotInUseZeroValueRecords() throws Exception
    {
        // given
        int nDegreeTwoNodes = 10;
        StoreAccess store = createStoreWithOneHighDegreeNodeAndSeveralDegreeTwoNodes( nDegreeTwoNodes );
        RecordStore<RelationshipRecord> relationshipStore = store.getRelationshipStore();
        breakTheChain( relationshipStore );

        // when
        int relationshipIdInMiddleOfChain = 10;
        RecordSet<RelationshipRecord> records = new RelationshipChainExplorer( relationshipStore )
                .exploreRelationshipRecordChainsToDepthTwo(
                        relationshipStore.getRecord( relationshipIdInMiddleOfChain ) );

        // then
        int recordsInaccessibleBecauseOfBrokenChain = 3;
        assertEquals( nDegreeTwoNodes * 2 - recordsInaccessibleBecauseOfBrokenChain, records.size() );
    }

    private void breakTheChain( RecordStore<RelationshipRecord> relationshipStore )
    {
        int relationshipTowardsEndOfChain = 16;
        relationshipStore.updateRecord( new RelationshipRecord( relationshipTowardsEndOfChain, 0, 0, 0 ) );
    }

    enum TestRelationshipType implements RelationshipType
    {
        CONNECTED
    }

    private StoreAccess createStoreWithOneHighDegreeNodeAndSeveralDegreeTwoNodes( int nDegreeTwoNodes )
    {
        File storeDirectory = storeLocation.directory();
        EmbeddedGraphDatabase database = new EmbeddedGraphDatabase( storeDirectory.getPath() );
        Transaction transaction = database.beginTx();
        try
        {
            Node denseNode = database.createNode();
            for ( int i = 0; i < nDegreeTwoNodes; i++ )
            {
                Node degreeTwoNode = database.createNode();
                Node leafNode = database.createNode();
                if ( i % 2 == 0 )
                {
                    denseNode.createRelationshipTo( degreeTwoNode, TestRelationshipType.CONNECTED );
                }
                else
                {
                    degreeTwoNode.createRelationshipTo( denseNode, TestRelationshipType.CONNECTED );
                }
                degreeTwoNode.createRelationshipTo( leafNode, TestRelationshipType.CONNECTED );
            }
            transaction.success();
        }
        finally
        {
            transaction.finish();
        }
        database.shutdown();
        return new StoreAccess( storeDirectory.getPath() );
    }
}
