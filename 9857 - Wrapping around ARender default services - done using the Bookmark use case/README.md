In this :octocat: sample, we describe how ARender can be wrapped around its core DocumentService calls on the front end side in order to feed custom third party ressource before the call to the rendition is being done.

This sample focus on wrapping the bookmark call and replace it with a custom one.


The customisation is made using two components : 
* adding configuration files to the front end war file
  * this configures AOP to invoke custom code logic around ARender code
* adding to your connector the code to be called when certain methods of ARender are being called

1. The front end customisation

In the front end server configuration folder, you will find a custom server integration xml file prefilled with what you need to load the xml named *arender-hmi-bookmark-interceptor.xml* which does the actual AOP configuration.

This xml file declares first the method to be wrapped around: 

``` xml
<aop:pointcut id="bookmarkInterception" expression="execution(* com.arondor.viewer.common.rendition.connector.ClientDocumentService.getBookmarks(..))" />
```
A full list of methods existing in this DocumentService can be found in our public Javadoc of the ARender Rendition API in the Interface *__DocumentService__*.


Next declaration in the xml file is a reference to which method will be called in the interceptor when a matching method is trapped:

``` xml
<aop:aspect id="bookmarkAspect" ref="bookmarkInterceptor">
    <aop:around method="doBookmarkWrapping" pointcut-ref="bookmarkInterception" />
</aop:aspect>
```

Finally we declare the actual code that will wrap around the method invocation : 

``` xml
	<bean id="bookmarkInterceptor" class="com.arondor.viewer.common.interceptor.BookmarkInterceptor" />
```

2. The Java code wrapping around the *__public List\<Bookmark\> getBookmarks(DocumentId documentId)__* method

We cannot stress enough how important it will to log/monitor what you will be doing as we cannot provide product level support over this kind of customisation :boom: of ARender code. However, if you follow this sample :wrench: carrefully you should not have issues doing it. 

The getBookmarks method contains a single parameter, the documentId concerned by this call. We retrieve it in the *__BookmarkInterceptor.java__* class using the method named *extractDocumentId* : 

``` java
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
```

Once we obtain the documentId, it is then possible to do any code logic desired to change the bookmarks regarding the documentId being called.

For the example, we are here modifying the bookmarks of the default ARender document. Its DocumentId is always _**b64_I2RlZmF1bHQ=**_.

``` java
// null safety is being tested before this line
if ("b64_I2RlZmF1bHQ=".equals(documentId.toString()))
{
    return forgeBookmarks();
}
```

Now it is up to you to forge custom bookmarks (here we do a simple one) : 

``` java
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
```
and obtain this kind of result :sparkles:! 

![](images/result.png?raw=true)

Or if it is not the documentId you were searching for :weary:, simply return the normal method result: 

``` java
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
```