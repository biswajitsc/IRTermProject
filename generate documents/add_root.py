import sys 
def main() :
	
	for i in range(419) :	
		fo = open('cam/cam_' + str(i + 1) + '.xml', "rb")
		content = fo.read()
		print 'cams/cam_' + str(i + 1) + '.xml','opened'
		fo.close()
		content = content.replace('&','&amp;')
		fo1 = open('cams/cams_' + str(i + 1) + '.xml', "wb")
		fo1.write(content)
		fo1.close()
		print 'cams/cams_' + str(i + 1) + '.xml','opened' 	
		i += 1

if __name__ == "__main__" :
	main()