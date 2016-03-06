package pb.gtd;

public class Tag {
    public String title;
    public int count;

    public Tag(String title, int count) {
        this.title = title;
        this.count = count;
    }

    public Tag(Tag t) {
        title = new String(t.title);
        count = t.count;
    }

    @Override
    public String toString() {
        if (count == 0) {
            return title;
        } else {
            return title + " (" + count + ")";
        }
    }

    public boolean equals(Object o) {
        return title.equals(((Tag) o).title);
    }

    public static String displayTag(String tag, String today) {
        if (tag == null) {
            return "inbox";
        } else if (tag.charAt(0) == '$') {
            if (tag.substring(1).compareTo(today) > 0) {
                return "tickler";
            } else {
                return "inbox";
            }
        } else {
            return tag;
        }
    }
}
