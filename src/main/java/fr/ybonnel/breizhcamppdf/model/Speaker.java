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

public class Speaker {
    private String id;
    private String avatar;
    private String description;
    private String fullname;
    private List<Lien> liens;

    public String getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getDescription() {
        return description;
    }

    public String getFullname() {
        return fullname;
    }

    public List<Lien> getLiens() {
        if (liens == null) {
            liens = new ArrayList<>();
        }
        return liens;
    }
}
