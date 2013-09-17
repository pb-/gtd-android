package pb.gtd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Random;

import android.os.Environment;
import android.util.Log;

public class Util {
	public final static String alpha = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static int decodeNum(String num, int len) {
		int n = 0;
		int m = 1;

		for (int i = 0; i < len; i++) {
			n += alpha.indexOf(num.charAt(i)) * m;
			m *= alpha.length();
		}

		return n;
	}

	public static String encodeNum(int num, int len) {
		char[] s = new char[len];

		for (int i = 0; i < len; i++) {
			s[i] = alpha.charAt(num % alpha.length());
			num /= alpha.length();
		}

		return new String(s);
	}

	public static int genNum(int len) {
		Random r = new Random();
		return r.nextInt((int) Math.pow(alpha.length(), len));
	}

	public static void writeCrashReport(RuntimeException re) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String now = new Date().toString();
			File f = null;

			f = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
					"GTD Crash " + now + ".txt");

			Log.d("sync", "crash file in " + f.getAbsolutePath());
			try {
				OutputStreamWriter osw = new OutputStreamWriter(
						new FileOutputStream(f));

				re.printStackTrace(new PrintWriter(osw));
				if (re.getCause() != null) {
					re.getCause().printStackTrace(new PrintWriter(osw));
				}

				osw.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		throw re;
	}
}
