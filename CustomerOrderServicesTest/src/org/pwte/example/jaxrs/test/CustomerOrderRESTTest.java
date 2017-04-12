package org.pwte.example.jaxrs.test;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.client.ApacheHttpClientConfig;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

import junit.framework.TestCase;

public class CustomerOrderRESTTest extends TestCase {

	private static String urlPrefix = "https://localhost:9443/CustomerOrderServicesWeb/";
	private static String urlTestPrefix = "http://localhost:9080/CustomerOrderServicesTest/";
	private ClientConfig config = new ApacheHttpClientConfig();
	private ClientConfig config2 = new ApacheHttpClientConfig();
		
	public void setUp() throws Exception 
	{
		BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
		basicAuth.setUserName("rbarcia");
		basicAuth.setPassword("bl0wfish");
		config.handlers(basicAuth);
		
		BasicAuthSecurityHandler basicAuth2 = new BasicAuthSecurityHandler();
		basicAuth2.setUserName("kbrown");
		basicAuth2.setPassword("bl0wfish");
		config2.handlers(basicAuth2);
	}
	
	public void testLoadCustomer()
	{
		RestClient client = new RestClient(config);
		
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		ClientResponse resourceResponse = resource.accept("application/json").get();
		String customerString = resourceResponse.getEntity(String.class);
		JSONObject customer=null;
		try {
			customer = JSONObject.parse(customerString);
			System.out.println("Test: testLoadCustomer " + "customer JSON RETURNED: " + customer.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Old code for deserializing the JSON returned
		//JSONObject customer = resource.accept("application/json").get(JSONObject.class);
		//ClientResponse resourceResponse = resource.accept("application/json").get();
		//JSONObject customer = resourceResponse.getEntity(JSONObject.class);
		
		RestClient clientTest = new RestClient();
		Resource resourceTest = clientTest.resource(urlTestPrefix+"sampleJSON/customer.json");
		ClientResponse clientTestResponse = resourceTest.accept("application/json").get();
		
		assertEquals(200, resourceResponse.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, resourceResponse.getHeaders().get("Content-Type").get(0));
		
		String customerTestString = clientTestResponse.getEntity(String.class);
		JSONObject customerTest = null;
		try {
		customerTest = JSONObject.parse(customerTestString);
		System.out.println("Test: testLoadCustomer " + "customerTest JSON RETURNED: " + customerTest.toString());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(customer.get("name"), customerTest.get("name"));
		assertEquals(customer.get("householdSize"), customerTest.get("householdSize"));
		assertEquals(customer.get("RESIDENTIAL"), customerTest.get("RESIDENTIAL"));
		assertEquals(customer.get("frequentCustomer"), customerTest.get("frequentCustomer"));
		
	}
	
	public void testUpdateAddress() throws IOException
	{
		
		RestClient clientTest = new RestClient();

		Resource resourceTest = clientTest.resource(urlTestPrefix+"sampleJSON/newAddress1.json");
		JSONObject newAddress1 = resourceTest.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateAddress " + "newAddres1 JSON RETURNED: " + newAddress1.toString());
		
		RestClient client = new RestClient(config);

		Resource customerAddress = client.resource(urlPrefix + "jaxrs/Customer/Address");
		ClientResponse clientResponse = customerAddress.contentType(MediaType.APPLICATION_JSON).put(newAddress1.serialize());
		
		assertEquals(204, clientResponse.getStatusCode());
		
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		JSONObject customer = resource.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateAddress " + "customer JSON RETURNED: " + customer.toString());
		
		assertEquals(newAddress1, customer.get("address"));
		
		Resource resourceTest2 = clientTest.resource(urlTestPrefix+"sampleJSON/newAddress2.json");
		JSONObject newAddress2 = resourceTest2.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateAddress " + "newAddres2 JSON RETURNED: " + newAddress2.toString());
		
		Resource customerAddress2 = client.resource(urlPrefix + "jaxrs/Customer/Address");
		ClientResponse clientResponse2 = customerAddress2.contentType(MediaType.APPLICATION_JSON).put(newAddress2.serialize());
		
		assertEquals(204, clientResponse2.getStatusCode());
		
		Resource resource2 = client.resource(urlPrefix + "jaxrs/Customer");
		JSONObject customer2 = resource2.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateAddress " + "customer2 JSON RETURNED: " + customer2.toString());
		
		assertEquals(newAddress2, customer2.get("address"));
	}
	
	public void testOrderProcess() throws IOException
	{
		RestClient client = new RestClient(config);
		RestClient clientTest = new RestClient();
		
		Resource liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem1.json");
		JSONObject li1 = liTest.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "li1 JSON RETURNED: " + li1.toString());
		
		Resource addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		ClientResponse clientResponse = addTest.accept("application/json").contentType("application/json").post(li1.serialize());
		MultivaluedMap<String, String> headers = clientResponse.getHeaders();
		List<String> etag = headers.get("ETag");
		System.out.println("ETag -> " + etag);
		String version = "-1";
		if(etag != null) version = etag.get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		// Old mechanism to deserialize the JSON returned
		//JSONObject openOrder = clientResponse.getEntity(JSONObject.class);
		String openOrderString = clientResponse.getEntity(String.class);
		JSONObject openOrder=null;
		try {
			openOrder = JSONObject.parse(openOrderString);
			System.out.println("Test: testOrderProcess " + "openOrder JSON RETURNED: " + openOrder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(1, ((JSONArray)openOrder.get("lineitems")).size());
		
		Resource custOrder = client.resource(urlPrefix + "jaxrs/Customer");
		JSONObject customer = custOrder.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "customer JSON RETURNED: " + customer.toString());
		
		JSONObject openOrder2 = (JSONObject)customer.get("openOrder");
		System.out.println("Test: testOrderProcess " + "openOrder2 JSON RETURNED: " + openOrder2.toString());
		
		assertEquals(openOrder2.get("total"), openOrder.get("total"));
		assertEquals(openOrder2.get("status"), openOrder.get("status"));
		assertEquals(openOrder2.get("orderId"), openOrder.get("orderId"));
		assertEquals(((JSONArray)openOrder2.get("lineitems")).size(), ((JSONArray)openOrder.get("lineitems")).size());
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem2.json");
		JSONObject li2 = liTest.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "li2 JSON RETURNED: " + li2.toString());
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.accept("application/json").contentType("application/json").post(li1.serialize());
		assertEquals(412, clientResponse.getStatusCode());
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li2.serialize());
		
		assertEquals(200, clientResponse.getStatusCode());
		
		// Old mechanism to deserialize the JSON returned
		//openOrder = clientResponse.getEntity(JSONObject.class);
		
		openOrderString = clientResponse.getEntity(String.class);
		openOrder=null;
		try {
			openOrder = JSONObject.parse(openOrderString);
			System.out.println("Test: testOrderProcess " + "openOrder JSON RETURNED: " + openOrder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		assertEquals(2, ((JSONArray)openOrder.get("lineitems")).size());
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem3.json");
		JSONObject li3 = liTest.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "li3 JSON RETURNED: " + li3.toString());
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li3.serialize());
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		
		// Old mechanism to deserialize the returned JSON
		//openOrder = clientResponse.getEntity(JSONObject.class);
		openOrderString = clientResponse.getEntity(String.class);
		openOrder=null;
		try {
			openOrder = JSONObject.parse(openOrderString);
			System.out.println("Test: testOrderProcess " + "openOrder JSON RETURNED: " + openOrder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		assertEquals(2, ((JSONArray)openOrder.get("lineitems")).size());
		
		long newQuan = ((Long)li2.get("quantity")) +((Long)li3.get("quantity"))  ;
		
		JSONArray lis = (JSONArray)openOrder.get("lineitems");
		ListIterator<JSONObject> liJSON=  lis.listIterator();
		while(liJSON.hasNext())
		{
			JSONObject liCheck = liJSON.next();
			if(liCheck.get("productId") == li2.get("productId"))
			{
				assertEquals(newQuan, liCheck.get("quantity"));
				break;
			}
		}
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem4.json");
		JSONObject li4 = liTest.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "li4 JSON RETURNED: " + li4.toString());
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li4.serialize());
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		// Old mechanism to deserialize the returned JSON
		//openOrder = clientResponse.getEntity(JSONObject.class);
		openOrderString = clientResponse.getEntity(String.class);
		openOrder=null;
		try {
			openOrder = JSONObject.parse(openOrderString);
			System.out.println("Test: testOrderProcess " + "openOrder JSON RETURNED: " + openOrder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(3, ((JSONArray)openOrder.get("lineitems")).size());
		
		
		Resource removeTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem/"+li4.get("productId"));
		clientResponse = removeTest.accept("application/json").delete();
		assertEquals(412, clientResponse.getStatusCode());
		
		clientResponse = removeTest.header("If-Match",version).accept("application/json").delete();
		assertEquals(200, clientResponse.getStatusCode());
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		Resource submitTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder");
		clientResponse = submitTest.post(null);
		assertEquals(412, clientResponse.getStatusCode());
		
		clientResponse = submitTest.header("If-Match",version).post(null);
		assertEquals(204, clientResponse.getStatusCode());
		
		customer = custOrder.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testOrderProcess " + "customer JSON RETURNED: " + customer.toString());
		assertNull(customer.get("openOrder"));
		
	}
	
	public void testOrderHistory() throws IOException
	{
		RestClient client = new RestClient(config);
		Resource orderHistoryTest = client.resource(urlPrefix + "jaxrs/Customer/Orders");
		ClientResponse clientResponse = orderHistoryTest.accept("application/json").get();
		
		// Old mechanism to deserialize the returned JSON
		//JSONArray orderHistory = clientResponse.getEntity(JSONArray.class);
		String orderHistoryString = clientResponse.getEntity(String.class);
		JSONArray orderHistory=null;
		try {
			orderHistory = JSONArray.parse(orderHistoryString);
			System.out.println("Test: testOrderHistory " + "orderHistory JSON ARRAY RETURNED: " + orderHistory.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(200, clientResponse.getStatusCode());
		int size = orderHistory.size();
		String lastModified = clientResponse.getHeaders().get("Last-Modified").get(0);
		
		
		clientResponse = orderHistoryTest.accept("application/json").header("If-Modified-Since", lastModified).get();
		assertEquals(304, clientResponse.getStatusCode());
		
		testOrderProcess();
		clientResponse = orderHistoryTest.accept("application/json").header("If-Modified-Since", lastModified).get();
		// Old mechanism to deserialize the returned JSON
		//orderHistory = clientResponse.getEntity(JSONArray.class);
		orderHistoryString = clientResponse.getEntity(String.class);
		orderHistory=null;
		try {
			orderHistory = JSONArray.parse(orderHistoryString);
			System.out.println("Test: testOrderHistory " + "orderHistory JSON ARRAY RETURNED: " + orderHistory.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int newSize = orderHistory.size();
		assertEquals(newSize,size+1);
		assertEquals(200, clientResponse.getStatusCode());
	}
	
	public void testFormMetaData ()
	{
		//Residential User
		RestClient client = new RestClient(config);
		Resource info = client.resource(urlPrefix + "jaxrs/Customer/TypeForm");
		ClientResponse clientResponse = info.accept("application/json").get();
		// Old mechanism to deserialize the returned JSON
		//JSONObject formData = clientResponse.getEntity(JSONObject.class);
		String formDataString = clientResponse.getEntity(String.class);
		JSONObject formData=null;
		try {
			formData = JSONObject.parse(formDataString);
			System.out.println("Test: testFormMetaData " + "formData JSON RETURNED: " + formData.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(formData.get("type"),"residential");
		assertEquals(formData.get("label"),"Residential Customer");
		JSONArray groups = (JSONArray)formData.get("formData");
		for (int i = 0; i < groups.size();i++)
		{
			JSONObject item = (JSONObject)groups.get(i);
			if(item.get("name").equals("frequentCustomer"))
			{
				assertEquals(item.get("name"),"frequentCustomer");
				assertEquals(item.get("label"),"Frequent Customer");
				assertEquals(item.get("type"),"string");
				assertEquals(item.get("readonly"),"true");
			}
			else if(item.get("name").equals("householdSize"))
			{
				assertEquals(item.get("name"),"householdSize");
				assertEquals(item.get("label"),"Household Size");
				assertEquals(item.get("type"),"number");
				assertEquals(item.get("required"),"true");
				assertEquals(item.get("constraints"),"{min:1,max:10,places:0}");
			}
		}
		
		
		//Business User
		RestClient client2 = new RestClient(config2);
		Resource info2 = client2.resource(urlPrefix + "jaxrs/Customer/TypeForm");
		ClientResponse clientResponse2 = info2.accept("application/json").get();
		// Old mechanism to deserialize the returned JSON
		//formData = clientResponse2.getEntity(JSONObject.class);
		formDataString = clientResponse2.getEntity(String.class);
		formData=null;
		try {
			formData = JSONObject.parse(formDataString);
			System.out.println("Test: testFormMetaData " + "formData JSON RETURNED: " + formData.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(formData.get("type"),"business");
		assertEquals(formData.get("label"),"Business Customer");
		groups = (JSONArray)formData.get("formData");
		for (int i = 0; i < groups.size();i++)
		{
			JSONObject item = (JSONObject)groups.get(i);
			if(item.get("name").equals("description"))
			{
				assertEquals(item.get("name"),"description");
				assertEquals(item.get("label"),"Description");
				assertEquals(item.get("type"),"text");
			}
			else if(item.get("name").equals("businessPartner"))
			{
				assertEquals(item.get("name"),"businessPartner");
				assertEquals(item.get("label"),"Business Partner");
				assertEquals(item.get("type"),"string");
				assertEquals(item.get("readonly"),"true");
			}
			else if(item.get("name").equals("volumeDiscount"))
			{
				assertEquals(item.get("name"),"volumeDiscount");
				assertEquals(item.get("label"),"Volume Discount");
				assertEquals(item.get("type"),"string");
				assertEquals(item.get("readonly"),"true");
			}
		}
	}
	
	public void testUpdateInfo() throws IOException
	{
		//Residential User
		RestClient client = new RestClient(config);
		long householdSize = 3;
		JSONObject data = new JSONObject();
		data.put("type", "RESIDENTIAL");
		data.put("householdSize",householdSize);
		Resource customerInfo = client.resource(urlPrefix + "jaxrs/Customer/Info");
		ClientResponse clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.serialize());
		assertEquals(204, clientResponse.getStatusCode());
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		JSONObject customer = resource.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateInfo " + "customer JSON RETURNED: " + customer.toString());
		assertEquals(customer.get("householdSize"),data.get("householdSize"));
		data.put("householdSize",6);
		clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.serialize());
		assertEquals(204, clientResponse.getStatusCode());
		
		//Business User
		RestClient client2 = new RestClient(config2);
		String desc = "High Tech Partner";
		data = new JSONObject();
		data.put("type", "BUSINESS");
		data.put("description", desc);
		customerInfo = client2.resource(urlPrefix + "jaxrs/Customer/Info");
		clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.serialize());
		assertEquals(204, clientResponse.getStatusCode());
		resource = client2.resource(urlPrefix + "jaxrs/Customer");
		customer = resource.accept("application/json").get(JSONObject.class);
		System.out.println("Test: testUpdateInfo " + "customer JSON RETURNED: " + customer.toString());
		assertEquals(customer.get("description"),desc);
	}
	

}
