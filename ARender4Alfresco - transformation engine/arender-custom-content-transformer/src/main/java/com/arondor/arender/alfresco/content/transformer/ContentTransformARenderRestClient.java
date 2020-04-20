package com.arondor.arender.alfresco.content.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class ContentTransformARenderRestClient
{
    private static final Logger LOGGER = Logger.getLogger(ContentTransformARenderRestClient.class.getName());

    private String address;

    private final RestTemplate template;

    // upgrade transfer partial size, 8MB at a time
    private static final int partialTransferBlockSize = 8 << 20;

    public ContentTransformARenderRestClient()
    {
        template = new RestTemplate();
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(120000);
        clientHttpRequestFactory.setReadTimeout(120000);
        template.setRequestFactory(clientHttpRequestFactory);
    }

    public float getWeatherPerformance()
    {
        try
        {
            return template.getForObject(UriComponentsBuilder.fromHttpUrl(address + "weather").build().toUri(),
                    Float.class);
        }
        catch (Exception e)
        {
            LOGGER.info("Caught exception while trying to fetch weather score, is rendition server down?");
            return -1;
        }
    }

    public void setAddress(String address)
    {
        if (!StringUtils.isEmpty(address))
        {
            if (!address.endsWith("/"))
            {
                address = address + "/";
            }
        }
        else
        {
            throw new UnsupportedOperationException("Sent null or empty rendition server address");
        }
        this.address = address;
    }

    public void uploadDocument(String uuid, InputStream contentInputStream) throws IOException
    {
        // start partial loading
        ResponseEntity<Void> voidResponseEntity = template.postForEntity(
                UriComponentsBuilder.fromHttpUrl(address + "document/" + uuid + "/startPartialLoading?mimeType=null"
                        + "&documentTitle=" + uuid + "&contentSize=-1").build().toString(),
                null, Void.class);
        if (voidResponseEntity.getStatusCode() == HttpStatus.OK)
        {
            // continue with partial loading
            byte[] buffer = new byte[partialTransferBlockSize];
            long count = 0;
            int n = contentInputStream.read(buffer);
            byte[] copyBuffer;
            do
            {
                copyBuffer = (n < partialTransferBlockSize) ? Arrays.copyOfRange(buffer, 0, n) : buffer;
                // compute if next read will have data
                long offset = count;
                count += n;
                n = contentInputStream.read(buffer);
                try
                {
                    final HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    HttpEntity<byte[]> fragment = new HttpEntity<>(copyBuffer, headers);
                    voidResponseEntity = template.postForEntity(UriComponentsBuilder.fromHttpUrl(address + "document/"
                            + uuid + "/continuePartialLoading?offset=" + offset + "&finished=" + (n == -1)).build()
                            .toUri(), fragment, Void.TYPE);
                    if (voidResponseEntity.getStatusCode() != HttpStatus.OK)
                    {
                        throw new IOException("could not transfer document fragment");
                    }
                }
                catch (Exception e)
                {
                    throw new IOException("Exception during partial loading", e);
                }
            }
            while (n != -1);
        }
    }

    public InputStream getInputStream(String uuid, String selector) throws IOException
    {
        // wait for layout
        ResponseEntity<Resource> responseEntity = template.exchange(
                UriComponentsBuilder.fromHttpUrl(address + "/document/" + uuid + "/layout").build().toUri(),
                HttpMethod.GET, null, Resource.class);
        if (!(responseEntity.getStatusCode() == HttpStatus.OK))
        {
            throw new IOException("Could not obtain document layout");
        }
        // now get document content
        responseEntity = template.exchange(UriComponentsBuilder
                .fromHttpUrl(address + "accessor/getContent/raw/" + uuid + "/" + selector).build().toUri(),
                HttpMethod.GET, null, Resource.class);
        return responseEntity.getBody().getInputStream();
    }
}
