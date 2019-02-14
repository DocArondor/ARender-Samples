package com.arondor.arender.alfresco.content.transformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ARenderContentTransformerWorkerTest
{

    @Test
    public void testConvertDocument() throws Exception
    {
        ARenderContentTransformerWorker contentTransformer = new ARenderContentTransformerWorker();
        ContentReader mockReader = Mockito.mock(ContentReader.class);
        ContentWriter mockWriter = Mockito.mock(ContentWriter.class);

        Mockito.doAnswer(new Answer<InputStream>()
        {

            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable
            {
                return new FileInputStream(
                        "src/main/resources/alfresco/module/arender-custom-content-transformer/alfresco-global.properties");
            }
        }).when(mockReader).getContentInputStream();

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Mockito.doAnswer(new Answer<OutputStream>()
        {
            @Override
            public OutputStream answer(InvocationOnMock invocation) throws Throwable
            {
                return bos;
            }
        }).when(mockWriter).getContentOutputStream();

        contentTransformer.transform(mockReader, mockWriter, null);
        byte[] byteArray = bos.toByteArray();
        new String(byteArray).startsWith("%PDF");
        IOUtils.copy(new ByteArrayInputStream(byteArray), new FileOutputStream("target/alfresco.pdf"));
    }
}
