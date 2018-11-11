/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.mssql.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSAttributeBase;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;

/**
 * SQLServerProcedureParameter
 */
public class SQLServerProcedureParameter implements DBSProcedureParameter, DBSAttributeBase, DBSObject {
    private SQLServerProcedure procedure;
    private String paramName;
    private int ordinalPosition;
    private SQLServerDataType dataType;
    private int maxLength;
    private int scale;
    private int precision;
    private DBSProcedureParameterKind parameterKind;
    private boolean nullable;
    private String defaultValue;

    public SQLServerProcedureParameter(
        DBRProgressMonitor monitor,
        SQLServerProcedure procedure,
        JDBCResultSet dbResult)
        throws DBException
    {
        this.procedure = procedure;
        this.paramName = JDBCUtils.safeGetString(dbResult, "name");
        this.ordinalPosition = JDBCUtils.safeGetInt(dbResult, "parameter_id");

        int typeID = JDBCUtils.safeGetInt(dbResult, "user_type_id");
        this.dataType = procedure.getContainer().getDatabase().getDataType(monitor, typeID);

        boolean isOutput = JDBCUtils.safeGetInt(dbResult, "is_output") != 0;
        boolean isCursor = JDBCUtils.safeGetInt(dbResult, "is_cursor_ref") != 0;
        boolean readonly = JDBCUtils.safeGetInt(dbResult, "is_readonly") != 0;
        this.parameterKind = isCursor ? DBSProcedureParameterKind.RESULTSET :
            (isOutput ? DBSProcedureParameterKind.OUT :
                (readonly ? DBSProcedureParameterKind.IN : DBSProcedureParameterKind.INOUT));

        this.maxLength = JDBCUtils.safeGetInt(dbResult, "max_length");
        this.scale = JDBCUtils.safeGetInt(dbResult, "scale");
        this.precision = JDBCUtils.safeGetInt(dbResult, "precision");

        if (JDBCUtils.safeGetInt(dbResult, "has_default_value") != 0) {
            defaultValue = JDBCUtils.safeGetString(dbResult, "default_value");
        }
        nullable = JDBCUtils.safeGetInt(dbResult, "is_nullable") != 0;
    }

    @NotNull
    @Override
    public SQLServerDataSource getDataSource() {
        return procedure.getDataSource();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public SQLServerProcedure getParentObject() {
        return procedure;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return paramName;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 2)
    public SQLServerDataType getParameterType() {
        return dataType;
    }

    @NotNull
    @Override
    @Property(viewable = true, order = 3)
    public DBSProcedureParameterKind getParameterKind() {
        return parameterKind;
    }

    @Override
    @Property(viewable = true, order = 4)
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    @Override
    public boolean isRequired() {
        return !nullable;
    }

    @Override
    public boolean isAutoGenerated() {
        return false;
    }

    @Override
    public String getTypeName() {
        return dataType.getTypeName();
    }

    @Override
    public String getFullTypeName() {
        return DBUtils.getFullTypeName(this);
    }

    @Override
    public int getTypeID() {
        return dataType.getTypeID();
    }

    @Override
    public DBPDataKind getDataKind() {
        return dataType.getDataKind();
    }

    @Override
    public Integer getScale() {
        return scale;
    }

    @Override
    public Integer getPrecision() {
        return precision;
    }

    @Override
    public long getMaxLength() {
        return maxLength;
    }

    @Property(viewable = true, order = 5)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
