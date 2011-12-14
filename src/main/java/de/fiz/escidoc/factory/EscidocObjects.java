package de.fiz.escidoc.factory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import de.escidoc.core.resources.common.ContentStream;
import de.escidoc.core.resources.common.ContentStreams;
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
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.OrganizationalUnitProperties;

public abstract class EscidocObjects {
	public static final Random RANDOM = new Random();

	public static Item createItem(final String contextId, final String contentModelId,
			final List<ContentStream> contentStreams, final List<MetadataRecord> mdRecords) {
		final ItemProperties properties = new ItemProperties.Builder(PublicStatus.PENDING)
				.context(new ContextRef(contextId))
				.contentModel(new ContentModelRef(contentModelId))
				.build();
		final ContentStreams streams = new ContentStreams();
		streams.addAll(contentStreams);
		MetadataRecords records = new MetadataRecords();
		records.addAll(mdRecords);
		return new Item.Builder(properties)
				.mdRecords(records)
				.contentStreams(streams)
				.build();
	}

	public static ContentStream createContentStreamFromURI(final URI uri) {
		ContentStream stream = new ContentStream("test-content", StorageType.INTERNAL_MANAGED.toString(), "application/octet-stream");
		stream.setHrefOrBase64Content(uri.toString());
		return stream;
	}

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

	public static Item createItem(final String contextId, final String contentModelId,
			List<ContentStream> contentStreams) {
		return createItem(contextId, contentModelId, contentStreams, Collections.EMPTY_LIST);
	}

	public static Item createItem(final String contextId, final String contentModelId) {
		return createItem(contextId, contentModelId, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
	}

	public static Context createContext(final String name, final String ouId) {
		return createContext(name, Arrays.asList(ouId));
	}

	public static Context createContext(final String name, final List<String> ouIds) {
		final OrganizationalUnitRefs refs = new OrganizationalUnitRefs();
		for (String ouId : ouIds) {
			refs.add(new OrganizationalUnitRef(ouId));
		}
		final ContextProperties properties = new ContextProperties.Builder(name, PublicStatus.PENDING)
				.organizationUnitRefs(refs)
				.build();
		return new Context(properties, new AdminDescriptors());
	}

	public static OrganizationalUnit createOrganizationalUnit(final String name) {
		final OrganizationalUnitProperties properties = new OrganizationalUnitProperties.Builder(name, PublicStatus.PENDING)
				.build();
		return new OrganizationalUnit.Builder(properties)
				.build();
	}
}
