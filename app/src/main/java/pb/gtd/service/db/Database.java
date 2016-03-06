package pb.gtd.service.db;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import pb.gtd.Constants;
import pb.gtd.Util;
import pb.gtd.command.Command;
import pb.gtd.command.EncryptedCommand;
import pb.gtd.service.DataChunk;
import pb.gtd.service.GTDService;
import pb.gtd.service.Heads;

public class Database {

    private GTDService service;
    private Heads heads;
    private short name;

    public synchronized void insertData(DataChunk dc) {
        if (heads.map.containsKey(dc.origin) && heads.map.get(dc.origin) != dc.offset ||
                !heads.map.containsKey(dc.origin) && dc.offset != 0) {
            Log.w("db", "bad insert (offset mismatch");
            return;

        }
        try {
            RandomAccessFile raf = new RandomAccessFile(service.getFileStreamPath(
                    Util.encodeNum(dc.origin, Constants.ORIGIN_LEN) + ".cmd"), "rw");
            if (dc.offset > 0) {
                raf.seek(dc.offset);
            }
            raf.write(dc.data.getBytes("UTF-8"));
            raf.close();

            if (!heads.map.containsKey(dc.origin)) {
                heads.map.put(dc.origin, 0);
            }
            runCommands(heads);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized DataChunk selectData(short o, int from, int diff) {
        try {
            RandomAccessFile raf = new RandomAccessFile(service.getFileStreamPath(
                    Util.encodeNum(o, Constants.ORIGIN_LEN) + ".cmd"), "r");
            byte[] buffer = new byte[diff];
            raf.seek(from);
            raf.readFully(buffer);
            raf.close();
            return new DataChunk(o, from, new String(buffer));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized boolean insertCommand(Command c) {
        try {
            int offset;
            if (!heads.map.containsKey(name)) {
                offset = 0;
            } else {
                offset = heads.map.get(name);
            }
            String data = c.getEncrypted(getKey(), name, offset).toString();
            DataChunk dc = new DataChunk(name, offset, data);
            insertData(dc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Database(GTDService service) {
        this.service = service;
        loadHeads();
        loadName();

        runCommands(heads);

        Log.i("db", "database ready, " + heads.map.size() + " heads");
    }

    static public byte[] hashPassphrase(String passphrase) {
        byte[] salt = {-8, -103, -118, -116, 42, 58, -108, 8, 97, -125, 10, 77, -85, 98, -2, 70,};

        try {
            MessageDigest digester = MessageDigest.getInstance("SHA256");
            digester.update(salt);
            digester.update(passphrase.getBytes("UTF-8"));
            return digester.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Log.e("db", "no sha256");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("db", "no utf-8");
        }

        return new byte[0];
    }

    private byte[] getKey() {
        return hashPassphrase(service.getPassphrase());
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

    private void runCommands(Heads start) {
        class FileContext implements Comparable<FileContext> {
            public long time;
            short origin;
            long offset;
            public String line;
            RandomAccessFile file;

            public FileContext(RandomAccessFile raf, short origin) throws IOException {
                this.origin = origin;
                this.offset = raf.getFilePointer();
                line = raf.readLine();
                if (line == null) {
                    raf.close();
                    throw new IOException("EOF");
                }
                file = raf;
                byte[] iv = Base64.decode(line.substring(0, 10) + "A=", Base64.NO_WRAP);
                time = Util.unpackIVTimestamp(iv);
            }

            @Override
            public int compareTo(FileContext that) {
                return Long.compare(this.time, that.time);
            }
        }

        try {
            ArrayList<FileContext> lines = new ArrayList<FileContext>();
            for (Map.Entry<Short, Integer> entry : heads.map.entrySet()) {
                RandomAccessFile raf = new RandomAccessFile(
                        service.getFileStreamPath(
                                Util.encodeNum(entry.getKey(), Constants.ORIGIN_LEN) + ".cmd"),
                        "r");
                if (entry.getValue() > 0) {
                    raf.seek(entry.getValue());
                }

                entry.setValue((int) raf.length());

                try {
                    lines.add(new FileContext(raf, entry.getKey()));
                } catch (IOException e) {
                    // all good
                }
            }

            while (lines.size() > 0) {
                Collections.sort(lines);
                FileContext fc = lines.remove(0);

                //Log.d("db", "read command: " + fc.line);
                try {
                    service.evaluateCommand(EncryptedCommand.parse(fc.origin, fc.offset, fc.line)
                                    .getDecrypted(getKey()),
                            false);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("db", "crypto problem");
                }

                try {
                    lines.add(0, new FileContext(fc.file, fc.origin));
                } catch (IOException e) {
                    // all good
                }
            }
            service.notifyObservers();
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
            if (f.getName().endsWith(".cmd")) {
                heads.map.put((short) Util.decodeNum(f.getName(),
                        Constants.ORIGIN_LEN), 0);
            }
        }
    }

    public synchronized boolean copyCommands(File to) {
        return false;
        /*File from = service.getFileStreamPath("commands");

        try {
            FileInputStream in = new FileInputStream(from);
            FileOutputStream out = new FileOutputStream(to);

            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;*/
    }
}
