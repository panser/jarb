/*
 * (C) 2011 Nidera (www.nidera.com). All rights reserved.
 */
package org.jarbframework.populator.excel.workbook.validator;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jarbframework.populator.excel.metamodel.EntityDefinition;
import org.jarbframework.populator.excel.metamodel.MetaModel;
import org.jarbframework.populator.excel.metamodel.PropertyDatabaseType;
import org.jarbframework.populator.excel.metamodel.PropertyDefinition;

class WorkbookExpectation {
    private Set<String> sheetNames = new HashSet<String>();
    private Map<String, Set<String>> columnNamesMap = new HashMap<String, Set<String>>();

    public WorkbookExpectation(MetaModel metamodel) {
        for (EntityDefinition<?> entity : metamodel.entities()) {
            final String sheetName = entity.getTableName();
            // Each entity type has a specific sheet name
            sheetNames.add(sheetName);
            Set<String> columnNames = new HashSet<String>();
            if (entity.hasDiscriminatorColumn()) {
                columnNames.add(entity.getDiscriminatorColumnName());
            }
            for (PropertyDefinition property : entity.properties()) {
                if (property.getDatabaseType() == PropertyDatabaseType.COLLECTION_REFERENCE) {
                    // Join table properties got their own sheet
                    final String joinSheetName = property.getJoinTableName();
                    sheetNames.add(joinSheetName);
                    // With the join and inverse join as columns
                    Set<String> joinColumnNames = new HashSet<String>();
                    joinColumnNames.add(property.getJoinColumnName());
                    joinColumnNames.add(property.getInverseJoinColumnName());
                    columnNamesMap.put(joinSheetName, joinColumnNames);
                } else {
                    // Regular properties are mapped to a column name
                    columnNames.add(property.getColumnName());
                }
            }
            columnNamesMap.put(sheetName, columnNames);
        }
    }

    public Set<String> getSheetNames() {
        return unmodifiableSet(sheetNames);
    }

    public boolean isExpectedSheet(String sheetName) {
        return sheetNames.contains(sheetName);
    }

    public Set<String> getColumnNames(String sheetName) {
        Set<String> columnNames = columnNamesMap.get(sheetName);
        if (columnNames == null) {
            return emptySet();
        } else {
            return unmodifiableSet(columnNames);
        }
    }

    public boolean isExpectedColumn(String sheetName, String columnName) {
        return getColumnNames(sheetName).contains(columnName);
    }
}