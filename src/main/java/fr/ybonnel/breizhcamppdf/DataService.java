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
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.ybonnel.breizhcamppdf.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import org.apache.commons.lang3.time.FastDateFormat;

public class DataService {
	
	public static class TalkItem {
	    public String active;
	    public String description;
	    public Date event_end;
	    public String event_key;
	    public Date event_start;
	    public String event_type;
	    public String format;
	    public String goers;
	    public int id;
	    public String invite_only;
	    public String name;
	    public String seats;
	    public String speakers;
	    public String venue;
	    public int venue_id;
	}

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    private Programme programme;

    private Programme getProgramme() {
        if (programme == null) {
            try {
                //URL url = new URL("file:///D:/sources/Breizhcamp-cfp/conf/breizhcamp.json");
                URL url = new URL("http://www.breizhcamp.org/json/2016/schedule.json");
                URLConnection connection = url.openConnection();
                TalkItem[] items = gson.fromJson(new InputStreamReader(connection.getInputStream()), TalkItem[].class);
                
                FastDateFormat dateFormat = FastDateFormat.getInstance("dd/MM/yyyy");
                FastDateFormat timeFormat = FastDateFormat.getInstance("HH:mm");
                
                Map<String, String> trackToAmphi = new HashMap<>();
                trackToAmphi.put("Track1", "Amphi A");
                trackToAmphi.put("Track2", "Amphi B");
                trackToAmphi.put("Track3", "Amphi C");
                trackToAmphi.put("Track4", "Amphi D");
                trackToAmphi.put("Track5 (labs)", "Esp. Lab");
                trackToAmphi.put("Track6", "Hall");
                
                Map<Integer,Jour> jours = new HashMap<>();
                for (TalkItem item : items) {
                	Jour jour = jours.get(item.event_start.getDate());
                	if (jour == null) {
                		jour = new Jour();
                		jour.date = dateFormat.format(item.event_start);
                		jour.title = jour.date;
                		jours.put(item.event_start.getDate(), jour);
                	}
                	
                	Talk talk = new Talk();
                	talk.id = String.valueOf(item.id);
                	talk.title = item.name;
                	talk.track = item.venue;
                	talk.format = item.format;
                	talk.room = trackToAmphi.get(talk.track);
                	talk.start = timeFormat.format(item.event_start);
                	talk.end = timeFormat.format(item.event_end);
                	
                	jour.getProposals().add(talk);
                }
                
                programme = new Programme();
                programme.getJours().addAll(jours.values());

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


    private List<Talk> talks = null;

    public List<Talk> getTalks() {
        if (talks == null) {
            talks = new ArrayList<>();
            for (Jour jour : getProgramme().getJours()) {
                talks.addAll(jour.getProposals());
            }
        }
        return talks;
    }

    private Map<String, List<Talk>> talksByDate;

    public Map<String, List<Talk>> getTalksByDate() {
        if (talksByDate == null) {
            talksByDate = new HashMap<>();
            for (Jour jour : getProgramme().getJours()) {
                talksByDate.put(jour.getDate(), new ArrayList<Talk>());
                talksByDate.get(jour.getDate()).addAll(jour.getProposals());
            }
        }
        return talksByDate;
    }

    private Map<String,List<String>> roomsByDate = new HashMap<>();

    public List<String> getRooms(String date) {
        return getRooms(date, true);
    }

    public List<String> getRooms(String date, boolean skipHall) {
        List<String> rooms = roomsByDate.get(date);
        if (rooms == null) {
            Set<String> roomsInSet = new HashSet<>();
            for (Talk talk : getTalksByDate().get(date)) {
                if (talk.getRoom() == null) {
                    System.err.println("Talk without room : " + talk.getTitle());
                }
                roomsInSet.add(talk.getRoom());
            }
            rooms = new ArrayList<>(roomsInSet);

            Collections.sort(rooms);
            roomsByDate.put(date, rooms);
        }

        if (skipHall) {
            rooms = new ArrayList<>(rooms);
            rooms.remove("hall");
        }

        return rooms;
    }

    private Map<String, List<String>> creneaux;

    public Map<String, List<String>> getCreneaux() {
        if (creneaux == null) {
            creneaux = new HashMap<>();
            for (Map.Entry<String, List<Talk>> entry : getTalksByDate().entrySet()) {
                Set<String> creneauxForDate = new HashSet<>();
                for (Talk talk : entry.getValue()) {
                    creneauxForDate.add(talk.getStart());
                }
                List<String> creneauxInList = new ArrayList<>(creneauxForDate);
                Collections.sort(creneauxInList);
                creneaux.put(entry.getKey(), creneauxInList);
            }
        }
        return creneaux;
    }

    public Talk getTalkByDateAndCreneauxAndRoom(String date, String creneau, String room) {
        Talk talkSelected = null;
        for (Talk talk : getTalksByDate().get(date)) {
            if (creneau.equals(talk.getStart()) && room.equals(talk.getRoom())) {
                if (talkSelected != null) {
                    System.out.println("Two talk for " + date + " " + creneau + " " + room + " : " + talkSelected.title + " & " + talk.title);
                    //throw new RuntimeException("Two talk for " + date + " " + creneau + " " + room);
                }
                talkSelected = talk;
            }
        }
        return talkSelected;
    }
}
