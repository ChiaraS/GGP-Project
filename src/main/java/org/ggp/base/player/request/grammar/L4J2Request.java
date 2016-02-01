package org.ggp.base.player.request.grammar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class L4J2Request
{

	/**
	 * Static reference to the logger
	 */
	protected static final Logger LOGGER;

	static{

		LOGGER = LogManager.getRootLogger();

	}


	public abstract String process(long receptionTime);

	public abstract String getMatchId();

	@Override
	public abstract String toString();

}
