package org.arcanum.common.io.disk;

/**
 * @author Angelo De Caro (arcanumlib@gmail.com)
 * @since 1.0.0
 */
public interface ArraySector<T> extends Sector {

    int getSize();

    T getAt(int index);

    void setAt(int index, T value);


    T getAt(String label);

    void setAt(String label, T value);

}
