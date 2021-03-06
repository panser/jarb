package org.jarbframework.constraint.metadata.enhance;

import org.jarbframework.constraint.metadata.PropertyConstraintDescription;
import org.jarbframework.utils.bean.Annotations;

/**
 * Whenever a property is annotated as @NotNull, it is required.
 * 
 * @author Jeroen van Schagen
 * @since 31-05-2011
 */
public class NotNullPropertyConstraintEnhancer implements PropertyConstraintEnhancer {

    @Override
    public void enhance(PropertyConstraintDescription description) {
        if (Annotations.hasAnnotation(description.toReference(), javax.validation.constraints.NotNull.class)) {
            description.setRequired(true);
        }
    }

}
