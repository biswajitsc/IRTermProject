#include <bits/stdc++.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <fstream>

using namespace std;

int main()
{
	FILE* fin;
	ifstream myfile("cams/out.txt");

	vector<string> filenames;
	string name;
	while(getline(myfile,name)) {
		filenames.push_back(name);
		cout << name << endl;
	}
	cout << filenames.size() << endl;
	for(int i = 0;i < filenames.size();i++) {

		string str = "";
		for(int j=0;j<filenames[i].length();j++){
			if(filenames[i][j] == '&' || filenames[i][j] == ' ' || filenames[i][j] == '(' || filenames[i][j] == ')')
				str = str+"\\"+filenames[i].substr(j,1);
			else
				str = str+filenames[i][j];
		}
		char buffer[1024];
		sprintf(buffer,"%d",i + 1);
		string strng = "mv cams/" + str + " cam/cam_" + string(buffer) + ".xml";
		cout << strng << endl;
			system(strng.c_str());
	}
	return 0;
}