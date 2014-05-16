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
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.*;
import com.petebevin.markdown.MarkdownProcessor;
import fr.ybonnel.breizhcamppdf.model.Speaker;
import fr.ybonnel.breizhcamppdf.model.Talk;
import fr.ybonnel.breizhcamppdf.model.TalkDetail;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.List;

public class RoomPdfRenderer {

    private static final Font talkFont =
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font speakerFont =
            FontFactory.getFont(FontFactory.HELVETICA, 9,
                    Font.NORMAL, BaseColor.BLACK);

    private static final Font themeFont =
            FontFactory.getFont(FontFactory.HELVETICA, 8,
                    Font.ITALIC, BaseColor.GRAY);

    private static final Font presentFont =
            FontFactory.getFont(FontFactory.HELVETICA, 13,
                    Font.BOLD, BaseColor.GRAY);

    private static final Font talkFontTitle =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, Font.UNDERLINE, BaseColor.DARK_GRAY);

    private Document document;
    private PdfWriter pdfWriter;
    private DataService service = new DataService();

    public RoomPdfRenderer(Document document, PdfWriter pdfWriter) {
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
        paragraph.setFont(font);
        paragraph.add(new Phrase(content));
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPaddingBottom(10);
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
        document.setPageSize(PageSize.A4);
        Font font = new Font();
        font.setStyle(Font.BOLD);
        font.setSize(14);

        for (String date : service.getDates()) {


            for (String room : service.getRooms(date)) {

                Set<String> tracksInPage = new HashSet<>();

                Map<String, Talk> precedentTalk = new HashMap<>();
                PdfPTable table = createBeginningOfPage(font, date, room);
                for (String creneau : service.getCreneaux().get(date)) {


                    PdfPCell cellCreneau = new PdfPCell();
                    cellCreneau.setPaddingBottom(10);
                    Paragraph startTime = new Paragraph(creneau);
                    startTime.setAlignment(Element.ALIGN_CENTER);
                    cellCreneau.addElement(startTime);
                    Paragraph endTime = new Paragraph(getEndTime(date, creneau));
                    endTime.setAlignment(Element.ALIGN_CENTER);
                    cellCreneau.addElement(endTime);
                    table.addCell(cellCreneau);

                    PdfPCell cell = new PdfPCell();
                    cell.setPaddingBottom(10);
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                    Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
                    if (talk != null) {
                        talksToExplain.add(talk);
                        remplirCellWithTalk(cell, talk);
                        cell.setRowspan(getRowSpan(date, talk));
                        precedentTalk.put(room, talk);
                        tracksInPage.add(talk.getTrack());
                        table.addCell(cell);
                    } else {
                        talk = precedentTalk.get(room);
                        if (!(talk != null && talk.getEnd().compareTo(creneau) > 0)) {
                            table.addCell(cell);
                        }
                    }

                }
                document.add(table);
                addLegend(tracksInPage);
            }
        }
        return talksToExplain;
    }

    private String getEndTime(String date, String creneau) {
        String endTime = "99:99";
        for (String room : service.getRooms(date)) {
            Talk talk = service.getTalkByDateAndCreneauxAndRoom(date, creneau, room);
            if (talk != null && talk.getEnd().compareTo(endTime) < 0) {
                endTime = talk.getEnd();
            }
        }
        return endTime;
    }

    private void addLegend(Set<String> tracksInPage) throws DocumentException {
        PdfPTable legend = new PdfPTable(tracksInPage.size() + 1);
        legend.setWidthPercentage(100f);
        PdfPCell cellTitle = new PdfPCell(new Phrase("Légende : ", speakerFont));
        cellTitle.setBorder(Rectangle.NO_BORDER);
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitle.setPadding(2);
        legend.addCell(cellTitle);

        for (String track : tracksInPage) {
            PdfPCell color = new PdfPCell(new Phrase(track, speakerFont));
            color.setHorizontalAlignment(Element.ALIGN_CENTER);
            color.setPadding(2);
            color.setBackgroundColor(mapTrack.get(track));
            legend.addCell(color);
        }
        tracksInPage.clear();
        document.add(legend);
    }

    private PdfPTable createBeginningOfPage(Font font, String date, String room) throws DocumentException {
        Paragraph titre;
        document.newPage();
        titre = new Paragraph();
        titre.setFont(font);
        titre.setAlignment(Paragraph.ALIGN_CENTER);
        titre.add(new Phrase("Programme du " + date + " - " + room));
        titre.setSpacingAfter(20);
        document.add(titre);

        float[] relativeWidth = new float[2];
        Arrays.fill(relativeWidth, 1f);
        relativeWidth[0] = 0.2f;

        PdfPTable table = new PdfPTable(relativeWidth);

        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        table.addCell(createHeaderCell("Heure"));
        table.addCell(createHeaderCell(""));

        return table;
    }

    private static final Map<String, BaseColor> mapTrack = new HashMap<String, BaseColor>() {{
        BaseColor jaune = new BaseColor(Color.decode("#F9DB0B"));
        BaseColor orange = new BaseColor(Color.decode("#FF8C00"));
        BaseColor vert = new BaseColor(Color.decode("#56C566"));
        BaseColor bleu = new BaseColor(Color.decode("#B0C4DE"));
        BaseColor violet = new BaseColor(Color.decode("#A174B9"));
        
        put("Web", jaune);
        put("Cloud et BigData", orange);
        put("Agilité", vert);
        put("DevOps", bleu);
        put("Internet of Things", violet);
        put("Hardware", violet);
        
        put("Keynote", new BaseColor(Color.decode("#F8ECDE")));
        put("Architecture", orange);
        put("Langages", bleu);
        put("découverte", violet);
        put("Web et Mobile", jaune);
        put("Tooling", vert);
        put("Tool", vert);
    }};


    private void remplirCellWithTalk(PdfPCell cell, Talk talk) throws DocumentException, IOException {
        Image image = AvatarService.INSTANCE.getImage(RoomPdfRenderer.class.getResource("/formats/" + talk.getFormat().replaceAll(" ", "") + ".png"));


        float[] widths = {0.05f, 0.95f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100f);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.addCell(image);
        PdfPCell subCell = new PdfPCell();
        Chunk chunk = new Chunk(talk.getTitle(), talkFont);
        chunk.setLocalGoto("talk" + talk.getId());
        Paragraph titleTalk = new Paragraph();
        titleTalk.add(chunk);
        titleTalk.setAlignment(Paragraph.ALIGN_CENTER);
        subCell.addElement(titleTalk);
        Paragraph track = new Paragraph(new Phrase(talk.getTrack(), themeFont));
        track.setAlignment(Paragraph.ALIGN_CENTER);

        subCell.addElement(track);
        TalkDetail detail = TalkService.INSTANCE.getTalkDetail(talk);
        if (detail != null) {
            for (Speaker speaker : detail.getSpeakers()) {
                Paragraph speakerText = new Paragraph(speaker.getFullname(), speakerFont);
                speakerText.setAlignment(Paragraph.ALIGN_CENTER);
                subCell.addElement(speakerText);
            }
        }
        subCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(subCell);
        cell.setBackgroundColor(mapTrack.get(talk.getTrack()));
        cell.addElement(table);
    }
}
