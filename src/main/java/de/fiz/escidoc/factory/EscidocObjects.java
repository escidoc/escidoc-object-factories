package de.fiz.escidoc.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.escidoc.core.resources.common.ContentStream;
import de.escidoc.core.resources.common.MetadataRecord;
import de.escidoc.core.resources.common.MetadataRecords;
import de.escidoc.core.resources.common.properties.PublicStatus;
import de.escidoc.core.resources.common.reference.ContentModelRef;
import de.escidoc.core.resources.common.reference.ContextRef;
import de.escidoc.core.resources.common.reference.OrganizationalUnitRef;
import de.escidoc.core.resources.om.context.AdminDescriptors;
import de.escidoc.core.resources.om.context.Context;
import de.escidoc.core.resources.om.context.ContextProperties;
import de.escidoc.core.resources.om.context.OrganizationalUnitRefs;
import de.escidoc.core.resources.om.item.Item;
import de.escidoc.core.resources.om.item.ItemProperties;
import de.escidoc.core.resources.om.item.StorageType;
import de.escidoc.core.resources.om.item.component.Component;
import de.escidoc.core.resources.om.item.component.ComponentContent;
import de.escidoc.core.resources.om.item.component.ComponentProperties;
import de.escidoc.core.resources.om.item.component.Components;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.OrganizationalUnitProperties;

/**
 * Abstract factory class for generating different kinds of eScidoc objects like items, contexts and OUs This class is
 * intended to be used in testing environments for data generation
 * 
 * @author fasseg
 */
public abstract class EscidocObjects {
	private static final Random RANDOM = new Random();

	/**
	 * Create an item from the given information
	 * 
	 * @param contextId
	 *            the context's ID to be linked to this item
	 * @param contentModelId
	 *            the content model's ID which is to be linked to this item
	 * @param componentList
	 *            this item's list of {@link Component}s
	 * @param mdRecords
	 *            this item's list of metadata records
	 * @return a new instance of {@link Item}
	 * @throws ParserConfigurationException
	 */
	public static Item createItem(final String contextId, final String contentModelId,
			final List<Component> componentList, final List<MetadataRecord> mdRecords)
			throws ParserConfigurationException {
		// create the Item' properties
		final ItemProperties properties = new ItemProperties();
		properties.setPublicStatus(PublicStatus.PENDING);
		properties.setContext(new ContextRef(contextId));
		properties.setContentModel(new ContentModelRef(contentModelId));
		// a list of Components
		final Components components = new Components();
		components.addAll(componentList);
		// and metadata records
		MetadataRecords records;
		if (mdRecords == null || mdRecords.size() == 0) {
			records = createMetadataRecords("test-object", "item");
		} else {
			records = new MetadataRecords();
			records.addAll(mdRecords);
		}
		Item i = new Item();
		i.setProperties(properties);
		i.setMetadataRecords(records);
		i.setComponents(components);
		return i;
	}

	/**
	 * Create a {@link Component} from a URI with a given PID
	 * 
	 * @param pid
	 *            the pid to be associated with this {@link Component}
	 * @param uri
	 *            the URI of the {@link Component}'s data
	 * @return a new {@link Component} instance
	 */
	public static Component createComponentFromURI(final String pid, final String fileName) {
		ComponentContent content = new ComponentContent();
		content.setXLinkHref("file:" + fileName);
		content.setStorageType(StorageType.INTERNAL_MANAGED);
		Component comp = new Component();
		ComponentProperties props = new ComponentProperties();
		props.setPid(pid);
		props.setFileName(fileName);
		props.setMimeType("application/octet-stream");
		props.setContentCategory("pre-print");
		props.setValidStatus("valid");
		props.setVisibility("public");
		comp.setProperties(props);
		comp.setContent(content);
		return comp;
	}

	/**
	 * create a {@link ContentStream} from a given URI
	 * 
	 * @param uri
	 *            the URI which holds the {@link ContentStream}'s data
	 * @return a new {@link ContentStream} instance
	 */
	public static ContentStream createContentStreamFromURI(final URI uri) {
		ContentStream stream = new ContentStream("test-content", StorageType.INTERNAL_MANAGED,
				"application/octet-stream");
		stream.setXLinkHref(uri.toString());
		return stream;
	}

	/**
	 * create a {@link ContentStream} from randomly generated data
	 * 
	 * @param targetDirectory
	 *            the directory to write the content files
	 * @param size
	 *            the size the random data should have
	 * @return a new {@link ContentStream} instance
	 * @throws IOException
	 */
	public static ContentStream createContentStreamFromRandomData(final File targetDirectory, final long size)
			throws IOException {
		File f = File.createTempFile("testdata-", ".content", targetDirectory);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			long numWritten = 0;
			byte[] buffer = (size > 1024) ? new byte[1024] : new byte[(int) size];
			while (numWritten < size) {
				RANDOM.nextBytes(buffer);
				int len = (size - numWritten < buffer.length) ? (int) (size - numWritten) : buffer.length;
				out.write(buffer, 0, len);
				numWritten += len;
			}
			out.flush();
			return createContentStreamFromURI(f.toURI());
		} finally {
			out.close();
		}
	}

	/**
	 * Create a {@link Component} from random data and the data into content file in a given directory
	 * 
	 * @param targetDirectory
	 *            the directory to write the content file to
	 * @param size
	 *            the size the random data should have
	 * @return a new {@link Component} instance
	 * @throws IOException
	 */
	public static Component createComponentFromRandomData(final File targetDirectory, final long size)
			throws IOException {
		File f = File.createTempFile("item-", ".content", targetDirectory);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			long numWritten = 0;
			byte[] buffer = (size > 1024) ? new byte[1024] : new byte[(int) size];
			while (numWritten < size) {
				RANDOM.nextBytes(buffer);
				int len = (size - numWritten < buffer.length) ? (int) (size - numWritten) : buffer.length;
				out.write(buffer, 0, len);
				numWritten += len;
			}
			out.flush();
			return createComponentFromURI("why?", f.getName());
		} finally {
			out.close();
		}
	}

	/**
	 * Create an item from the given information
	 * 
	 * @param contextId
	 *            the context's ID to be linked to this item
	 * @param contentModelId
	 *            the content model's ID which is to be linked to this item
	 * @param componentList
	 *            this item's list of {@link Component}s
	 * @return a new instance of {@link Item}
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static Item createItem(final String contextId, final String contentModelId,
			List<Component> componentList) throws ParserConfigurationException {
		return createItem(contextId, contentModelId, componentList, Collections.EMPTY_LIST);
	}

	/**
	 * Create an item from the given information
	 * 
	 * @param contextId
	 *            the context's ID to be linked to this item
	 * @param contentModelId
	 *            the content model's ID which is to be linked to this item
	 * @return a new instance of {@link Item}
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static Item createItem(final String contextId, final String contentModelId)
			throws ParserConfigurationException {
		return createItem(contextId, contentModelId, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
	}

	/**
	 * Create a new {@link Context} instance with a given name and ou id
	 * 
	 * @param name
	 *            the name of the {@link Context}
	 * @param ouId
	 *            the organizational unit id of the {@link Context}
	 * @return a new {@link Context} instance
	 */
	public static Context createContext(final String name, final String ouId) {
		return createContext(name, Arrays.asList(ouId));
	}

	/**
	 * Create a new {@link Context} instance with a given name and ou id
	 * 
	 * @param name
	 *            the name of the {@link Context}
	 * @param ouIds
	 *            the {@link List} of organizational unit ids of the {@link Context}
	 * @return a new {@link Context} instance
	 */
	public static Context createContext(final String name, final List<String> ouIds) {
		final OrganizationalUnitRefs refs = new OrganizationalUnitRefs();
		for (String ouId : ouIds) {
			refs.add(new OrganizationalUnitRef(ouId));
		}
		final ContextProperties properties = new ContextProperties();
		properties.setName(name);
		properties.setPublicStatus(PublicStatus.PENDING);
		properties.setOrganizationalUnitRefs(refs);
		Context ctx=new Context();
		ctx.setProperties(properties);
		ctx.setAdminDescriptors(new AdminDescriptors());
		return ctx;
	}

	/**
	 * Create a new {@link OrganizationalUnit} instance with a given name
	 * 
	 * @param name
	 *            the name of the {@link OrganizationalUnit}
	 * @return a new {@link OrganizationalUnit} instance
	 */
	public static OrganizationalUnit createOrganizationalUnit(final String name) {
		final OrganizationalUnitProperties properties = new OrganizationalUnitProperties();
		properties.setName(name);
		properties.setPublicStatus(PublicStatus.PENDING);
		OrganizationalUnit ou=new OrganizationalUnit();
		ou.setProperties(properties);
		return ou;
	}

	public static MetadataRecords createMetadataRecords(String title, String nameSpace)
			throws ParserConfigurationException {
		MetadataRecords records = new MetadataRecords();
		MetadataRecord escidoc = new MetadataRecord("escidoc");
		escidoc.setLastModificationDate(new DateTime());
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setCoalescing(true);
		factory.setValidating(true);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document doc = builder.newDocument();
		final Element mdRecordContent = doc.createElementNS(null, nameSpace);
		final Element titleElmt = doc.createElementNS("http://purl.org/dc/elements/1.1/", "title");
		titleElmt.setPrefix("dc");
		titleElmt.setTextContent(title);
		mdRecordContent.appendChild(titleElmt);
		escidoc.setContent(mdRecordContent);
		records.add(escidoc);
		return records;
	}
}
