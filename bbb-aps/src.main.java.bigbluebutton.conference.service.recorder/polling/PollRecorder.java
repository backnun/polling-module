/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
* 
*/

package org.bigbluebutton.conference.service.recorder.polling;

import java.net.InetAddress;

import javax.servlet.ServletRequest;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.apache.commons.lang.time.DateFormatUtils;

import org.bigbluebutton.conference.service.poll.Poll;

public class PollRecorder {
        private static Logger log = Red5LoggerFactory.getLogger( PollRecorder.class, "bigbluebutton");

        JedisPool redisPool;

         public PollRecorder() {
        	 super();
                  log.debug("[TEST] initializing PollRecorder");

         }

        public JedisPool getRedisPool() {
        	 return redisPool;
        }

        public void setRedisPool(JedisPool pool) {
        	 this.redisPool = pool;
        }

        public void record(Poll poll) {
            log.debug("[TEST] inside pollRecorder record");
            // Reads IP from Java, for portability
            String serverIP = "INVALID IP";
            try
            {
            	InetAddress addr = InetAddress.getLocalHost();
                // Get hostname
                String hostname = addr.getHostName();
                serverIP = hostname;
            	log.debug("[TEST] IP capture successful, IP is " + serverIP);
            } catch (Exception e)
            {
            	log.debug("[TEST] IP capture failed...");
            }
            
            redisPool = new JedisPool(serverIP, 6379);
            Jedis jedis = redisPool.getResource();
			
            // Merges the poll title, room into a single string seperated by a hyphen
			String pollKey = poll.room + "-" + poll.title;
			log.debug("[TEST] Saving poll " + pollKey);
			
			// Saves all relevant information about the poll as fields in a hash; dynamically generates
			// enough fields for each answer and the number of votes for each answer
			jedis.hset(pollKey, "title", poll.title);
			jedis.hset(pollKey, "question", poll.question);
			jedis.hset(pollKey, "multiple", poll.isMultiple.toString());
			jedis.hset(pollKey, "room", poll.room);
			jedis.hset(pollKey, "time", poll.time); 			
			for (int i = 1; i <= poll.answers.size(); i++)
			{
				jedis.hset(pollKey, "answer"+i+"text", poll.answers.toArray()[i-1].toString());
				jedis.hset(pollKey, "answer"+i, "0");
			}

			log.debug("[TEST] Poll " + pollKey + " saved!");
			redisPool.returnResource(jedis);
			
			
			//PollInvoker invoker = new PollInvoker();
			/*/ Testing invoke() to make sure it logs properly (it does)
			invoker.invoke(pollKey);
			*/
			
			/*/ Testing pollList to see if it works how I think (it does)
			invoker.pollList();
			*/
        }
}