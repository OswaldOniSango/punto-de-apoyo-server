package com.puntodeapoyo.inspectioncases.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.puntodeapoyo.inspectioncases.model.CaseAssignment;
import com.puntodeapoyo.inspectioncases.model.InspectionCase;
import com.puntodeapoyo.inspectioncases.model.PhotoEvidence;
import com.puntodeapoyo.inspectioncases.model.TechnicalObservation;
import com.puntodeapoyo.inspectioncases.repository.CaseAssignmentRepository;
import com.puntodeapoyo.inspectioncases.repository.InspectionCaseRepository;
import com.puntodeapoyo.inspectioncases.repository.PhotoEvidenceRepository;
import com.puntodeapoyo.inspectioncases.repository.TechnicalObservationRepository;
import com.puntodeapoyo.inspectioncases.service.InspectionCasePdfService;
import com.puntodeapoyo.users.model.InternalUser;
import com.puntodeapoyo.users.model.UserRole;
import com.puntodeapoyo.users.repository.InternalUserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InspectionCasePdfServiceImpl implements InspectionCasePdfService {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;
    private static final float PHOTO_WIDTH = 220;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InspectionCaseRepository inspectionCaseRepository;
    private final CaseAssignmentRepository caseAssignmentRepository;
    private final PhotoEvidenceRepository photoEvidenceRepository;
    private final TechnicalObservationRepository technicalObservationRepository;
    private final InternalUserRepository userRepository;
    private final Path uploadDir;

    public InspectionCasePdfServiceImpl(
            InspectionCaseRepository inspectionCaseRepository,
            CaseAssignmentRepository caseAssignmentRepository,
            PhotoEvidenceRepository photoEvidenceRepository,
            TechnicalObservationRepository technicalObservationRepository,
            InternalUserRepository userRepository,
            @Value("${app.storage.upload-dir}") String uploadDir
    ) {
        this.inspectionCaseRepository = inspectionCaseRepository;
        this.caseAssignmentRepository = caseAssignmentRepository;
        this.photoEvidenceRepository = photoEvidenceRepository;
        this.technicalObservationRepository = technicalObservationRepository;
        this.userRepository = userRepository;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public byte[] generateInspectionReport(Long caseId, Long currentUserId, String currentUserRole) {
        InspectionCase inspectionCase = inspectionCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso no encontrado"));
        validateAccess(caseId, currentUserId, currentUserRole);

        List<CaseAssignment> assignments = caseAssignmentRepository.findByCaseId(caseId);
        List<TechnicalObservation> observations = technicalObservationRepository.findByCaseId(caseId);
        List<PhotoEvidence> photos = photoEvidenceRepository.findByCaseId(caseId);

        try (PDDocument document = new PDDocument()) {
            PdfWriter writer = new PdfWriter(document);
            writeHeader(writer, inspectionCase);
            writeCaseData(writer, inspectionCase, assignments);
            writeObservations(writer, observations);
            writePhotos(writer, photos);
            writer.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo generar el PDF de inspeccion", exception);
        }
    }

    private void validateAccess(Long caseId, Long currentUserId, String currentUserRole) {
        if (UserRole.ENGINEER.name().equals(currentUserRole)
                && !caseAssignmentRepository.existsByCaseIdAndEngineerId(caseId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El ingeniero no esta asignado a este caso");
        }
    }

    private void writeHeader(PdfWriter writer, InspectionCase inspectionCase) throws IOException {
        writer.title("Reporte de inspeccion");
        writer.text("Punto de Apoyo", 12, true);
        writer.text("Tracking: " + inspectionCase.trackingCode(), 12, true);
        writer.space();
    }

    private void writeCaseData(
            PdfWriter writer,
            InspectionCase inspectionCase,
            List<CaseAssignment> assignments
    ) throws IOException {
        writer.section("Datos de vivienda");
        writer.field("Solicitante", inspectionCase.applicantName());
        writer.field("Telefono", inspectionCase.applicantPhone());
        writer.field("Direccion", inspectionCase.address());
        writer.field("Ciudad", inspectionCase.city());
        writer.field("Estado/Region", inspectionCase.stateRegion());
        writer.field("Ubicacion", formatLocation(inspectionCase.latitude(), inspectionCase.longitude()));
        writer.field("Prioridad", inspectionCase.priority().name());
        writer.field("Estado final", inspectionCase.status().name());
        writer.field("Descripcion", inspectionCase.description());
        writer.space();

        writer.section("Responsables");
        if (assignments.isEmpty()) {
            writer.text("Sin responsables asignados.", 11, false);
        } else {
            for (CaseAssignment assignment : assignments) {
                writer.bullet("%s %s <%s>".formatted(
                        nullToDash(assignment.engineerFirstName()),
                        nullToDash(assignment.engineerLastName()),
                        nullToDash(assignment.engineerEmail())
                ));
            }
        }
        writer.space();
    }

    private void writeObservations(PdfWriter writer, List<TechnicalObservation> observations) throws IOException {
        writer.section("Observaciones tecnicas");
        if (observations.isEmpty()) {
            writer.text("Sin observaciones tecnicas registradas.", 11, false);
            writer.space();
            return;
        }

        for (TechnicalObservation observation : observations) {
            String author = userRepository.findById(observation.createdByUserId())
                    .map(this::formatUser)
                    .orElse("Usuario " + observation.createdByUserId());
            writer.text("Observacion #" + observation.id(), 12, true);
            writer.field("Responsable", author);
            writer.field("Riesgo estructural", observation.structuralRisk().name());
            writer.field("Fecha", observation.createdAt() == null
                    ? null
                    : observation.createdAt().format(DATE_TIME_FORMATTER));
            writer.field("Observaciones", observation.observations());
            writer.field("Recomendaciones", observation.recommendations());
            writer.space();
        }
    }

    private void writePhotos(PdfWriter writer, List<PhotoEvidence> photos) throws IOException {
        writer.section("Fotos");
        if (photos.isEmpty()) {
            writer.text("Sin fotos registradas.", 11, false);
            return;
        }

        for (PhotoEvidence photo : photos) {
            Path photoPath = resolvePhotoPath(photo.fileUrl());
            if (photoPath == null || !photoPath.toFile().isFile()) {
                writer.bullet("Foto no disponible: " + photo.fileName());
                continue;
            }
            try {
                writer.image(photo.fileName(), photoPath.toFile());
            } catch (IOException exception) {
                writer.bullet("Foto no compatible con PDF: " + photo.fileName());
            }
        }
    }

    private Path resolvePhotoPath(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return null;
        }
        Path path = uploadDir.resolve(fileUrl.substring("/uploads/".length())).normalize();
        return path.startsWith(uploadDir) ? path : null;
    }

    private String formatLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return "-";
        }
        return latitude + ", " + longitude;
    }

    private String formatUser(InternalUser user) {
        return "%s %s <%s>".formatted(user.firstName(), user.lastName(), user.email());
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static final class PdfWriter {

        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            addPage();
        }

        private void title(String text) throws IOException {
            writeWrapped(text, 18, true);
            space();
        }

        private void section(String text) throws IOException {
            space();
            writeWrapped(text, 14, true);
        }

        private void field(String label, String value) throws IOException {
            writeWrapped(label + ": " + (value == null || value.isBlank() ? "-" : value), 11, false);
        }

        private void text(String text, int fontSize, boolean bold) throws IOException {
            writeWrapped(text, fontSize, bold);
        }

        private void bullet(String text) throws IOException {
            writeWrapped("- " + text, 11, false);
        }

        private void space() throws IOException {
            ensureSpace(LINE_HEIGHT);
            y -= LINE_HEIGHT;
        }

        private void image(String label, File file) throws IOException {
            writeWrapped(label, 10, false);
            PDImageXObject image = PDImageXObject.createFromFileByExtension(file, document);
            float scale = Math.min(PHOTO_WIDTH / image.getWidth(), 160f / image.getHeight());
            float width = image.getWidth() * scale;
            float height = image.getHeight() * scale;
            ensureSpace(height + LINE_HEIGHT);
            contentStream.drawImage(image, MARGIN, y - height, width, height);
            y -= height + LINE_HEIGHT;
        }

        private void writeWrapped(String text, int fontSize, boolean bold) throws IOException {
            PDType1Font font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
            float maxWidth = page.getMediaBox().getWidth() - (MARGIN * 2);
            for (String paragraph : sanitizeText(text).split("\\R", -1)) {
                List<String> lines = wrap(paragraph, font, fontSize, maxWidth);
                if (lines.isEmpty()) {
                    lines = List.of("");
                }
                for (String line : lines) {
                    ensureSpace(fontSize + 4);
                    contentStream.beginText();
                    contentStream.setFont(font, fontSize);
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText(line);
                    contentStream.endText();
                    y -= fontSize + 4;
                }
            }
        }

        private String sanitizeText(String text) {
            return nullToEmpty(text)
                    .replace('\u00A0', ' ')
                    .replace('\u202F', ' ')
                    .replace('\u2007', ' ')
                    .replace('\u2013', '-')
                    .replace('\u2014', '-')
                    .replace('\u2018', '\'')
                    .replace('\u2019', '\'')
                    .replace('\u201C', '"')
                    .replace('\u201D', '"')
                    .replaceAll("[^\\r\\n\\t\\x20-\\x7E\\xA1-\\xFF]", "?");
        }

        private List<String> wrap(String text, PDType1Font font, int fontSize, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            String[] words = text.split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (font.getStringWidth(candidate) / 1000 * fontSize <= maxWidth) {
                    line = new StringBuilder(candidate);
                } else {
                    if (!line.isEmpty()) {
                        lines.add(line.toString());
                    }
                    line = new StringBuilder(word);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
            return lines;
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (y - requiredHeight < MARGIN) {
                addPage();
            }
        }

        private void addPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
        }

        private void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }

        private String nullToEmpty(String text) {
            return text == null ? "" : text;
        }
    }
}
