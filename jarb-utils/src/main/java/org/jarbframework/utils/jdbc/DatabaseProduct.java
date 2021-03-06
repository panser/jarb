package org.jarbframework.utils.jdbc;

import static org.jarbframework.utils.jdbc.JdbcUtils.doWithConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jarbframework.utils.StringUtils;

/**
 * Describes the product name and version of a specific database.
 * 
 * @author Jeroen van Schagen
 */
public final class DatabaseProduct {
    
    private final DatabaseProductType type;
    
    private final String name;
    
    private final String version;

    public DatabaseProduct(String name, String version) {
        this.type = DatabaseProductType.findByName(name);
        this.name = name;
        this.version = version;
    }
    
    /**
     * Retrieve the database product type, if known.
     * @return the type, else {@code null}
     */
    public DatabaseProductType getType() {
        return type;
    }
    
    /**
     * Retrieve the database product name.
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Retrieve the database product version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Retrieve the short name of the database product.
     * @return the short name, before first white space
     */
    public String getShortName() {
        return StringUtils.substringBefore(name, " ").toLowerCase();
    }

    /**
     * Determine the database product from a data source.
     * @param dataSource the data source
     * @return the database product
     */
    public static DatabaseProduct fromDataSource(DataSource dataSource) {
        return doWithConnection(dataSource, new JdbcConnectionCallback<DatabaseProduct>() {

            @Override
            public DatabaseProduct doWork(Connection connection) throws SQLException {
                DatabaseMetaData metaData = connection.getMetaData();
                String productName = metaData.getDatabaseProductName();
                String productVersion = metaData.getDatabaseProductVersion();
                return new DatabaseProduct(productName, productVersion);
            }

        });
    }
    
}
