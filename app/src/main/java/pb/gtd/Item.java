package pb.gtd;

public class Item {
    public int num;
    public String title;
    public String tag;

    public Item(int num, String title) {
        this(num, title, null);
    }

    public Item(int num, String title, String tag) {
        this.num = num;
        this.title = title;
        this.tag = tag;
    }

    public boolean equals(Object o) {
        return num == ((Item) o).num;
    }
}
