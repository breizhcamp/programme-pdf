/*
 * Copyright 2013- Yan Bonnel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ybonnel.breizhcamppdf.model;

import java.util.ArrayList;
import java.util.List;

public class Track {
    private String type;
    private List<Talk> talks;

    public String getType() {
        return type;
    }

    public List<Talk> getTalks() {
        if (talks == null) {
            talks = new ArrayList<>();
        }
        return talks;
    }
}
