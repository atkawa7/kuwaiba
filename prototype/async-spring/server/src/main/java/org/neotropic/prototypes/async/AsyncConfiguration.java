/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.prototypes.async;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A simple configuration class.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    /**
     * Max number of threads.
     */
    @Value("${max_threads}")
    private int maxThreads;
    /**
     * Max number of CPU cores to be used (if available).
     */
    @Value("${max_cores}")
    private int maxCores;
    /**
     * Max size of the queue of tasks to be executed.
     */
    @Value("${queue_size}")
    private int queueSize;
    
    @Bean (name = "taskManager")
    public Executor taskExecutor() {
        System.out.println("New Task Manager Created");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxCores);
        executor.setMaxPoolSize(maxThreads);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("Thread-");
        executor.initialize();
        return executor;
    }
}
