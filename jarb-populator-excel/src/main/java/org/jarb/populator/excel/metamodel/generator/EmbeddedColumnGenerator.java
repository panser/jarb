package org.jarb.populator.excel.metamodel.generator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;

import org.jarb.populator.excel.metamodel.ColumnDefinition;
import org.jarb.populator.excel.metamodel.FieldPath;
import org.springframework.util.ReflectionUtils;

/**
 * Creates columnDefinitions for embedded fields.
 * @author Sander Benschop
 *
 */
public final class EmbeddedColumnGenerator {

    /** Private constructor. */
    private EmbeddedColumnGenerator() {
    }

    /**
     * Returns a list of ColumnDefinitions for an embedded field.
     * @param field Embedded field to create ColumnDefinitions from 
     * @return List of ColumnDefinitions
     * @throws InstantiationException Thrown when function is used on a class that cannot be instantiated (abstract or interface)
     * @throws IllegalAccessException Thrown when function does not have access to the definition of the specified class, field, method or constructor 
     */
    public static List<ColumnDefinition> createColumnDefinitionsForEmbeddedField(Field embeddableField) throws InstantiationException, IllegalAccessException {
        List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();
        // This means there are embedded attributes available. Find all attributes in the embeddable class.
        for (Field embeddedPropertyField : embeddableField.getType().getDeclaredFields()) {
            if (!ReflectionUtils.isPublicStaticFinal(embeddedPropertyField)) {
                ColumnDefinition columnDefinition = FieldAnalyzer.analyzeField(embeddedPropertyField);
                columnDefinition.setEmbeddableFieldPath(FieldPath.singleField(embeddableField));
                columnDefinition.setEmbeddedAttribute(true);
                overrideAttributes(embeddableField, columnDefinition, embeddedPropertyField);
                columnDefinitions.add(columnDefinition);
            }
        }
        return columnDefinitions;
    }

    /**
     * If an attribute has got an @AttributeOverrides annotation the column name will be changed here.
     * @param field Embedded object
     * @param columnDefinition ColumnDefinition for embedded field
     * @param embeddedField EmbeddedField
     */
    private static void overrideAttributes(Field field, ColumnDefinition columnDefinition, Field embeddedField) {
        javax.persistence.AttributeOverrides annotation = field.getAnnotation(javax.persistence.AttributeOverrides.class);
        if (annotation != null) {
            overrideColumnName(columnDefinition, embeddedField, annotation);
        }
    }

    /**
     * Overrides the Column name of an embedded object if an @OverrideAttributes annotation is present.
     * @param columnDefinition ColumnDefinition to set the column name for
     * @param embeddedField Embedded field
     * @param annotation @OverrideAttributes Annotation 
     */
    private static void overrideColumnName(ColumnDefinition columnDefinition, Field embeddedField, javax.persistence.AttributeOverrides annotation) {
        for (AttributeOverride overrideAnnotation : annotation.value()) {
            if (overrideAnnotation.name().equals(embeddedField.getName())) {
                columnDefinition.setColumnName(overrideAnnotation.column().name());
            }
        }
    }

}
