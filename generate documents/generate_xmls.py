import libxml2
import re

key_map = {}

for t in range(1) :
	doc = libxml2.parseFile("cams/cams_" + str(t + 1) + ".xml")
	ctxt = doc.xpathNewContext()
	res = ctxt.xpathEval("//specsKey/@name")

	for result in res :
		xpath = "//specsKey[@name = \"" + str(result.content) + "\"]/text()"
		value = ctxt.xpathEval(xpath)
		
		vals = []
		for values in value :
			vals.append(values.content)

		key_map[result.content] = vals

	fo1 = open('xmls/cam_xml_' + str(t + 1) + '.xml', "wb")
	fo1.write("<root>\n")
	for key in key_map :
		key_underscore = re.findall(r"\w+",key)
		key_underscored = ""
		i = 0
		for k in key_underscore :
			i += 1
			if (i > 1) :
				key_underscored += "_" + k
			else :
				key_underscored += k
		for val in key_map[key] :
			fo1.write("<" + str(key_underscored) + ">" + " " + str(val) + " " + "</" + str(key_underscored) + ">" + "\n")
	fo1.write("</root>\n")		
	fo1.close()		
