package org.pwte.example.jpa.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;

import org.junit.*;
import org.dbunit.DBTestCase;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.tools.RunScript;
import org.pwte.example.domain.Category;
import org.pwte.example.domain.Product;
import org.pwte.example.service.ProductSearchService;
import org.pwte.example.service.ProductSearchServiceImpl;

public class ProductSearchServiceTest {

	private static Context ctx;
	private static EJBContainer ejbContainer;
	private static ProductSearchService productSearchService;
	private static final String JDBC_DRIVER = org.h2.Driver.class.getName();
	private static final String JDBC_URL = "jdbc:h2:mem:ORDERDB;DB_CLOSE_DELAY=-1";
	private static final String USER = "sa";
	private static final String PASSWORD = "";
	
		
		
		@BeforeClass
		public static void createSchema() throws Exception {
			// Create ORDER DB
			String createOrderDB = "classpath:/org/pwte/example/jpa/test/sql/createOrderDB.sql";
			RunScript.execute(JDBC_URL, USER, PASSWORD, createOrderDB, null, false);
			
			//Create EJB Container
			Properties properties = new Properties();
	        properties.setProperty(EJBContainer.MODULES, "CustomerOrderServices");
	        properties.setProperty(EJBContainer.PROVIDER, "com.ibm.websphere.ejbcontainer.EmbeddableContainerProvider");
			ejbContainer = javax.ejb.embeddable.EJBContainer.createEJBContainer(properties);
			
			// Get the EJB
			ctx = ejbContainer.getContext();
		    productSearchService = (ProductSearchService) ctx.lookup("java:global/classes/ProductSearchServiceImpl");
		}

		@Before
		public void importDataSet() throws Exception {
			IDataSet dataSet = readDataSet();
			cleanlyInsert(dataSet);
		}

		private IDataSet readDataSet() throws Exception {
			FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
			flatXmlDataSetBuilder.setColumnSensing(true);
			return  flatXmlDataSetBuilder.build(new File("src/org/pwte/example/jpa/test/xml/CustomerOrderInitialDataSet.xml"));
		}

		private void cleanlyInsert(IDataSet dataSet) throws Exception {
			IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER, PASSWORD);
			databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
			databaseTester.setDataSet(dataSet);
			databaseTester.onSetup();
		}
		
		
	@Test	
	public void testLoadCategory() {
		try
		{
			InputStream file = this.getClass().getResourceAsStream ("LoadCategoryParentExpectedResults.xml");
			IDataSet expectedParentDataSet = new FlatXmlDataSet(file);
			ITable expectedParentTable = expectedParentDataSet.getTable("CATEGORY");
			int categoryId = Integer.parseInt((String)expectedParentTable.getValue(0,"CAT_ID")); 
			Category cat = productSearchService.loadCategory(categoryId);
			assertEquals(Integer.parseInt((String)expectedParentTable.getValue(0,"CAT_ID")),cat.getCategoryID());
			assertEquals(expectedParentTable.getValue(0,"CAT_NAME"),cat.getName() );
			
			Collection<Category> childrenCats = cat.getSubCategories();
			InputStream childFile = this.getClass().getResourceAsStream ("LoadCategoryChildrenExpectedResults.xml");
			IDataSet expectedChildrenDataSet = new FlatXmlDataSet(childFile);
			ITable expectedChildrenTable = expectedChildrenDataSet.getTable("CATEGORY");
			assertEquals(expectedChildrenTable.getRowCount(),childrenCats.size());
			Iterator<Category> iterCat = childrenCats.iterator();
			for(int i = 0; i < childrenCats.size();i++)
			{
				cat = iterCat.next();
				if(Integer.parseInt((String)expectedChildrenTable.getValue(i,"CAT_ID"))==cat.getCategoryID())
				{
					assertEquals(Integer.parseInt((String)expectedChildrenTable.getValue(i,"CAT_ID")),cat.getCategoryID());
					assertEquals(expectedChildrenTable.getValue(i,"CAT_NAME"),cat.getName() );
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			fail(e.getLocalizedMessage());
		}
		
	}

	public void testLoadProduct() {
		try
		{
			InputStream file = this.getClass().getResourceAsStream ("LoadProductExpectedData.xml");
			IDataSet expectedProductDataSet = new FlatXmlDataSet(file);
			ITable expectedProductTable = expectedProductDataSet.getTable("PRODUCT");
			int productId = Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID"));
			Product product = productSearchService.loadProduct(productId);
			assertEquals(Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID")),product.getProductId());
			assertEquals(expectedProductTable.getValue(0,"NAME"),product.getName() );
			assertEquals(expectedProductTable.getValue(0,"DESCRIPTION"),product.getDescription() );
			assertEquals(expectedProductTable.getValue(0,"PRICE"),product.getPrice().toString());
			
			Collection <Category> categories = product.getCategories();
			InputStream childFile = this.getClass().getResourceAsStream ("LoadProductsExpectCats.xml");
			IDataSet expectedCatDataSet = new FlatXmlDataSet(childFile);
			ITable expectedCatTable = expectedCatDataSet.getTable("CATEGORY");
			assertEquals(expectedCatTable.getRowCount(),categories.size());
			Iterator<Category> iterCat = categories.iterator();
			for(int i = 0; i < categories.size();i++)
			{
				Category cat = iterCat.next();
				if(Integer.parseInt((String)expectedCatTable.getValue(i,"CAT_ID"))==cat.getCategoryID())
				{
					assertEquals(Integer.parseInt((String)expectedCatTable.getValue(i,"CAT_ID")),cat.getCategoryID());
					assertEquals(expectedCatTable.getValue(i,"CAT_NAME"),cat.getName() );
				}
			}
			
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			fail(e.getLocalizedMessage());
		}
	}

	public void testLoadProductsByCategory() {
		try
		{
			//TEST LOAD Product by Parent Category
			InputStream parentCatFile = this.getClass().getResourceAsStream ("LoadProductByCatParent.xml");
			IDataSet expectedParentDataSet = new FlatXmlDataSet(parentCatFile);
			ITable expectedParentTable = expectedParentDataSet.getTable("CATEGORY");
			int categoryId = Integer.parseInt((String)expectedParentTable.getValue(0,"CAT_ID")); 
			List<Product> products = productSearchService.loadProductsByCategory(categoryId);
			
			InputStream expectedParentCatFile = this.getClass().getResourceAsStream ("ExpectedCatsByParentCat.xml");
			IDataSet expectedProductDataSet = new FlatXmlDataSet(expectedParentCatFile);
			ITable expectedProductTable = expectedProductDataSet.getTable("PRODUCT");
			
			assertEquals(expectedProductTable.getRowCount(),products.size());
			for(int i= 0; i < products.size(); i++)
			{
				if(Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID"))==products.get(i).getProductId())
				{
					assertEquals(Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID")),products.get(i).getProductId());
					assertEquals(expectedProductTable.getValue(i,"NAME"),products.get(i).getName() );
					assertEquals(expectedProductTable.getValue(i,"DESCRIPTION"),products.get(i).getDescription() );
					assertEquals(expectedProductTable.getValue(i,"PRICE"),products.get(i).getPrice().toString());
				}
			}
			
			//TEST LOAD Product by Child Category
			InputStream childCatFile = this.getClass().getResourceAsStream ("LoadProductByCatChild.xml");
			IDataSet expectedChildDataSet = new FlatXmlDataSet(childCatFile);
			ITable expectedChildTable = expectedChildDataSet.getTable("CATEGORY");
			categoryId = Integer.parseInt((String)expectedChildTable.getValue(0,"CAT_ID")); 
			products = productSearchService.loadProductsByCategory(categoryId);
			
			InputStream expectedChildCatFile = this.getClass().getResourceAsStream ("ExpectedCatsByChildCat.xml");
			expectedProductDataSet = new FlatXmlDataSet(expectedChildCatFile);
			expectedProductTable = expectedProductDataSet.getTable("PRODUCT");
			
			assertEquals(expectedProductTable.getRowCount(),products.size());
			for(int i= 0; i < products.size(); i++)
			{
				if(Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID"))==products.get(i).getProductId())
				{
					assertEquals(Integer.parseInt((String)expectedProductTable.getValue(0,"PRODUCT_ID")),products.get(i).getProductId());
					assertEquals(expectedProductTable.getValue(i,"NAME"),products.get(i).getName() );
					assertEquals(expectedProductTable.getValue(i,"DESCRIPTION"),products.get(i).getDescription() );
					assertEquals(expectedProductTable.getValue(i,"PRICE"),products.get(i).getPrice().toString());
				}
			}
		}
		catch (Exception e) 
		{
			fail(e.getLocalizedMessage());
		}
	}

	public void testGetTopLevelCategories() {
		try
		{
			List<Category> categories = productSearchService.getTopLevelCategories();
			InputStream file = this.getClass().getResourceAsStream ("TopLevelCategoriesExpectedResults.xml");
			IDataSet expectedDataSet = new FlatXmlDataSet(file);
			ITable expectedTable = expectedDataSet.getTable("CATEGORY");
			assertEquals(expectedTable.getRowCount(),categories.size());
			for(int i = 0; i< categories.size();i++)
			{
				
				assertEquals(Integer.parseInt((String)expectedTable.getValue(i,"CAT_ID")),categories.get(i).getCategoryID());
				assertEquals(expectedTable.getValue(i,"CAT_NAME"),categories.get(i).getName() );
			}
		}
		catch (Exception e) {
			fail(e.getLocalizedMessage());
		}
	}

	

}
