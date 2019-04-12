/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neotropic.spring.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.stereotype.Component;

/**
 * The class that contains the tasks to be executed. The tasks start automatically. 
 * By default, the tasks use the standard thread pool of size 1, but you can make your 
 * own thread pool with more capacity using the interface {@link SchedulingConfigurer}. 
 * This is outside the scope of this prototype.
 * @author Charles Edward Bedon Cortazar {{@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class ScheduledTasks {
    /**
     * Reference to the default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTasks.class);
    private static int counter = 0;
    
    /**
     * This task runs every 5 seconds and starts its execution 10 seconds after the scheduler is initialized. 
     */
    @Scheduled(fixedRate = 5000, initialDelay = 10000)
    public void executeFixedRateScheduledTask() {
        LOGGER.info("I am a fixed-rate-scheduled that runs every five seconds");
    }
    
    /**
     * This task runs 5 seconds <b>after</b> the last execution.
     */
    @Scheduled(fixedDelay = 5000)
    public void executeFixedDelayScheduledTask() {
        LOGGER.info("I am a fixed-delay-scheduled that runs every five seconds");
    }
    
    /**
     * This task every Wednesday at 8.30 am. The syntax in the cron attribute follows 
     * a special format of <a href="https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html">cron jobs</a>. 
     * See the link for details about the specific syntax (basically it is Seconds/Minutes/Hour/Day/Month/Weekday).
     */
    @Scheduled(cron = "0 30 8 * * 3")
    public void executeCronScheduledTask() {
        LOGGER.info("I am a cron-scheduled that runs every Wednesday at 8.30h");
    }
}