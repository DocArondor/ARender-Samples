
In this scenario, you just opened a document into ARender and you know for sure somewhere in this document there is a bookmark named "ARender samples".

No problem, ARender got you covered :notebook_with_decorative_cover: .

To do so, you can hook yourself to the ARender __Javascript API__ and when the document is loaded ask the ARender __Javascript API__ which page your target destinations is located to. 

You can then jump to the page ARender returns you. You will find a sample code in the file named *ARender_jump_named_destination.js*.

This kind of files can be loaded into ARender easily using the ARender startup script mechanism, just append this parameter to your URL to test our script:

http://arender.fr/ARender/?arenderjs.startupScript=https://raw.githubusercontent.com/DocArondor/ARender-Samples/master/9797%20-%20Get%20the%20page%20corresponding%20to%20the%20current%20document%20named%20destination/ARender_jump_named_destination.js
