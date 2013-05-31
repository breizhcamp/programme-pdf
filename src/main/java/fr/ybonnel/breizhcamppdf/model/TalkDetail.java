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

public class TalkDetail {
    private String id;
    private String title;
    private String description;
    private List<Speaker> speakers;
    private List<String> tags;
    private Talk talk;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Speaker> getSpeakers() {
        if (speakers == null) {
            speakers = new ArrayList<>();
        }
        return speakers;
    }

    public List<String> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }

    public Talk getTalk() {
        return talk;
    }
}
