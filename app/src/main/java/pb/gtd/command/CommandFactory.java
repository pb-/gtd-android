package pb.gtd.command;

public interface CommandFactory {
    char getMnemonic();

    PlainCommand2 create(String s);
}
