package org.jarb.constraint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jarb.constraint.database.DatabaseConstraintRepository;
import org.jarb.constraint.domain.Car;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
public class BeanConstraintMetadataFactoryBeanTest {
    private BeanConstraintAccessorFactoryBean factoryBean;

    @Autowired
    private DatabaseConstraintRepository databaseConstraintRepository;

    @Before
    public void setUp() throws Exception {
        factoryBean = new BeanConstraintAccessorFactoryBean();
        factoryBean.setDatabaseConstraintRepository(databaseConstraintRepository);
    }

    /**
     * Ensure that the generated descriptor can access database and JSR constraint information.
     */
    @Test
    public void testGeneratedObject() throws Exception {
        BeanConstraintAccessor descriptor = factoryBean.getObject();
        BeanConstraintDescription<Car> carDescription = descriptor.describe(Car.class);
        PropertyConstraintDescription licenseDescription = carDescription.getPropertyMetadata("licenseNumber");
        assertEquals(String.class, licenseDescription.getJavaType()); // Retrieved by introspection
        assertTrue(licenseDescription.isRequired()); // Retrieved from database
        assertEquals(Integer.valueOf(6), licenseDescription.getMinimumLength()); // Retrieved from @Length
        assertEquals(Integer.valueOf(6), licenseDescription.getMaximumLength()); // Merged @Length and database
    }

}
