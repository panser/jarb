/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.jarbframework.populator.excel.entity;

/**
 * 
 *
 * @author Jeroen van Schagen
 * @since Mar 14, 2014
 */
public interface EntityWriter {
    
    void write(Iterable<Object> entities);

}
