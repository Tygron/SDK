package com.tygron.pub.api.data.item;

import java.util.HashMap;
import com.tygron.pub.utils.StringUtils;

public abstract class Item {

	private final static HashMap<Class<? extends Item>, Class<? extends Item>> classes = new HashMap<Class<? extends Item>, Class<? extends Item>>();

	@SuppressWarnings("unchecked")
	public final static <T extends Item> Class<T> get(Class<T> itemClass) {
		return (Class<T>) classes.getOrDefault(itemClass, itemClass);
	}

	public final static <T extends Item> void overwrite(Class<T> itemClass, Class<? extends T> newItemClass) {
		if (newItemClass != null && itemClass != null) {
			if (itemClass.isAssignableFrom(newItemClass)) {
				classes.put(itemClass, newItemClass);
			}
		}
	}

	private int id = StringUtils.NOTHING;

	public int getID() {
		return id;
	}
}
