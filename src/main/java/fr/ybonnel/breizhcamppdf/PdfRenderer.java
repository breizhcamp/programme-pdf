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
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import fr.ybonnel.breizhcamppdf.model.Speaker;
import fr.ybonnel.breizhcamppdf.model.Talk;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfRenderer {

    private static final Font talkFont =
            FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL, BaseColor.BLACK);
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

    public PdfRenderer(Document document, PdfWriter pdfWriter) {
        this.document = document;
        this.pdfWriter = pdfWriter;
    }

    public void render() throws DocumentException, IOException {

        // Footer
        pdfWriter.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                Rectangle rect = new Rectangle(36, 54, 559, 788);
                ColumnText.showTextAligned(
                        writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase("BreizhCamp 2013"),
                        (rect.getLeft() + rect.getRight()) / 2, rect.getBottom() - 18, 0);
            }
        });

        createFirstPage();
        List<Talk> talksToExplain = createProgrammePages();
    }



    private void createFirstPage() throws DocumentException, IOException {
        Rectangle savePagesize = document.getPageSize();
        document.setPageSize(PageSize.A4);
        document.newPage();
        Image imageLogo = Image.getInstance(PdfRenderer.class.getResource("/logo.png"));
        imageLogo.scaleAbsolute(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin(), imageLogo.getHeight());
        document.add(imageLogo);

        Paragraph paragraph = new Paragraph("13 et 14 Juin 2013");
        paragraph.setSpacingAfter(25);
        paragraph.getFont().setSize(25);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);

        document.setPageSize(savePagesize);
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

    private List<Talk> createProgrammePages() throws DocumentException {
        List<Talk> talksToExplain = new ArrayList<>();
        document.setPageSize(PageSize.A4.rotate());
        Font font = new Font();
        font.setStyle(Font.BOLD);
        font.setSize(14);
        Paragraph titre = null;


        for (String date : service.getDates()) {
            PdfPTable table = createBeginningOfPage(font, date);
            for (String creneau : service.getCreneaux().get(date)) {
                // Nouvelle page à 14h
                if (creneau.startsWith("14")) {
                    document.add(table);
                    table = createBeginningOfPage(font, date);
                }

                table.addCell(createCellCentree(creneau));
                for (String room : service.getRooms()) {
                    PdfPCell cell = new PdfPCell();
                    cell.setPaddingBottom(10);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    Talk talk = service.getTalkByDateAndCreneauxAndRoom(date,creneau,room);
                    if (talk != null) {
                        talksToExplain.add(talk);
                        remplirCellWithTalk(cell, talk);
                    }
                    table.addCell(cell);
                }
            }
            document.add(table);
        }
        return talksToExplain;
    }

    private PdfPTable createBeginningOfPage(Font font, String date) throws DocumentException {
        Paragraph titre;
        document.newPage();
        titre = new Paragraph();
        titre.setFont(font);
        titre.setAlignment(Paragraph.ALIGN_CENTER);
        titre.add(new Phrase("Programme du " + date));
        document.add(titre);

        PdfPTable table = new PdfPTable(service.getRooms().size() + 1);

        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);

        table.addCell(createHeaderCell("Heure"));
        for (String room : service.getRooms()) {
            table.addCell(createHeaderCell(room));
        }
        return table;
    }

    private PdfPCell createCellCentree(String content) {
        return createCellCentree(content, null);
    }

    private PdfPCell createCellCentree(String content, Font font) {
        Paragraph creneau = null;
        if (font == null) {
            creneau = new Paragraph(content);
        } else {
            creneau = new Paragraph(content, font);
        }
        creneau.setAlignment(Paragraph.ALIGN_CENTER);

        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingBottom(10);
        cell.addElement(creneau);
        return cell;
    }

    private static final Map<String, BaseColor> mapFormats = new HashMap<String, BaseColor>(){{
        put("quickie", BaseColor.GREEN);
        put("tools in action", new BaseColor(Color.decode("#B0C4DE")));
        put("keynote", new BaseColor(Color.decode("#B0C4DE")));
        put("conference", new BaseColor(Color.decode("#FF8C00")));
        put("lab", new BaseColor(Color.decode("#FF8C00")));
        put("biglab", new BaseColor(Color.decode("#FF8C00")));
        put("universite", new BaseColor(Color.decode("#D8BFD8")));
        put("hands-on", new BaseColor(Color.decode("#D8BFD8")));
    }};

    private void remplirCellWithTalk(PdfPCell cell, Talk talk) {



        Chunk chunk = new Chunk(talk.getTitle(), talkFont);
        chunk.setLocalGoto("talk" + talk.getId());
        Paragraph titleTalk = new Paragraph(chunk);
        titleTalk.setAlignment(Paragraph.ALIGN_CENTER);
        cell.addElement(titleTalk);
        Paragraph theme = new Paragraph(new Phrase(talk.getFormat(), themeFont));
        theme.setAlignment(Paragraph.ALIGN_CENTER);

        cell.setBackgroundColor(mapFormats.get(talk.getFormat()));
        cell.addElement(theme);
        for (Speaker speaker : TalkService.INSTANCE.getTalkDetail(talk.getId()).getSpeakers()) {
            Paragraph speakerText = new Paragraph(speaker.getFullname(), speakerFont);
            speakerText.setAlignment(Paragraph.ALIGN_CENTER);
            cell.addElement(speakerText);
        }
    }
}
