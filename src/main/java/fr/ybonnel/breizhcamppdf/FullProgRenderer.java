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

public class FullProgRenderer {

    private static final Font talkFont =
            FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font speakerFont =
            FontFactory.getFont(FontFactory.HELVETICA, 9,
                    Font.NORMAL, BaseColor.BLACK);

    private static final Font themeFont =
            FontFactory.getFont(FontFactory.HELVETICA, 8,
                    Font.ITALIC, BaseColor.DARK_GRAY);

    private static final Font presentFont =
            FontFactory.getFont(FontFactory.HELVETICA, 13,
                    Font.BOLD, BaseColor.GRAY);

    private static final Font talkFontTitle =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, Font.UNDERLINE, BaseColor.DARK_GRAY);

    private Document document;
    private PdfWriter pdfWriter;
    private DataService service = new DataService();

    public FullProgRenderer(Document document, PdfWriter pdfWriter) {
        this.document = document;
        this.pdfWriter = pdfWriter;
    }

    public void render() throws DocumentException, IOException {

        // Footer
        pdfWriter.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {

                if (writer.getPageNumber() > 1) {
                    Rectangle rect = document.getPageSize();
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_CENTER,
                            new Phrase("BreizhCamp 2015"),
                            (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() + 18, 0);
                }
            }
        });

        createFirstPage();
        List<Talk> talksToExplain = createProgrammePages();
        createTalksPages(talksToExplain);
    }


    private void createFirstPage() throws DocumentException, IOException {
        Rectangle savePagesize = document.getPageSize();
        document.setPageSize(PageSize.A4);
        document.newPage();
        Image imageLogo = Image.getInstance(FullProgRenderer.class.getResource("/logo.png"));
        imageLogo.scaleToFit(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin(), imageLogo.getHeight());
        document.add(imageLogo);

        Paragraph paragraph = new Paragraph("10, 11 et 12 juin");
        paragraph.setSpacingAfter(80);
        paragraph.getFont().setSize(20);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        Paragraph title = new Paragraph("Programme 2015");
        title.setSpacingAfter(100);
        title.getFont().setSize(56);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        PdfPTable sponsors = new PdfPTable(4);
        sponsors.setWidthPercentage(90f);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_zenika.png", 2);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_ovh.png", 2);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_google.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_jfrog.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_sii.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_jouve.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_sqli.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_cloudbees.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_github.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_sonarsource.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_ippon.png", 1);
        addSponsor(sponsors, "http://www.breizhcamp.org/img/logo_netapsys.jpg", 1);
        document.add(sponsors);

        Image istic = Image.getInstance("http://www.breizhcamp.org/img/logo_istic.png");
        istic.scaleToFit(150, 150);
        istic.setAlignment(Element.ALIGN_CENTER);
        document.add(istic);

        document.setPageSize(savePagesize);
    }

    private void addSponsor(PdfPTable sponsors, String imageUrl, int colspan) throws DocumentException, IOException {
        PdfPCell sponsor = new PdfPCell();
        sponsor.setImage(Image.getInstance(imageUrl));
        sponsor.setColspan(colspan);
        sponsor.setBorder(Rectangle.NO_BORDER);
        sponsor.setVerticalAlignment(Element.ALIGN_MIDDLE);
        sponsors.addCell(sponsor);
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
        document.setPageSize(PageSize.A4.rotate());
        Font font = new Font();
        font.setStyle(Font.BOLD);
        font.setSize(14);


        for (String date : service.getDates()) {

            Set<String> tracksInPage = new HashSet<>();

            Map<String, Talk> precedentTalk = new HashMap<>();
            PdfPTable table = createBeginningOfPage(font, date);
            for (String creneau : service.getCreneaux().get(date)) {
                // Nouvelle page à 14h
                if (creneau.startsWith("14:00") && !tracksInPage.isEmpty()) {
                    document.add(table);

                    addLegend(tracksInPage);
                    table = createBeginningOfPage(font, date);
                }

                PdfPCell cellCreneau = new PdfPCell();
                cellCreneau.setPaddingBottom(10);
                Paragraph startTime = new Paragraph(creneau);
                startTime.setAlignment(Element.ALIGN_CENTER);
                cellCreneau.addElement(startTime);
                Paragraph endTime = new Paragraph(getEndTime(date, creneau));
                endTime.setAlignment(Element.ALIGN_CENTER);
                cellCreneau.addElement(endTime);
                table.addCell(cellCreneau);
                for (String room : service.getRooms(date)) {

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
            }
            document.add(table);
            addLegend(tracksInPage);
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

    private void addLegend(Set<String> tracksInPage) throws DocumentException {
        PdfPTable legend = new PdfPTable(tracksInPage.size() + 1);
        legend.setWidthPercentage(100f);
        PdfPCell cellTitle = new PdfPCell(new Phrase("Légende : ", speakerFont));
        cellTitle.setBorder(Rectangle.NO_BORDER);
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitle.setPadding(2);
        legend.addCell(cellTitle);

        for (String track : tracksInPage) {
            PdfPCell color = new PdfPCell(new Phrase(mapTrackTitle.get(track), speakerFont));
            color.setHorizontalAlignment(Element.ALIGN_CENTER);
            color.setPadding(2);
            color.setBackgroundColor(mapTrack.get(track));
            legend.addCell(color);
        }
        tracksInPage.clear();
        document.add(legend);
    }

    private PdfPTable createBeginningOfPage(Font font, String date) throws DocumentException {
        Paragraph titre;
        document.newPage();
        titre = new Paragraph();
        titre.setFont(font);
        titre.setAlignment(Paragraph.ALIGN_CENTER);
        titre.add(new Phrase("Programme du " + date));
        document.add(titre);

        float[] relativeWidth = new float[service.getRooms(date).size() + 1];
        Arrays.fill(relativeWidth, 1f);
        relativeWidth[0] = 0.5f;

        PdfPTable table = new PdfPTable(relativeWidth);

        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        table.addCell(createHeaderCell("Heure"));
        for (String room : service.getRooms(date)) {
            table.addCell(createHeaderCell(room));
        }
        return table;
    }

    private static BaseColor getAlphaColor(String rgb) {
        Color opaqueColor = Color.decode(rgb);
        return new BaseColor(opaqueColor.getRed(), opaqueColor.getGreen(), opaqueColor.getBlue(), 50);
    }

    protected static final Map<String, BaseColor> mapTrack = new HashMap<String, BaseColor>() {{

        put("keynote", getAlphaColor("#93D2C5"));
        put("track1", getAlphaColor("#FF69BB"));
        put("track2", getAlphaColor("#8ADAFF"));
        put("track3", getAlphaColor("#EDFF75"));
        put("track4", getAlphaColor("#FFDF6C"));
        put("track5", getAlphaColor("#C3B6FA"));
        put("track6", getAlphaColor("#FFA5FF"));
        put("track7", getAlphaColor("#FFFF66"));
        put("track8", getAlphaColor("#66FFFF"));
        put("track9", getAlphaColor("#66FFFF"));

    }};

    protected static final Map<String, String> mapTrackTitle = new HashMap<String, String>() {{

        put("keynote", "Keynote");
        put("track1", "Architecture, Performance & Securité");
        put("track2", "Agilité, Méthodologie & Tests");
        put("track3", "Java, JVM, Javas SE/EE");
        put("track4", "Cloud, DevOps, Outils");
        put("track5", "BigData et Analytics");
        put("track6", "Objects connectés, IoT, Robotique");
        put("track7", "Languages");
        put("track8", "Web, Mobile & UX");
        put("track9", "Web, Mobile & UX");

    }};


    private void remplirCellWithTalk(PdfPCell cell, Talk talk) throws DocumentException, IOException {
        Image image = AvatarService.INSTANCE.getImage(FullProgRenderer.class.getResource("/formats/" + talk.getFormat().replaceAll(" ", "").replaceAll("-", "").replaceAll("'", "").toLowerCase() + ".png"));


        float[] widths = {0.15f, 0.85f};
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
        Paragraph track = new Paragraph(new Phrase(mapTrackTitle.get(talk.getTrack()), themeFont));
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

        BaseColor color =  mapTrack.get(talk.getTrack());
        cell.setBackgroundColor(color);

        cell.addElement(table);
    }


    private MarkdownProcessor markdownProcessor = new MarkdownProcessor();


    private void createTalksPages(List<Talk> talksToExplain) throws DocumentException, IOException {
        document.setPageSize(PageSize.A4);
        document.newPage();

        Paragraph paragraph = new Paragraph("Liste des talks");
        paragraph.setSpacingAfter(25);
        paragraph.getFont().setSize(25);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        for (TalkDetail talk : Lists.transform(talksToExplain, new Function<Talk, TalkDetail>() {
            @Override
            public TalkDetail apply(Talk input) {
                return TalkService.INSTANCE.getTalkDetail(input);
            }
        })) {

            if (talk == null) {
                continue;
            }

            Paragraph empty = new Paragraph(" ");
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setKeepTogether(true);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            PdfPCell cell;
            Chunk titleTalk = new Chunk(talk.getTitle(), talkFontTitle);
            titleTalk.setLocalDestination("talk" + talk.getId());
            float[] withTitle = {0.05f, 0.95f};
            PdfPTable titleWithFormat = new PdfPTable(withTitle);
            titleWithFormat.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            titleWithFormat.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            Image image = AvatarService.INSTANCE.getImage(FullProgRenderer.class.getResource("/formats/" + talk.getTalk().getFormat().replaceAll(" ", "").replaceAll("-", "").replaceAll("'", "").toLowerCase() + ".png"));
            titleWithFormat.addCell(image);
            titleWithFormat.addCell(new Paragraph(titleTalk));

            table.addCell(titleWithFormat);

            table.addCell(empty);

            table.addCell(new Paragraph("Salle " + talk.getTalk().getRoom()
                    + " de " + talk.getTalk().getStart()
                    + " à " + talk.getTalk().getEnd(), presentFont));

            table.addCell(empty);


            cell = new PdfPCell();
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
            for (Element element : HTMLWorker.parseToList(new StringReader(markdownProcessor.markdown(talk.getDescription())), null)) {
                if (element instanceof Paragraph) {
                    ((Paragraph)element).setAlignment(Element.ALIGN_JUSTIFIED);
                }
                cell.addElement(element);
            }
            table.addCell(cell);

            table.addCell(empty);

            table.addCell(new Paragraph("Présenté par :", presentFont));

            float[] widthSpeaker = {0.05f, 0.95f};
            for (Speaker speaker : talk.getSpeakers()) {
                PdfPTable speakerWithAvatar = new PdfPTable(widthSpeaker);
                speakerWithAvatar.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                speakerWithAvatar.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

                speakerWithAvatar.addCell(AvatarService.INSTANCE.getImage(speaker.getAvatar()));
                speakerWithAvatar.addCell(new Phrase(speaker.getFullname()));
                table.addCell(speakerWithAvatar);
            }

            table.addCell(empty);
            table.addCell(empty);
            document.add(table);
        }
    }
}
