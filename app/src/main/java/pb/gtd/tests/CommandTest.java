package pb.gtd.tests;

import android.test.InstrumentationTestCase;

import java.util.IllegalFormatCodePointException;

import pb.gtd.command.CommandSite;
import pb.gtd.command.PlainCommand2;
import pb.gtd.command.SetTitleCommand;

public class CommandTest extends InstrumentationTestCase {
    public void testBadConstruction() {
        try {
            new SetTitleCommand(new String[0]);
        } catch (IllegalArgumentException e) {

        }

        try {
            new SetTitleCommand(new String[1]);
        } catch (IllegalArgumentException e) {

        }
    }

    public void testSerialize() {
        SetTitleCommand c = new SetTitleCommand(new String[]{"abc", "here is a title"});
        assertEquals(c.toString(), "t abc here is a title");
    }

    public void testBadDeserialization() {
        try {
            new SetTitleCommand("");
        } catch (IllegalArgumentException e) {

        }

        try {
            new SetTitleCommand("q");
        } catch (IllegalArgumentException e) {

        }

        try {
            new SetTitleCommand("t");
        } catch (IllegalArgumentException e) {

        }

        try {
            new SetTitleCommand("t abc");
        } catch (IllegalArgumentException e) {

        }
    }

    public void testDeserialization() {
        SetTitleCommand c = new SetTitleCommand("t abc hello world!");
    }

    public void testSerializeDeserialize() {
        String s = "t abc hello world!";
        assertEquals(new SetTitleCommand(s).toString(), s);
    }

    public void testBasicSite() {
        String s = "t abc hello world!";
        CommandSite cs = new CommandSite();

        PlainCommand2 c = cs.parse(s);
        assertEquals(c.toString(), s);
    }
}
