package brova.toshiba.client.storages.implement;

import java.util.ArrayList;
import java.util.List;

public class StaffStorage {

    private final List<String> staffs = new ArrayList<>();

    public void add(String text) {
        if (!text.isEmpty()) {
            this.staffs.add(text);
        }
    }

    public void remove(String text) {
        this.staffs.remove(text);
    }

    public void clear() {
        this.staffs.clear();
    }

    public boolean isStaff(String text) {
        return this.staffs.contains(text);
    }

    public boolean isEmpty() {
        return this.staffs.isEmpty();
    }

    public List<String> getStaffs() {
        return this.staffs;
    }
}
