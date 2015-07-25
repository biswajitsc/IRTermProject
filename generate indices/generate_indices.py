def main() :

	indices = set()

	for i in range(200) :
		fo = open('Documents/Document_' + str(i + 1) + '.txt','r')
		content = fo.read().strip()
		fo.close()

		content = content.replace('\n',' ')
		contents = content.split(' ')

		for word in contents :
			indices.add(word)

	fout = open('indices.txt','wb')
	for word in indices : 
		if (word != '\n') :
			fout.write(word + '\n')

	fout.close()
	
if __name__ == "__main__" :
	main()