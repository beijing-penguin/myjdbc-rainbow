package org.dc.jdbc.core.init;

import org.dc.jdbc.core.inter.InitHandler;

public class SQLInitAnalysis implements InitHandler{

	@Override
	public void init() throws Exception {
		System.out.println("SQLInitAnalysis.init()");
	}
}
