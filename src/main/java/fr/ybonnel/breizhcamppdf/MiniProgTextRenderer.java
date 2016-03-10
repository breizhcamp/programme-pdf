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
    
    private static final Font hashFont =
            FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLDITALIC, BaseColor.DARK_GRAY);

    private Document document;
    private PdfWriter pdfWriter;
    private DataService service = new DataService();

    public MiniProgTextRenderer(Document document, PdfWriter pdfWriter) {
        this.document = document;
        this.pdfWriter = pdfWriter;
    }
    
    private void addBackground() throws DocumentException, IOException {
    	PdfContentByte canvas = pdfWriter.getDirectContentUnder();
    	URL mapURL = this.getClass().getResource("/logo.png");
        Image image = Image.getInstance(mapURL);
        image.scalePercent(80);
        image.setAbsolutePosition(28, 90);
        canvas.saveState();
        PdfGState state = new PdfGState();
        state.setFillOpacity(0.2f);
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }

    public void render() throws DocumentException, IOException {
        List<Talk> talksToExplain = new ArrayList<>();
        document.setPageSize(PageSize.A6);
        
        int page = 1;
        
        addBackground();
        
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
            	
            	//System.out.println("Creneau " + creneau);
            	
                if ("13:30".equals(creneau)) {
                	
                    Paragraph twitter = new Paragraph(new Phrase("#BzhCmp", hashFont));
                    twitter.setAlignment(Element.ALIGN_CENTER);
                    twitter.setSpacingBefore(20);
                    column.addElement(twitter);
                   
                    dateTable.addCell(column);

                    column = new PdfPCell();
                    column.setBorder(Rectangle.NO_BORDER);
                    column.setPadding(0);
                }

                PdfPTable creneauTable = new PdfPTable(new float[]{ 1f, 6f });
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
                	
                	//System.out.println("Room " + room);
                	
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
                        
                        //System.out.println("Talk " + talk.title + " Room " + room);

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
            
            page++;
            
            if (page <= 3) {
            	addBackground();
            }
        }
    }

}
