package org.jarb.populator.excel.entity.persist;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jarb.populator.excel.DefaultExcelTestDataCase;
import org.jarb.populator.excel.entity.EntityRegistry;
import org.jarb.populator.excel.entity.EntityTable;
import org.jarb.populator.excel.mapping.importer.ExcelImporter;
import org.jarb.populator.excel.mapping.importer.ExcelRow;
import org.jarb.populator.excel.metamodel.ClassDefinition;
import org.jarb.populator.excel.metamodel.generator.ClassDefinitionsGenerator;
import org.jarb.populator.excel.workbook.Workbook;
import org.jarb.populator.excel.workbook.reader.PoiExcelParser;
import org.junit.Before;
import org.junit.Test;

import domain.entities.CompanyVehicle;
import domain.entities.Customer;
import domain.entities.Employee;
import domain.entities.Project;
import domain.entities.ServiceLevelAgreement;

public class DataWriterTest extends DefaultExcelTestDataCase {
    private Set<Object> connectionInstances;
    private Set<Object> actualConnectionInstanceClassNames;
    private Set<Object> expectedConnectionInstanceClassNames;
    private Map<ClassDefinition<?>, Map<Object, ExcelRow>> parseExcelMap;
    private Workbook excel;
    private ClassDefinition<?> customer;
    private ClassDefinition<?> project;
    private ClassDefinition<?> sla;
    private Metamodel metamodel;
    private EntityType<?> customerEntity;
    private EntityType<?> projectEntity;
    private EntityType<?> slaEntity;

    @Before
    public void setUpDatabaseConnectionTest() throws InvalidFormatException, IOException, InstantiationException, IllegalAccessException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        excel = new PoiExcelParser().parse(new FileInputStream("src/test/resources/ExcelUnitTesting.xls"));
        connectionInstances = new HashSet<Object>();

        metamodel = getEntityManagerFactory().getMetamodel();
        customerEntity = ClassDefinitionsGenerator.getEntityFromMetamodel(Customer.class, metamodel);
        projectEntity = ClassDefinitionsGenerator.getEntityFromMetamodel(Project.class, metamodel);
        slaEntity = ClassDefinitionsGenerator.getEntityFromMetamodel(ServiceLevelAgreement.class, metamodel);

        actualConnectionInstanceClassNames = new HashSet<Object>();
        expectedConnectionInstanceClassNames = new HashSet<Object>();

        //For code coverage purposes:
        Constructor<DataWriter> constructor = DataWriter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();

    }

    @Test
    public void testCreateConnectionInstanceSet() throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException,
            ClassNotFoundException {
        List<ClassDefinition<?>> classDefinitionList = new ArrayList<ClassDefinition<?>>();
        customer = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), customerEntity, false);
        project = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), projectEntity, false);
        sla = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), slaEntity, false);
        classDefinitionList.add(customer);
        classDefinitionList.add(project);
        classDefinitionList.add(sla);

        parseExcelMap = ExcelImporter.parseExcel(excel, classDefinitionList);
        connectionInstances = DataWriter.createInstanceSet(toRegistry(parseExcelMap));

        for (Object connectionInstance : connectionInstances) {
            actualConnectionInstanceClassNames.add(connectionInstance.getClass().getName());
        }

        expectedConnectionInstanceClassNames.add("domain.entities.Customer");
        expectedConnectionInstanceClassNames.add("domain.entities.Project");
        expectedConnectionInstanceClassNames.add("domain.entities.ServiceLevelAgreement");
        assertEquals(expectedConnectionInstanceClassNames, actualConnectionInstanceClassNames);
    }

    @Test
    public void testSaveEntity() throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException, ClassNotFoundException,
            InvalidFormatException, IOException {
        List<ClassDefinition<?>> classDefinitionList = new ArrayList<ClassDefinition<?>>();

        customer = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), customerEntity, false);
        project = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), projectEntity, false);
        sla = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), slaEntity, false);

        classDefinitionList.add(customer);
        classDefinitionList.add(project);
        classDefinitionList.add(sla);

        parseExcelMap = ExcelImporter.parseExcel(excel, classDefinitionList);
        connectionInstances = DataWriter.createInstanceSet(toRegistry(parseExcelMap));
        DataWriter.saveEntity(connectionInstances, getEntityManagerFactory());
    }

    @Test
    public void testEntityReferencing() throws InstantiationException, ClassNotFoundException, IllegalAccessException, NoSuchFieldException,
            InvalidFormatException, IOException {
        List<ClassDefinition<?>> classDefinitionList = new ArrayList<ClassDefinition<?>>();
        excel = new PoiExcelParser().parse(new FileInputStream("src/test/resources/ExcelEmployeesVehicles.xls"));

        EntityType<?> employeeEntity = ClassDefinitionsGenerator.getEntityFromMetamodel(Employee.class, metamodel);
        EntityType<?> vehicleEntity = ClassDefinitionsGenerator.getEntityFromMetamodel(CompanyVehicle.class, metamodel);

        ClassDefinition<?> employee = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), employeeEntity, false);
        ClassDefinition<?> vehicle = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(getEntityManagerFactory(), vehicleEntity, true);
        //  project = ClassDefinitionsGenerator.createSingleClassDefinitionFromMetamodel(entityManagerFactory, projectEntity, true);

        classDefinitionList.add(employee);
        classDefinitionList.add(vehicle);
        //  classDefinitionList.add(project);

        try {
        parseExcelMap = ExcelImporter.parseExcel(excel, classDefinitionList);
        connectionInstances = DataWriter.createInstanceSet(toRegistry(parseExcelMap));
        DataWriter.saveEntity(connectionInstances, getEntityManagerFactory());
        } catch(Exception e ) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private EntityRegistry toRegistry(Map<ClassDefinition<?>, Map<Object, ExcelRow>> entitiesMap) {
        EntityRegistry registry = new EntityRegistry();
        for (Map.Entry<ClassDefinition<?>, Map<Object, ExcelRow>> entitiesEntry : entitiesMap.entrySet()) {
            @SuppressWarnings("rawtypes")
            final Class entityClass = entitiesEntry.getKey().getEntityClass();
            EntityTable<Object> table = new EntityTable<Object>(entityClass);
            for (Map.Entry<Object, ExcelRow> excelRowEntry : entitiesEntry.getValue().entrySet()) {
                table.add(excelRowEntry.getKey(), excelRowEntry.getValue().getCreatedInstance());
            }
            registry.addAll(table);
        }
        return registry;
    }

}
