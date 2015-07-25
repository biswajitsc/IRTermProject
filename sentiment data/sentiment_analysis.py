import libxml2
from senti_classifier import senti_classifier

def main() :
	
	fo = open("sentiment_data.txt","wb")
	
	prev_pos_scores = {}
	prev_neg_scores = {}
	for t in range(418) :
		doc = libxml2.parseFile("cams/cams_" + str(t + 1) + ".xml")
		ctxt = doc.xpathNewContext()
		res = ctxt.xpathEval("//specsKey[@name=\"reviewText\"]/text()")
		# print len(res),"//specsKey[@name=\"reviewText\"]/@name"
		
		pos_scores = 0
		neg_scores = 0
		num_reviews = 0
		for val in res :
			vals = val.content.split('.')
			sentences = []
			for valu in vals :
				sentences_temp = valu.split(',')
				for sentence in sentences_temp :
					sentences.append(sentence)

			tuple_sentences = tuple(sentences)
			if tuple_sentences in prev_pos_scores and tuple_sentences in prev_neg_scores :
				pos_score = prev_pos_scores[tuple_sentences]
				neg_score = prev_neg_scores[tuple_sentences]	
			else :	 
				try :	
					pos_score, neg_score = senti_classifier.polarity_scores(sentences)
				except :
					pos_score = neg_score = 0
			pos_scores += pos_score
			neg_scores += neg_score
			prev_pos_scores[tuple_sentences] = pos_score
			prev_neg_scores[tuple_sentences] = neg_score
			print tuple_sentences
			num_reviews += 1
					
		if num_reviews == 0 :
			fo.write("Document " + str(t + 1)  + " : 0 0\n")
			print "Document " + str(t + 1)  + " : 0 0\n"
		else :
			fo.write("Document " + str(t + 1) + " : " + str(pos_scores/num_reviews) + " " + str(neg_scores/num_reviews) + "\n")
			print "Document " + str(t + 1) + " : " + str(pos_scores/num_reviews) + " " + str(neg_scores/num_reviews) + "\n"
	
	fo.close()		 		

if __name__ == "__main__" :
	main()	