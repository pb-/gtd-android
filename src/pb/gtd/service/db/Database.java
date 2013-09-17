package pb.gtd.service.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import pb.gtd.AnnotatedCommand;
import pb.gtd.Command;
import pb.gtd.Constants;
import pb.gtd.Heads;
import pb.gtd.Util;
import pb.gtd.service.GTDService;
import android.content.Context;
import android.util.Log;

public class Database {

	private GTDService service;
	private Heads heads;
	private short name;

	public static class PushItem implements Comparable<PushItem> {
		short origin;
		int revision;
		int offset;
		int length;

		public PushItem(short origin, int revision, int offset, int length) {
			this.origin = origin;
			this.revision = revision;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public int compareTo(PushItem it) {
			return offset - it.offset;
		}
	}

	public Database(GTDService service) {
		this.service = service;
		loadHeads();
		loadName();

		runCommands();

		Log.i("db", "database ready, " + heads.map.size() + " heads");
	}

	private void loadName() {
		try {
			InputStreamReader r = new InputStreamReader(
					service.openFileInput("name"), "UTF-8");
			char[] s = new char[Constants.ORIGIN_LEN];
			r.read(s);
			r.close();
			name = (short) Util.decodeNum(new String(s), Constants.ORIGIN_LEN);
			Log.d("db", "loaded name is " + new String(s));
		} catch (FileNotFoundException e) {
			name = (short) Util.genNum(Constants.ORIGIN_LEN);
			String s = Util.encodeNum(name, Constants.ORIGIN_LEN);
			Log.d("db", "generating new name: " + s);

			try {
				FileOutputStream fos = service.openFileOutput("name",
						Context.MODE_PRIVATE);
				fos.write(s.getBytes("UTF-8"));
				fos.close();
			} catch (FileNotFoundException ee) {
				ee.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runCommands() {
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					service.openFileInput("commands"), "UTF-8"));
			String line;
			while ((line = r.readLine()) != null) {
				// Log.d("db", "read command: " + line);
				service.evaluateCommand(Command.parse(line), false);
			}

			r.close();
			r = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("db", "problem reading commands");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e("db", "problem reading commands");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("db", "problem reading commands");
		}
	}

	public synchronized Heads getHeads() {
		return heads;
	}

	protected void loadHeads() {
		heads = new Heads();
		File[] files = service.getFilesDir().listFiles();

		for (File f : files) {
			if (f.getName().endsWith(".index")) {
				heads.map.put((short) Util.decodeNum(f.getName(),
						Constants.ORIGIN_LEN), (int) (f.length() / 16));
			}
		}
	}

	protected boolean checkCommands(ArrayList<AnnotatedCommand> cmds) {
		Heads h = new Heads();

		for (AnnotatedCommand ac : cmds) {
			if (h.map.containsKey(ac.origin)) {
				if (h.map.get(ac.origin) + 1 != ac.revision) {
					// don't accept non-sequential ordering per origin
					return false;
				}
			} else {
				if (heads.map.containsKey(ac.origin)) {
					if (ac.revision - heads.map.get(ac.origin) > 1) {
						// don't accept origins that start further ahead than
						// just after what we have locally
						return false;
					}
				} else if (ac.revision != 1) {
					// don't accept new origins that don't start with initial
					// revision
					return false;
				}
			}

			h.map.put(ac.origin, ac.revision);
		}

		h = null;

		return true;
	}

	protected synchronized int nextLocalRevision() {
		if (heads.map.containsKey(name)) {
			return heads.map.get(name) + 1;
		} else {
			return 1;
		}
	}

	public synchronized boolean insertCommand(AnnotatedCommand ac, boolean local) {
		ArrayList<AnnotatedCommand> cmds = new ArrayList<AnnotatedCommand>();
		cmds.add(ac);
		return insertCommands(cmds, local);
	}

	public synchronized boolean insertCommands(
			ArrayList<AnnotatedCommand> cmds, boolean local) {
		if (!local && !checkCommands(cmds)) {
			return false;
		}

		try {
			HashMap<Short, FileOutputStream> streams = new HashMap<Short, FileOutputStream>();
			File file = service.getFileStreamPath("commands");
			long offs = file.length();

			FileOutputStream fos = service.openFileOutput("commands",
					Context.MODE_APPEND);

			for (AnnotatedCommand ac : cmds) {
				if (local) {
					ac.origin = name;
					if (heads.map.containsKey(name)) {
						ac.revision = heads.map.get(name) + 1;
					} else {
						ac.revision = 1;
					}
				}

				if (!heads.map.containsKey(ac.origin)
						|| ac.revision > heads.map.get(ac.origin)) {

					// Log.d("db", "got new command");
					service.evaluateCommand(ac, false);

					byte[] data = ac.toStringCommand().getBytes("UTF-8");
					fos.write(data);
					fos.write('\n');

					if (!streams.containsKey(ac.origin)) {
						streams.put(ac.origin, service.openFileOutput(
								""
										+ Util.encodeNum(ac.origin,
												Constants.ORIGIN_LEN)
										+ ".index", Context.MODE_APPEND));
					}

					streams.get(ac.origin).write(
							String.format("%010d %05d", offs, data.length + 1)
									.getBytes("UTF-8"));

					offs += data.length + 1;
					heads.map.put(ac.origin, ac.revision);
				}
			}

			service.notifyObservers();

			fos.close();

			for (FileOutputStream s : streams.values()) {
				s.close();
			}

			file = null;
			streams = null;
			fos = null;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void retrieveRevisions(ArrayList<PushItem> pl, StringBuilder sb) {
		try {
			RandomAccessFile raf = null;
			PushItem it;
			byte[] bufOff = new byte[10];
			byte[] bufLen = new byte[5];

			for (int i = 0; i < pl.size(); i++) {
				it = pl.get(i);
				if (i == 0 || it.origin != pl.get(i - 1).origin) {
					if (raf != null) {
						raf.close();
					}
					raf = new RandomAccessFile(service.getFileStreamPath(Util
							.encodeNum(it.origin, Constants.ORIGIN_LEN)
							+ ".index"), "r");
				}

				raf.seek((it.revision - 1) * 16);
				raf.readFully(bufOff);
				raf.readByte();
				raf.readFully(bufLen);

				it.offset = Integer.parseInt(new String(bufOff));
				it.length = Integer.parseInt(new String(bufLen));
			}

			if (raf != null) {
				raf.close();
			}

			Collections.sort(pl);
			raf = new RandomAccessFile(service.getFileStreamPath("commands"),
					"r");

			for (PushItem item : pl) {
				sb.append(Util.encodeNum(item.origin, Constants.ORIGIN_LEN));
				sb.append(' ');
				sb.append(item.revision);
				sb.append(' ');
				byte[] cmd = new byte[item.length];
				raf.seek(item.offset);
				raf.readFully(cmd);
				sb.append(new String(cmd));
				sb.append('\n');
			}

			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
