package pb.gtd.ui;

import pb.gtd.Item;

public class ItemListItem {
    public String title;
    public int num;

    public ItemListItem(int num, String title) {
        this.num = num;
        this.title = title;
    }

    public ItemListItem(Item it) {
        num = it.num;
        title = it.title;
    }

    public String extractLink() {
        int start = Math.max(title.indexOf("http://"),
                title.indexOf("https://"));
        if (start == -1) {
            return null;
        }

        int end = title.indexOf(" ", start);

        if (end == -1) {
            return title.substring(start);
        } else {
            return title.substring(start, end);
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
