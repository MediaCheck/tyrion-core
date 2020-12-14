package core.storage;

public class StoredItem implements StorageItem {

    public String name;
    public String path;
    public String link;

    public StoredItem() {}

    public StoredItem(String name, String path, String link) {
        this.name = name;
        this.path = path;
        this.link = link;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getLink() {
        return this.link;
    }
}
