package pb.gtd.tests;

import android.test.InstrumentationTestCase;

import java.util.Arrays;

import javax.crypto.BadPaddingException;

import pb.gtd.Util;
import pb.gtd.command.Command;
import pb.gtd.command.EncryptedCommand;
import pb.gtd.command.PlainCommand;
import pb.gtd.service.db.Database;

public class EncryptionTest extends InstrumentationTestCase {
    static private String maulCiphertext(String ciphertext, int index) {
        char replacement = ciphertext.charAt(index) == 'x' ? 'y' : 'x';
        return ciphertext.substring(0, index) + replacement + ciphertext.substring(index + 1);
    }

    public void testEncDec() {
        PlainCommand cmd = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello");
        try {
            PlainCommand cmd2 = cmd.getEncrypted(new byte[32], (short) 391, 4019).getDecrypted
                    (new byte[32]);
            assertEquals(cmd, cmd2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDecStatic() {
        try {
            Command cmd = new EncryptedCommand((short) Util.decodeNum("Q4", 2), 4711,
                    "Vq6DP5s9Ve UDYPJAqw/DA4RiNr73kQXQ +blgDi40JgPr+FG4uvksQWo\n");
            assertEquals(cmd.getDecrypted(new byte[32]).toString(), "t QI0 hello world");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInvalidTag() {
        try {
            EncryptedCommand cmd = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello").
                    getEncrypted(new byte[32], (short) 124, 4891);
            String data = maulCiphertext(cmd.toString(), 14);
            new EncryptedCommand((short) 124, 4891, data).getDecrypted(new byte[32]);
            fail();
        } catch (BadPaddingException e) {
            // success
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInvalidAAD() {
        byte[] key = new byte[32];
        short origin = 100;
        long offset = 4711;

        PlainCommand plain = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello");
        try {
            String data = plain.getEncrypted(key, origin, offset).toString();

            try {
                new EncryptedCommand((short) (origin + 1), offset, data).getDecrypted(key);
            } catch (BadPaddingException e) {
                // success
            }

            try {
                new EncryptedCommand(origin, offset + 1, data).getDecrypted(key);
            } catch (BadPaddingException e) {
                // success
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInvalidKey() {
        byte[] badKey = new byte[32];
        badKey[0] = 1;

        PlainCommand cmd = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello");
        try {
            cmd.getEncrypted(new byte[32], (short) 3, 49).getDecrypted(badKey);
            fail();
        } catch (BadPaddingException e) {
            // success
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testHashPassphrase() {
        byte[] key = Database.hashPassphrase("passwörd");
        byte[] expectedKey = {-99, 60, -113, -114, -88, -68, -67, -37, -10, 79, -115, 58, 103,
                -86, -8, 16, 58, -42, -79, 75, -44, -104, 78, 111, -124, 34, -94, 4, 39, -74, 39,
                -117};
        assertTrue(Arrays.equals(key, expectedKey));
    }
}
