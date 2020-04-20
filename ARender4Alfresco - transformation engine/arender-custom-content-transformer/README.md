# ARender4Alfresco - Using ARender as a rendition/transformation module for Alfresco

Using *ARender* powerful REST API java client implementation, it is very simple to use ARender as a custom rendition service for Alfresco.

In this sample code, you'll find a ready to build module for Alfresco to deposit into alfresco *WEB-INF/lib/* folder. 

In order to be able to build this artifact, you'll need to be able to access ARender private maven repository and therefore have a valid ARender licence. 

## Configuration

To configure the module, you have to alter into the file named *service-context.xml* the hostname of your rendition server  

### 1/ In *service-context.xml*, setting the hostname of the rendition server

By default, it will contain this value: 

```xml
<bean id="transformer.worker.arender2pdf" class="com.arondor.arender.alfresco.content.transformer.ARenderContentTransformerWorker">
    <property name="arenderRenditionServerAddress" value="http://localhost:8761/" />
</bean>
```

If your rendition server is, as an example on hostname *ARenderRenditionServer*, this is how you should change the XML file content : 

```xml
<bean id="transformer.worker.arender2pdf" class="com.arondor.arender.alfresco.content.transformer.ARenderContentTransformerWorker">
    <property name="arenderRenditionServerAddress" value="http://ARenderRenditionServer:8761/" />
</bean>
```

