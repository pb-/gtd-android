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

	public Item(Item it) {
		this.num = it.num;
		this.title = new String(title);
		if (it.tag != null) {
			this.tag = new String(tag);
		}
	}

	public boolean equals(Object o) {
		return num == ((Item) o).num;
	}
}
