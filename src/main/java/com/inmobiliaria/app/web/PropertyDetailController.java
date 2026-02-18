package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.Property;
import com.inmobiliaria.app.domain.PropertyMedia;
import com.inmobiliaria.app.repo.ClientPropertyInteractionRepository;
import com.inmobiliaria.app.repo.PropertyMediaRepository;
import com.inmobiliaria.app.repo.PropertyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
public class PropertyDetailController {

    private final PropertyRepository propertyRepository;
    private final PropertyMediaRepository mediaRepository;
    private final ClientPropertyInteractionRepository interactionRepository;

    @Value("${app.upload.dir:uploads/property-media}")
    private String uploadDir;

    public PropertyDetailController(PropertyRepository propertyRepository,
                                    PropertyMediaRepository mediaRepository,
                                    ClientPropertyInteractionRepository interactionRepository) {
        this.propertyRepository = propertyRepository;
        this.mediaRepository    = mediaRepository;
        this.interactionRepository = interactionRepository;
    }

    // ── GET /inmuebles/{id} ──────────────────────────────────
    @GetMapping("/inmuebles/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<PropertyMedia> media = mediaRepository.findByPropertyIdOrderByIdAsc(id);
        long interestedCount = interactionRepository.countByPropertyId(id);

        model.addAttribute("property", property);
        model.addAttribute("mediaList", media);
        model.addAttribute("interestedCount", interestedCount);
        return "property_detail";
    }

    // ── POST /inmuebles/{id}/actualizar ──────────────────────
    @PostMapping("/inmuebles/{id}/actualizar")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String province,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String municipality,
                         @RequestParam(required = false) String propertyType,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "false") boolean occupied,
                         @RequestParam(defaultValue = "false") boolean hasAlarm,
                         @RequestParam(required = false) String alarmCode,
                         @RequestParam(required = false) String notes) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        p.setProvince(t(province));
        p.setAddress(t(address));
        p.setMunicipality(t(municipality));
        p.setPropertyType(t(propertyType));
        p.setDescription(t(description));
        p.setOccupied(occupied);
        p.setHasAlarm(hasAlarm);
        p.setAlarmCode(hasAlarm ? t(alarmCode) : "");
        p.setNotes(t(notes));
        propertyRepository.save(p);

        return "redirect:/inmuebles/" + id;
    }

    // ── POST /inmuebles/{id}/media ───────────────────────────
    @PostMapping("/inmuebles/{id}/media")
    public String uploadMedia(@PathVariable Long id,
                              @RequestParam("files") List<MultipartFile> files) throws IOException {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path dir = Paths.get(uploadDir, String.valueOf(id));
        Files.createDirectories(dir);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String contentType = file.getContentType() != null ? file.getContentType() : "";
            String mediaType = contentType.startsWith("video/") ? "VIDEO" : "IMAGE";
            String ext = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + ext;

            Files.copy(file.getInputStream(), dir.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            PropertyMedia media = new PropertyMedia();
            media.setProperty(property);
            media.setFileName(fileName);
            media.setOriginalName(file.getOriginalFilename());
            media.setMediaType(mediaType);
            media.setContentType(contentType);
            mediaRepository.save(media);
        }
        return "redirect:/inmuebles/" + id;
    }

    // ── DELETE /inmuebles/{id}/media/{mediaId} ───────────────
    @PostMapping("/inmuebles/{id}/media/{mediaId}/eliminar")
    public String deleteMedia(@PathVariable Long id,
                              @PathVariable Long mediaId) throws IOException {
        PropertyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path file = Paths.get(uploadDir, String.valueOf(id), media.getFileName());
        Files.deleteIfExists(file);
        mediaRepository.delete(media);

        return "redirect:/inmuebles/" + id;
    }

    // ── GET /inmuebles/{id}/media/{mediaId}/raw ──────────────
    // Sirve el archivo directamente (para <img> y <video>)
    @GetMapping("/inmuebles/{id}/media/{mediaId}/raw")
    @ResponseBody
    public ResponseEntity<byte[]> serveMedia(@PathVariable Long id,
                                              @PathVariable Long mediaId) throws IOException {
        PropertyMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path file = Paths.get(uploadDir, String.valueOf(id), media.getFileName());
        if (!Files.exists(file)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        byte[] bytes = Files.readAllBytes(file);
        MediaType mt = MediaType.parseMediaType(
                media.getContentType() != null ? media.getContentType() : "application/octet-stream");

        return ResponseEntity.ok().contentType(mt).body(bytes);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private static String t(String s) { return s == null ? "" : s.trim(); }
}
