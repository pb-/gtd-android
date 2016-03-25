package pb.gtd.command;

import java.util.ArrayList;

import pb.gtd.Constants;
import pb.gtd.Item;
import pb.gtd.Tag;
import pb.gtd.Util;

public class SetTitleCommand extends PlainCommand2 {
    private static char mnemonic = 't';

    public static class Factory implements CommandFactory {

        @Override
        public char getMnemonic() {
            return mnemonic;
        }

        @Override
        public PlainCommand2 create(String s) {
            return new SetTitleCommand(s);
        }
    }

    public SetTitleCommand(String s) {
        super(s);
    }

    public SetTitleCommand(String[] arguments) {
        super(arguments);
    }

    @Override
    public char getMnemonic() {
        return mnemonic;
    }

    @Override
    public String[] getArgumentNames() {
        return new String[]{"itemId", "title"};
    }

    @Override
    public void apply(ArrayList<Tag> tags, ArrayList<Item> items) {
        /*int num = Util.decodeNum(getArgument("itemId"), Constants.NUM_LEN);
        int idx = findItemIndex(num, items);

        if (idx == -1) {
            items.add(new Item(num, getArgument("title")));
            findTag("inbox", tags).count++;
        } else {
            items.get(idx).title = getArgument("title");
        }*/
    }
}
