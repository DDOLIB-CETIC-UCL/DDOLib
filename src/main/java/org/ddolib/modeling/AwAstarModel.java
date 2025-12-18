package org.ddolib.modeling;

public interface AwAstarModel<T> extends Model<T> {

    default double weight() {
        return 5.0;
    }

}
