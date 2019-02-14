package com.arondor.arender.alfresco.content.transformer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.InitializingBean;

import com.arondor.arender.viewer.common.rest.DocumentServiceRestClient;
import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.common.document.id.DocumentIdFactory;
import com.arondor.viewer.rendition.api.document.DocumentAccessorSelector;

public class ARenderContentTransformerWorker implements ContentTransformerWorker, InitializingBean
{
    private DocumentServiceRestClient restClient = new DocumentServiceRestClient();

    private String arenderRenditionServerAddress = "http://localhost:8761/";

    @Override
    public boolean isAvailable()
    {
        return restClient.getWeatherPerformance() != -1;
    }

    @Override
    public String getVersionString()
    {
        return "1.0.0";
    }

    @Override
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        if (!"application/pdf".equals(targetMimetype))
        {
            return false;
        }
        return true;
    }

    @Override
    public String getComments(boolean available)
    {
        // no comments yet to add
        return null;
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception
    {
        DocumentId uuid = DocumentIdFactory.getInstance().generate();
        try (InputStream contentInputStream = reader.getContentInputStream();
                OutputStream contentOutputStream = writer.getContentOutputStream())
        {
            restClient.uploadDocument(uuid, null, UUID.randomUUID().toString(), contentInputStream);
            try (InputStream inputStream = restClient.getDocumentAccessor(uuid, DocumentAccessorSelector.RENDERED)
                    .getInputStream())
            {
                IOUtils.copy(inputStream, contentOutputStream);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        restClient.setAddress(getArenderRenditionServerAddress());
    }

    public String getArenderRenditionServerAddress()
    {
        return arenderRenditionServerAddress;
    }

    public void setArenderRenditionServerAddress(String arenderRenditionServerAddress)
    {
        this.arenderRenditionServerAddress = arenderRenditionServerAddress;
    }
}
