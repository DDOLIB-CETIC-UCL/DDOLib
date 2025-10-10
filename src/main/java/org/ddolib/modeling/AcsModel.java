package org.ddolib.modeling;

public interface AcsModel<T> extends Model<T> {

    default int columnWidth() {
        return 5;
    }
}
