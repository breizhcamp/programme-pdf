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
package fr.ybonnel.breizhcamppdf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ybonnel.breizhcamppdf.model.Talk;
import fr.ybonnel.breizhcamppdf.model.TalkDetail;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public enum TalkService {
    INSTANCE;

    private Map<String, TalkDetail> talks = new HashMap<>();

    private Gson gson = new GsonBuilder().create();

    public TalkDetail getTalkDetail(Talk talk) {
        if (!talks.containsKey(talk.getId())) {
            try {
                System.out.println("Getting talk detail " + talk.getId());
                URL url = new URL("http://cfp.breizhcamp.org/accepted/talk/" + talk.getId());
                URLConnection connection = url.openConnection();
                TalkDetail detail = gson.fromJson(new InputStreamReader(connection.getInputStream()), TalkDetail.class);
                detail.setTalk(talk);
                talks.put(talk.getId(), detail);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return talks.get(talk.getId());
    }

}
