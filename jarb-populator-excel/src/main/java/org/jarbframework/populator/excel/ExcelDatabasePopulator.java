package org.jarbframework.populator.excel;

import java.io.IOException;
import java.util.List;

import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.jarbframework.populator.DatabasePopulator;
import org.jarbframework.populator.excel.entity.JpaEntityWriter;
import org.jarbframework.populator.excel.metadata.EntityMetadataRegistry;
import org.jarbframework.populator.excel.metadata.HibernateJpaEntityMetadataGenerator;
import org.jarbframework.populator.excel.workbook.Workbook;
import org.jarbframework.populator.excel.workbook.reader.PoiWorkbookParser;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Populates the database with content from an excel file.
 * 
 * @author Jeroen van Schagen
 * @since 7-6-2011
 */
public class ExcelDatabasePopulator implements DatabasePopulator {
        
    private final HibernateEntityManagerFactory entityManagerFactory;
    
    private final Resource resource;
    
    public ExcelDatabasePopulator(HibernateEntityManagerFactory entityManagerFactory, Resource resource) {
        Assert.state(resource != null, "Excel resource cannot be null");
        Assert.state(entityManagerFactory != null, "Entity manager factory cannot be null");
        
        this.entityManagerFactory = entityManagerFactory;
        this.resource = resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate() {
        try {
            Workbook workbook = new PoiWorkbookParser().parse(resource.getInputStream());
            EntityMetadataRegistry metadata = new HibernateJpaEntityMetadataGenerator(entityManagerFactory.getSessionFactory()).generate();
            List<Object> entities = metadata.mapToEntities(workbook);
            new JpaEntityWriter(entityManagerFactory).write(entities);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
