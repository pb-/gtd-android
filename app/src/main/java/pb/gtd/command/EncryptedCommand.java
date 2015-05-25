package pb.gtd.command;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedCommand implements Command {
    private String data;

    public EncryptedCommand(String data) {
        this.data = data;
    }

    public static EncryptedCommand parse(String s) {
        int newline = s.indexOf('\n');
        if (newline == -1) {
            return new EncryptedCommand(s);
        } else {
            return new EncryptedCommand(s.substring(0, newline));
        }
    }

    public String toString() {
        return data;
    }

    @Override
    public PlainCommand getDecrypted(byte[] key) throws Exception {
        byte[] iv = Base64.decode(data.substring(0, 10) + "A=", Base64.NO_WRAP);
        byte[] tag = Base64.decode(data.substring(11, 35), Base64.NO_WRAP);
        byte[] payload = Base64.decode(data.substring(35), Base64.NO_WRAP);

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec ivspec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

        byte[] ciphertextWithTag = new byte[payload.length + tag.length];
        System.arraycopy(payload, 0, ciphertextWithTag, 0, payload.length);
        System.arraycopy(tag, 0, ciphertextWithTag, payload.length, tag.length);

        byte[] plain = cipher.doFinal(ciphertextWithTag);

        return PlainCommand.parse(new String(plain));

    }

    @Override
    public EncryptedCommand getEncrypted(byte[] key) throws Exception {
        return this;
    }
}
