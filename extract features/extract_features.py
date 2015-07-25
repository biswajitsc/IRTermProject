import libxml2

def main() :

	features = set()
		
	for i in range(200) :
		doc = libxml2.parseFile("cams/cams_" + str(i + 1) + ".xml")
		ctxt = doc.xpathNewContext()
		res = ctxt.xpathEval("//specsKey/@name")

		for result in res :
			features.add(result.content)
		doc.freeDoc()
		ctxt.xpathFreeContext()

	fo1 = open('features.txt', "wb")
	fo1.write(str(len(features)) + '\n')
	for feature in features :
		fo1.write(feature + '\n')
	fo1.close()

if __name__ == "__main__":
	main()      