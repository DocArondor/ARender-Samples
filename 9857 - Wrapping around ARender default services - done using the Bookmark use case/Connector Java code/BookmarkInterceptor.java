package com.arondor.viewer.common.interceptor;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import com.arondor.viewer.client.api.document.Bookmark;
import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.client.api.document.DocumentNotAvailableException;

public class BookmarkInterceptor
{
    private static final Logger LOG = Logger.getLogger(BookmarkInterceptor.class);

    private static final String METHOD_ENTRY_PHRASE = "=== IN method : ";

    private static final String METHOD_OUTPUT_PHRASE = "=== OUT method : ";

    public Object doBookmarkWrapping(ProceedingJoinPoint pjp) throws Throwable
    {
        Signature signature = pjp.getSignature();

        LOG.info(METHOD_ENTRY_PHRASE + signature.toShortString());

        Object returnedValue = null;
        DocumentId documentId = extractDocumentId(pjp);
        if (documentId == null)
        {
            // error, ID should not be null at this point, throw error
            throw new DocumentNotAvailableException("Null documentId");
        }
        if ("b64_I2RlZmF1bHQ=".equals(documentId.toString()))
        {
            return forgeBookmarks();
        }
        else
        {
            try
            {
                returnedValue = pjp.proceed();
            }
            catch (Throwable e)
            {
                LOG.info(METHOD_OUTPUT_PHRASE + signature.toShortString());
                throw e;
            }
            LOG.info(METHOD_OUTPUT_PHRASE + signature.toShortString());
            return returnedValue;
        }
    }

    private List<Bookmark> forgeBookmarks()
    {
        // here bookmarks could be fed from a network API REST call, etc...

        // bookmarks are built using the target page number, the title of the
        // bookmark, and its destination in the page in a height ratio
        Bookmark bookmark = new Bookmark(0, "new Bookmark", 0.5f);

        // you can as well provide a named destination and a filename to open
        // external bookmarks, it would need to have targetPage at -1 to 
    	// be considered external though
        bookmark.setNamedDestination("Target1");
        bookmark.setFileName("otherFile.pdf");
        return Arrays.asList(bookmark);
    }

    private DocumentId extractDocumentId(ProceedingJoinPoint pjp)
    {
        if (pjp.getArgs() == null)
        {
            return null;
        }
        // this is just to ensure in case Args is empty
        // we still return the first found DocumentId
        for (Object argument : pjp.getArgs())
        {
            if (argument instanceof DocumentId)
            {
                DocumentId documentId = (DocumentId) argument;
                return documentId;
            }
        }
        return null;
    }

}
