/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.jarbframework.populator.excel.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Mar 14, 2014
 */
public class JpaEntityWriter implements EntityWriter {
    
    private final EntityManagerFactory entityManagerFactory;

    public JpaEntityWriter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void write(Iterable<Object> entities) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        
        try {
            for (Object entity : entities) {
                entityManager.persist(entity);
            }
            
            transaction.commit();
        } catch (RuntimeException rte) {
            transaction.rollback();
        }
    }
    
}
