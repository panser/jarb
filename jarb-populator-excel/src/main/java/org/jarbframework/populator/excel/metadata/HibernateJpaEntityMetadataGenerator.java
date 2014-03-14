/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.jarbframework.populator.excel.metadata;

import java.beans.PropertyDescriptor;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.jarbframework.populator.excel.metadata.EntityMetadataRegistry.EntityMetadata;
import org.springframework.beans.BeanUtils;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Mar 14, 2014
 */
public class HibernateJpaEntityMetadataGenerator implements EntityMetadataGenerator {
    
    /** Provides access to the class mapping meta-data. **/
    private final SessionFactory sessionFactory;
    
    public HibernateJpaEntityMetadataGenerator(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public EntityMetadataRegistry generate() {
        EntityMetadataRegistry registry = new EntityMetadataRegistry();
        for (ClassMetadata classMetadata : sessionFactory.getAllClassMetadata().values()) {
            registry.add(describeClass((AbstractEntityPersister) classMetadata));
        }
        return registry;
    }
    
    private EntityMetadata describeClass(AbstractEntityPersister classMetadata) {
        Class<?> entityClass = classMetadata.getMappedClass();
        EntityMetadata entityMapping = new EntityMetadata(entityClass, classMetadata.getTableName());
        
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(entityClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String[] columnNames = classMetadata.getPropertyColumnNames(propertyDescriptor.getName());
            entityMapping.add(propertyDescriptor.getName(), columnNames[0]);
        }

        return entityMapping;
    }


}
