/*
 * Copyright (C) 2010-2014 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.model.impl.jdbc.cache;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.RuntimeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple objects cache.
 */
public final class JDBCObjectSimpleCache<OWNER extends DBSObject, OBJECT extends DBSObject> extends JDBCObjectCache<OWNER, OBJECT> {
    private final String query;
    private final Class<OBJECT> objectType;
    private final Object[] queryParameters;
    private Constructor<OBJECT> objectConstructor;

    public JDBCObjectSimpleCache(Class<OBJECT> objectType, String query, Object ... args)
    {
        this.query = query;
        this.objectType = objectType;
        this.queryParameters = args;
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCSession session, OWNER owner)
        throws SQLException
    {
        JDBCPreparedStatement dbStat = session.prepareStatement(query);
        if (queryParameters != null) {
            for (int i = 0; i < queryParameters.length; i++) {
                dbStat.setObject(i + 1, queryParameters[i]);
            }
        }
        return dbStat;
    }

    @Override
    protected OBJECT fetchObject(JDBCSession session, OWNER owner, ResultSet resultSet)
        throws SQLException, DBException
    {
        try {
            if (objectConstructor == null) {
                for (Class<?> argType = owner.getClass(); argType != null; argType = argType.getSuperclass()) {
                    try {
                        objectConstructor = objectType.getConstructor(argType, ResultSet.class);
                        break;
                    } catch (Exception e) {
                        // Not found - check interfaces
                        for (Class<?> intType : argType.getInterfaces()) {
                            try {
                                objectConstructor = objectType.getConstructor(intType, ResultSet.class);
                                break;
                            } catch (Exception e2) {
                                // Not found
                            }
                        }
                        if (objectConstructor != null) {
                            break;
                        }
                    }
                }
                if (objectConstructor == null) {
                    throw new DBException("Can't find proper constructor for object '" + objectType.getName() + "'");
                }
            }
            return objectConstructor.newInstance(owner, resultSet);
        } catch (Exception e) {
            throw new DBException(
                "Error creating cache object",
                e instanceof InvocationTargetException ? ((InvocationTargetException)e).getTargetException() : e);
        }
    }

}
