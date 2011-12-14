package de.fiz.escidoc.factory;

import static de.fiz.escidoc.factory.EscidocObjects.createContext;
import static de.fiz.escidoc.factory.EscidocObjects.createItem;
import static de.fiz.escidoc.factory.EscidocObjects.createOrganizationalUnit;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import de.escidoc.core.common.jibx.Marshaller;
import de.escidoc.core.resources.common.ContentStream;
import de.escidoc.core.resources.om.context.Context;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.oum.OrganizationalUnit;

public class EscidocObjectsTest {
	
	private Marshaller<OrganizationalUnit> ouMarshaller=Marshaller.getMarshaller(OrganizationalUnit.class);
	private Marshaller<Item> itemMarshaller=Marshaller.getMarshaller(Item.class);
	private Marshaller<Context> contextMarshaller=Marshaller.getMarshaller(Context.class);
	
	@Test
	public void testCreateOU() throws Exception {
		OrganizationalUnit ou=createOrganizationalUnit("random-ou" + UUID.randomUUID());
		String xml=ouMarshaller.marshalDocument(ou);
		assertNotNull(xml);
		assertTrue(xml.length() > 0);
		System.out.println(xml);
	}
	
	@Test
	public void testCreateContext() throws Exception{
		Context ctx=createContext("random-context" + UUID.randomUUID(), "invalid-ou-id");
		String xml=contextMarshaller.marshalDocument(ctx);
		assertNotNull(xml);
		assertTrue(xml.length() > 0);
		System.out.println(xml);
	}
	
	@Test
	public void testCreatItem1() throws Exception{
		ContentStream stream=new ContentStream("test-content1", StorageType.INTERNAL_MANAGED.toString(), "application/octet-stream");
		stream.setHrefOrBase64Content(Base64.encodeBase64String("it's a test".getBytes()));
		Item item=createItem("invalid-context-id","invalid-content-model-id",Arrays.asList(stream));
		String xml=itemMarshaller.marshalDocument(item);
		assertNotNull(xml);
		assertTrue(xml.length() > 0);
		System.out.println(xml);
	}

	@Test
	public void testCreatItem2() throws Exception{
		Item item=createItem("invalid-context-id","invalid-content-model-id");
		String xml=itemMarshaller.marshalDocument(item);
		assertNotNull(xml);
		assertTrue(xml.length() > 0);
		System.out.println(xml);
	}
}
