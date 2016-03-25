package pb.gtd.command;

import java.util.ArrayList;
import java.util.Arrays;

import pb.gtd.Item;
import pb.gtd.Tag;

public abstract class PlainCommand2 {
    private String[] arguments;

    public abstract char getMnemonic();

    public abstract String[] getArgumentNames();

    public abstract void apply(ArrayList<Tag> tags, ArrayList<Item> items);

    public PlainCommand2(String[] arguments) {
        if (arguments.length != getArgumentNames().length) {
            throw new IllegalArgumentException("Not enough arguments");
        }

        this.arguments = arguments;
    }

    protected PlainCommand2(String s) {
        int argumentCount = getArgumentNames().length;
        String[] parts = s.split(" ", argumentCount + 1);

        if (parts.length < 1 || parts[0].length() != 1) {
            throw new IllegalArgumentException("Invalid command");
        }

        if (parts[0].charAt(0) != getMnemonic()) {
            throw new IllegalArgumentException("Wrong command mnemonic for this command");
        }

        if (parts.length < argumentCount + 1) {
            throw new IllegalArgumentException("Not enough arguments to command");
        }

        arguments = Arrays.copyOfRange(parts, 1, parts.length);
    }

    protected String getArgument(String name) {
        String[] names = getArgumentNames();

        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return arguments[i];
            }
        }

        throw new IllegalArgumentException("No such argument");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getMnemonic());
        for (String argument : arguments) {
            builder.append(' ');
            builder.append(argument);
        }

        return builder.toString();
    }
}
