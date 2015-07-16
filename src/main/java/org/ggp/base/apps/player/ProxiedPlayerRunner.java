package org.ggp.base.apps.player;

import java.io.IOException;

import org.apache.logging.log4j.ThreadContext;
import org.ggp.base.player.gamer.Gamer;
import org.ggp.base.player.gamer.statemachine.random.RandomGamer;
import org.ggp.base.player.proxy.ProxyGamePlayer;

/**
 * ATTENTION! DO NOT RUN THIS MAIN METHOD BECAUSE WHENEVER YOU KILL THE PROCESS THE CHILD PROCESS
 * PROXYGAMEPLAYERCLIENT DOESN'T DIE AND KEEPS LOKKING FOREVER!
 *
 * @author C.Sironi
 *
 */
public final class ProxiedPlayerRunner
{
    public static void main(String[] args) throws IOException
    {

    	System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
    	System.setProperty("isThreadContextMapInheritable", "true");

    	ThreadContext.put("GENERAL", System.currentTimeMillis() + "ProxyPlayer");

        Class<? extends Gamer> toLaunch = RandomGamer.class;
        ProxyGamePlayer player = new ProxyGamePlayer(9147, toLaunch);
        player.start();
    }
}
