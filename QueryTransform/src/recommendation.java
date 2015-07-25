import java.io.*;
import java.util.*;


public class recommendation
{
	static final int lastsize = 5;
	static final int maxresults = 20;
	static int values[][] = new int [lastsize][maxresults];
	static int vsize [] = new int [lastsize];

	static void loadResults() throws Exception
	{
		BufferedReader fin = new BufferedReader(new FileReader("PastResults.txt"));
		String line;
		String [] res;
		int ind = 0;
		while((line = fin.readLine()) != null)
		{
			vsize[ind] = 0;
			res = line.split(" +");
			for(int i=0; i<res.length; i++)
			if(!res[i].isEmpty())
			{
				values[ind][i] = Integer.parseInt(res[i]);
				vsize[ind] ++;
			}
			ind++;
		}

		fin.close();

		FileWriter fout = new FileWriter("PastResults.txt");
		for(int i=1; i<lastsize; i++)
		{
			for(int j=0; j<vsize[i]; j++) fout.write(values[i][j]+" ");
			fout.write("\n");
		}

		fout.close();
	}


	static final int numcluster = 10;
	static final int maxcameras = 200;
	static int clusters[][] = new int [numcluster][maxcameras];
	static int csize [] = new int [numcluster];

	static int clusternum[] = new int [maxcameras];

	static void loadClusters() throws Exception
	{
		BufferedReader fin = new BufferedReader(new FileReader("numcluster"+numcluster+".txt"));
		String line;
		String [] res;
		int ind = 0;
		while((line = fin.readLine()) != null)
		{
			csize[ind] = 0;
			res = line.split(" +");
			for(int i=0; i<res.length; i++)
			if(!res[i].isEmpty())
			{
				clusters[ind][i] = Integer.parseInt(res[i]);
				csize[ind] ++;
			}
			ind++;
		}

		fin.close();

		for(int i=0; i<numcluster; i++)
			for(int j=0; j<csize[i]; j++)
				clusternum[clusters[i][j]] = i;

	}

	static final double alpha = 0.8;
	static final double beta = 0.6;
	static double weights [] = new double [maxcameras];
	static int indices [] = new int [maxcameras];

	public static void main(String [] args) throws Exception
	{
		loadResults();
		loadClusters();

		for(int i=0; i<weights.length; i++) weights[i] = 0.0;
		double curr = 1.0;
		for(int i=lastsize-1; i>=0; i--)
		{
			double tcurr = curr;
			for(int j=0; j<vsize[i]; j++)
			{
				weights[values[i][j]] += tcurr;
				tcurr *= beta;
			}

			curr *= alpha;
		}

		for(int i=1; i<20; i++) System.out.print(weights[i]+" ");
		System.out.println();

		for(int i=0; i<maxcameras; i++) indices[i] = i;

		for(int i=1; i<maxcameras; i++)
			for(int j=maxcameras-1; j>i; j--)
				if(weights[indices[j]] > weights[indices[j-1]])
				{
					int temp = indices[j];
					indices[j] = indices[j-1];
					indices[j-1] = indices[j];
				}

		// for(int i=1; i<20; i++) System.out.print(indices[i]+" ");
		// System.out.println();

		int printind[] = new int[numcluster];
		for(int i=0; i<numcluster; i++) printind[i] = 0;

		for(int i=1; i<20; i++)
		{
			int cn = clusternum[indices[i]];
			if(printind[cn] < csize[cn])
			{
				System.out.println(clusters[cn][printind[cn]]);
				printind[cn] ++;
			}
		}

	}
}