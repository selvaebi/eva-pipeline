/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.t2d.entity;

import uk.ac.ebi.eva.t2d.model.T2DTableStructure;
import uk.ac.ebi.eva.t2d.model.T2dColumnDefinition;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Entity
@Table(name = "SAMPLES_PROP")
public class SampleProperty implements EntityWithId<String> {

    private static final Map<Class<?>, String> translations;

    static {
        Map<Class<?>, String> temp = new HashMap<>();
        temp.put(Boolean.class, "BOOLEAN");
        temp.put(Integer.class, "INTEGER");
        temp.put(Float.class, "FLOAT");
        temp.put(Double.class, "DOUBLE");
        temp.put(String.class, "STRING");
        translations = Collections.unmodifiableMap(temp);
    }

    private static final String FALSE = "FALSE";
    private static final String STRING = "STRING";
    @Id
    @Column(name = "PROP")
    private String id;

    @Column(name = "COM_DS")
    private String comDs;

    @Column(name = "COM_PH")
    private String comPh;

    @Column(name = "DB_COL")
    private String columnName;

    @Column(name = "PROP_TYPE")
    private String type;

    @Column(name = "SEARCHABLE")
    private String searchable;

    @Column(name = "DISPLAYABLE")
    private String displayable;

    @Column(name = "SORT")
    private Double sort;

    @Column(name = "MEANING")
    private String meaning;

    SampleProperty() {
    }

    public SampleProperty(String property) {
        this(property, STRING);
    }

    public SampleProperty(String property, String type) {
        id = property;
        comDs = FALSE;
        comPh = FALSE;
        columnName = property;
        this.type = type;
        searchable = FALSE;
        displayable = FALSE;
        meaning = property;
    }

    @Override
    public String getId() {
        return id;
    }

    public static List<SampleProperty> generate(T2DTableStructure structure) {
        return structure.getOrderedColumnIdAndDefinition().stream()
                .map(SampleProperty::generate)
                .collect(Collectors.toList());
    }

    private static SampleProperty generate(Map.Entry<String, T2dColumnDefinition> entry) {
        return new SampleProperty(entry.getKey(), translations.get(entry.getValue().getType()));
    }

    public String getType() {
        return type;
    }
}