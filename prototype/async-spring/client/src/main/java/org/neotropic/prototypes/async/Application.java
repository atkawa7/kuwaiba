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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main client application entry point
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@SpringBootApplication
public class Application {
    public static void main (String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
            return args -> {
                
                for (int i = 0; i < 5; i++) { // First we launch 5 jobs
                    LocalManagedJob localManagedJob = restTemplate.getForObject(
                                    "http://localhost:8080/async/launcher", LocalManagedJob.class);
                    System.out.println(localManagedJob == null ? "Job could not be created" : "Created job with id " + localManagedJob.getId());
                    Thread.sleep(5000);
                }
                
                System.out.println("Starting polling cycles");
                
                int pollingCycles = 0; // The number of polling cycles executed at a given moment. This can be used as timeout.
                boolean keepPolling = true;
                while (keepPolling) { // Poll every 5 seconds.
                    boolean allFinished = true;
                    // Now we check the progress and state of the jobs
                    // Using http://localhost:8080/async/manager is possible to track a particular job instead of all
                    LocalManagedJob[] allJobs = restTemplate.getForObject(
                                        "http://localhost:8080/async/manager", LocalManagedJob[].class);
                    for (LocalManagedJob aJob : allJobs) {
                        System.out.println(aJob.getId() + " -> " + aJob.getProgress() + "%");
                        allFinished = allFinished && (aJob.getState() != LocalManagedJob.STATE_RUNNING && 
                                aJob.getState() != LocalManagedJob.STATE_STOPPED); // Check if all jobs are finished or not.
                        Thread.sleep(3000);
                    }
                    pollingCycles++;
                    keepPolling = !(allFinished || (pollingCycles > 9)); // Poll until all jobs are finished or before 10 attempts. 
                }
            };
    }
}
