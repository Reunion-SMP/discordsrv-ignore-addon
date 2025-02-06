package com.github.jenbroek.discordsrv_ignore_addon.data;

public interface SimpleSet<E> {

	boolean add(E e);

	void remove(E e);

	boolean contains(E e);

}
