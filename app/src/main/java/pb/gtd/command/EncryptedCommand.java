package pb.gtd.command;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pb.gtd.Constants;
import pb.gtd.Util;

public class EncryptedCommand implements Command {
    private String data;
    private short origin;
    private long offset;

    public EncryptedCommand(short origin, long offset, String data) {
        this.data = data;
        this.origin = origin;
        this.offset = offset;
    }

    public static EncryptedCommand parse(short origin, long offset, String s) {
        return new EncryptedCommand(origin, offset, s);
    }

    public String toString() {
        return data;
    }

    @Override
    public PlainCommand getDecrypted(byte[] key) throws Exception {
        String[] parts = data.trim().split(" ");

        byte[] iv = Base64.decode(parts[0] + "A", Base64.NO_WRAP);
        byte[] tag = Base64.decode(parts[1], Base64.NO_WRAP);
        byte[] payload = Base64.decode(parts[2], Base64.NO_WRAP);

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec ivspec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

        byte[] ciphertextWithTag = new byte[payload.length + tag.length];
        System.arraycopy(payload, 0, ciphertextWithTag, 0, payload.length);
        System.arraycopy(tag, 0, ciphertextWithTag, payload.length, tag.length);

        String aad = Util.encodeNum(origin, Constants.ORIGIN_LEN) + ' ' + offset;
        cipher.updateAAD(aad.getBytes());
        byte[] plain = cipher.doFinal(ciphertextWithTag);

        return PlainCommand.parse(new String(plain));

    }

    @Override
    public EncryptedCommand getEncrypted(byte[] key, short origin, long offset) throws Exception {
        return this;
    }
}
