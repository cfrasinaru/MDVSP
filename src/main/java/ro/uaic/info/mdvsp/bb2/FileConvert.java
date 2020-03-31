package ro.uaic.info.mdvsp.bb2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileConvert {

	public static String convert(String dataFile, String vtype, int n, int m, int inst) {
		inst = Integer.parseInt(dataFile.substring(dataFile.length() - 5, dataFile.length() - 4));
		// System.out.println("inst = " + inst);
		File file = new File("../data/" + dataFile);
		String lp_fileName = "lp_" + dataFile.substring(0, dataFile.length() - 3) + "lp";
		File lp_file = new File("../data/results/" + lp_fileName);
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String text = null;
		String[] nors = null;
		double[] r = new double[n + m];

		try {
			if (!lp_file.exists()) {
				lp_file.createNewFile();
			}
			FileWriter fw = new FileWriter(lp_file);
			writer = new BufferedWriter(fw);
			reader = new BufferedReader(new FileReader(file));
			if ((text = reader.readLine()) != null) {
				nors = text.split("\t", 0);
			}
			m = Integer.parseInt(nors[0]);// the number of depots
			n = Integer.parseInt(nors[1]);// the number of trips

			if (m != nors.length - 2) {
				return "-1";
			}
			System.out.println(m + " depots & " + n + " trips.");
			r = new double[n + m];
			for (int i = 0; i < m; i++) {
				r[i] = Double.parseDouble(nors[i + 2]);// the capacities of depots
			}
			for (int i = m; i < m + n; i++) {
				r[i] = 1;
			}
			double[][] c = new double[n + m][n + m];
			for (int i = 0; i < n + m; i++)
				for (int j = 0; j < n + m; j++)
					c[i][j] = -1.0;
			for (int i = 0; i < m; i++) {
				if ((text = reader.readLine()) != null) {
					nors = text.split("\t", 0);
					for (int j = 0; j < m; j++) {
						c[i][j] = -1;
					}
					c[i][i] = 0;
					for (int j = m; j < n + m; j++) {
						c[i][j] = Double.parseDouble(nors[j]);
					}
				}
			}
			for (int i = m; i < n + m; i++) {
				if ((text = reader.readLine()) != null) {
					nors = text.split("\t", 0);
					for (int j = 0; j < n + m; j++) {
						c[i][j] = Double.parseDouble(nors[j]);
					}
					c[i][i] = -1.0;
				}
			}
			writer.write("Minimize ");
			writer.newLine();
			writer.write(c[0][0] + " x0y0");
			for (int j = 1; j < n + m; j++) {
				if (c[0][j] >= 0.0)
					writer.write(" + " + c[0][j] + " x0y" + j);
			}
			for (int i = 1; i < n + m; i++) {
				for (int j = 0; j < n + m; j++) {
					if (c[i][j] >= 0.0)
						writer.write(" + " + c[i][j] + " x" + i + "y" + j);
				}
			}

			writer.newLine();
			writer.write("Subject To ");

			for (int i = 0; i < n + m; i++) {
				writer.newLine();
				writer.write("ca" + i + ": ");
				for (int j = 0; j < n + m; j++) {
					if (c[j][i] >= 0.0)
						writer.write(" + " + 1 + " x" + j + "y" + i);
				}
				writer.write(" = " + r[i]);
			}
			for (int i = 0; i < n + m; i++) {
				writer.newLine();
				writer.write("cb" + i + ": ");
				for (int j = 0; j < n + m; j++) {
					if (c[i][j] >= 0.0)
						writer.write(" + " + 1 + " x" + i + "y" + j);
				}
				writer.write(" = " + r[i]);
			}

			writer.newLine();
			writer.write("Bounds ");
			for (int i = 0; i < n + m; i++) {
				for (int j = 0; j < n + m; j++) {
					if (c[i][j] >= 0.0) {
						writer.newLine();
						writer.write("0.0 <= x" + i + "y" + j);
					}
				}
			}
			if (vtype == "Integers") {
				writer.newLine();
				writer.write("Integers ");

				for (int i = 0; i < n + m; i++) {
					for (int j = 0; j < n + m; j++) {
						if (c[i][j] >= 0.0) {
							writer.newLine();
							writer.write(" x" + i + "y" + j);
						}
					}
				}
			}
			writer.newLine();
			writer.write("End ");
			writer.newLine();
			System.out.println("End writing to file");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
                               System.err.println(e);
			}
			try {
				if (writer != null)
					writer.close();
			} catch (Exception ex) {
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}

		return lp_fileName;
	}
	
}
