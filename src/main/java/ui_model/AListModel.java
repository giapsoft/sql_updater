package ui_model;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AListModel<E> extends AbstractListModel<E> {
    List<E> list = new ArrayList<>();

    public void setValue(Collection<E> eList) {
        list  = new ArrayList<>(eList);
        fireContentsChanged(this, 0, list.size() - 1);
    }

    public void add(E e) {
        list.add(e);
    }

    public void remove(E e) {
        list.remove(e);
    }

    public void removeAt(int idx) {
        list.remove(idx);
    }

    public void clear() {
        list.clear();
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public E getElementAt(int index) {
        return list.get(index);
    }
}
