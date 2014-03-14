/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.jarbframework.populator.excel.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jarbframework.populator.excel.workbook.Sheet;
import org.jarbframework.populator.excel.workbook.Workbook;
import org.springframework.beans.BeanUtils;

public class EntityMetadataRegistry {
    
    private final Map<String, EntityMetadata> entities = new HashMap<>();
    
    public void add(EntityMetadata entityMetadata) {
        entities.put(entityMetadata.getTableName(), entityMetadata);
    }
    
    public EntityMetadata getEntity(String tableName) {
        return entities.get(tableName);
    }
    
    public List<Object> mapToEntities(Workbook workbook) {
        List<Object> objects = new ArrayList<>();
        
        for (Sheet sheet : workbook.getSheets()) {
            EntityMetadata entityMetadata = getEntity(sheet.getName());
            Object object = BeanUtils.instantiateClass(entityMetadata.getEntityClass());
            for (String columnName : sheet.getColumnNames()) {
                
            }
            objects.add(object);
        }
        
        return objects;
    }
    
    public static class EntityMetadata {
        
        private final Class<?> entityClass;
        
        private final String tableName;
        
        private final Map<String, PropertyMetadata> properties = new HashMap<>();
        
        public EntityMetadata(Class<?> entityClass, String tableName) {
            this.entityClass = entityClass;
            this.tableName = tableName;
        }
        
        public void add(String propertyPath, String columnName) {
            properties.put(propertyPath, new PropertyMetadata(propertyPath, columnName));
        }
        
        public Class<?> getEntityClass() {
            return entityClass;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public PropertyMetadata getProperty(String propertyPath) {
            return properties.get(propertyPath);
        }

    }
    
    public static class PropertyMetadata {
        
        private final String propertyPath;
        
        private final String columnName;
        
        public PropertyMetadata(String propertyPath, String columnName) {
            this.propertyPath = propertyPath;
            this.columnName = columnName;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public String getPropertyPath() {
            return propertyPath;
        }
        
    }

}