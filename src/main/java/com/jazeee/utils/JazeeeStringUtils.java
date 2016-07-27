package com.jazeee.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public abstract class JazeeeStringUtils {
	private JazeeeStringUtils() {
		throw new StatelessUtilityClassException();
	}

	public static <E extends Enum<E>> String toCamelCase(E enumType) {
		assert (enumType != null);
		String camelCase;
		camelCase = StringUtils.join(StringUtils.split(enumType.name(), '_'), ' ');
		camelCase = WordUtils.capitalizeFully(camelCase);
		camelCase = camelCase.replace(" ", "");
		String camelCaseToReturn = "";
		if (camelCase.length() > 0) {
			camelCaseToReturn += Character.toLowerCase(camelCase.charAt(0));
			if (camelCase.length() > 1) {
				camelCaseToReturn += camelCase.substring(1);
			}
		}
		return camelCaseToReturn;
	}
}
