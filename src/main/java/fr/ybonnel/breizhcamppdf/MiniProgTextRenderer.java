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

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import fr.ybonnel.breizhcamppdf.model.Talk;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MiniProgTextRenderer {


    private static final Font titleFont =
            FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

    private static final Font talkFont =
            FontFactory.getFont(FontFactory.HELVETICA, 6, Font.NORMAL, BaseColor.BLACK);

    private static final Font roomFont =
            FontFactory.getFont(FontFactory.HELVETICA, 5, Font.ITALIC, BaseColor.DARK_GRAY);

    private Document document;
    private PdfWriter pdfWriter;
    private DataService service = new DataService();

    public MiniProgTextRenderer(Document document, PdfWriter pdfWriter) {
        this.document = document;
        this.pdfWriter = pdfWriter;
    }

    public void render() throws DocumentException, IOException {
        List<Talk> talksToExplain = new ArrayList<>();
        document.setPageSize(PageSize.A6);

        for (String date : service.getDates()) {

            Paragraph titre = new Paragraph("Programme du " + date, titleFont);
            titre.getFont().setStyle(Font.BOLD);
            titre.setAlignment(Paragraph.ALIGN_CENTER);
            titre.setSpacingAfter(10);
            document.setMargins(0, 0, 0, 0);
            document.add(titre);

            PdfPTable dateTable = new PdfPTable(new float[]{ 1f, 1f });
            dateTable.setWidthPercentage(100);

            PdfPCell column = new PdfPCell();
            column.setBorder(Rectangle.NO_BORDER);
            column.setPadding(0);

            for (String creneau : service.getCreneaux().get(date)) {

                if ("14:00".equals(creneau)) {
                    URL mapURL = this.getClass().getResource("/plan.png");
                    Image map = Image.getInstance(mapURL);
                    map.setSpacingBefore(10f);
                    map.setBottom(0f);
                    column.addElement(map);

                    Paragraph twitter = new Paragraph(new Phrase("#BzhCmp", roomFont));
                    twitter.setAlignment(Element.ALIGN_CENTER);
                    column.addElement(twitter);

                    dateTable.addCell(column);

                    column = new PdfPCell();
                    column.setBorder(Rectangle.NO_BORDER);
                    column.setPadding(0);
                }

                PdfPTable creneauTable = new PdfPTable(new float[]{ 1f, 7f });
                creneauTable.setWidthPercentage(100);


                PdfPCell time = new PdfPCell();
                time.setPadding(0);
                time.setPaddingTop(4);
                time.setBorder(Rectangle.NO_BORDER);

                Paragraph startTime = new Paragraph(creneau);
                startTime.setAlignment(Element.ALIGN_CENTER);
                startTime.getFont().setSize(6);
                startTime.getFont().setStyle(Font.BOLD);
                time.addElement(startTime);

                creneauTable.addCell(time);

                PdfPCell talks = new PdfPCell();
                talks.setBorder(Rectangle.NO_BORDER);
                talks.setPadding(0);
                talks.setPaddingTop(5);

                boolean hasTalk = false;
                for (String room : service.getRooms(date)) {
                    Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
                    if (talk != null) {

                        PdfPTable talkTable = new PdfPTable(new float[]{2.2f, 10f});
                        talkTable.setWidthPercentage(100);
                        talkTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                        //talkTable.getDefaultCell().setPadding(1.5f);

                        PdfPCell icon = new PdfPCell();
                        icon.setPadding(0);
                        icon.setHorizontalAlignment(Element.ALIGN_RIGHT);

                        //Image image = AvatarService.INSTANCE.getImage(FullProgRenderer.class.getResource("/formats/" + talk.getFormat().replaceAll(" ", "").replaceAll("-", "").replaceAll("'", "").toLowerCase() + ".png"));

                        Phrase p = new Phrase(room.equals("Belle-Ile-en-Mer") ? "Belle-Ile" :  room, roomFont);
                        icon.addElement(p);
                        //icon.setPaddingTop(3);
                        icon.setBorder(Rectangle.NO_BORDER);
                        talkTable.addCell(icon);

                        Paragraph titleTalk = new Paragraph();
                        titleTalk.add(new Phrase(talk.getTitle(), talkFont));
                        //titleTalk.add(new Phrase(talk.getRoom(), roomFont));
                        talkTable.addCell(titleTalk);

                        talks.addElement(talkTable);

                        hasTalk = true;
                    }
                }
                creneauTable.addCell(talks);

                if (hasTalk) {
                    column.addElement(creneauTable);
                }
            }

            dateTable.addCell(column);
            document.add(dateTable);

            document.newPage();
        }
    }

}
