package pb.gtd.command;

public interface Command {
    PlainCommand getDecrypted(byte[] key) throws Exception;

    EncryptedCommand getEncrypted(byte[] key, short origin, long offset) throws Exception;
}
