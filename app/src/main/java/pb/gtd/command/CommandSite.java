package pb.gtd.command;

import java.util.HashMap;

public class CommandSite {
    HashMap<Character, CommandFactory> factoryMap = new HashMap<Character, CommandFactory>();

    public CommandSite() {
        CommandFactory[] factories = new CommandFactory[]{
                new SetTitleCommand.Factory(),
        };

        for (CommandFactory factory : factories) {
            factoryMap.put(factory.getMnemonic(), factory);
        }
    }

    public PlainCommand2 parse(String s) {
        return factoryMap.get(s.charAt(0)).create(s);
    }
}
