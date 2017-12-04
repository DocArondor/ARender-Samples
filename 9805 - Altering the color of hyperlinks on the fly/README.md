

In this :octocat: sample, we will discover how to alter links to other documents/URLs, or to the current document :+1:. To do so, we need a couple of Javascript hooks well setup.


# The first step: Hooking the ARender Javascript methods and listen for link display events

*__getARenderJS().registerDisplayLinkHandler(function (docId, Id, isXFDFAnnotation, displayLinkEvent) {})__*

This method will call you back and inform you at each display of a link from which documentId this link is coming from, its unique ID (this will also be set as ID of the DOM element), a boolean to know if this link is an XFDF annotation or is stored inside the PDF and finally the display event, allowing us to alter the PDF color.

# The second step: altering the links colors

A set of methods allows for altering the link color : 

*__getARenderJS().setLinkStyle(displayLinkEvent, style)__* changes the style of the link to either : push (background color), outline (border), none (transparent). A last style exist, *__inverted__* (text color is altered) but it is only for XFDF annotations as it overlays the existing document, it is therefor incompatible with embedded links (ARender does not alter embedded document contents).
Style value is taken as a string and case insensitive. 


*__getARenderJS().setLinkColor(displayLinkEvent, color)__*

Allows to change the link color. Color is expected to be received as string in HTML compatible hex color format. Example : "#FFFFFF" for white. 

*__getARenderJS().setLinkOpacity(displayLinkEvent, opacity)__*

Sets the link opacity. This is to impact the opacity of the link and a float value is expected ranging from 0 (transparent) to 1 (opaque).

Using those methods, you can generate every kind of links, from (as an example) this:

![](images/border-samples-orig.png?raw=true)

to that :

![](images/border-samples.png?raw=true)


# The last step : reacting to clicks in order to alter links for future opening/browsing of this document

Finally, you can as well react to a click on a link and decide if an annotation should have its color/style changed or not. Using the method *__getARenderJS().getAnnotationJSAPI().registerFollowLinkHandler( function(documentId, targetPage, destination, action, linkId) {})__* you can react to any link click and decide if this link is needed for color alteration. Ids are stable and you can therefor decide on the next displayLinkEvent if you should edit or not the link. 

If you want to edit the link the instant the click has been done, you can search in the DOM the link using the selector *__link=document.getElementById(linkId)__*. Customization are then possible on the element style : 

*__link.style.opacity=0.5__*

*__link.style.backgroundColor="yellow"__*