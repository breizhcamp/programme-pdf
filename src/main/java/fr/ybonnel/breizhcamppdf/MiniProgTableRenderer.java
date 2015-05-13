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
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import fr.ybonnel.breizhcamppdf.model.Talk;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class MiniProgTableRenderer {


    private static final Font titleFont =
            FontFactory.getFont(FontFactory.HELVETICA, 4, Font.NORMAL, BaseColor.BLACK);

    private static final Font talkFont =
            FontFactory.getFont(FontFactory.HELVETICA, 3, Font.NORMAL, BaseColor.BLACK);

    private Document document;
    private PdfWriter pdfWriter;
    private DataService service = new DataService();

    public MiniProgTableRenderer(Document document, PdfWriter pdfWriter) {
        this.document = document;
        this.pdfWriter = pdfWriter;
    }

    public void render() throws DocumentException, IOException {
        createProgrammePages();
    }

    private PdfPCell createHeaderCell(String content) {
        Paragraph paragraph = new Paragraph();
        Font font = new Font();
        font.setColor(BaseColor.WHITE);
        font.setSize(4);
        paragraph.setFont(font);
        paragraph.add(new Phrase(content));
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.GRAY);
        return cell;
    }

    private int getRowSpan(String date, Talk talk) {

        int count = 0;
        for (String otherCreneau : service.getCreneaux().get(date)) {
            if (otherCreneau.compareTo(talk.getStart()) >= 0 && otherCreneau.compareTo(talk.getEnd()) < 0) {
                count++;
            }
        }
        return count;
    }

    private List<Talk> createProgrammePages() throws DocumentException, IOException {
        List<Talk> talksToExplain = new ArrayList<>();
        document.setPageSize(PageSize.A6);

        for (String date : service.getDates()) {

            Map<String, Talk> precedentTalk = new HashMap<>();
            PdfPTable table = createBeginningOfPage(date);
            for (String creneau : service.getCreneaux().get(date)) {

                PdfPCell cellCreneau = new PdfPCell();
                Paragraph startTime = new Paragraph(creneau);
                startTime.setAlignment(Element.ALIGN_CENTER);
                startTime.getFont().setSize(3);
                cellCreneau.addElement(startTime);
                Paragraph endTime = new Paragraph(getEndTime(date, creneau));
                endTime.setAlignment(Element.ALIGN_CENTER);
                endTime.getFont().setSize(3);
                cellCreneau.addElement(endTime);

                boolean hasTalks = false;
                List<PdfPCell> cells = new ArrayList<>();
                for (String room : service.getRooms(date)) {

                    PdfPCell cell = new PdfPCell();
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                    Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
                    if (talk != null) {
                        talksToExplain.add(talk);
                        remplirCellWithTalk(cell, talk);
                        cell.setRowspan(getRowSpan(date, talk));
                        precedentTalk.put(room, talk);
                        cells.add(cell);
                        hasTalks = true;

                    } else {
                        talk = precedentTalk.get(room);
                        if (!(talk != null && talk.getEnd().compareTo(creneau) > 0)) {
                            cells.add(cell);
                        }
                        else {
                            hasTalks = true;
                        }
                    }
                }
                if (hasTalks) {
                    table.addCell(cellCreneau);
                    for (PdfPCell cell : cells) {
                        table.addCell(cell);
                    }
                }
            }
            document.add(table);
        }
        return talksToExplain;
    }

    protected String getEndTime(String date, String creneau) {
        String endTime = "99:99";
        for (String room : service.getRooms(date, false)) {
            Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
            if (talk != null && talk.getEnd().compareTo(endTime) < 0) {
                endTime = talk.getEnd();
            }
        }
        return endTime;
    }

    private PdfPTable createBeginningOfPage(String date) throws DocumentException {

        Paragraph titre = new Paragraph("Programme du " + date, titleFont);
        titre.getFont().setStyle(Font.BOLD);
        titre.setAlignment(Paragraph.ALIGN_CENTER);
        titre.setSpacingAfter(3);
        document.setMargins(0, 0, 0, 0);
        document.add(titre);

        float[] relativeWidth = new float[service.getRooms(date).size() + 1];
        Arrays.fill(relativeWidth, 1f);
        relativeWidth[0] = 0.5f;

        PdfPTable table = new PdfPTable(relativeWidth);

        table.setWidthPercentage(100);

        table.addCell(createHeaderCell("Heure"));
        for (String room : service.getRooms(date)) {
            table.addCell(createHeaderCell(room));
        }
        return table;
    }

    private void remplirCellWithTalk(PdfPCell cell, Talk talk) throws DocumentException, IOException {

        Paragraph titleTalk = new Paragraph(talk.getTitle(), talkFont);
        titleTalk.setAlignment(Paragraph.ALIGN_CENTER);
        cell.addElement(titleTalk);

        BaseColor color =  FullProgRenderer.mapTrack.get(talk.getTrack());
        cell.setBackgroundColor(color);
    }

}
