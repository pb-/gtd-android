package pb.gtd.command;

public interface Command {
    public PlainCommand getDecrypted(byte[] key) throws Exception;

    public EncryptedCommand getEncrypted(byte[] key) throws Exception;
}