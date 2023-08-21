package model;

public enum ListenType {
    added, removed, changed, silence;

    public boolean isAdded() {
        return this == added;
    }

    public boolean isRemoved() {
        return this == removed;
    }

    public boolean isChanged() {
        return this == changed;
    }

    public boolean isSilence() {
        return this == silence;
    }

    public boolean isAddedOrRemoved() {
        return isAdded() || isRemoved();
    }
}
