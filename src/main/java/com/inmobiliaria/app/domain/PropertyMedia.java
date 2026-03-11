package com.inmobiliaria.app.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "property_media")
public class PropertyMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(name = "media_type", length = 20)
    private String mediaType; // IMAGE, VIDEO, PDF, DOCUMENT

    @Column(name = "content_type", length = 80)
    private String contentType;

    @Column(name = "cloudinary_url", length = 512)
    private String cloudinaryUrl;

    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    @Column(name = "download_url", length = 512)
    private String downloadUrl;

    // ── Getters & Setters ────────────────────────────────

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }

    public Property getProperty()                 { return property; }
    public void setProperty(Property property)    { this.property = property; }

    public String getOriginalName()               { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getMediaType()                  { return mediaType; }
    public void setMediaType(String mediaType)    { this.mediaType = mediaType; }

    public String getContentType()                { return contentType; }
    public void setContentType(String contentType){ this.contentType = contentType; }

    public String getCloudinaryUrl()              { return cloudinaryUrl; }
    public void setCloudinaryUrl(String cloudinaryUrl) { this.cloudinaryUrl = cloudinaryUrl; }

    public String getCloudinaryPublicId()         { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public String getDownloadUrl()                { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl){ this.downloadUrl = downloadUrl; }
}
