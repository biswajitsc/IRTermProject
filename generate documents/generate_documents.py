import libxml2
from stemming.porter2 import stem
import re

def round_to_n(x, n):
	if n < 1:
		raise ValueError("number of significant digits must be >= 1")
	# Use %e format to get the n most significant digits, as a string.
	format = "%." + str(n-1) + "e"
	as_string = format % x
	return int(float(as_string))

def isFloat(value):
  try:
    float(value)
    return True
  except ValueError:
    return False	

def main() :

	units = []
	units.append("rs")
	units.append("inch")
	units.append("mm")
	units.append("cm")
	units.append("mp")
	units.append("megapixel")
	units.append("megapixels")
	units.append("fps")
	units.append("sec")
	units.append("dots")
	units.append("g")
	units.append("p")

	for t in range(418) :
		doc = libxml2.parseFile("cams/cams_" + str(t + 1) + ".xml")
		ctxt = doc.xpathNewContext()
		res = ctxt.xpathEval("//specsKey/@name")
		values = []
		bigrams = []
		features = []
		# print "Document ",t

		for result in res :
			xpath = "//specsKey[@name = \"" + str(result.content) + "\"]/text()"
			value = ctxt.xpathEval(xpath)
			attr = result.content.lower()
			# features.append(attr)

			if (attr == "color_list") :
				continue

			value_contents = []
			for valu in value :
				val = valu.content.lower()
				val_comma = val.split(', ')
				for val1 in val_comma :
					val_slash = val1.split('/ ')
					for val2 in val_slash :
						value_contents.append(val2)

			for val in value_contents :
				words = val.split(' ')
				valu = val
				valu = valu.replace(' ',"")
				# values.append(valu)
				if (attr == "model id" or attr == "modelid") :
					features.append(val)

				i = 0

				while i < len(words) :
					costing = words[i].replace(',',"")
					if ("review" in attr or "img" in attr) : 
						break
					if (costing != "infinity" and (isFloat(costing) == True or costing.isdigit() == True) and (i + 1) < len(words)) :
						attr = attr.replace(' ',"")
						if attr == "modelid" :
							bigrams.append(attr + ":" + str(costing))
							i += 1		
						elif (words[i + 1] in units) :
							if (costing.isdigit() == True)  :
								num = int(costing)
								bigrams.append(words[i + 1] + ":" + str(round_to_n(num,2)))
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
							else :
								num = float(costing)
								bigrams.append(words[i + 1] + ":" + str(round_to_n(num,2)))
								bigrams.append(attr + ":" + str(round_to_n(num,2)))		
							i += 2
						elif words[i + 1] == "x" and (i + 2) < len(words) : 
							if (words[i + 2].isdigit() == True) :
								num1 = int(costing)
								num2 = int(words[i + 2])
								bigrams.append(attr + ":" + str(round_to_n(num1,2)) + "x" + str(round_to_n(num2,2)))
								i += 3
							elif (isFloat(words[i + 2]) == True) :
								num1 = float(costing)
								num2 = float(words[i + 2])
								bigrams.append(attr + ":" + str(round_to_n(num1,2)) + "x" + str(round_to_n(num2,2)))
								i += 3
							else :
								if (costing.isdigit() == True) :
									num = int(costing)
									bigrams.append(attr + ":" + str(round_to_n(num,2)))
									bigrams.append("x:" + str(round_to_n(num,2)))
									i += 2
								elif isFloat(costing) == True :
									num = float(costing)
									bigrams.append(attr + ":" + str(round_to_n(num,2)))
									bigrams.append("x:" + str(round_to_n(num,2)))	
									i += 2
						elif words[i + 1] == "x" :
							if (costing.isdigit() == True) :
								num = int(costing)
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
								bigrams.append("x:" + str(round_to_n(num,2)))
								i += 2
							elif isFloat(costing) == True :
								num = float(costing)
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
								bigrams.append("x:" + str(round_to_n(num,2)))	
								i += 2			
						elif words[i + 1] == ":" and (i + 2) < len(words) : 
							if (words[i + 2].isdigit() == True) :
								num1 = int(costing)
								num2 = int(words[i + 2])
								bigrams.append(attr + ":" + str(round_to_n(num1,2)) + ":" + str(round_to_n(num2,2)))
								i += 3
							elif (isFloat(words[i + 2]) == True) :
								num1 = float(costing)
								num2 = float(words[i + 2])
								bigrams.append(attr + ":" + str(round_to_n(num1,2)) + ":" + str(round_to_n(num2,2)))
								i += 3
							else :
								if (costing.isdigit() == True) :
									num = int(costing)
									bigrams.append(attr + ":" + str(round_to_n(num,2)))
									i += 2
								elif isFloat(costing) == True :
									num = int(costing)
									bigrams.append(attr + ":" + str(round_to_n(num,2)))	
									i += 2			
						else :
							if (costing.isdigit() == True)  :
								num = int(costing)
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
								i += 1
							else :
								num = float(costing)
								# print costing,words[i],val,attr
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
								i += 1			
					elif (costing != "infinity" and costing.isdigit() == True) :
						attr = attr.replace(' ',"")
						num = int(costing)
						bigrams.append(attr + ":" + str(round_to_n(num,2)))
						i += 1
					elif (costing != "infinity" and isFloat(costing) == True) :
						attr = attr.replace(' ',"")
						num = float(costing)
						bigrams.append(attr + ":" + str(round_to_n(num,2)))
						i += 1
					elif (words[i] == "rs" and (i + 1) < len(words)) :
						attr = attr.replace(' ',"")
						cost = words[i + 1].replace(',',"")
						if (cost.isdigit() == True)  :
							num = int(cost)
							bigrams.append("rs" + ":" + str(round_to_n(num,2)))
							bigrams.append(attr + ":" + str(round_to_n(num,2)))
						elif (isFloat(cost) == True) :
							num = float(cost)
							bigrams.append("rs" + ":" + str(round_to_n(num,2)))
							bigrams.append(attr + ":" + str(round_to_n(num,2)))
						elif (cost[-1] == 'k') :
							changed_cost = cost[0:-1]
							print changed_cost
							if (changed_cost.isdigit() == True)  :
								num = int(changed_cost) * 1000
								bigrams.append("rs" + ":" + str(round_to_n(num,2)))
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
							elif (isFloat(changed_cost) == True) :
								num = float(changed_cost) * 1000
								bigrams.append("rs" + ":" + str(round_to_n(num,2)))
								bigrams.append(attr + ":" + str(round_to_n(num,2)))
							
						i += 2		
					else :
						if words[i] == "yes" :
							attr_values = re.findall(r"\w+",attr)
							for attr_value in attr_values :
								vals_attr = attr_value.split(' ')
								for val_attr in vals_attr : 
									features.append(val_attr)
						else : 	
							attr = attr.replace(' ',"")
							word_values = re.findall(r"\w+",words[i])
							# print words[i],word_values
							for word_value in word_values :
								bigrams.append(attr + ":" + word_value)
								values.append(word_value)
						i += 1

		bigram_set = ""
		for bigram in bigrams :
			bigram = bigram.replace('\n',"")
			bigram_set += (bigram + " ")
		
		feature_set = ""
		for feature in features :
			feature = feature.replace('\n',"")
			feature_set += (feature + " ")
		
		value_set = ""
		for value in values :
			value = value.replace('\n',"")
			value_set += (value + " ")

		fo1 = open('Documents/Document_' + str(t + 1) + '.txt', "wb")
		fo1.write(feature_set + "\n" + bigram_set + "\n" + value_set)
		fo1.close()		


if __name__ == "__main__" :
	main()