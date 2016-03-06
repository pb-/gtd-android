package pb.gtd.command;

import android.util.Base64;

import java.security.SecureRandom;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pb.gtd.Constants;
import pb.gtd.Item;
import pb.gtd.Tag;
import pb.gtd.Util;

public class PlainCommand implements Command {
    public static final char OP_ITEM_SET_TITLE = 't';
    public static final char OP_ITEM_DELETE = 'd';
    public static final char OP_ITEM_SET_TAG = 'T';
    public static final char OP_ITEM_REMOVE_TAG = 'D';
    public static final char OP_TAG_REMOVE = 'r';
    public static final char OP_TAG_REORDER = 'o';

    public char op;
    public String argument;

    public PlainCommand(char op, String argument) {
        this.op = op;
        this.argument = argument;
    }

    public String toString() {
        return "" + op + ' ' + argument.replace('\n', ' ');
    }

    public static PlainCommand parse(String s) {
        int newline = s.indexOf('\n');
        if (newline == -1) {
            return new PlainCommand(s.charAt(0), s.substring(2));
        } else {
            return new PlainCommand(s.charAt(0), s.substring(2, newline));
        }
    }

    protected synchronized int findTagIndex(String tag, ArrayList<Tag> tagList) {
        for (int i = 0; i < tagList.size(); i++) {
            if (tagList.get(i).title.equals(tag)) {
                return i;
            }
        }

        return -1;
    }

    protected synchronized Tag findTag(String tag, ArrayList<Tag> tagList) {
        int i = findTagIndex(tag, tagList);

        if (i == -1) {
            return null;
        } else {
            return tagList.get(i);
        }
    }

    protected synchronized int findItemIndex(int num, ArrayList<Item> itemList) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).num == num) {
                return i;
            }
        }

        return -1;
    }

    protected synchronized Item findItem(int num, ArrayList<Item> itemList) {
        int i = findItemIndex(num, itemList);

        if (i == -1) {
            return null;
        } else {
            return itemList.get(i);
        }
    }

    public void apply(ArrayList<Tag> tagList, ArrayList<Item> itemList) {
        if (op == OP_TAG_REMOVE) {
            int idx = findTagIndex(argument, tagList);
            if (idx != -1) {
                tagList.remove(idx);
            }
        } else if (op == OP_TAG_REORDER) {
            int pos = argument.indexOf(' ');
            int anchorIdx = findTagIndex(argument.substring(0, pos), tagList);
            int targetIdx = findTagIndex(argument.substring(pos + 1), tagList);

            if (anchorIdx != -1 && targetIdx != -1) {
                Tag t = tagList.remove(targetIdx);
                if (targetIdx > anchorIdx) {
                    tagList.add(anchorIdx + 1, t);
                } else {
                    tagList.add(anchorIdx, t);
                }
            }
        } else {
            int num = Util.decodeNum(argument, Constants.NUM_LEN);
            int idx = findItemIndex(num, itemList);

            if (op == OP_ITEM_DELETE) {
                if (idx != -1) {
                    findTag(Tag.displayTag(itemList.get(idx).tag, "0000-00-00"),
                            tagList).count--;
                    itemList.remove(idx);
                }
            } else {

                if (op == OP_ITEM_SET_TITLE) {
                    String arg = argument.substring(Constants.NUM_LEN + 1);
                    if (idx == -1) {
                        itemList.add(new Item(num, arg));
                        findTag("inbox", tagList).count++;
                    } else {
                        itemList.get(idx).title = arg;
                    }
                } else if (idx != -1) {
                    if (op == OP_ITEM_REMOVE_TAG || op == OP_ITEM_SET_TAG) {
                        String newTag = op == OP_ITEM_REMOVE_TAG ? null
                                : argument.substring(Constants.NUM_LEN + 1);
                        Tag t = findTag(Tag.displayTag(itemList.get(idx).tag,
                                "0000-00-00"), tagList);
                        if (t != null) {
                            t.count--;
                        }

                        itemList.get(idx).tag = newTag;

                        t = findTag(Tag.displayTag(newTag, "0000-00-00"),
                                tagList);
                        if (t != null) {
                            t.count++;
                        } else {
                            tagList.add(new Tag(newTag, 1));
                        }
                    }
                }
            }
        }

    }

    @Override
    public PlainCommand getDecrypted(byte[] key) {
        return this;
    }

    @Override
    public EncryptedCommand getEncrypted(byte[] key, short origin, long offset) throws Exception {
        byte[] plain = this.toString().getBytes("UTF-8");

        long timestamp = System.currentTimeMillis();
        SecureRandom sr = new SecureRandom();
        byte[] random = new byte[3];
        sr.nextBytes(random);

        byte[] iv = Util.packIV(timestamp, random);
        String aad = Util.encodeNum(origin, Constants.ORIGIN_LEN) + ' ' + offset;

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec ivspec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

        cipher.updateAAD(aad.getBytes());
        byte[] output = cipher.doFinal(plain);

        String ivString = Base64.encodeToString(iv, Base64.NO_WRAP | Base64.NO_PADDING).substring
                (0, 10);
        String tag = Base64.encodeToString(output, plain.length, 16, Base64.NO_WRAP | Base64
                .NO_PADDING);
        String payload = Base64.encodeToString(output, 0, plain.length, Base64.NO_WRAP | Base64
                .NO_PADDING);

        return new EncryptedCommand(origin, offset, ivString + ' ' + tag + ' ' + payload + '\n');
    }

    @Override
    public boolean equals(Object o) {
        PlainCommand cmd = (PlainCommand) o;

        return this.op == cmd.op && this.argument.equals(cmd.argument);
    }
}
