package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;

import java.util.Set;

public interface Aggregate<T> {


    public T compress(T t);


    public double getLB(T t, double value);


}
