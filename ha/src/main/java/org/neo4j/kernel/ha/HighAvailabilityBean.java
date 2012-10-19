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
package org.neo4j.kernel.ha;

import java.util.ArrayList;
import java.util.List;

import javax.management.NotCompliantMBeanException;

import org.neo4j.helpers.Format;
import org.neo4j.helpers.Service;
import org.neo4j.jmx.impl.ManagementBeanProvider;
import org.neo4j.jmx.impl.ManagementData;
import org.neo4j.jmx.impl.Neo4jMBean;
import org.neo4j.kernel.HighlyAvailableKernelData;
import org.neo4j.management.ClusterMemberInfo;
import org.neo4j.management.HighAvailability;

@Service.Implementation(ManagementBeanProvider.class)
public final class HighAvailabilityBean extends ManagementBeanProvider
{
    public HighAvailabilityBean()
    {
        super( HighAvailability.class );
    }

    @Override
    protected Neo4jMBean createMXBean( ManagementData management ) throws NotCompliantMBeanException
    {
        if ( !isHA( management ) )
        {
            return null;
        }
        return new HighAvailabilityImpl( management, true );
    }

    @Override
    protected Neo4jMBean createMBean( ManagementData management ) throws NotCompliantMBeanException
    {
        if ( !isHA( management ) )
        {
            return null;
        }
        return new HighAvailabilityImpl( management );
    }

    private static boolean isHA( ManagementData management )
    {
        return management.getKernelData().graphDatabase() instanceof HighlyAvailableGraphDatabase;
    }

    private static class HighAvailabilityImpl extends Neo4jMBean implements HighAvailability
    {
        private final HighlyAvailableKernelData kernelData;

        HighAvailabilityImpl( ManagementData management )
                throws NotCompliantMBeanException
        {
            super( management );
            this.kernelData = (HighlyAvailableKernelData) management.getKernelData();
        }

        HighAvailabilityImpl( ManagementData management, boolean isMXBean )
        {
            super( management, isMXBean );
            this.kernelData = (HighlyAvailableKernelData) management.getKernelData();
        }

        public String getServerId()
        {
            return kernelData.getMemberInfo().getInstanceId();
        }

        public ClusterMemberInfo[] getInstancesInCluster()
        {
            return kernelData.getClusterInfo();
        }

        public String getInstanceState()
        {
            return kernelData.getMemberInfo().getStatus();
        }

        public ClusterMemberInfo[] getConnectedSlaves()
        {

            List<ClusterMemberInfo> result = new ArrayList<ClusterMemberInfo>();
            return result.toArray( new ClusterMemberInfo[result.size()] );
        }

        public String getLastUpdateTime()
        {
            return Format.date( kernelData.getMemberInfo().getLastUpdateTime() );
        }

        public String update()
        {
            long time = System.currentTimeMillis();
            try
            {
                // TODO make this work through a MemberOps class passed in as a dependency
            }
            catch ( Exception e )
            {
                return "Update failed: " + e;
            }
            time = System.currentTimeMillis() - time;
            return "Update completed in " + time + "ms";
        }

    }
}