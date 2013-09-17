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
		title = new String(it.title);
	}

	@Override
	public String toString() {
		return title;
	}
}
