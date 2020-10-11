# XMLtoJSON
Parses XML to JSON using only native Java.  

## Approach
Instead of parsing the XML into java objects and then to JSON, XML is parsed directly into JSON using a string buffer.
The XML is read as a tree (a tag, a value and a closing tag is referred to as a node), and uses a depth-search to find child nodes, when a leaf node is discovered, it will conduct a breadth-search to find other nodes on the same level. Every node discovered is treated as a potential 'parent', recursion is used to find other child nodes.  
A pointer increments over each character in the file and directly parses it to JSON, so the algorithm theoritcally has a time complexity of O(N).

## Testing
There are three tests, one for a complete XML document, one for an empty XML document and one for an XML document with an error (tag with no name) to validate the exception handling.
