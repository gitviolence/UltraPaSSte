package brova.toshiba.client.storages;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ObjectStorage<T> {

    private final ObjectArrayList<T> object = new ObjectArrayList<>();

    public ObjectArrayList<T> getObject() {
        return this.object;
    }
}
