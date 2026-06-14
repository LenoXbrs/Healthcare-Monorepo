package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Diagnostico;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.MedicamentoPrescrito;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.SinaisVitais;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Triagem;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] gerar(
            Diagnostico diagnostico,
            String pacienteNome,
            String pacienteCpf,
            String enfermeiroNome,
            String medicoNome
    ) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Definitions
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(18, 53, 91));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(18, 53, 91));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(51, 51, 51));
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(51, 51, 51));
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(102, 102, 102));
            Font hashFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 9, new Color(0, 102, 51));

            // Header Section
            Paragraph mainTitle = new Paragraph("RELATÓRIO CLÍNICO E RECEITUÁRIO MÉDICO", mainTitleFont);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            mainTitle.setSpacingAfter(20);
            document.add(mainTitle);

            // Paciente Block
            document.add(createSectionTitle("1. IDENTIFICAÇÃO DO PACIENTE", sectionTitleFont));
            
            PdfPTable pacTable = new PdfPTable(2);
            pacTable.setWidthPercentage(100);
            pacTable.setSpacingAfter(15);
            pacTable.setWidths(new float[]{3f, 1f});
            
            pacTable.addCell(createLabelValueCell("Nome do Paciente:", pacienteNome, boldFont, regularFont));
            pacTable.addCell(createLabelValueCell("CPF:", pacienteCpf, boldFont, regularFont));
            
            document.add(pacTable);

            // Triagem & Sinais Vitais Block
            Triagem triagem = diagnostico.getTriagem();
            document.add(createSectionTitle("2. DADOS DA TRIAGEM E SINAIS VITAIS", sectionTitleFont));

            PdfPTable triagemTable = new PdfPTable(2);
            triagemTable.setWidthPercentage(100);
            triagemTable.setSpacingAfter(10);
            triagemTable.setWidths(new float[]{1f, 1f});

            String dataTriagem = triagem.getCreatedAt() != null ? triagem.getCreatedAt().format(DATE_FORMATTER) : "N/A";
            triagemTable.addCell(createLabelValueCell("Data/Hora Entrada:", dataTriagem, boldFont, regularFont));
            triagemTable.addCell(createLabelValueCell("Profissional de Triagem:", enfermeiroNome, boldFont, regularFont));
            
            String prioridade = triagem.getPrioridade() != null ? triagem.getPrioridade().name() : "NÃO CLASSIFICADO";
            triagemTable.addCell(createLabelValueCell("Prioridade de Atendimento:", prioridade, boldFont, regularFont));
            triagemTable.addCell(createLabelValueCell("Status:", triagem.getStatus().name(), boldFont, regularFont));

            document.add(triagemTable);

            // Queixa Principal
            PdfPTable queixaTable = new PdfPTable(1);
            queixaTable.setWidthPercentage(100);
            queixaTable.setSpacingAfter(15);
            queixaTable.addCell(createLabelValueCell("Queixa Principal:", 
                    triagem.getQueixaPrincipal() != null ? triagem.getQueixaPrincipal() : "Não informada", 
                    boldFont, regularFont));
            document.add(queixaTable);

            // Sinais Vitais Table
            SinaisVitais sv = triagem.getSinaisVitais();
            if (sv != null) {
                Paragraph svHeader = new Paragraph("Sinais Vitais Coletados:", boldFont);
                svHeader.setSpacingAfter(5);
                document.add(svHeader);

                PdfPTable svTable = new PdfPTable(5);
                svTable.setWidthPercentage(100);
                svTable.setSpacingAfter(15);
                svTable.addCell(createHeaderCell("P.A. (Sistólica/Diastólica)", boldFont));
                svTable.addCell(createHeaderCell("Temperatura", boldFont));
                svTable.addCell(createHeaderCell("Frequência Cardíaca", boldFont));
                svTable.addCell(createHeaderCell("Saturação O2", boldFont));
                svTable.addCell(createHeaderCell("Coletado em", boldFont));

                svTable.addCell(createValueCell(sv.getPressaoSist() + "/" + sv.getPressaoDiast() + " mmHg", regularFont));
                svTable.addCell(createValueCell(sv.getTemperatura() != null ? sv.getTemperatura().toString() + " °C" : "N/A", regularFont));
                svTable.addCell(createValueCell(sv.getFrequencia() + " bpm", regularFont));
                svTable.addCell(createValueCell(sv.getSaturacao() + "%", regularFont));
                
                String coletadoEmStr = sv.getColetadoEm() != null ? sv.getColetadoEm().format(DATE_FORMATTER) : "N/A";
                svTable.addCell(createValueCell(coletadoEmStr, regularFont));

                document.add(svTable);
            }

            // Diagnóstico Clínico Block
            document.add(createSectionTitle("3. DIAGNÓSTICO CLÍNICO", sectionTitleFont));
            PdfPTable diagTable = new PdfPTable(1);
            diagTable.setWidthPercentage(100);
            diagTable.setSpacingAfter(15);
            
            String diagDesc = diagnostico.getDescricao() != null ? diagnostico.getDescricao() : "Diagnóstico clínico em preenchimento.";
            diagTable.addCell(createLabelValueCell("Avaliação Médica:", diagDesc, boldFont, regularFont));
            document.add(diagTable);

            // Prescrição de Medicamentos Block
            document.add(createSectionTitle("4. RECEITUÁRIO E PRESCRIÇÃO", sectionTitleFont));
            if (diagnostico.getMedicamentos() == null || diagnostico.getMedicamentos().isEmpty()) {
                Paragraph noMed = new Paragraph("Nenhum medicamento prescrito para este caso.", regularFont);
                noMed.setSpacingAfter(15);
                document.add(noMed);
            } else {
                PdfPTable medTable = new PdfPTable(4);
                medTable.setWidthPercentage(100);
                medTable.setSpacingAfter(15);
                medTable.setWidths(new float[]{2f, 1f, 1f, 1f});

                medTable.addCell(createHeaderCell("Medicamento", boldFont));
                medTable.addCell(createHeaderCell("Dosagem", boldFont));
                medTable.addCell(createHeaderCell("Frequência", boldFont));
                medTable.addCell(createHeaderCell("Duração/Prazo", boldFont));

                for (MedicamentoPrescrito med : diagnostico.getMedicamentos()) {
                    medTable.addCell(createValueCell(med.getNome(), regularFont));
                    medTable.addCell(createValueCell(med.getDosagem(), regularFont));
                    medTable.addCell(createValueCell(med.getFrequencia(), regularFont));
                    medTable.addCell(createValueCell(med.getPrazoUso(), regularFont));
                }
                document.add(medTable);
            }

            // Digital Signature Block
            document.add(createSectionTitle("5. CERTIFICAÇÃO DIGITAL", sectionTitleFont));

            PdfPTable sigTable = new PdfPTable(1);
            sigTable.setWidthPercentage(100);
            sigTable.setSpacingAfter(20);

            PdfPCell cell = new PdfPCell();
            cell.setPadding(12);
            cell.setBackgroundColor(new Color(240, 248, 255)); // Light blue tint
            cell.setBorderColor(new Color(18, 53, 91));
            cell.setBorderWidth(1);

            Paragraph docInfo = new Paragraph();
            docInfo.add(new Chunk("Médico Responsável: ", boldFont));
            docInfo.add(new Chunk(medicoNome + " (ID: " + diagnostico.getMedicoId() + ")\n", regularFont));
            
            String dataAssinado = diagnostico.getAssinadoEm() != null ? diagnostico.getAssinadoEm().format(DATE_FORMATTER) : "Aguardando assinatura";
            docInfo.add(new Chunk("Data da Assinatura: ", boldFont));
            docInfo.add(new Chunk(dataAssinado + "\n\n", regularFont));

            docInfo.add(new Chunk("Assinatura Eletrônica (Hash SHA-256):\n", boldFont));
            
            String hash = diagnostico.getAssinaturaHash() != null ? diagnostico.getAssinaturaHash() : "DOCUMENTO NÃO ASSINADO / RASCUNHO";
            docInfo.add(new Chunk(hash, hashFont));
            
            cell.addElement(docInfo);

            // Render hand-drawn signature if present
            if (diagnostico.getAssinaturaBase64() != null && !diagnostico.getAssinaturaBase64().isEmpty()) {
                try {
                    String base64Data = diagnostico.getAssinaturaBase64();
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                    }
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                    Image signatureImg = Image.getInstance(imageBytes);
                    signatureImg.scaleToFit(120, 50);
                    signatureImg.setSpacingBefore(8);
                    cell.addElement(signatureImg);
                } catch (Exception e) {
                    System.err.println("Erro ao renderizar imagem da assinatura no PDF: " + e.getMessage());
                }
            }

            sigTable.addCell(cell);
            document.add(sigTable);

            // Footer / Metadata
            Paragraph footer = new Paragraph("Este documento foi assinado digitalmente e possui validade jurídica respaldada pelas diretrizes hospitalares do Healthcare Management System.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private Paragraph createSectionTitle(String text, Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(10);
        p.setSpacingAfter(8);
        return p;
    }

    private PdfPCell createLabelValueCell(String label, String value, Font labelFont, Font valFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorderColor(new Color(220, 220, 220));
        cell.setBackgroundColor(new Color(250, 250, 250));

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labelFont));
        p.add(new Chunk(value != null ? value : "", valFont));
        cell.addElement(p);

        return cell;
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        Font headerFont = new Font(font.getFamily(), font.getSize(), font.getStyle(), Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(18, 53, 91));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderColor(new Color(255, 255, 255));
        return cell;
    }

    private PdfPCell createValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(6);
        cell.setBorderColor(new Color(220, 220, 220));
        return cell;
    }
}
