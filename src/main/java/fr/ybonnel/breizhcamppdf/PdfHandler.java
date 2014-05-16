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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PdfHandler extends AbstractHandler {


    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!baseRequest.getPathInfo().equals("/programme.pdf")) {
            return;
        }
        generate(response.getOutputStream(), true);
    }

    public static void main(String[] args) throws Exception {
        generate(new FileOutputStream("programme.pdf"), true);
        generate(new FileOutputStream("salles.pdf"), false);
    }

    protected static void generate(OutputStream output, boolean schedule) throws IOException {
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter pdfWriter = PdfWriter.getInstance(document, output);

            document.open();

            if (schedule) {
                new PdfRenderer(document, pdfWriter).render();
            }
            else {
                new RoomPdfRenderer(document, pdfWriter).render();
            }

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

}
