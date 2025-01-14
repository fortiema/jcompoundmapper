package de.zbit.jcmapper.tools.progressbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Various utils, which I need quite often.
 * 
 * @author wrzodek
 */
public class Utils {

	/**
	 * 
	 * @param arr
	 * @param s
	 * @return
	 */
	public static boolean ArrayContains(String[][] arr, String s) {
		s = s.toLowerCase().trim();
		for (int i = 0; i < arr.length; i++)
			for (int j = 0; j < arr[i].length; j++)
				if (arr[i][j].toLowerCase().trim().equals(s))
					return true;
		return false;
	}

	/**
	 * Mittelwertberechnung. Versuchts erst schneller und nimmt sonst den
	 * langsameren, aber sicheren Algorithmus.
	 */
	public static double average(double[] d) {
		double average = average1(d);
		if (Double.isNaN(average) || Double.isInfinite(average))
			return average2(d);
		return average;
	}

	/**
	 * Spaltenweise mittelwertberechnung. Versuchts erst schneller und nimmt
	 * sonst den langsameren, aber sicheren Algorithmus.
	 */
	public static double[] average(double[][] d) {
		double[] average = average1(d);
		if (average == null)
			return null; // Koomt vor wenn er alle sequenzen nicht mappen kann
		for (int i = 0; i < average.length; i++)
			if (Double.isNaN(average[i]) || average[i] == Double.POSITIVE_INFINITY || average[i] == Double.NEGATIVE_INFINITY)
				return average2(d);
		return average;
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static double average1(double[] d) { // Schneller
		if (d == null || d.length < 1)
			return Double.NaN;
		double retVal = 0;

		int countNonNAN = 0;
		for (int i = 0; i < d.length; i++) {
			if (Double.isNaN(d[i]) || Double.isInfinite(d[i]))
				continue;
			countNonNAN++;
			retVal += d[i];
		}

		if (countNonNAN <= 0)
			return Double.NaN;
		return (retVal / countNonNAN);
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static double[] average1(double[][] d) { // Schneller
		if (d.length < 1)
			return new double[0];
		double[] retVal = null;

		int countNonNull = 0;
		for (int i = 0; i < d.length; i++) {
			if (d[i] == null)
				continue; // kommt vor wenn er sequenz i nicht mappen kann
			countNonNull++;
			if (retVal == null)
				retVal = new double[d[i].length];
			for (int j = 0; j < d[i].length; j++)
				retVal[j] += d[i][j];
		}

		if (retVal == null)
			return null; // Koomt vor wenn er alle sequenzen nicht mappen kann
		for (int i = 0; i < retVal.length; i++)
			retVal[i] /= countNonNull;

		return retVal;
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static double average2(double[] d) { // Keine to-large-numbers
		if (d.length < 1)
			return Double.NaN;
		double retVal = 0;

		int countNonNAN = 0;
		for (int i = 0; i < d.length; i++) {
			if (Double.isNaN(d[i]) || Double.isInfinite(d[i]))
				continue;
			countNonNAN++;

			// retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
			retVal = retVal * (countNonNAN - 1) / (countNonNAN) + d[i] * 1 / (countNonNAN);
		}

		// Wenn irgendwo nur NaNs waren, das auch so wiedergeben
		if (countNonNAN <= 0)
			return Double.NaN;
		return retVal;
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static double[] average2(double[][] d) { // Keine to-large-numbers
		if (d.length < 1)
			return new double[0];
		double[] retVal = null;
		ArrayList<Integer> spaltenCounter = new ArrayList<Integer>();
		for (int i = 0; i < d.length; i++) {
			if (d[i] == null)
				continue; // kommt vor wenn er sequenz i nicht mappen kann
			if (retVal == null)
				retVal = new double[d[i].length];
			for (int j = 0; j < d[i].length; j++) {
				if (spaltenCounter.size() <= j)
					spaltenCounter.add(0);
				if (Double.isNaN(d[i][j]))
					continue; // Deshalb auch der Spaltencounter: Skip NaN
								// eintr?ge.
				// retVal[j]=retVal[j] * i/(i+1) + d[i][j] * 1/(i+1);
				retVal[j] = retVal[j] * spaltenCounter.get(j) / (spaltenCounter.get(j) + 1) + d[i][j] * 1 / (spaltenCounter.get(j) + 1);
				spaltenCounter.set(j, spaltenCounter.get(j) + 1);
			}
		}
		// Wenn irgendwo nur NaNs waren, das auch so wiedergeben
		for (int i = 0; i < spaltenCounter.size(); i++)
			if (spaltenCounter.get(i) == 0)
				retVal[i] = Double.NaN;
		return retVal;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String complement(String s) {
		StringBuffer ret = new StringBuffer(s.length());
		char[] a = s.toLowerCase().toCharArray();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == 'a')
				ret.append('t');
			if (a[i] == 'c')
				ret.append('g');
			if (a[i] == 'g')
				ret.append('c');
			if (a[i] == 't')
				ret.append('a');
		}
		return ret.toString();
	}

	/**
	 * Empirical Correlation Coefficient computes the correlation coefficient
	 * between y (lables) and x (predictions)
	 * 
	 * @param y
	 * @param x
	 * @param mean_y
	 * @param mean_x
	 * @return
	 */
	public static double computeCorrelation(double[] y, double[] x, double mean_y, double mean_x) {
		double numerator = 0.0;
		for (int i = 0; i < y.length; i++) {
			numerator = numerator + (x[i] - mean_x) * (y[i] - mean_y);
		}
		numerator = numerator / y.length;

		double denominator_x = 0.0;
		double denominator_y = 0.0;
		for (int i = 0; i < y.length; i++) {
			denominator_x = denominator_x + Math.pow((x[i] - mean_x), 2);
		}
		for (int i = 0; i < y.length; i++) {
			denominator_y = denominator_y + Math.pow((y[i] - mean_y), 2);
		}

		denominator_x = Math.sqrt(denominator_x / y.length);
		denominator_y = Math.sqrt(denominator_y / y.length);

		return numerator / (denominator_x * denominator_y);
	}

	public static boolean containsWord(String containingLine, String containedString) {
		return isWord(containingLine, containedString);
	}

	/**
	 * Copies a file. Does NOT check if out already exists. Will overwrite out
	 * if it already exists.
	 * 
	 * @param in
	 * @param out
	 * @return success.
	 */
	public static boolean copyFile(File in, File out) {
		if (!in.exists()) {
			System.err.println("File '" + in.getName() + "' does not exist.");
			return false;
		}
		boolean success = false;
		try {
			FileChannel inChannel = new FileInputStream(in).getChannel();
			FileChannel outChannel = new FileOutputStream(out).getChannel();
			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel.transferTo(position, maxCount, outChannel);
			}
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			if (in.length() == out.length())
				success = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 
	 * @param sequence
	 * @param xMeres
	 * @return
	 */
	public static int[] countNucleotides(String sequence, int xMeres) {
		int counts[] = new int[(int) Math.pow(4, xMeres)];
		for (int i = 0; i < sequence.length() - xMeres + 1; i++)
			counts[DNA2Num(sequence.substring(i, i + xMeres))]++;
		return counts;
	}

	/**
	 * Cut at dot. E.g. 1.68 => 1 In contrary, decimal format "#" would return
	 * 2!
	 * 
	 * @param d
	 * @return
	 */
	public static String cut(double d) {
		String s = Double.toString(d);
		int ep = s.indexOf(".");
		if (ep < 1)
			ep = s.length();
		return s.substring(0, ep);
	}

	/**
	 * 
	 * @param arr1
	 * @param arr2
	 * @return
	 */
	public static double[][] divide(double[][] arr1, double[][] arr2) {
		double[][] ret = new double[arr1.length][];
		for (int i = 0; i < arr1.length; i++) {
			ret[i] = new double[arr1[i].length];
			for (int j = 0; j < arr1[i].length; j++) {
				if (arr2[i][j] == 0)
					ret[i][j] = Double.NaN;
				else
					ret[i][j] = arr1[i][j] / arr2[i][j];
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param arr1
	 * @param arr2
	 * @return
	 */
	public static double[][] divide(int[][] arr1, int[][] arr2) {
		double[][] ret = new double[arr1.length][];
		for (int i = 0; i < arr1.length; i++) {
			ret[i] = new double[arr1[i].length];
			for (int j = 0; j < arr1[i].length; j++) {
				if (arr2[i][j] == 0)
					ret[i][j] = 0;
				else
					ret[i][j] = (double) arr1[i][j] / arr2[i][j];
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param a
	 * @return
	 */
	public static int DNA2Num(char a) {
		if (a == 'A' || a == 'a')
			return 0;
		if (a == 'C' || a == 'c')
			return 1;
		if (a == 'G' || a == 'g')
			return 2;
		if (a == 'T' || a == 't')
			return 3;

		System.err.println("Unknwon DNA Character: '" + a + "'.");
		return -1;
	}

	/**
	 * Example: AA: 0 AC: 1 AG: 2 AT: 3 CA: 4 TA: 12 TT: 15
	 **/
	public static int DNA2Num(String a) {
		int ret = 0;
		char[] arr = reverse(a).toCharArray();
		for (int i = 0; i < arr.length; i++)
			ret += (DNA2Num(arr[i])) * Math.pow(4, (i));
		return ret;
	}

	/**
	 * Ensures that path ends with a slash (for folder processing).
	 * 
	 * @param path
	 */
	public static String ensureSlash(String path) {
		if (!path.endsWith("\\") && !path.endsWith("/"))
			if (path.contains("/"))
				path += "/";
			else if (path.contains("\\"))
				path += "\\";
			else
				path += "/";
		return path;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String firstUppercase(String s) {
		if (s == null)
			return null;
		s = s.trim().toLowerCase();
		if (s.length() == 0)
			return "";
		return Character.toString(s.charAt(0)).toUpperCase() + s.substring(1);
	}

	/**
	 * 
	 * @param startAtPos
	 * @param toParse
	 * @return
	 */
	public static int getNumberFromString(int startAtPos, String toParse) {
		int i = startAtPos;
		if (i <= 0)
			return -1; // Schlechte R?ckgabe... aber was sonst? Exception
						// throwen ist schei?e

		String ret = "";
		while (Character.isDigit(toParse.charAt(i)))
			ret += toParse.charAt(i++);

		return Integer.parseInt(ret);
	}

	/**
	 * Funzt nur f?r positive, nat?rliche Zahlen!
	 */
	public static int getNumberFromString(String behindLastIndexOfString, String toParse) {
		int i = toParse.lastIndexOf(behindLastIndexOfString) + 1;
		return getNumberFromString(i, toParse);
	}

	/**
	 * Given the miliseconds elapsed, returns a formatted time string up to a
	 * max deph of 3. e.g. "16h 4m 4s" or "2d 16h 4m" or "4s 126dms"
	 * 
	 * @param miliseconds
	 * @return
	 */
	public static String getTimeString(long miliseconds) {
		double seconds = (miliseconds / 1000.0);
		double minutes = (seconds / 60.0);
		double hours = (minutes / 60.0);
		double days = hours / 24;

		String ret;
		if (days >= 1) {
			ret = cut(days) + "d " + cut(hours % 24.0) + "h " + cut(minutes % 60) + "m";
		} else if (hours >= 1) {
			ret = cut(hours % 24.0) + "h " + cut(minutes % 60) + "m " + cut(seconds % 60) + "s";
		} else if (minutes >= 1) {
			ret = cut(minutes % 60) + "m " + cut(seconds % 60) + "s " + cut(miliseconds % 1000.0) + "ms";
		} else if (seconds >= 1) {
			ret = cut(seconds % 60) + "s " + cut(miliseconds % 1000.0) + "ms";
		} else {
			ret = cut(miliseconds % 1000.0) + "ms";
		}
		return ret;
	}

	/**
	 * Nicht ganz korrekt da auch 4.345,2.1 als nummer erkannt wird, aber das
	 * reicht mir so.
	 **/
	public static boolean isNumber(String s, boolean onlyDigits) {
		if (s.trim().length() == 0)
			return false;
		char[] a = s.trim().toCharArray();
		boolean atLeastOneDigit = false;
		for (int i = 0; i < a.length; i++) {
			if (!atLeastOneDigit && Character.isDigit(a[i]))
				atLeastOneDigit = true;

			if (onlyDigits) {
				if (Character.isDigit(a[i]))
					continue;
				else
					return false;
			} else {
				if (Character.isDigit(a[i]))
					continue;
				else if (i == 0 && a[i] == '-')
					continue;
				else if (a[i] == '.' || a[i] == ',')
					continue;
				else if (a[i] == 'E' || a[i] == 'e')
					continue;
				// if (a[i]=='-' || a[i]=='.' || a[i]==',' || a[i]=='E' ||
				// a[i]=='e') continue;
				return false;
			}
		}
		if (!atLeastOneDigit)
			return false; // Only "-" or "..." is no number.
		return true;
	}

	/**
	 * Kann auch als Synonym f?r "containsWord" gebraucht werden.
	 * 
	 * @param containingLine
	 * @param containedString
	 * @return
	 */
	public static boolean isWord(String containingLine, String containedString) {
		return isWord(containingLine, containedString, false);
	}

	/**
	 * 
	 * @param containingLine
	 * @param containedString
	 * @param ignoreDigits
	 * @return
	 */
	public static boolean isWord(String containingLine, String containedString, boolean ignoreDigits) {
		// Check if it's a word
		int pos = -1;
		while (true) {
			if (pos + 1 >= containedString.length())
				break;
			pos = containingLine.indexOf(containedString, pos + 1);
			if (pos < 0)
				break;

			boolean linksOK = true;
			if (pos > 0) {
				char l = containingLine.charAt(pos - 1);
				if ((Character.isDigit(l) && !ignoreDigits) || Character.isLetter(l))
					linksOK = false;
			}
			boolean rechtsOK = true;
			if (pos + containedString.length() < containingLine.length()) {
				char l = containingLine.charAt(pos + containedString.length());
				if ((Character.isDigit(l) && !ignoreDigits) || Character.isLetter(l))
					rechtsOK = false;
			}

			if (rechtsOK && linksOK)
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	public static Object loadObject(File file) {
		try {
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object ret = in.readObject();
			in.close();
			fileIn.close();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param inn
	 * @return
	 */
	public static Object loadObject(InputStream inn) {
		try {
			ObjectInputStream in = new ObjectInputStream(inn);
			Object ret = in.readObject();
			in.close();
			inn.close();
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	public static Object loadObject(String filename) {
		try {
			FileInputStream fileIn = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Object ret = in.readObject();
			in.close();
			fileIn.close();
			return ret;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param a
	 * @return
	 */
	public static char Num2DNA(int a) {
		if (a == 0)
			return 'A';
		if (a == 1)
			return 'C';
		if (a == 2)
			return 'G';
		if (a == 3)
			return 'T';

		System.err.println("To large input parameter on Num2DNA. Use xMeres variant of this function instead." + a);
		return 'N';
	}

	/**
	 * 
	 * @param n
	 * @param xMeres
	 * @return
	 */
	public static String Num2DNA(int n, int xMeres) {
		String ret = "";
		for (int i = xMeres - 1; i > 0; i--) {
			int k = n / (int) Math.pow(4, (i));
			ret += Num2DNA(k % 4);
		}
		int k = n % 4;
		ret += Num2DNA(k);
		return ret;
	}

	/**
	 * Calculates and returns the variance of the given list of double values
	 * 
	 * @param d
	 *            the list of double values
	 * @return
	 */
	public static double variance(double[] d) {
		double mean = average(d);
		return variance(d, mean);
	}

	/**
	 * Calculates and returns the variance of the given list of double values.
	 * This version of the method also takes the precalculated mean of the
	 * values as parameter to prevent redundant calculations.
	 * 
	 * @param d
	 *            the list of double values
	 * @param mean
	 *            the mean of the double values
	 * @return
	 */
	public static double variance(double[] d, double mean) {
		if (d.length <= 1) {
			return 0.0;
		}
		double sum = 0.0;
		for (int i = 0; i < d.length; i++) {
			sum += Math.pow(d[i] - mean, 2);
		}
		return sum / (d.length - 1);
	}

	/**
	 * Usefull for parsing command line arguments.
	 * 
	 * @param args
	 *            - Command line arguments.
	 * @param searchForCommand
	 *            - Command to search for
	 * @param hasArgument
	 *            - Has the command an argument? If yes, the Argument will be
	 *            returned.
	 * @return The Argument, if 'hasArgument' is set. Otherwise: "true" if
	 *         command is available. "false" if not.
	 * @throws Exception
	 *             "Missing argument.". If that's the case.
	 */
	public static String parseCommandLine(String[] args, String searchForCommand, boolean hasArgument) throws Exception {
		searchForCommand = searchForCommand.replace("-", "").trim();
		for (int i = 0; i < args.length; i++) {
			if (args[i].replace("-", "").trim().equalsIgnoreCase(searchForCommand)) {
				if (hasArgument) {
					if (i == (args.length - 1))
						throw new Exception("Missing argument.");
					else
						return args[i + 1];
				} else {
					return "true";
				}
			}
		}

		if (!hasArgument)
			return "false";
		return ""; // Don't return "false". May interfere with hasArgument
	}

	/**
	 * 
	 * @param arr
	 */
	public static void printMinMaxInfNaN(double[] arr) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int nan = 0;
		int inf = 0;
		for (double v : arr) {
			if (Double.isInfinite(v)) {
				inf++;
				continue;
			}
			if (Double.isNaN(v)) {
				nan++;
				continue;
			}
			if (v < min)
				min = v;
			if (v > max)
				max = v;
		}
		System.out.println("Min: " + min + "\t Max:" + max + "\t Infinity:" + inf + "\t NaN:" + nan);
	}

	/**
	 * 
	 * @param c
	 * @param times
	 * @return
	 */
	public static StringBuffer replicateCharacter(char c, int times) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < times; i++)
			s.append(c);
		return s;
	}

	/**
	 * 
	 * @param ch
	 * @param times
	 * @return
	 */
	public static String replicateCharacter(String ch, int times) {
		String retval = "";
		for (int i = 0; i < times; i++) {
			retval += ch;
		}
		return retval;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String reverse(String s) {
		StringBuffer a = new StringBuffer(s);
		return a.reverse().toString();
	}

	/**
	 * 
	 * @param zahl
	 * @param stellen
	 * @return
	 */
	public static double round(double zahl, int stellen) {
		double d = Math.pow(10, stellen);
		return Math.round(zahl * ((long) d)) / d;
	}

	/**
	 * 
	 * @param filename
	 * @param obj
	 * @return
	 */
	public static boolean saveObject(String filename, Object obj) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(obj);
			out.close();
			fileOut.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean shutdownSystem() {
		boolean isWindows = (System.getProperty("os.name").toLowerCase().contains("windows"));

		String sysDir = System.getProperty("os.home");
		if (sysDir == null)
			sysDir = "";
		sysDir = sysDir.trim();
		if (sysDir == null || sysDir.length() == 0 && isWindows)
			sysDir = System.getenv("WinDir").trim();
		if (sysDir.length() != 0 && !sysDir.endsWith("/") && !sysDir.endsWith("\\"))
			sysDir += File.separator;
		if (isWindows && sysDir.length() != 0)
			sysDir += "system32\\";

		Runtime run = Runtime.getRuntime();
		if (isWindows)
			sysDir += "shutdown.exe"; // -s -c "TimeLogger shutdown command."
										// (-f)
		else {
			// Unter linux mit "which" Rscript suchen
			try {
				Process pr = run.exec("which shutdown");
				pr.waitFor();
				pr.getOutputStream();

				// read the child process' output
				InputStreamReader r = new InputStreamReader(pr.getInputStream());
				BufferedReader in = new BufferedReader(r);
				String line = in.readLine(); // eine reicht
				if (!line.toLowerCase().contains("no shutdown") && !line.contains(":"))
					sysDir = line;
			} catch (Exception e) {
			}
			if (sysDir.length() != 0 && !sysDir.endsWith("/") && !sysDir.endsWith("\\"))
				sysDir += File.separator;
			sysDir += "shutdown";
		}

		boolean successValue = false;
		try {
			String dir = sysDir.substring(0, sysDir.lastIndexOf(File.separator));
			String command = sysDir.substring(sysDir.lastIndexOf(File.separator) + 1);

			String[] commandToRun;
			if (isWindows)
				commandToRun = new String[] { command, "-s", "-c", "\"TimeLogger shutdown command.\"" }; // -f
			else
				commandToRun = new String[] { command, "-h", "now" }; // Linux

			try { // Try to execute in input dir.
				// Process pr = run.exec(cmd, null, new File(inputFolder));
				Process pr = run.exec(commandToRun, null, new File(dir));
				pr.waitFor();
				successValue = (pr.exitValue() == 0);

			} catch (Exception e) {
				e.printStackTrace();
				try {
					run.exec(new String[] { sysDir }); // don't wait as it may
														// not terminate...
				} catch (Exception ex2) {
					run.exec(new String[] { "shutdown" }); // don't wait as it
															// may not
															// terminate...
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return successValue;
	}

}
