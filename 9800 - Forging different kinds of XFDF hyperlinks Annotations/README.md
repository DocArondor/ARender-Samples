

In this :octocat: sample, we will discover how to have XFDF annotations hyperlinking other documents/URLs :+1: imported into ARender. To do so, we have two files : a PDF and an XFDF file.

Remember that in ARender, XFDF is a norm using the XML format to store annotations. 

The PDF is made to be opened in ARender, the XFDF file is made to be imported onto the PDF in ARender.

To do so, open as an example this link : http://arender.fr/ARender/?topPanel.documentMenu.xfdfUpload=true

The parameter topPanel.documentMenu.xfdfUpload=true allows to activate the upload XFDF button in the upload/download menu.

![](images/defaultMenu.png?raw=true)

Upload the fw4.pdf document into ARender using the upload/download menu.

![](images/upload.png?raw=true)

Upload the fw4.xfdf file into ARender using the upload/download menu.

![](images/xfdfUpload.png?raw=true)

If your version is up to date, you should now see three types of hyperlinks being displayed : an inverted text color, a border and background.

To obtain an inversion of text color to the desired color (here color is taken as blue, for the example): 

*__highlight_mode="Inverted" color="#0000FF"__*

To obtain a border color :

*__highlight_mode="Outline" color="#0000FF"__*

To obtain a background color : 

*__highlight_mode="Push" color="#0000FF"__*

