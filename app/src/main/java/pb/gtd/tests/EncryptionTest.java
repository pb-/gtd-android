package pb.gtd.tests;

import android.test.InstrumentationTestCase;

import javax.crypto.BadPaddingException;

import pb.gtd.command.Command;
import pb.gtd.command.EncryptedCommand;
import pb.gtd.command.PlainCommand;

public class EncryptionTest extends InstrumentationTestCase {
    public void testEncDec() {
        PlainCommand cmd = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello");
        try {
            PlainCommand cmd2 = cmd.getEncrypted(new byte[32]).getDecrypted(new byte[32]);
            assertEquals(cmd, cmd2);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDecStatic() {
        try {
            Command cmd = new EncryptedCommand(
                    "VSrjJStdUE cwtEkiA02vYQ0ozBas+gXA==3L9fRIhCSCNXi8PnncBuMeI=");
            assertEquals(cmd.getDecrypted(new byte[32]).toString(), "t 2Bg hello world");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInvalidTag() {
        try {
            EncryptedCommand cmd = new PlainCommand(PlainCommand.OP_ITEM_SET_TITLE, "hello").
                    getEncrypted(new byte[32]);
            String data = cmd.toString().substring(0, 10) + " AAAAAAAAAAAAAAAAAAAAAA==" +
                    cmd.toString().substring(35);
            PlainCommand cmd2 = new EncryptedCommand(data).getDecrypted(new byte[32]);
            fail();
        } catch (BadPaddingException e) {
            // success
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
            PlainCommand cmd2 = cmd.getEncrypted(new byte[32]).getDecrypted(badKey);
            fail();
        } catch (BadPaddingException e) {
            // success
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
