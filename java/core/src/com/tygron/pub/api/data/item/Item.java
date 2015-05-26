package com.tygron.pub.api.data.item;

import java.util.HashMap;
import com.tygron.pub.utils.JsonUtils;
import com.tygron.pub.utils.StringUtils;

public abstract class Item {

	private static final HashMap<Class<? extends Item>, Class<? extends Item>> classes = new HashMap<Class<? extends Item>, Class<? extends Item>>();

	@SuppressWarnings("unchecked")
	public static final <T extends Item> Class<T> get(Class<T> itemClass) {
		return (Class<T>) classes.getOrDefault(itemClass, itemClass);
	}

	public static final <T extends Item> T mapJsonToItem(final String json, final Class<T> itemClass) {
		return JsonUtils.mapJsonToType(json, Item.get(itemClass));
	}

	public static final <T extends Item> void overwrite(Class<T> itemClass, Class<? extends T> newItemClass) {
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
