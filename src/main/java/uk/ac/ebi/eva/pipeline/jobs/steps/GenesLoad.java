/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.pipeline.jobs.steps;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uk.ac.ebi.eva.pipeline.configuration.VariantJobsArgs;
import uk.ac.ebi.eva.pipeline.io.readers.GeneReader;
import uk.ac.ebi.eva.pipeline.io.writers.GeneWriter;
import uk.ac.ebi.eva.pipeline.jobs.steps.processors.GeneFilterProcessor;
import uk.ac.ebi.eva.pipeline.listeners.SkipCheckingListener;
import uk.ac.ebi.eva.pipeline.model.FeatureCoordinates;

import java.io.IOException;

/**
 * Created by jmmut on 2016-08-16.
 *
 * @author Jose Miguel Mut Lopez &lt;jmmut@ebi.ac.uk&gt;
 *
 * Step class that:
 * - READ: read a list of {@link FeatureCoordinates} from flat file
 * - LOAD: write the {@link FeatureCoordinates} into Mongo db
 *
 */

@Configuration
@EnableBatchProcessing
@Import(VariantJobsArgs.class)
public class GenesLoad {

    public static final String LOAD_FEATURES = "Load features";
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private VariantJobsArgs variantJobsArgs;

    @Bean
    @Qualifier("genesLoadStep")
    public Step genesLoadStep() throws IOException {
        return stepBuilderFactory.get(LOAD_FEATURES).<FeatureCoordinates, FeatureCoordinates>chunk(10)
                .reader(new GeneReader(variantJobsArgs.getPipelineOptions().getString("input.gtf")))
                .processor(new GeneFilterProcessor())
                .writer(new GeneWriter(variantJobsArgs.getMongoOperations(), variantJobsArgs.getDbCollectionsFeaturesName()))
                .faultTolerant().skipLimit(50).skip(FlatFileParseException.class)
                .listener(new SkipCheckingListener())
                .build();
    }

}