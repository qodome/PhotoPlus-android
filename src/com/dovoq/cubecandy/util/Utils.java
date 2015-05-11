package com.dovoq.cubecandy.util;

import com.dovoq.cubecandy.Constants;

public class Utils implements Constants {

	public static String getEndpoint(String string) {
		return BASE_URL + "/" + API_VERSION + "/" + string + "/";
	}
}
