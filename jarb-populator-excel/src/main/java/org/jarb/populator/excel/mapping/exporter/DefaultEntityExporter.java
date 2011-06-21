package org.jarb.populator.excel.mapping.exporter;

import java.util.Date;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.jarb.populator.excel.entity.EntityRegistry;
import org.jarb.populator.excel.mapping.ValueConversionService;
import org.jarb.populator.excel.metamodel.ClassDefinition;
import org.jarb.populator.excel.metamodel.ColumnType;
import org.jarb.populator.excel.metamodel.MetaModel;
import org.jarb.populator.excel.metamodel.PropertyDefinition;
import org.jarb.populator.excel.metamodel.PropertyPath;
import org.jarb.populator.excel.util.JpaUtils;
import org.jarb.populator.excel.workbook.BooleanValue;
import org.jarb.populator.excel.workbook.CellValue;
import org.jarb.populator.excel.workbook.DateValue;
import org.jarb.populator.excel.workbook.EmptyValue;
import org.jarb.populator.excel.workbook.NumericValue;
import org.jarb.populator.excel.workbook.Row;
import org.jarb.populator.excel.workbook.Sheet;
import org.jarb.populator.excel.workbook.StringValue;
import org.jarb.populator.excel.workbook.Workbook;
import org.jarb.utils.BeanPropertyHandler;

/**
 * Default implementation of {@link EntityExporter}.
 * 
 * @author Jeroen van Schagen
 * @since 12-05-2011
 */
public class DefaultEntityExporter implements EntityExporter {
    private EntityManagerFactory entityManagerFactory;
    private ValueConversionService valueConversionService;
    
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
    
    public void setValueConversionService(ValueConversionService valueConversionService) {
        this.valueConversionService = valueConversionService;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Workbook export(EntityRegistry registry, MetaModel metamodel) {
        Workbook workbook = createTemplate(metamodel);
        for (ClassDefinition<?> classDefinition : metamodel.getClassDefinitions()) {
            includeEntities(registry, classDefinition, workbook);
        }
        return workbook;
    }
    
    // Create template (sheets for each type, and each column name)
    
    private Workbook createTemplate(MetaModel metamodel) {
        Workbook workbook = new Workbook();
        for (ClassDefinition<?> classDefinition : metamodel.getClassDefinitions()) {
            createClassSheet(classDefinition, workbook);
        }
        return workbook;
    }
    
    /**
     * Create the sheet for a specific type of entity.
     * @param classDefinition description of the entity structure being stored
     * @param workbook the workbook that will hold our sheet
     */
    private void createClassSheet(ClassDefinition<?> classDefinition, Workbook workbook) {
        Sheet sheet = workbook.createSheet(classDefinition.getTableName());
        storeColumnNames(sheet, classDefinition);
        for(PropertyDefinition propertyDefinition : classDefinition.getPropertyDefinitions()) {
            if(propertyDefinition.getColumnType() == ColumnType.JOIN_TABLE) {
                createJoinSheet(propertyDefinition, workbook);
            }
        }
    }
    
    /**
     * Store all column names in the sheet.
     * @param sheet the sheet that should contain our columns
     * @param classDefinition description of all columns
     */
    private void storeColumnNames(Sheet sheet, ClassDefinition<?> classDefinition) {
        int columnNumber = 0;
        Set<String> columnNames = classDefinition.getColumnNames();
        columnNames.add("id");
        for(String columnName : columnNames) {
            sheet.setColumnNameAt(columnNumber++, columnName);
        }
    }
    
    /**
     * Create the join sheet between two types of entities.
     * @param propertyDefinition definition of the join table property
     * @param workbook the workbook that will hold our sheet
     */
    private void createJoinSheet(PropertyDefinition propertyDefinition, Workbook workbook) {
        Sheet joinSheet = workbook.createSheet(propertyDefinition.getJoinTableName());
        joinSheet.setColumnNameAt(0, propertyDefinition.getJoinColumnName());
        joinSheet.setColumnNameAt(1, propertyDefinition.getInverseJoinColumnName());
    }
    
    // Include entities into our workbook
    
    private <T> void includeEntities(EntityRegistry registry, ClassDefinition<T> classDefinition, Workbook workbook) {
        Sheet sheet = workbook.getSheet(classDefinition.getTableName());
        for(T entity : registry.getAll(classDefinition.getPersistentClass())) {
            includeEntity(entity, classDefinition, sheet);
        }
    }
   
    /**
     * Store a specific entity in our sheet.
     * @param <T> type of entity being stored
     * @param entity the entity being stored
     * @param sheet the sheet in which we store the entity
     * @param classDefinition description of the entity
     */
    private <T> void includeEntity(T entity, ClassDefinition<T> classDefinition, Sheet sheet) {
        Row row = sheet.createRow();
        for(PropertyDefinition propertyDefinition : classDefinition.getPropertyDefinitions()) {
            final ColumnType columnType = propertyDefinition.getColumnType();
            if(columnType == ColumnType.BASIC) {
                // Retrieve the property value and store it as cell value
                Object propertyValue = getPropertyValue(entity, propertyDefinition);
                row.setCellValueAt(propertyDefinition.getColumnName(), createCellValue(propertyValue));
            } else if(columnType == ColumnType.JOIN_COLUMN) {
                // Retrieve the entity as property value and store its identifier
                Object referenceEntity = getPropertyValue(entity, propertyDefinition);
                if(referenceEntity != null) {
                    Object referenceIdentifier = JpaUtils.getIdentifier(referenceEntity, entityManagerFactory);
                    row.setCellValueAt(propertyDefinition.getColumnName(), createCellValue(referenceIdentifier));
                }
            } else if(columnType == ColumnType.JOIN_TABLE) {
                includeJoinTable(entity, propertyDefinition, sheet.getWorkbook());
            }
        }
        if(classDefinition.hasDiscriminatorColumn()) {
            final String discriminatorValue = classDefinition.getDiscriminatorValue(entity.getClass());
            row.setCellValueAt(classDefinition.getDiscriminatorColumnName(), new StringValue(discriminatorValue));
        }
        Object entityIdentifier = JpaUtils.getIdentifier(entity, entityManagerFactory);
        row.setCellValueAt("id", createCellValue(entityIdentifier));
    }
    
    /**
     * Store a collection of reference entities in a seperate "join" sheet.
     * This seperate sheet represents the join between two entities, as in
     * the relational database.
     * @param entity the entity containing our joins
     * @param propertyDefinition description of the join property
     * @param workbook the workbook that will contain our "join" sheet
     */
    private void includeJoinTable(Object entity, PropertyDefinition propertyDefinition, Workbook workbook) {
        Iterable<?> referenceEntities = (Iterable<?>) getPropertyValue(entity, propertyDefinition);
        if(referenceEntities != null) {
            Sheet joinSheet = workbook.getSheet(propertyDefinition.getJoinTableName());
            Object entityIdentifier = JpaUtils.getIdentifier(entity, entityManagerFactory);
            for(Object referenceEntity : referenceEntities) {
                Row joinRow = joinSheet.createRow();
                joinRow.setCellValueAt(0, createCellValue(entityIdentifier));
                Object referenceIdentifier = JpaUtils.getIdentifier(referenceEntity, entityManagerFactory);
                joinRow.setCellValueAt(1, createCellValue(referenceIdentifier));
            }
        }
    }
    
    /**
     * Retrieve the property value of an entity.
     * @param entity the entity that contains our value
     * @param propertyDefinition description of the property being retrieved
     * @return value of the property in our entity
     */
    private Object getPropertyValue(Object entity, PropertyDefinition propertyDefinition) {
        Object value = null;
        if(propertyDefinition.isEmbeddedAttribute()) {
            // Whenever our property is embedded, retrieve the embeddable that contains it
            final PropertyPath embeddablePath = propertyDefinition.getEmbeddablePath();
            if(BeanPropertyHandler.hasProperty(entity, embeddablePath.getStart().getName())) {
                Object leafEmbeddable = embeddablePath.traverse(entity);
                value = BeanPropertyHandler.getValue(leafEmbeddable, propertyDefinition.getName());
            }
        } else if(BeanPropertyHandler.hasProperty(entity, propertyDefinition.getName())) {
            value = BeanPropertyHandler.getValue(entity, propertyDefinition.getName());
        }
        return value;
    }
    
    /**
     * Create a cell value based on some raw value.
     * @param value raw value being stored
     * @return cell value containing our raw value
     */
    private CellValue createCellValue(Object value) {
        CellValue cellValue = null;
        if(value == null) {
            cellValue = new EmptyValue();
        } else {
            final Class<?> valueType = value.getClass();
            if(String.class.equals(valueType)) {
                cellValue = new StringValue((String) value);
            } if(Boolean.class.isAssignableFrom(valueType)) {
                cellValue = new BooleanValue((Boolean) value);
            } else if(Date.class.isAssignableFrom(valueType)) {
                cellValue = new DateValue((Date) value);
            } else if(Number.class.isAssignableFrom(valueType)) {
                cellValue = new NumericValue((Number) value);
            } else {
                // Remaining types are converted into string values
                final String valueAsString = valueConversionService.convert(value, String.class);
                cellValue = new StringValue(valueAsString);
            }
        }
        return cellValue;
    }

}
