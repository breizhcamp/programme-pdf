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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ybonnel.breizhcamppdf.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public enum DataService {
    INSTANCE;

    private Gson gson = new GsonBuilder().create();

    private Programme programme;

    private Programme getProgramme() {
        if (programme == null) {
            try {
                //URL url = new URL("file:///D:/sources/Breizhcamp-cfp/conf/breizhcamp.json");
                URL url = new URL("http://cfp.breizhcamp.org/programme");
                URLConnection connection = url.openConnection();
                programme = gson.fromJson(new InputStreamReader(connection.getInputStream()), Event.class).getProgramme();
                for (Talk talk : getTalks()) {
                    if (talk.getTime().length() < 5) {
                        talk.setTime("0" + talk.getTime());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return programme;
    }

    public List<String> getDates() {
        return Lists.transform(getProgramme().getJours(), new Function<Jour, String>() {
            @Override
            public String apply(Jour input) {
                return input.getDate();
            }
        });
    }

    public List<Talk> getTalks() {
        List<Talk> talks = new ArrayList<>();
        for (Jour jour : getProgramme().getJours()) {
            for (Track track : jour.getTracks()) {
                talks.addAll(track.getTalks());
            }
        }
        return talks;
    }

    public Map<String, List<Talk>> getTalksByDate() {
        Map<String, List<Talk>> talksByDate = new HashMap<>();
        for (Jour jour : getProgramme().getJours()) {
            talksByDate.put(jour.getDate(), new ArrayList<Talk>());
            for (Track track : jour.getTracks()) {
                talksByDate.get(jour.getDate()).addAll(track.getTalks());
            }
        }
        return talksByDate;
    }


    public List<String> getRooms() {
        Set<String> rooms = new HashSet<>();
        for (Talk talk : getTalks()) {
            if (talk.getRoom() == null) {
                System.err.println("Le talk " + talk.getTitle() + " n'a pas de room");
            }
            rooms.add(talk.getRoom());
        }
        List<String> roomsInList =  new ArrayList<>(rooms);
        Collections.sort(roomsInList);
        return roomsInList;
    }

    public Map<String, List<String>> getCreneaux() {
        Map<String, List<String>> creneaux = new HashMap<>();
        for (Map.Entry<String, List<Talk>> entry : getTalksByDate().entrySet()) {
            Set<String> creneauxForDate = new HashSet<>();
            for (Talk talk : entry.getValue()) {
                creneauxForDate.add(talk.getTime());
            }
            List<String> creneauxInList = new ArrayList<>(creneauxForDate);
            Collections.sort(creneauxInList);
            creneaux.put(entry.getKey(), creneauxInList);
        }
        return creneaux;
    }

    public Talk getTalkByDateAndCreneauxAndRoom(String date, String creneau, String room) {
        Talk talkSelected = null;
        for (Talk talk : getTalksByDate().get(date)) {
            if (creneau.equals(talk.getTime()) && room.equals(talk.getRoom())) {
                if (talkSelected != null) {
                    throw new RuntimeException("Two talk for " + date + " " + creneau + " " + room);
                }
                talkSelected = talk;
            }
        }
        return talkSelected;
    }
}
